package ru.nubby.playstream.data.twitchapi.converter;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This is used to convert plain text response.
 * Decision on converter type is based on that annotation.
 * Watch {@link AnnotatedConverterFactory}.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Scalars {}
