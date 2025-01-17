package me.vihara.atemwrapper.example;

import me.vihara.atemwrapper.core.EventManager;
import me.vihara.atemwrapper.example.command.CommandManager;
import me.vihara.atemwrapper.example.command.Command;
import me.vihara.atemwrapper.example.command.SetRouteCommand;
import me.vihara.atemwrapper.example.command.listener.CommandListener;
import me.vihara.atemwrapper.example.device.manager.DeviceManager;
import me.vihara.atemwrapper.example.listener.AtemListener;

import java.util.logging.Logger;

public class ExampleApp {
    public static ExampleApp INSTANCE;
    public static Logger LOGGER;
    private CommandManager commandManager;

    protected ExampleApp() {
        INSTANCE = this;
        LOGGER = Logger.getLogger("ExampleApp");

        EventManager.INSTANCE.addListener(new AtemListener());
        DeviceManager.INSTANCE.postBootstrap();

        commandManager = new CommandManager();
        registerCommands();

        CommandListener listener = new CommandListener(commandManager);
        new Thread(listener).start();

        LOGGER.info("ExampleApp done (???ms)");
    }

    private void registerCommands() {
        commandManager.register("setroute", new SetRouteCommand());
    }
}