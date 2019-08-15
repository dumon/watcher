package com.dumon.watcher.service.watcher;

import com.dumon.watcher.entity.Device;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class WinDeviceWatcher extends DeviceWatcher {

    private static final String[] ARP_CMD = {"arp", "-a"};
    private static final Pattern MAC_ADDRESS_PATTERN =
            Pattern.compile("[0-9a-f]+-[0-9a-f]+-[0-9a-f]+-[0-9a-f]+-[0-9a-f]+-[0-9a-f]+");

    public WinDeviceWatcher() {
        super(ARP_CMD, MAC_ADDRESS_PATTERN);
    }

    @Override
    public Map<String, String> scanNetwork() {
        return null;
    }

    @Override
    public Optional<Device> pingDevice(int macAddress) {
        return Optional.empty();
    }

    @Override
    public Optional<Device> pingByIp(int ipAddress) {
        return Optional.empty();
    }

    @Override
    public boolean checkExisted(Device device) {
        return false;
    }
}
