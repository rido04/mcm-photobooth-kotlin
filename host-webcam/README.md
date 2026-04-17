# Host Webcam Draft

Draft ini memindahkan akses webcam ke Linux host.

Arsitektur:
- Preview: RTSP `rtsp://HOST_IP:8554/booth`
- Capture: HTTP `http://HOST_IP:5000/capture`
- Health check: HTTP `http://HOST_IP:5000/health`

## Kebutuhan host

- Linux host
- Python 3.10+
- `ffmpeg`
- webcam tersedia sebagai `/dev/video0`
- Waydroid bisa reach IP host

## Install dependency Python

```bash
cd host-webcam
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

## Jalankan HTTP capture service

```bash
source .venv/bin/activate
python camera_host.py --device /dev/video0 --host 0.0.0.0 --port 5000
```

Tes dari host:

```bash
curl http://127.0.0.1:5000/health
curl http://127.0.0.1:5000/capture --output test.jpg
```

## Jalankan RTSP preview

1. Download `mediamtx` dari release resmi dan jalankan:

```bash
./mediamtx
```

2. Publish webcam ke RTSP:

```bash
./publish_rtsp.sh /dev/video0
```

Default output:

```text
rtsp://HOST_IP:8554/booth
```

## Di Android app

Edit:

- `app/src/main/java/com/example/photoprintapp/camera/HostCameraConfig.kt`

Ganti `HOST_IP` ke IP Linux host yang bisa diakses Waydroid.

## Catatan mini PC yang perlu diketahui

Info ini akan membantu kalau mau saya rapikan lebih lanjut:

- distro Linux
- apakah `ffmpeg` sudah ada
- apakah mau preview 720p atau 1080p
- apakah webcam output MJPEG atau YUYV
- IP host yang bisa diakses dari Waydroid
- apakah kamu mau semua service jalan via `systemd`
