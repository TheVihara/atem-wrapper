package me.vihara.atemwrapper.example.listener;

import me.vihara.atemwrapper.api.device.event.AtemEventHandler;
import me.vihara.atemwrapper.api.device.event.AtemEventListener;
import me.vihara.atemwrapper.api.device.event.impl.AtemRouteChangeEvent;

public class AtemListener implements AtemEventListener {
    @AtemEventHandler
    public void onRouteChange(AtemRouteChangeEvent event) {
        System.out.println("Output: " + event.getOutput() + " Old Input: " + event.getOldInput() + " New Input: " + event.getNewInput());
    }
}
