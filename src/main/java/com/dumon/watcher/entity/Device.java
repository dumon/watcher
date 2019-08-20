package com.dumon.watcher.entity;

import lombok.Data;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class Device {

    @Id
    private long macAddress;

    private String ipAddress;
    private String name;
    private LocalDateTime lastActiveTime;
    private boolean active;

}
