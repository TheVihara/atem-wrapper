package me.vihara.atemwrapper.example.device.manager;

import me.vihara.atemwrapper.api.device.AtemDevice;
import me.vihara.atemwrapper.core.AtemDeviceImpl;

import java.util.concurrent.ConcurrentHashMap;

public class DeviceManager {
    public final static DeviceManager INSTANCE = new DeviceManager();
    private ConcurrentHashMap<Integer, AtemDevice> devices = new ConcurrentHashMap<>();

    public void postBootstrap() {
        AtemDeviceImpl device = new AtemDeviceImpl(
                "10.200.86.65",
                9990
        );

        AtemDeviceImpl device2 = new AtemDeviceImpl(
                "10.200.86.60",
                9990
        );

        AtemDeviceImpl device3 = new AtemDeviceImpl(
                "10.200.86.47",
                9990
        );

        AtemDeviceImpl device4 = new AtemDeviceImpl(
                "10.200.86.46",
                9990
        );


        devices.put(1, device);
        devices.put(2, device2);
        devices.put(3, device3);
        devices.put(4, device4);

        device.connect();
       /* device2.connect();
        device3.connect();
        device4.connect();*/


        System.out.println("INFOOO " + device);
    }

    public AtemDevice getDevice(int id) {
        return devices.get(id);
    }
}