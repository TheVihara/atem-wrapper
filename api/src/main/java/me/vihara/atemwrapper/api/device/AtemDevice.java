package me.vihara.atemwrapper.api.device;

import java.util.Map;

public interface AtemDevice {
    float getProtocolVersion();
    String getModelName();
    int getVideoInputs();
    int getVideoOutputs();
    Map<String, Object> getExtraData();
    Integer[] getRouting();
    Boolean[] getOutputLocks();

    void setRoute(int output, int input);
    void setOutputLock(int output);
}
