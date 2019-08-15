package com.dumon.watcher.service.watcher;

import com.dumon.watcher.entity.Device;

import java.util.Map;
import java.util.Optional;

public interface Watcher {

    /**
     * Scan full subnet to detect devices
     * @return
     */
    Map<String, String> scanNetwork();

    /**
     * Ping existed device by IP
     * @param macAddress
     * @return
     */
    Optional<Device> pingDevice(int macAddress);

    /**
     * Ping
     * @param ipAddress
     * @return
     */
    Optional<Device> pingByIp(int ipAddress);

    /**
     * Check existed device
     * @param device
     * @return
     */
    boolean checkExisted(Device device);

    void setPingTimeout(int pingTimeout);
}
