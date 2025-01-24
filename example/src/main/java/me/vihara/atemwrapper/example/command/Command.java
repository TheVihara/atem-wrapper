package me.vihara.atemwrapper.example.command;

public abstract class Command {
    String name;
    String description;

    public abstract void execute(String[] args);
}
