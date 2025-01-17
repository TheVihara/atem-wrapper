package me.vihara.atemwrapper.example.command;

import java.util.HashMap;
import java.util.Map;

public class CommandManager {
    private Map<String, Command> commands = new HashMap<>();

    public void register(String name, Command command) {
        commands.put(name, command);
    }

    public void execute(String name, String[] args) {
        Command command = commands.get(name);
        if (command != null) {
            command.execute(args);
        } else {
            System.out.println("No such command: " + name);
        }
    }
}