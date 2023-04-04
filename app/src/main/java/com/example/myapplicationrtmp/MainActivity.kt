package com.example.myapplicationrtmp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val surfaceView = findViewById<SurfaceView>(R.id.surface_view)
// Usage example:
        val rtspServer = RtspServer()
        rtspServer.startServer()

        // rtsp://192.168.0.119:8554/fox-and-bird
        val videoStreamer = VideoStreamer(applicationContext, surfaceView)
        videoStreamer.startStreaming()

    }

}