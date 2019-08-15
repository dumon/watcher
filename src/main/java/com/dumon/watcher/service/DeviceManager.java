package com.dumon.watcher.service;

import com.dumon.watcher.entity.Device;
import com.dumon.watcher.repo.DeviceRepository;
import com.dumon.watcher.service.watcher.DeviceWatcherFactory;
import com.dumon.watcher.service.watcher.Watcher;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Service
public class DeviceManager {

    @Resource
    private DeviceRepository deviceRepository;
    @Value("${app.watcher.ping.timeout:2000}")
    private int pingTimeout;

    private Watcher watcher;

    @PostConstruct
    public void init() {
        watcher = DeviceWatcherFactory.getWatcher();
    }

    public void assignName(final String deviceId, final String name) {
        deviceRepository.findById(deviceId).ifPresent(device -> device.setName(name));
    }

    /**
     * Scan subnet and obtain all devices
     */
    public void scanNetwork() {
        watcher.scanNetwork().entrySet().stream().parallel().forEach(entry -> {
            String mac = entry.getKey();
            String ip = entry.getValue();
            Device device = deviceRepository.findById(mac).orElseGet(Device::new);
            populateDevice(device, ip, mac);
            deviceRepository.save(device);
        });
    }

    private void populateDevice(final Device device, final String ip, final String mac) {
        device.setActive(true);
        device.setLastActiveTime(LocalDateTime.now());
        device.setIpAddress(ip);
        device.setMacAddress(mac);
    }

    /**
     * Refresh device state by ping
     */
    public void renewExisted() {
        deviceRepository.findAll().forEach(device -> {
            if (pingDevice(device)) {
                device.setLastActiveTime(LocalDateTime.now());
                device.setActive(true);
            } else {
                device.setActive(false);
            }
        });
    }

    private boolean pingDevice(final Device device) {
        return watcher.checkExisted(device);
    }

    public void setPingTimeout(final int timeout) {
        Preconditions.checkArgument(timeout > 0, "must be greater then 0");
        watcher.setPingTimeout(timeout);
    }
}
