package com.example.myapplicationrtmp

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.view.SurfaceView
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer

class VideoStreamer(private val context: Context, private val surfaceView: SurfaceView) {

    private lateinit var mediaCodec: MediaCodec
    private lateinit var mediaMuxer: MediaMuxer

    fun startStreaming() {
        try {
           // mediaMuxer = MediaMuxer("/sdcard/video.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val dir = context.filesDir // get the directory for storing files in internal storage
            val videoFile = File(dir, "video.mp4") // create a File object for the video file
            mediaMuxer = MediaMuxer(videoFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4) // set the path to the video file

            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)

            val mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 640, 480)
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1000000)
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

            val surface = surfaceView.holder.surface
            mediaCodec.setInputSurface(surface)
            mediaCodec.start()

            // send the encoded data to the MediaMuxer
            var trackIndex = -1
            var isMuxerStarted = false
            while (true) {
                val bufferInfo = MediaCodec.BufferInfo()
                val outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, -1)
                if (outputBufferIndex >= 0) {
                    val outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex)
                    if (!isMuxerStarted) {
                        trackIndex = mediaMuxer.addTrack(mediaCodec.outputFormat)
                        mediaMuxer.start()
                        isMuxerStarted = true
                    }
                    outputBuffer?.let { mediaMuxer.writeSampleData(trackIndex, it, bufferInfo) }
                    mediaCodec.releaseOutputBuffer(outputBufferIndex, false)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
