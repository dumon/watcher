package com.dumon.watcher.entity;

import lombok.Data;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class Device {

    @Id
    private long macAddress;

    private String ipAddress;
    private String name;
    private Date lastActiveTime;
    private boolean active;

}
