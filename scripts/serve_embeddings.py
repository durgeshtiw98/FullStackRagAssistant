#!/usr/bin/env python3
"""Run real MiniLM ONNX embeddings without the separate ONNX project.

    python3 scripts/serve_embeddings.py

Downloads the pinned public model once into data/models, then works offline.
Uses masked mean pooling and L2 normalization from the model card:
https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2
"""
import argparse
from pathlib import Path
import shutil
import urllib.request

import numpy as np
import onnxruntime as ort
from fastapi import FastAPI
from pydantic import BaseModel, Field
from tokenizers import Tokenizer
import uvicorn

MODEL = "sentence-transformers/all-MiniLM-L6-v2"
REVISION = "1110a243fdf4706b3f48f1d95db1a4f5529b4d41"


def download_model(directory: Path) -> None:
    directory.mkdir(parents=True, exist_ok=True)
    for remote, local in (("onnx/model.onnx", "model.onnx"), ("tokenizer.json", "tokenizer.json")):
        target = directory / local
        if target.exists():
            continue
        url = f"https://huggingface.co/{MODEL}/resolve/{REVISION}/{remote}"
        print(f"Downloading {remote} ({REVISION})", flush=True)
        temporary = target.with_suffix(target.suffix + ".tmp")
        with urllib.request.urlopen(url, timeout=120) as response, temporary.open("wb") as output:
            shutil.copyfileobj(response, output)
        temporary.replace(target)


class EmbedRequest(BaseModel):
    texts: list[str] = Field(min_length=1, max_length=32)


def create_app(directory: Path, threads: int) -> FastAPI:
    tokenizer = Tokenizer.from_file(str(directory / "tokenizer.json"))
    tokenizer.enable_truncation(max_length=128)
    tokenizer.enable_padding(pad_id=0, pad_token="[PAD]")
    options = ort.SessionOptions()
    options.intra_op_num_threads = threads
    session = ort.InferenceSession(str(directory / "model.onnx"), sess_options=options,
                                   providers=["CPUExecutionProvider"])
    app = FastAPI(title="Local MiniLM ONNX embeddings")

    @app.get("/health")
    def health():
        return {"status": "ok", "model": MODEL, "revision": REVISION,
                "dimension": 384, "maxTokens": 128, "threads": threads}

    @app.post("/embed")
    def embed(request: EmbedRequest):
        encoded = tokenizer.encode_batch(request.texts)
        inputs = {
            "input_ids": np.asarray([e.ids for e in encoded], dtype=np.int64),
            "attention_mask": np.asarray([e.attention_mask for e in encoded], dtype=np.int64),
            "token_type_ids": np.asarray([e.type_ids for e in encoded], dtype=np.int64),
        }
        hidden = session.run(None, {i.name: inputs[i.name] for i in session.get_inputs()})[0]
        mask = inputs["attention_mask"][..., None].astype(np.float32)
        pooled = (hidden * mask).sum(axis=1) / np.maximum(mask.sum(axis=1), 1e-9)
        vectors = pooled / np.maximum(np.linalg.norm(pooled, axis=1, keepdims=True), 1e-9)
        return {"embeddings": vectors.tolist()}

    return app


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--model-dir", type=Path,
                        default=Path(__file__).resolve().parents[1] / "data" / "models" / REVISION)
    parser.add_argument("--port", type=int, default=8000)
    parser.add_argument("--threads", type=int, default=4)
    args = parser.parse_args()
    if args.threads < 1:
        parser.error("--threads must be positive")
    download_model(args.model_dir)
    uvicorn.run(create_app(args.model_dir, args.threads), host="127.0.0.1", port=args.port)


if __name__ == "__main__":
    main()
