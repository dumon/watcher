package com.dumon.watcher.service;

import com.dumon.watcher.config.AppProperties;
import com.dumon.watcher.converter.DeviceConverter;
import com.dumon.watcher.entity.Device;
import com.dumon.watcher.helper.ConversionHelper;
import com.dumon.watcher.repo.DeviceRepository;
import com.dumon.watcher.service.watcher.DeviceWatcherFactory;
import com.dumon.watcher.service.watcher.Watcher;
import com.google.common.base.Preconditions;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Service
public class DeviceManager {

    @Resource
    private DeviceRepository deviceRepository;
    @Resource
    private DeviceConverter deviceConverter;
    @Resource
    private AppProperties appProperties;

    private Watcher watcher;

    @PostConstruct
    public void init() {
        AppProperties.Watcher watcherConfig = appProperties.getWatcher();
        this.watcher = DeviceWatcherFactory.getWatcher(watcherConfig.getLocalIp());
        this.watcher.setPingTimeout(watcherConfig.getPingTimeout());
    }

    public void addByIp(final String ip) {
        watcher.getDeviceByIp(ip).map(deviceConverter::convert).ifPresent(deviceRepository::save);
    }

    public void assignName(final String deviceId, final String name) {
        long id = ConversionHelper.stringMacToLong(deviceId);
        deviceRepository.findById(id).ifPresent(device -> device.setName(name));
    }

    /**
     * Scan subnet and obtain all devices
     */
    public void scanNetwork() {
        List<Device> foundDevices = watcher.scanNetwork().stream().parallel()
                .map(deviceConverter::convert)
                .collect(Collectors.toList());
        List<Long> foundIds = foundDevices.stream().map(Device::getMacAddress).collect(Collectors.toList());
        updateNonActiveDevices(foundIds);
        deviceRepository.saveAll(foundDevices);
    }

    /**
     * Refresh device state by ping
     */
    public void renewExisted() {
        deviceRepository.findAll().forEach(device -> {
            if (pingDevice(device)) {
                device.setLastActiveTime(new Date());
                device.setActive(true);
            } else {
                device.setActive(false);
            }
        });
    }

    private void updateNonActiveDevices(final List<Long> activeDeviceIds) {
        deviceRepository.findAll().forEach(device -> {
            if (!activeDeviceIds.contains(device.getMacAddress())) {
                device.setActive(false);
            }
        });
    }

    public void setPingTimeout(final int timeout) {
        Preconditions.checkArgument(timeout > 0, "must be greater then 0");
        watcher.setPingTimeout(timeout);
    }

    private boolean pingDevice(final Device device) {
        return watcher.checkExisted(device);
    }
}
