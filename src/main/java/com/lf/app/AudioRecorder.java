package com.lf.app;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 音频录制
 * @author auler
 * @date 2024/2/26
 */
public class AudioRecorder {
    private static final String recorderPath = "./video";
    private static final int frameRate = 30;// 录制的帧率
    private boolean started = false;
    private ScheduledThreadPoolExecutor timer;
    private FFmpegFrameRecorder recorder;
    private TargetDataLine line;
    private byte[] audioBytes;
    private int sampleRate;
    private int numChannels;

    /**
     * 开启录制
     *
     * @throws FFmpegFrameRecorder.Exception
     */
    public void startRecorder() throws FFmpegFrameRecorder.Exception {
        try {
            int nBytesRead = 0;
            while (nBytesRead == 0) {
                nBytesRead = line.read(audioBytes, 0, line.available());
            }
            int nSamplesRead = nBytesRead / 2;
            short[] samples = new short[nSamplesRead];

            ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
            ShortBuffer sBuff = ShortBuffer.wrap(samples, 0, nSamplesRead);

            recorder.recordSamples(sampleRate, numChannels, sBuff);
//                    System.out.println("recorder samples size: " + nSamplesRead);
        } catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 初始化录制
     *
     * @throws Exception
     */
    public void initRecorder() throws Exception {
        System.out.println("init recorder");
        String recorderName = recorderPath + "/" + System.currentTimeMillis() + ".mp3";
        recorder = new FFmpegFrameRecorder(recorderName, 2);
        recorder.setAudioOption("crf", "0");
        recorder.setAudioQuality(0);// Highest quality
        recorder.setAudioBitrate(16000);// 16 Kbps
        recorder.setSampleRate(44100);// 44.1MHZ
        recorder.setAudioChannels(2);// 2 channel
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_MP3);// mp3
        recorder.start();
        AudioFormat audioFormat = new AudioFormat(44100, 16, 2, true, false);

        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
        try {
            line = (TargetDataLine) AudioSystem.getLine(dataLineInfo);//这个就是查找默认可用的录音设备，没有特殊指定
            line.open(audioFormat);
            line.start();

            sampleRate = (int) audioFormat.getSampleRate();
            numChannels = audioFormat.getChannels();

            int audioBufferSize = sampleRate * numChannels;
            audioBytes = new byte[audioBufferSize];

        } catch (LineUnavailableException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * 停止录制
     *
     * @throws Exception
     */
    public void releaseRecorder() throws Exception {
        System.out.println("release recorder");
        try {
            // 停止
            recorder.stop();
            line.stop();
            line.close();
            // 释放
            recorder.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 停止
    }
}
