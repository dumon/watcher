package com.dumon.watcher.converter;

import static com.dumon.watcher.helper.ConversionHelper.stringMacToLong;

import com.dumon.watcher.dto.DeviceData;
import com.dumon.watcher.entity.Device;
import com.dumon.watcher.repo.DeviceRepository;
import org.springframework.stereotype.Component;

import java.util.Date;
import javax.annotation.Resource;

@Component
public class DeviceConverter implements Converter<DeviceData, Device> {

    @Resource
    private DeviceRepository deviceRepository;

    @Override
    public Device convert(final DeviceData source) {
        Device device = getDevice(source.getMacAddress());
        populate(source, device);
        return device;
    }

    @Override
    public void populate(final DeviceData source, final Device device) {
        device.setActive(true);
        device.setLastActiveTime(new Date());
        device.setIpAddress(source.getIpAddress());
        device.setName(source.getHostname());
    }

    private Device getDevice(final String macAddress) {
        long deviceId = stringMacToLong(macAddress);
        return deviceRepository.findById(deviceId).orElseGet(() -> createDevice(deviceId));
    }

    private Device createDevice(final long deviceId) {
        Device device = new Device();
        device.setMacAddress(deviceId);
        return device;
    }
}
