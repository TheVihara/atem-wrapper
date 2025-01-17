package me.vihara.atemwrapper.example.command.listener;

import me.vihara.atemwrapper.example.command.CommandManager;

import java.util.Scanner;

public class CommandListener implements Runnable {
    private CommandManager commandManager;

    public CommandListener(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();
            String[] parts = input.split(" ");
            String commandName = parts[0];
            String[] args = new String[parts.length - 1];
            System.arraycopy(parts, 1, args, 0, parts.length - 1);
            commandManager.execute(commandName, args);
        }
    }
}