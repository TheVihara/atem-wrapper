package me.vihara.atemwrapper.example;

import me.vihara.atemwrapper.example.device.manager.DeviceManager;

import java.util.logging.Logger;

public class ExampleApp {
    public static ExampleApp INSTANCE;
    public static Logger LOGGER;


    protected ExampleApp() {
        INSTANCE = this;
        LOGGER = Logger.getLogger("ExampleApp");

        DeviceManager.INSTANCE.postBootstrap();

        LOGGER.info("ExampleApp done (???ms)");
    }
}
