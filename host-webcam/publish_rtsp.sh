#!/usr/bin/env bash
set -euo pipefail

DEVICE="${1:-/dev/video0}"
FRAMERATE="${FRAMERATE:-30}"
VIDEO_SIZE="${VIDEO_SIZE:-1280x720}"
RTSP_URL="${RTSP_URL:-rtsp://127.0.0.1:8554/booth}"

exec ffmpeg \
  -f v4l2 \
  -input_format mjpeg \
  -framerate "${FRAMERATE}" \
  -video_size "${VIDEO_SIZE}" \
  -i "${DEVICE}" \
  -an \
  -vf format=yuv420p \
  -c:v libx264 \
  -preset veryfast \
  -tune zerolatency \
  -pix_fmt yuv420p \
  -f rtsp \
  "${RTSP_URL}"
