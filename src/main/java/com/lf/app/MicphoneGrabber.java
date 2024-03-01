package com.lf.app;

import org.bytedeco.javacv.FFmpegFrameRecorder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * 音频输入线
 *
 * @author auler
 * @date 2024-03-01
 */
public class MicphoneGrabber {
    private TargetDataLine line;// 音频输入线
    private boolean stopped = false;
    private byte[] audioBytes;
    private int sampleRate;
    private int numChannels;

    public void startGrabber(FFmpegFrameRecorder recorder) {
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
        try {
            recorder.recordSamples(sampleRate, numChannels, sBuff);
        } catch (FFmpegFrameRecorder.Exception e) {
            e.printStackTrace();
        }
    }

    public void initGrabber() throws Exception {
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
    }

    public void stopGrabber() {
        line.stop();
        line.close();
    }
}
