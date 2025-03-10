package me.vihara.atemwrapper.api.event.impl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.vihara.atemwrapper.api.device.AtemDevice;
import me.vihara.atemwrapper.api.event.AtemEvent;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Getter
public class AtemInputLabelChangeEvent implements AtemEvent {
    AtemDevice device;
    int output;
    String oldLabel;
    String newLabel;
}
