from fastapi import FastAPI, UploadFile, File, HTTPException
import tensorflow as tf
import numpy as np
from PIL import Image
import json
import io
import os
import logging

from tensorflow.keras.applications.resnet50 import preprocess_input

# Logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Weather Image Classification API")

model = None
label_mapping = None


@app.on_event("startup")
async def load_model():
    global model, label_mapping

    try:
        model_path = "/app/models/model.h5"  

        if not os.path.exists(model_path):
            logger.error(f"MODEL FILE NOT FOUND at {model_path}")
            return

        logger.info(f"Loading model from {model_path}...")

        model = tf.keras.models.load_model(model_path)

        logger.info("MODEL LOADED SUCCESSFULLY")
        logger.info(f"TensorFlow version: {tf.__version__}")

        with open("/app/models/label_mapping.json", "r") as f:
            label_mapping = json.load(f)

    except Exception as e:
        logger.error(" ERROR LOADING MODEL")
        logger.error(e)
        model = None


@app.get("/")
def root():
    return {
        "message": "Weather Image Classification API",
        "status": "ok" if model else "model_not_loaded"
    }


@app.get("/health")
def health():
    return {
        "status": "ok" if model else "error",
        "model_loaded": model is not None,
        "tensorflow_version": tf.__version__,
        "num_classes": len(label_mapping) if label_mapping else 0
    }


@app.post("/predict")
async def predict(file: UploadFile = File(...)):

    if model is None:
        raise HTTPException(status_code=503, detail="Model not loaded")

    try:
        image_bytes = await file.read()
        image = Image.open(io.BytesIO(image_bytes)).convert("RGB")
        original_size = image.size

        image = image.resize((64, 64))

        img_array = np.array(image, dtype=np.float32)

        img_array = preprocess_input(img_array)

        img_array = np.expand_dims(img_array, axis=0)

        predictions = model.predict(img_array, verbose=0)

        predicted_class = int(np.argmax(predictions[0]))
        confidence = float(np.max(predictions[0]))

        all_predictions = {
            label_mapping[str(i)]: round(float(p) * 100, 2)
            for i, p in enumerate(predictions[0])
        }

        sorted_predictions = dict(
            sorted(all_predictions.items(), key=lambda x: x[1], reverse=True)
        )

        return {
            "label": label_mapping[str(predicted_class)],
            "confidence": round(confidence * 100, 2),
            "predictions": sorted_predictions,
            "image_size": f"{original_size[0]}x{original_size[1]}"
        }

    except Exception as e:
        logger.error(f"Prediction error: {e}")
        raise HTTPException(status_code=500, detail=str(e))