package com.example.photoprintapp.camera

object HostCameraConfig {
    // Waydroid harus akses host Linux via waydroid0, bukan localhost.
    private const val HOST_IP = "192.168.240.1"

    const val RTSP_URL = "rtsp://$HOST_IP:8554/booth"
    const val CAPTURE_URL = "http://$HOST_IP:5000/capture"
    const val HEALTH_URL = "http://$HOST_IP:5000/health"
    const val RECONNECT_DELAY_MS = 1500L
}
