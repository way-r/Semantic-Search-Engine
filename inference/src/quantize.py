from transformers import AutoTokenizer
from optimum.onnxruntime import ORTQuantizer, ORTModelForFeatureExtraction
from optimum.onnxruntime.configuration import AutoQuantizationConfig
import os

model_id = "sentence-transformers/all-MiniLM-L6-v2"
onnx_model = ORTModelForFeatureExtraction.from_pretrained(model_id, export=True)
tokenizer = AutoTokenizer.from_pretrained(model_id)
quantizer = ORTQuantizer.from_pretrained(onnx_model)

onnx_model_path = os.path.join("models", "onnx_model")
onnx_model.save_pretrained(onnx_model_path)
tokenizer.save_pretrained(onnx_model_path)
quantizer_config = AutoQuantizationConfig.avx512(is_static=False, per_channel=True)

quantizer.quantize(save_dir=os.path.join("models", "onnx_model_quantized"), quantization_config=quantizer_config)
