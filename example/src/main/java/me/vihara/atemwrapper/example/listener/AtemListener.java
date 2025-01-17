package me.vihara.atemwrapper.example.listener;

import me.vihara.atemwrapper.api.event.AtemEventHandler;
import me.vihara.atemwrapper.api.event.AtemEventListener;
import me.vihara.atemwrapper.api.event.impl.AtemOutputRouteChangeEvent;

public class AtemListener implements AtemEventListener {
    @AtemEventHandler
    public void onRouteChange(AtemOutputRouteChangeEvent event) {
        System.out.println("Output: " + event.getOutput() + " Old Input: " + event.getOldInput() + " New Input: " + event.getNewInput());
    }
}
