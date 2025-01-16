package me.vihara.atemwrapper.api.device.event;

public interface AtemEventManager {
    void addListener(AtemEventListener listener);
    void removeListener(AtemEventListener listener);
    void fireEvent(AtemEvent event);
}
