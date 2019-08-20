package com.dumon.watcher.converter;

public interface Converter<S,T> {
    T convert(S source);
    void populate(S source, T target);
}
