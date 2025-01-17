package me.vihara.atemwrapper.api.device;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

public interface AtemDevice {
    float getProtocolVersion();
    Info getInfo();
    String getConfiguration();
    Map<Integer, String> getInputLabels();
    Map<Integer, String> getOutputLabels();
    Map<Integer, String> getVideoInputStatus();
    Map<Integer, Integer> getVideoOutputRouting();
    Map<Integer, AtemLock> getVideoOutputLocks();

    void setInputLabel(int output, String label);
    void setOutputLabel(int output, String label);
    void setOutputRoute(int output, int input);
    void setOutputLock(int output, AtemLock lock);

    @FieldDefaults(level = PRIVATE)
    @Getter
    @Setter
    class Info {
        boolean devicePresent;
        String modelName;
        String friendlyName;
        String uniqueId;
        int videoInputs;
        int videoOutputs;

        @Override
        public String toString() {
            return "Info{" +
                    "devicePresent=" + devicePresent +
                    ", modelName='" + modelName + '\'' +
                    ", friendlyName='" + friendlyName + '\'' +
                    ", uniqueId='" + uniqueId + '\'' +
                    ", videoInputs=" + videoInputs +
                    ", videoOutputs=" + videoOutputs +
                    '}';
        }
    }
}
