package com.dumon.watcher.controller;

import com.dumon.watcher.service.DeviceManager;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;

@RestController
@RequestMapping(
        value = "/app",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class ApplicationController {

    @Resource
    private DeviceManager deviceManager;

    /**
     * Scan sub-network to obtain devices
     */
    @GetMapping("/scan")
    public void scanNetwork() {
        deviceManager.scanNetwork();
    }

    /**
     * Check actual state for already found devices
     */
    @GetMapping("/refresh")
    public void refreshDevices() {
        deviceManager.renewExisted();
    }

    /**
     * Set ping timeout for watcher (ms)
     * url sample: http://localhost:8080/pingTimeout?timeout=2000
     */
    @GetMapping("/pingTimeout")
    public void setPingTimeout(@NotEmpty @RequestParam final int timeout) {
        deviceManager.setPingTimeout(timeout);
    }
}
