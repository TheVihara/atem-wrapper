package me.vihara.atemwrapper.api.parser.impl;

import me.vihara.atemwrapper.api.parser.AtemParser;

import java.util.HashMap;
import java.util.Map;

public class InputLabelsAtemParser implements AtemParser<Map<Integer, String>> {
    @Override
    public Map<Integer, String> parse(String text) {
        Map<Integer, String> inputLabels = new HashMap<>();
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.matches("\\d+\\s+.*")) {
                String[] parts = line.split("\\s+", 2);
                int index = Integer.parseInt(parts[0]);
                String label = parts[1];
                inputLabels.put(index, label);
            }
        }
        return inputLabels;
    }
}