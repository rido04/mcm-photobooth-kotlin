import argparse
import threading
import time
from pathlib import Path

import cv2
from flask import Flask, Response, jsonify


class CameraService:
    def __init__(self, device: str, width: int, height: int):
        self.device = device
        self.width = width
        self.height = height
        self.lock = threading.Lock()
        self.cap = cv2.VideoCapture(device)
        self.cap.set(cv2.CAP_PROP_FRAME_WIDTH, width)
        self.cap.set(cv2.CAP_PROP_FRAME_HEIGHT, height)
        self.cap.set(cv2.CAP_PROP_BUFFERSIZE, 1)

        if not self.cap.isOpened():
            raise RuntimeError(f"cannot open camera: {device}")

    def snapshot(self) -> bytes:
        with self.lock:
            ok, frame = self.cap.read()
            if not ok or frame is None:
                raise RuntimeError("failed to read frame from camera")

        ok, encoded = cv2.imencode(".jpg", frame, [int(cv2.IMWRITE_JPEG_QUALITY), 95])
        if not ok:
            raise RuntimeError("failed to encode jpeg")
        return encoded.tobytes()

    def info(self) -> dict:
        return {
            "device": self.device,
            "width": int(self.cap.get(cv2.CAP_PROP_FRAME_WIDTH)),
            "height": int(self.cap.get(cv2.CAP_PROP_FRAME_HEIGHT)),
            "fps": self.cap.get(cv2.CAP_PROP_FPS),
            "timestamp": int(time.time()),
        }


def create_app(service: CameraService) -> Flask:
    app = Flask(__name__)

    @app.get("/health")
    def health():
        return jsonify({"ok": True, **service.info()})

    @app.get("/capture")
    def capture():
        jpeg = service.snapshot()
        return Response(jpeg, mimetype="image/jpeg")

    return app


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Host camera HTTP capture service")
    parser.add_argument("--device", default="/dev/video0")
    parser.add_argument("--host", default="0.0.0.0")
    parser.add_argument("--port", type=int, default=5000)
    parser.add_argument("--width", type=int, default=1280)
    parser.add_argument("--height", type=int, default=720)
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    service = CameraService(args.device, args.width, args.height)
    app = create_app(service)
    app.run(host=args.host, port=args.port, threaded=True)


if __name__ == "__main__":
    main()
