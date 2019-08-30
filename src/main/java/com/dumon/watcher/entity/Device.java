package com.dumon.watcher.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private boolean active;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS")
    private Date lastActiveTime;
}
