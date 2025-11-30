from transformers import AutoTokenizer
import onnxruntime as ort
import numpy as np
import os

tokenizer = AutoTokenizer.from_pretrained(os.path.join("models", "onnx_model_quantized"))
session = ort.InferenceSession(os.path.join("models", "onnx_model_quantized", "model_quantized.onnx"))

def mean_pooling(model_output, attention_mask):
    token_embeddings = model_output[0]
    input_mask_expanded = np.expand_dims(attention_mask, axis=-1)
    input_mask_expanded = np.broadcast_to(input_mask_expanded, token_embeddings.shape).astype(float)
    
    weighted_embeddings = token_embeddings * input_mask_expanded
    sum_embeddings = np.sum(weighted_embeddings, axis=1)

    sum_mask = np.sum(input_mask_expanded, axis=1)
    sum_mask = np.maximum(sum_mask, 1e-9)
    
    return sum_embeddings / sum_mask

def embed_text(text):
    inputs = tokenizer(text, return_tensors="np", padding=False, truncation=False)
    ids = inputs["input_ids"][0]
    masks = inputs["attention_mask"][0]
    types = inputs.get("token_type_ids")[0] if "token_type_ids" in inputs else None

    embeddings = []
    total_tokens = len(ids)

    for i in range(0, total_tokens, 512):
        end = min(total_tokens, i + 512)
        chunk_ids = ids[i:end]
        chunk_mask = masks[i:end]
        
        ort_inputs = {
            "input_ids": chunk_ids[None, :].astype(np.int64),
            "attention_mask": chunk_mask[None, :].astype(np.int64)
        }
        
        if types is not None:
            chunk_types = types[i:end]
            ort_inputs["token_type_ids"] = chunk_types[None, :].astype(np.int64)

        outputs = session.run(None, ort_inputs)
        vector = mean_pooling(outputs, ort_inputs['attention_mask'])
        embeddings.append(vector[0].tolist())
    
    return embeddings
