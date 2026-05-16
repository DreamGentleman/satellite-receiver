package com.yxh.fangs.bean;

public class FrequencyChannel {

    private final int channelNo;
    private final double frequencyMhz;
    private int offsetHz;

    public FrequencyChannel(int channelNo, double frequencyMhz, int offsetHz) {
        this.channelNo = channelNo;
        this.frequencyMhz = frequencyMhz;
        this.offsetHz = offsetHz;
    }

    public int getChannelNo() {
        return channelNo;
    }

    public double getFrequencyMhz() {
        return frequencyMhz;
    }

    public int getOffsetHz() {
        return offsetHz;
    }

    public void setOffsetHz(int offsetHz) {
        this.offsetHz = offsetHz;
    }
}
