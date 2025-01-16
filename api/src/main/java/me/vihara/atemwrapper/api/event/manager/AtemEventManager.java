package me.vihara.atemwrapper.api.event.manager;

import me.vihara.atemwrapper.api.event.AtemEvent;
import me.vihara.atemwrapper.api.event.AtemEventListener;

public interface AtemEventManager {
    void addListener(AtemEventListener listener);
    void removeListener(AtemEventListener listener);
    void fireEvent(AtemEvent event);
}
