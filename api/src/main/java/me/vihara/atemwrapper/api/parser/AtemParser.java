package me.vihara.atemwrapper.api.parser;

public interface AtemParser<T> {
    T parse(String text);
}
