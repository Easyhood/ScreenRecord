package com.easyhood.screenrecord;

import android.hardware.display.DisplayManager;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 功能：H264码流输出管理类
 * 详细描述：
 * 作者：guan_qi
 * 创建日期：2023-03-20
 */
public class H264Encoder extends Thread{

    private static final String TAG = "H264Encoder";
    /**
     * 数据源
     */
    private MediaProjection mMediaProjection;

    private MediaFormat format;

    private int width;
    private int height;

    /**
     * 编码器
     */
    private MediaCodec mediaCodec;

    /**
     * 构造方法
     *
     * @param mMediaProjection 数据源
     */
    public H264Encoder(MediaProjection mMediaProjection) {
        this.mMediaProjection = mMediaProjection;
        this.width = 640;
        this.height = 1920;
        // 编码格式
        format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
        try {
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            // 设置帧率
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 20);
            // 设置关键帧 每过30帧 一个I帧
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 30);
            // 设置比特率/码率
            format.setInteger(MediaFormat.KEY_BIT_RATE, width * height);
            // 设置编码器颜色格式
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            // 编码标志位
            mediaCodec.configure(format, null, null,
                    MediaCodec.CONFIGURE_FLAG_ENCODE);
            Surface surface = mediaCodec.createInputSurface();
            // 创建虚拟的屏幕
            mMediaProjection.createVirtualDisplay(
                    "Easyhood", width, height,
                    2,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    surface,
                    null,null
            );

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        mediaCodec.start();
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        while (true) {
            //直接拿到输出，不用管输入，输入已经被实现了
            int outIndex = mediaCodec.dequeueOutputBuffer(info, 10000);
            if (outIndex >= 0) {
                //编码的数据
                ByteBuffer byteBuffer = mediaCodec.getOutputBuffer(outIndex);
                byte[] ba = new byte[byteBuffer.remaining()];
                // 将容器的byteBuffer  内部的数据 转移到 byte[]中
                byteBuffer.get(ba);
                FileUtils.writeBytes(ba);
                FileUtils.writeContent(ba);
                mediaCodec.releaseOutputBuffer(outIndex, false);
            }
        }
    }
}
