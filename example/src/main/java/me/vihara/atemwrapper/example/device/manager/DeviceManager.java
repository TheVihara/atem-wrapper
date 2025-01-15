package me.vihara.atemwrapper.example.device.manager;

import me.vihara.atemwrapper.api.device.AtemDevice;
import me.vihara.atemwrapper.core.AtemDeviceImpl;

import java.util.concurrent.ConcurrentHashMap;

public class DeviceManager {
    public final static DeviceManager INSTANCE = new DeviceManager();
    private ConcurrentHashMap<Integer, AtemDevice> devices = new ConcurrentHashMap<>();

    public void postBootstrap() {
        devices.put(1, new AtemDeviceImpl(
                "0.0.0.0",
                9990
        ));
    }

    public AtemDevice getDevice(int id) {
        return devices.get(id);
    }
}
