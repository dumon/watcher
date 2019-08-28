package com.dumon.watcher.helper;

public final class Constants {

    private Constants() {}

    public static final int DEFAULT_PING_TIMEOUT = 2000;
    public static final String DEFAULT_IP_ADDRESS = "10.0.0.1";
    public static final int THREAD_POOL_CAPACITY = 150;

    public static final class JVM {
        static final String JVM_PARAM_PREFIX = "-D";
        public static final String USERS = "users";
    }
}
