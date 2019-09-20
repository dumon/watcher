package com.dumon.watcher.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Component
@Validated
@ConfigurationProperties("app")
@PropertySources({
    @PropertySource("classpath:./config/app.config"),
    @PropertySource(value = "file:${app.config:./app.config}", ignoreResourceNotFound = true)})
public class AppProperties {

    @Value("${spring.datasource.driverClassName}")
    private String dataBaseDriver;
    @NotBlank
    private String usersFile;
    @NotNull
    private Watcher watcher;

    public String getUsersFile() {
        return usersFile;
    }

    public void setUsersFile(String usersFile) {
        this.usersFile = usersFile;
    }

    public String getDataBaseDriver() {
        return dataBaseDriver;
    }

    public Watcher getWatcher() {
        return watcher;
    }

    public void setWatcher(final Watcher watcher) {
        this.watcher = watcher;
    }

    public static class Watcher {

        @NotBlank
        @Pattern(regexp="([0-9]{1,3}.?){4}")
        private String localIp;
        @Min(value = 500, message = "no reasonable to make it faster")
        private int pingTimeout;
        @Min(value = 60000, message = "no reasonable to set less than 1min")
        private int scanInterval;

        public String getLocalIp() {
            return localIp;
        }

        public void setLocalIp(String localIp) {
            this.localIp = localIp;
        }

        public int getPingTimeout() {
            return pingTimeout;
        }

        public void setPingTimeout(int pingTimeout) {
            this.pingTimeout = pingTimeout;
        }

        public int getScanInterval() {
            return scanInterval;
        }

        public void setScanInterval(int scanInterval) {
            this.scanInterval = scanInterval;
        }
    }
}
