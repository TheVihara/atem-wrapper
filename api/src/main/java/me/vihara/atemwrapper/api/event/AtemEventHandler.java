package me.vihara.atemwrapper.api.event;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AtemEventHandler {
    int priority() default 0;
}
