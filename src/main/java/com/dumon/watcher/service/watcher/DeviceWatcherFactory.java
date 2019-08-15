package com.dumon.watcher.service.watcher;

import java.util.Optional;

public class DeviceWatcherFactory {

    public static Watcher getWatcher() {
        Watcher watcher = null;
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.contains("win")) {
            watcher = new WinDeviceWatcher();
        }
        return Optional.ofNullable(watcher).orElse(new UnixDeviceWatcher());
    }

}
