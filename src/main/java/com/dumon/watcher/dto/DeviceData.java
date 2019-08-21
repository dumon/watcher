package com.dumon.watcher.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeviceData {
    private String macAddress;
    private String ipAddress;
    private String hostname;
}
