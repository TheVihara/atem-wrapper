package me.vihara.atemwrapper.core

import me.vihara.atemwrapper.api.event.AtemEvent
import me.vihara.atemwrapper.api.event.AtemEventListener
import me.vihara.atemwrapper.api.event.manager.AtemEventManager
import me.vihara.atemwrapper.api.event.AtemEventHandler
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

object EventManager : AtemEventManager {
    private val priorityGroups: MutableMap<Int, MutableList<ListenerWrapper>> = ConcurrentHashMap()
    private val listenerMap: MutableMap<AtemEventListener, ListenerWrapper> = ConcurrentHashMap()
    private val METHOD_CACHE: MutableMap<Class<*>, List<Method>> = ConcurrentHashMap()
    private val maxPriority = AtomicInteger(Int.MIN_VALUE)
    private val minPriority = AtomicInteger(Int.MAX_VALUE)

    override fun addListener(listener: AtemEventListener) {
        val priority = getListenerPriority(listener)
        val wrapper = ListenerWrapper(listener, priority)

        listenerMap[listener] = wrapper
        priorityGroups.computeIfAbsent(priority) { mutableListOf() }.add(wrapper)

        maxPriority.updateAndGet { current -> maxOf(current, priority) }
        minPriority.updateAndGet { current -> minOf(current, priority) }
    }

    override fun removeListener(listener: AtemEventListener) {
        listenerMap.remove(listener)?.let { wrapper ->
            priorityGroups.computeIfPresent(wrapper.priority) { _, list ->
                list.remove(wrapper)
                if (list.isEmpty()) null else list
            }
        }
    }

    override fun fireEvent(event: AtemEvent) {
        for (priority in maxPriority.get() downTo minPriority.get()) {
            val listeners = priorityGroups[priority]
            listeners?.forEach { it.invoke(event) }
        }
    }

    private fun getListenerPriority(listener: AtemEventListener): Int {
        return METHOD_CACHE.computeIfAbsent(listener::class.java, ::findMethods)
            .firstOrNull()
            ?.getAnnotation(AtemEventHandler::class.java)
            ?.priority
            ?: 0
    }

    private fun findMethods(clazz: Class<*>): List<Method> {
        return clazz.methods.filter { it.isAnnotationPresent(AtemEventHandler::class.java) }.onEach { it.isAccessible = true }
    }

    private class ListenerWrapper(
        private val listener: AtemEventListener,
        val priority: Int
    ) {
        private val cachedMethods: List<Method> = METHOD_CACHE.computeIfAbsent(listener::class.java, ::findMethods)

        fun invoke(event: AtemEvent) {
            for (method in cachedMethods) {
                try {
                    if (method.parameterCount == 1 && method.parameterTypes[0].isAssignableFrom(event::class.java)) {
                        method.invoke(listener, event)
                    }
                } catch (e: ReflectiveOperationException) {
                    throw RuntimeException("Failed to invoke event handler", e)
                }
            }
        }
    }
}
