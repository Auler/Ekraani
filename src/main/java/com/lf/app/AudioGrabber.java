package com.lf.app;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

/**
 * 音频抓取器
 * 需要下载Screen.Capturer.Recorder
 * @author auler
 * @date 2024-03-01
 */
public class AudioGrabber {
    private FFmpegFrameGrabber grabber;

    public void startGrabber(FFmpegFrameRecorder recorder) throws FFmpegFrameGrabber.Exception {
        Frame frame = grabber.grabSamples();
        if (frame != null) {
            try {
                recorder.recordSamples(48000, 2, frame.samples);
            } catch (FFmpegFrameRecorder.Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void initGrabber() throws FFmpegFrameGrabber.Exception {
        grabber = new FFmpegFrameGrabber("audio=virtual-audio-capturer");
        grabber.setFormat("dshow");
        grabber.start();
    }
    public void stopGrabber() throws FFmpegFrameGrabber.Exception {
        grabber.stop();
        grabber.release();
    }
}
