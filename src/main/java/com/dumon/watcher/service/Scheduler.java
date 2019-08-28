package com.dumon.watcher.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class Scheduler {

    private static final Logger LOG = LoggerFactory.getLogger(Scheduler.class);

    @Resource
    private DeviceManager deviceManager;

    @Scheduled(fixedRateString = "${app.watcher.scan-interval}")
    public void runScan() {
        LOG.info("Network scan is running...");
        deviceManager.scanNetwork();
    }

}
