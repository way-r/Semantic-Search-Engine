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
    encoded_text = tokenizer(text, return_tensors="np", padding=False, truncation=False)

    if (token_count := len(encoded_text["input_ids"][0])) > 512:
        raise ValueError(f"Failed to embed. Text must be no longer than 512 tokens. Current text length is {token_count} \n {text}")
    
    ort_inputs = {
        "input_ids": encoded_text["input_ids"].astype(np.int64),
        "attention_mask": encoded_text["attention_mask"].astype(np.int64)
    }

    if "token_type_ids" in encoded_text:
        ort_inputs["token_type_ids"] = encoded_text["token_type_ids"].astype(np.int64)
    
    output = session.run(None, ort_inputs)
    embedding = mean_pooling(output, ort_inputs["attention_mask"])[0]
    
    magnitude = np.linalg.norm(embedding)
    if magnitude > 0:
        embedding = embedding / magnitude

    return embedding.tolist()
