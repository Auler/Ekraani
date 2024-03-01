package com.lf.app;

import javafx.scene.image.Image;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.JavaFXFrameConverter;

/**
 * 捕捉视频
 * @author auler
 * @date 2024-02-29
 */
public class ScreenGrabber {
    private FFmpegFrameGrabber grabber;// 视频捕捉器

    public Image startGrabber(FFmpegFrameRecorder recorder) throws Exception {
        // 视频
        // 获取屏幕捕捉的一帧
        Frame frame = grabber.grabFrame();
        // 将这帧放到录制
        recorder.record(frame);
        // frame转换为Image
        Image convert = new JavaFXFrameConverter().convert(frame);
        return convert;
    }

    public FFmpegFrameGrabber initGrabber(int frameRate) throws Exception {
        System.out.println("init screenGrabber");
        grabber = new FFmpegFrameGrabber("desktop");
        grabber.setFormat("gdigrab");
        grabber.setFrameRate(frameRate);//帧数
        // 捕获指定区域，不设置则为全屏
//        videoGrabber.setImageHeight(600);
//        videoGrabber.setImageWidth(800);
        grabber.start();
        return grabber;
    }
    public void stopGrabber() {
        System.out.println("stop screenGrabber");
        try {
            grabber.stop();
            grabber.release();
        } catch (FFmpegFrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }

}
