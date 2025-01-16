package me.vihara.atemwrapper.example.device.manager;

import io.ktor.network.sockets.Socket;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.BuildersKt;
import me.vihara.atemwrapper.api.device.AtemDevice;
import me.vihara.atemwrapper.core.AtemDeviceImpl;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class DeviceManager {
    public final static DeviceManager INSTANCE = new DeviceManager();
    private ConcurrentHashMap<Integer, AtemDevice> devices = new ConcurrentHashMap<>();

    public void postBootstrap() {
        AtemDeviceImpl device = new AtemDeviceImpl(
                "0.0.0.0",
                9990
        );

        devices.put(1, device);

        try {
            try (Socket socket = BuildersKt.runBlocking(
                    EmptyCoroutineContext.INSTANCE,
                    (scope, continuation) -> device.connect(continuation)
            )) {
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    public AtemDevice getDevice(int id) {
        return devices.get(id);
    }
}