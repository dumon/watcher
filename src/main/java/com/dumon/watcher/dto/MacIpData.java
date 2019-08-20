package com.dumon.watcher.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MacIpData {
    private String macAddress;
    private String ipAddress;
}
