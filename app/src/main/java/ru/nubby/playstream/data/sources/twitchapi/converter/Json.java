package ru.nubby.playstream.data.sources.twitchapi.converter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This is used by default.
 * Decision on converter type is based on that annotation.
 * Watch {@link AnnotatedConverterFactory},
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Json {}


