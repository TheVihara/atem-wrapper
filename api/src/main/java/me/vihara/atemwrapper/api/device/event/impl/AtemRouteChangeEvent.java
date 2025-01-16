package me.vihara.atemwrapper.api.device.event.impl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.vihara.atemwrapper.api.device.event.AtemEvent;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Getter
public class AtemRouteChangeEvent implements AtemEvent {
    int output;
    int oldInput;
    int newInput;
}
