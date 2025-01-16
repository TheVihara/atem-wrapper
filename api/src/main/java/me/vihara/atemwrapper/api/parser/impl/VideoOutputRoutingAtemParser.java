package me.vihara.atemwrapper.api.parser.impl;

import me.vihara.atemwrapper.api.parser.AtemParser;

import java.util.HashMap;
import java.util.Map;

public class VideoOutputRoutingAtemParser implements AtemParser<Map<Integer, Integer>> {
    @Override
    public Map<Integer, Integer> parse(String text) {
        Map<Integer, Integer> videoOutputRouting = new HashMap<>();
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.matches("\\d+\\s+\\d+")) {
                String[] parts = line.split("\\s+");
                int output = Integer.parseInt(parts[0]);
                int input = Integer.parseInt(parts[1]);
                videoOutputRouting.put(output, input);
            }
        }
        return videoOutputRouting;
    }
}