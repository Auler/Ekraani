package com.lf.app;

import org.bytedeco.javacv.FFmpegFrameRecorder;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * 捕捉扩音器声音
 * 需要设置声音里的录制，选择立体混声，并且开启
 *
 * @author auler
 * @date 2024/2/29
 */
public class SpeakerGrabber {
    private TargetDataLine line;
    private byte[] audioBytes;
    private int sampleRate;
    private int numChannels;

    public void startGrabber(FFmpegFrameRecorder recorder) {
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

    public void initGrabber() {
        System.out.println("init speaker grabber");
        AudioFormat audioFormat = new AudioFormat(44100, 16, 2, true, false);
//        AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 16, 44100, false);
        //选择
        Mixer.Info[] minfoSet = AudioSystem.getMixerInfo();
        Mixer mixer = AudioSystem.getMixer(minfoSet[7]);//根据机子的情况定
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
        System.out.println("mixer:" + mixer.getMixerInfo().getName());
        try {
//            line = (TargetDataLine) AudioSystem.getLine(dataLineInfo);//这个就是查找默认可用的录音设备，没有特殊指定
            line = (TargetDataLine) mixer.getLine(dataLineInfo);// 指定设备
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

    public void stopGrabber() {
        System.out.println("stop speaker grabber");
        try {
            // 停止
            line.stop();
            line.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
