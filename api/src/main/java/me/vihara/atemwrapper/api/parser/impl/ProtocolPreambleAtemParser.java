package me.vihara.atemwrapper.api.parser.impl;

import me.vihara.atemwrapper.api.parser.AtemParser;

public class ProtocolPreambleAtemParser implements AtemParser<Float> {
    @Override
    public Float parse(String text) {
        try {
            if (text.startsWith("Version: ")) {
                String numberPart = text.substring(9).trim();
                return Float.parseFloat(numberPart);
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid version format: " + text);
        }
        return 0f;
    }
}
