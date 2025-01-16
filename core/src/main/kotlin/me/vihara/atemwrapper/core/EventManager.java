package me.vihara.atemwrapper.core;

import me.vihara.atemwrapper.api.device.event.AtemEvent;
import me.vihara.atemwrapper.api.device.event.AtemEventListener;
import me.vihara.atemwrapper.api.device.event.AtemEventManager;
import me.vihara.atemwrapper.api.device.event.AtemEventHandler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class EventManager implements AtemEventManager {
    public static final EventManager INSTANCE = new EventManager();

    private final Map<Integer, List<ListenerWrapper>> priorityGroups = new ConcurrentHashMap<>();
    private final Map<AtemEventListener, ListenerWrapper> listenerMap = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<Method>> METHOD_CACHE = new ConcurrentHashMap<>();
    private final AtomicInteger maxPriority = new AtomicInteger(Integer.MIN_VALUE);
    private final AtomicInteger minPriority = new AtomicInteger(Integer.MAX_VALUE);

    @Override
    public void addListener(final AtemEventListener listener) {
        final int priority = getListenerPriority(listener);
        final ListenerWrapper wrapper = new ListenerWrapper(listener, priority);

        listenerMap.put(listener, wrapper);
        priorityGroups.computeIfAbsent(priority, k -> new ArrayList<>()).add(wrapper);

        maxPriority.updateAndGet(current -> Math.max(current, priority));
        minPriority.updateAndGet(current -> Math.min(current, priority));
    }

    @Override
    public void removeListener(final AtemEventListener listener) {
        final ListenerWrapper wrapper = listenerMap.remove(listener);
        if (wrapper != null) {
            priorityGroups.computeIfPresent(wrapper.priority, (k, list) -> {
                list.remove(wrapper);
                return list.isEmpty() ? null : list;
            });
        }
    }

    @Override
    public void fireEvent(final AtemEvent event) {
        for (int priority = maxPriority.get(); priority >= minPriority.get(); priority--) {
            final List<ListenerWrapper> listeners = priorityGroups.get(priority);
            if (listeners != null) {
                for (int i = 0; i < listeners.size(); i++) {
                    listeners.get(i).invoke(event);
                }
            }
        }
    }

    private static int getListenerPriority(final AtemEventListener listener) {
        return METHOD_CACHE.computeIfAbsent(listener.getClass(), EventManager::findMethods)
                .stream()
                .findFirst()
                .map(method -> method.getAnnotation(AtemEventHandler.class).priority())
                .orElse(0);
    }

    private static List<Method> findMethods(Class<?> clazz) {
        final List<Method> methods = new ArrayList<>();
        for (final Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(AtemEventHandler.class)) {
                method.setAccessible(true);
                methods.add(method);
            }
        }
        return methods;
    }

    private static final class ListenerWrapper {
        private final AtemEventListener listener;
        private final int priority;
        private final List<Method> cachedMethods;

        ListenerWrapper(final AtemEventListener listener, final int priority) {
            this.listener = listener;
            this.priority = priority;
            this.cachedMethods = METHOD_CACHE.computeIfAbsent(listener.getClass(), EventManager::findMethods);
        }

        void invoke(final AtemEvent event) {
            for (int i = 0; i < cachedMethods.size(); i++) {
                final Method method = cachedMethods.get(i);
                try {
                    if (method.getParameterCount() == 1 &&
                            method.getParameterTypes()[0].isAssignableFrom(event.getClass())) {
                        method.invoke(listener, event);
                    }
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Failed to invoke event handler", e);
                }
            }
        }
    }
}