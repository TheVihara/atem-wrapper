package me.vihara.atemwrapper.example.command;

public abstract class Command {
    String name;
    String description;

    protected abstract void execute(String[] args);
}
