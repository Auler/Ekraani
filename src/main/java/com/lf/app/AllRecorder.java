package com.lf.app;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 视频、音频录制
 *
 * @author auler
 * @date 2024-2-26
 */
public class AllRecorder {
    private static final String RECORDER_BASE_PATH = "./video";
    private static final int FRAME_RATE = 30;// 帧率
    private static final int AUTO_CHANNEL = 2;// 通道数
    private ScheduledThreadPoolExecutor timer;// 定时器
    private FFmpegFrameRecorder recorder;// 录制器
    private FFmpegFrameGrabber videoGrabber;// 视频捕捉器
    private FFmpegFrameGrabber audioGrabber;// 音频捕捉器
    private TargetDataLine line;// 音频输入线
    private boolean stopped = false;
    private byte[] audioBytes;
    private int sampleRate;
    private int numChannels;

    /**
     * 开始录制
     */
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
                        // 视频
                        // 获取屏幕捕捉的一帧
                        Frame frame = videoGrabber.grabFrame();
                        // 将这帧放到录制
                        recorder.record(frame);
                        // frame转换为Image
                        Image convert = new JavaFXFrameConverter().convert(frame);
                        // 更新UI
                        imageView.setImage(convert);
                        t += (1000 / FRAME_RATE);
                        timeText.setText(Utils.getHMS(t));
                        //音频
                        int nBytesRead = 0;
                        while (nBytesRead == 0) {
                            nBytesRead = line.read(audioBytes, 0, line.available());//读取音频流
                        }
                        //由于我们AutoFrameRecorder指定样本位数为16位，所以需要将byte数组（8位）转换到short数组（16位）
                        int nSamplesRead = nBytesRead / 2;
                        short[] samples = new short[nSamplesRead];
                        ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
                        ShortBuffer sBuff = ShortBuffer.wrap(samples, 0, nSamplesRead);

                        recorder.recordSamples(sampleRate, numChannels, sBuff);

                    } catch (FFmpegFrameRecorder.Exception | FrameGrabber.Exception e) {
                        e.printStackTrace();
                    }
                } else if (stopped) {
                    System.out.println("stop recorder");
                    stopRecorder();
                }

            }
        }, 0, 1000 / FRAME_RATE, TimeUnit.MILLISECONDS);
    }

    /**
     * 初始化录制器
     */
    public void initRecorder() throws Exception {
        // 视频捕捉器
        videoGrabber = new FFmpegFrameGrabber("desktop");
        videoGrabber.setFormat("gdigrab");
        videoGrabber.setFrameRate(FRAME_RATE);//帧数
        // 捕获指定区域，不设置则为全屏
//        videoGrabber.setImageHeight(600);
//        videoGrabber.setImageWidth(800);
        videoGrabber.start();
        // 音频捕捉器
        AudioFormat audioFormat = new AudioFormat(44100, 16, 2, true, false);
        // 得到所有Mixer信息，通俗的说就是声音设备信息
//        Mixer.Info[] minfoSet = AudioSystem.getMixerInfo();
//        Mixer mixer = AudioSystem.getMixer(minfoSet[3]);
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);//默认寻找第一个可用的录音设备
        //初始化TargeLine，与使用JDK一样
        // TargetDataLine line = (TargetDataLine)mixer.getLine(dataLineInfo); //可以使用声音设备索引来录制音频
        line = (TargetDataLine) AudioSystem.getLine(dataLineInfo);//这个就是查找默认可用的录音设备，没有特殊指定
        line.open(audioFormat);
        line.start();
        sampleRate = (int) audioFormat.getSampleRate();
        numChannels = audioFormat.getChannels();
        // Let's initialize our audio buffer...
        int audioBufferSize = sampleRate * numChannels;
        audioBytes = new byte[audioBufferSize];

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
        try {
            recorder.stop();
            videoGrabber.stop();
            recorder.release();
            videoGrabber.release();
            line.stop();
            line.close();
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
