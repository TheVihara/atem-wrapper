package me.vihara.atemwrapper.api.parser.impl;

import me.vihara.atemwrapper.api.device.AtemLock;
import me.vihara.atemwrapper.api.parser.AtemParser;

import java.util.HashMap;
import java.util.Map;

public class VideoOutputLocksAtemParser implements AtemParser<Map<Integer, AtemLock>> {
    @Override
    public Map<Integer, AtemLock> parse(String text) {
        Map<Integer, AtemLock> videoOutputLocks = new HashMap<>();
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.matches("\\d+\\s+[OUL]")) {
                String[] parts = line.split("\\s+");
                int output = Integer.parseInt(parts[0]);
                AtemLock lock = parts[1].equals("O")
                        ? AtemLock.LOCKED : parts[1].equals("L")
                        ? AtemLock.LOCKED_OTHER : AtemLock.UNLOCKED;
                videoOutputLocks.put(output, lock);
            }
        }
        return videoOutputLocks;
    }
}