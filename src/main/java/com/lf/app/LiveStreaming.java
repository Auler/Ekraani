package com.lf.app;

import org.bytedeco.javacv.*;

/**
 * 直播服务器
 *
 * @author auler
 * @date 2024/2/27
 */
public class LiveStreaming {

    public static void main(String[] args) {
        String input = "rtsp://your.rtsp.source"; // RTSP 源地址
        String output = "rtmp://your.rtmp.server/app/streamKey"; // RTMP 服务器地址和流密钥

        try {
            // 创建 FFmpegFrameGrabber 实例来捕获视频流
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(input);
            grabber.start();

            // 创建 FFmpegFrameRecorder 实例来推送视频流到 RTMP 服务器
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(output, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels());
            recorder.setVideoCodec(grabber.getVideoCodec());
            recorder.setAudioCodec(grabber.getAudioCodec());
            recorder.setFrameRate(grabber.getFrameRate());
            recorder.setSampleRate(grabber.getSampleRate());
            recorder.start();

            // 循环抓取并推送帧
            Frame frame;
            while ((frame = grabber.grabFrame()) != null) {
                recorder.record(frame);
            }

            // 停止抓取和推送，并释放资源
            recorder.stop();
            grabber.stop();
        } catch (FrameGrabber.Exception | FrameRecorder.Exception e) {
            e.printStackTrace();
        }
    }
}
