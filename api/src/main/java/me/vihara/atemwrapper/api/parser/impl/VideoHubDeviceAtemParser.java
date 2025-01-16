package me.vihara.atemwrapper.api.parser.impl;

import me.vihara.atemwrapper.api.device.AtemDevice;
import me.vihara.atemwrapper.api.parser.AtemParser;

public class VideoHubDeviceAtemParser implements AtemParser<AtemDevice.Info> {
    @Override
    public AtemDevice.Info parse(String text) {
        AtemDevice.Info deviceInfo = new AtemDevice.Info();
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.startsWith("Device present:")) {
                deviceInfo.setDevicePresent(Boolean.parseBoolean(line.split(":")[1].trim()));
            } else if (line.startsWith("Model name:")) {
                deviceInfo.setModelName(line.split(":")[1].trim());
            } else if (line.startsWith("Friendly name:")) {
                deviceInfo.setFriendlyName(line.split(":")[1].trim());
            } else if (line.startsWith("Unique ID:")) {
                deviceInfo.setUniqueId(line.split(":")[1].trim());
            } else if (line.startsWith("Video inputs:")) {
                deviceInfo.setVideoInputs(Integer.parseInt(line.split(":")[1].trim()));
            } else if (line.startsWith("Video outputs:")) {
                deviceInfo.setVideoOutputs(Integer.parseInt(line.split(":")[1].trim()));
            }
        }
        return deviceInfo;
    }
}