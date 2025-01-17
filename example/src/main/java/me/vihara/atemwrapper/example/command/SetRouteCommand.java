package me.vihara.atemwrapper.example.command;

import me.vihara.atemwrapper.api.device.AtemDevice;
import me.vihara.atemwrapper.example.device.manager.DeviceManager;

import static me.vihara.atemwrapper.example.ExampleApp.LOGGER;

public class SetRouteCommand extends Command {
    @Override
    protected void execute(String[] args) {
        if (args.length < 3) {
            LOGGER.warning("Usage: setroute <device> <output> <input>");
            return;
        }

        int device = Integer.parseInt(args[0]);
        int output = Integer.parseInt(args[1]);
        int input = Integer.parseInt(args[2]);

        AtemDevice atemDevice = DeviceManager.INSTANCE.getDevice(device);
        atemDevice.setOutputRoute(output, input);
    }
}
