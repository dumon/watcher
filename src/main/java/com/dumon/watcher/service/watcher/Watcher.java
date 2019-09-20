package com.dumon.watcher.service.watcher;

import com.dumon.watcher.dto.DeviceData;
import com.dumon.watcher.entity.Device;

import java.util.List;
import java.util.Optional;

public interface Watcher {

    /**
     * Scan full subnet to detect devices (multi-threading process)
     * @return
     */
    List<DeviceData> scanNetwork();

    /**
     * get reachable device by IP
     * @param ipAddress
     * @return
     */
    Optional<DeviceData> getDeviceByIp(String ipAddress);

    /**
     * Check existed device
     * @param device
     * @return
     */
    boolean checkExisted(Device device);

    void setPingTimeout(int pingTimeout);

    /**
     * Return current scanned subnet in CIDR format
     * @return
     */
    String getSubnet();
}
