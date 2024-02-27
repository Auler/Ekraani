package com.lf.app;

import javafx.scene.image.Image;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;

import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 视频录制
 *
 * @author auler
 * @date 2024/2/26
 */
public class VideoRecorder {
    private static final String recorderPath = "./video";
    private static final int frameRate = 30;// 录制的帧率
    private boolean started = false;
    private ScheduledThreadPoolExecutor timer;
    private FFmpegFrameRecorder recorder;
    private FrameGrabber grabber;
    private long time = 0;

    public Image startRecorder() throws FFmpegFrameRecorder.Exception {
        //时间
//            time += (1000 / frameRate);
//            timeText.setText(Utils.getHMS(time));
        try {
            // 获取屏幕捕捉的一帧
            Frame frame = grabber.grabFrame();
            // 将这帧放到录制
            recorder.record(frame);
            // frame转换为Image
            Image convert = new JavaFXFrameConverter().convert(frame);
            // 更新UI
//                imageVideo.setImage(convert);
            return convert;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 初始化录制器
     *
     * @throws Exception
     */
    public void initRecorder() throws Exception {
        // window 建议使用 FFmpegFrameGrabber("desktop") 进行屏幕捕捉
        grabber = new FFmpegFrameGrabber("desktop");
        grabber.setFormat("gdigrab");
        grabber.setFrameRate(frameRate);//帧数
        // 捕获指定区域，不设置则为全屏
//            grabber.setImageHeight(600);
//            grabber.setImageWidth(800);
        grabber.start();

        // 用于存储视频 , 先调用stop，在释放，就会在指定位置输出文件，，这里我保存到D盘
        String recorderName = recorderPath + "/" + new Date().getTime() + ".mp4";
        recorder = new FFmpegFrameRecorder(recorderName, grabber.getImageWidth(), grabber.getImageHeight());
        recorder.setFrameRate(frameRate);// 帧率
        recorder.setFormat("mp4");// 格式
//        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);// 编码，使用编码能让视频占用内存更小
        recorder.setVideoQuality(0);//高质量
        recorder.setVideoOption("crf", "23");//智能分配码率。设置crf实际上是更改编码参数。crf默认值23，一般的设置范围是16-26，数字越大质量越差
        recorder.setVideoBitrate(2000000);// 2000000 b/s, 720P视频的合理比特率范围
        recorder.setVideoOption("preset", "slow");
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P); // yuv420p
        recorder.start();
    }

    /**
     * 释放录制器
     *
     * @throws Exception
     */
    public void releaseRecorder() throws Exception {
        System.out.println("停止捕捉屏幕");
        try {
            // 停止
            recorder.stop();
            grabber.stop();

            // 释放
            recorder.release();
            grabber.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
