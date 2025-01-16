package me.vihara.atemwrapper.api.parser.impl;

import me.vihara.atemwrapper.api.parser.AtemParser;

public class ConfigurationAtemParser implements AtemParser<String> {
    @Override
    public String parse(String text) {
        return text;
    }
}
