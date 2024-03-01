package com.lf.app;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameGrabber;

import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 视频、音频录制
 *
 * @author auler
 * @date 2024-2-26
 */
public class Recorder {
    private static final String RECORDER_BASE_PATH = "./video";
    private static final int FRAME_RATE = 30;// 帧率
    private ScheduledThreadPoolExecutor timer;// 定时器
    private ScheduledThreadPoolExecutor timer2;// 定时器
    private ScheduledThreadPoolExecutor timer3;// 定时器
    private FFmpegFrameRecorder recorder;// 录制器
    private boolean stopped = false;
    private ScreenGrabber screenGrabber;
    private SpeakerGrabber speakerGrabber;
    private AudioGrabber audioGrabber;
    private MicphoneGrabber micphoneGrabber;

    public void startRecorder(ImageView imageView, Text timeText) throws Exception {
        initRecorder();
        timer = new ScheduledThreadPoolExecutor(1);
        timer.scheduleAtFixedRate(new Runnable() {
            long t = 0;

            @Override
            public void run() {
                if (!stopped) {
//                    System.out.println("start recorder");
                    try {
                        Image convert = screenGrabber.startGrabber(recorder);
                        // 更新UI
                        imageView.setImage(convert);
                        t += (1000 / FRAME_RATE);
                        timeText.setText(Utils.getHMS(t));
                    } catch (FFmpegFrameRecorder.Exception | FrameGrabber.Exception e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (stopped) {
                    System.out.println("stop recorder");
                    stopRecorder();
                }

            }
        }, 0, 1000 / FRAME_RATE, TimeUnit.MILLISECONDS);
        timer2 = new ScheduledThreadPoolExecutor(1);
        timer2.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!stopped) {
                    try {
                        audioGrabber.startGrabber(recorder);
//                        micphoneGrabber.startGrabber(recorder);
//                        speakerGrabber.startGrabber(recorder);
                    } catch (FFmpegFrameGrabber.Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }, 0, 10, TimeUnit.MILLISECONDS);

    }


    /**
     * 初始化录制器
     *
     * @throws Exception
     */
    public void initRecorder() throws Exception {
        System.out.println("init recorder");
        //捕捉器
        screenGrabber = new ScreenGrabber();
        FFmpegFrameGrabber videoGrabber = screenGrabber.initGrabber(FRAME_RATE);

//        micphoneGrabber = new MicphoneGrabber();
//        micphoneGrabber.initGrabber();
//        speakerGrabber = new SpeakerGrabber();
//        speakerGrabber.initGrabber();
        audioGrabber = new AudioGrabber();
        audioGrabber.initGrabber();

        // 初始化录制器
        String recorderName = RECORDER_BASE_PATH + "/" + new Date().getTime() + ".mp4";
        recorder = new FFmpegFrameRecorder(recorderName, videoGrabber.getImageWidth(), videoGrabber.getImageHeight(), videoGrabber.getAudioChannels());
        recorder.setFrameRate(FRAME_RATE);// 帧率
        recorder.setFormat("mp4");// 格式
//        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);// 编码，使用编码能让视频占用内存更小
        recorder.setVideoQuality(0);//高质量
        recorder.setVideoOption("crf", "23");//智能分配码率。设置crf实际上是更改编码参数。crf默认值23，一般的设置范围是16-26，数字越大质量越差
        recorder.setVideoBitrate(2000000);// 2000000 b/s, 720P视频的合理比特率范围
        recorder.setVideoOption("preset", "slow");
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P); // yuv420p
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);

        recorder.setInterleaved(true); // 设置交错的音频和视频帧
        recorder.setSampleRate(44100);// 采样频率
        recorder.setAudioBitrate(128000); // 音频比特率
        recorder.setAudioChannels(2); // 双通道
        recorder.setAudioOption("crf", "0"); // 分配码率越小越好
        recorder.setAudioQuality(0); // Highest quality
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.start();
    }

    /**
     * 停止录制
     */
    public void stopRecorder() {
        System.out.println("stop recorder");
        timer.shutdownNow();
        timer2.shutdownNow();
        timer3.shutdownNow();
        try {
//            speakerGrabber.stopGrabber();
//            micphoneGrabber.stopGrabber();
            audioGrabber.stopGrabber();
            recorder.stop();
            recorder.release();
            screenGrabber.stopGrabber();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean getStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
}
