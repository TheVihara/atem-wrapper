package me.vihara.atemwrapper.api.device;

import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(level = PRIVATE)
@Getter
public abstract class AtemDevice {
    float protocolVersion;
    String modelName;
    int videoInputs;
    int videoOutputs;
    Map<String, Object> extraData;
    Integer[] routing;
    Boolean[] outputLocks;
}
