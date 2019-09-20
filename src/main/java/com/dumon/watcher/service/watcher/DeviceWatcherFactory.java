package com.dumon.watcher.service.watcher;

import java.util.Optional;

public class DeviceWatcherFactory {

    public static Watcher getWatcher(final String ip, final String mask) {
        Watcher watcher = null;
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.contains("win")) {
            watcher = new WinDeviceWatcher(ip, mask);
        }
        return Optional.ofNullable(watcher).orElse(new UnixDeviceWatcher(ip, mask));
    }

}
