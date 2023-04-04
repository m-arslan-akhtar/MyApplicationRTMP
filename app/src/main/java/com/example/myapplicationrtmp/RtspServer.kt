package com.example.myapplicationrtmp
//import android.content.Context
import android.view.SurfaceView
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
//import net.majorkernelpanic.streaming.MediaStream
//import net.majorkernelpanic.streaming.SessionBuilder
//import net.majorkernelpanic.streaming.rtsp.RtspClient
//import net.majorkernelpanic.streaming.video.VideoQuality
import org.slf4j.LoggerFactory


////////////////////////////////////////////////////////////////////////////
import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Environment
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.handler.codec.rtsp.*
import io.netty.handler.codec.rtsp.RtspHeaderNames.CONTENT_LENGTH
import java.io.File
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.*
import io.netty.handler.codec.rtsp.RtspVersions


class RtspServer {

        private val logger = LoggerFactory.getLogger(RtspServer::class.java)
        private val bossGroup = NioEventLoopGroup()
        private val workerGroup = NioEventLoopGroup()

        fun startServer() {
            try {
                val bootstrap = ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel::class.java)
                    .childHandler(object : ChannelInitializer<SocketChannel>() {
                        override fun initChannel(ch: SocketChannel) {
                            ch.pipeline().addLast("decoder", RtspDecoder())
                            ch.pipeline().addLast("encoder", RtspEncoder())
                            ch.pipeline().addLast("requestDecoder", RtspRequestDecoder())
                            ch.pipeline().addLast("responseEncoder", RtspResponseEncoder())
                            ch.pipeline().addLast("handler", RtspServerHandler())
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)

                val channel = bootstrap.bind(8554).sync().channel()
                logger.info("RTSP server started on port 8554")

                channel.closeFuture().sync()
            } finally {
                workerGroup.shutdownGracefully()
                bossGroup.shutdownGracefully()
            }
        }

        class RtspServerHandler : SimpleChannelInboundHandler<Any>() {

            override fun channelRead0(ctx: ChannelHandlerContext, msg: Any) {
                // handle incoming RTSP messages here
            }

            override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
                // handle exceptions here
            }
        }




/*
    class RtspServerHandler :
        SimpleChannelInboundHandler<Any>() {

        private var videoStreamer: VideoStreamer? = null

        override fun channelRead0(ctx: ChannelHandlerContext, msg: Any) {
            if (msg is RtspRequest) {
                if (msg.method == RtspMethod.OPTIONS) {
                    // handle OPTIONS request
                    val response = RtspResponse(RtspVersions.RTSP_1_0, RtspResponseStatus.OK)
                    response.headers().add(RtspHeaders.Names.PUBLIC, "DESCRIBE, SETUP, TEARDOWN, PLAY")
                    ctx.writeAndFlush(response)
                } else if (msg.method == RtspMethod.DESCRIBE) {
                    // handle DESCRIBE request
                    val response = RtspResponse(RtspVersions.RTSP_1_0, RtspResponseStatus.OK)
                    response.headers().add(RtspHeaders.Names.CONTENT_TYPE, Values.APPLICATION_SDP)
                    response.headers().add(CONTENT_LENGTH, sdpString.length.toString())
                    response.content().writeBytes(sdpString.toByteArray())
                    ctx.writeAndFlush(response)
                } else if (msg.method == RtspMethod.SETUP) {
                    // extract the track id from the SETUP request
                    val trackId = msg.uri().substringAfterLast("=").toInt()

                    // initialize the VideoStreamer with the SurfaceView and start streaming
                    videoStreamer = VideoStreamer(
                        ctx.channel().eventLoop().parent().next(),
                        trackId,
                        surfaceView
                    )
                    videoStreamer?.startStreaming()

                    // send the response to the SETUP request
                    val response = RtspResponse(RtspVersions.RTSP_1_0, RtspResponseStatus.OK)
                    response.headers().add(Names.TRANSPORT, "RTP/AVP/TCP;unicast;client_port=${msg.headers().get(Names.TRANSPORT)?.substringAfterLast("=")}")
                    ctx.writeAndFlush(response)
                } else if (msg.method == RtspMethod.PLAY) {
                    // send the response to the PLAY request
                    val response = RtspResponse(RtspVersions.RTSP_1_0, RtspResponseStatus.OK)
                    ctx.writeAndFlush(response)
                } else if (msg.method == RtspMethod.TEARDOWN) {
                    // stop the VideoStreamer and send the response to the TEARDOWN request
                    videoStreamer?.stopStreaming()
                    val response = RtspResponse(RtspVersions.RTSP_1_0, RtspResponseStatus.OK)
                    ctx.writeAndFlush(response)
                }
            }
        }

//        override fun messageReceived(ctx: ChannelHandlerContext?, msg: Any?) {
//        }
    }*/


    /*class RtspServerHandler : SimpleChannelInboundHandler<Any>() {
        private var videoStreamer: VideoStreamer? = null

        override fun channelRead0(ctx: ChannelHandlerContext, msg: Any) {
            if (msg is RtspRequest) {
                if (msg.method == RtspMethod.SETUP) {
                    // extract the track id from the SETUP request
                    val trackId = msg.uri.substringAfterLast("=").toInt()

                    // initialize the VideoStreamer with the SurfaceView and start streaming
                    videoStreamer = VideoStreamer(
                        ctx.channel().eventLoop().parent().next(),
                        trackId,
                        surfaceView
                    )
                    videoStreamer?.startStreaming()

                    // send the response to the SETUP request
                    val response = RtspResponse(RtspResponseStatus.OK)
                    response.headers["Transport"] = "RTP/AVP/TCP;unicast;client_port=${
                        msg.headers["Transport"]!!.substringAfterLast("=")
                    }"
                    ctx.writeAndFlush(response)
                } else if (msg.method == RtspMethod.PLAY) {
                    // send the response to the PLAY request
                    val response = RtspResponse(RtspResponseStatus.OK)
                    ctx.writeAndFlush(response)
                } else if (msg.method == RtspMethod.TEARDOWN) {
                    // stop the VideoStreamer and send the response to the TEARDOWN request
                    videoStreamer?.stopStreaming()
                    val response = RtspResponse(RtspResponseStatus.OK)
                    ctx.writeAndFlush(response)
                }
            }
        }

        override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
            cause.printStackTrace()
            ctx.close()
        }
    }*/

}




/*class VideoStreamer(private val context: Context, private val surfaceView: SurfaceView) {

    fun startStreaming() {
//        val mediaStream = MediaStream(context, surfaceView)
//        mediaStream.videoEncoder = MediaStream.VIDEO_H264
//        mediaStream.videoQuality = VideoQuality(640, 480, 30)
        val builder = SessionBuilder.getInstance()
        builder.context = context
        builder.audioEncoder = SessionBuilder.AUDIO_NONE
        builder.videoEncoder = SessionBuilder.VIDEO_H264
//        builder.setVideoQuality(640, 480, 30)
        builder.videoQuality = VideoQuality.parseQuality(640.toString())
        builder.setSurfaceView(surfaceView)

        val client = RtspClient()
        client.session = builder.build()
        client.setCredentials("username", "password")
        client.setServerAddress("rtsp://<server-ip-address>:8554/stream", 8554)
        client.startStream(2)

//        val client = RtspClient()
//        client.session = mediaStream
//        client.setCredentials("username", "password")
//        client.setServerAddress("rtsp://<server-ip-address>:8554/stream")
//        client.startStream()
    }
}*/




