package com.dumon.watcher.service.watcher;

import com.dumon.watcher.entity.Device;
import com.google.common.base.Splitter;

import java.util.List;
import java.util.regex.Pattern;

public class WinDeviceWatcher extends DeviceWatcher {

    private static final String[] ARP_CMD = {"arp", "-a"};
    private static final Pattern MAC_ADDRESS_PATTERN =
            Pattern.compile("[0-9a-f]+-[0-9a-f]+-[0-9a-f]+-[0-9a-f]+-[0-9a-f]+-[0-9a-f]+");
    private static final Pattern NSLOOKUP_RESULT_LINE_PATTERN =
            Pattern.compile("^.*Name:.*$");

    WinDeviceWatcher() {
        super(ARP_CMD);
    }

    @Override
    public boolean checkExisted(Device device) {
        return false;
    }

    @Override
    protected String extractHostName(final String line) {
        List<String> lineWords = Splitter.on("Name:").splitToList(line);
        return lineWords.get(lineWords.size() - 1).trim();
    }

    @Override
    protected Pattern getNslookupPattern() {
        return NSLOOKUP_RESULT_LINE_PATTERN;
    }

    @Override
    protected Pattern getMacAddressPattern() {
        return MAC_ADDRESS_PATTERN;
    }

    //TODO: impl
    @Override
    protected Pattern getDnsIpPattern() {
        return null;
    }

    //TODO: impl
    @Override
    protected String getDnsIpGettingCmd() {
        return null;
    }
}
