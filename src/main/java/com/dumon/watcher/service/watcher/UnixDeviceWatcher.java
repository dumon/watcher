package com.dumon.watcher.service.watcher;

import com.dumon.watcher.entity.Device;
import com.google.common.base.Splitter;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.regex.Pattern;

public class UnixDeviceWatcher extends DeviceWatcher {

    private static final String[] ARP_CMD = {"arp"};
    private static final Pattern NSLOOKUP_RESULT_LINE_PATTERN =
            Pattern.compile("^.*name.*.=.*$");
    private static final String GET_DNS_IP_CMD =
            "cat /etc/resolv.conf |grep -i '^nameserver'|head -n1|cut -d ' ' -f2";

    UnixDeviceWatcher(final String ip) {
        super(ip, ARP_CMD);
    }

    @Override
    public boolean checkExisted(final Device device) {
        try {
            InetAddress address = InetAddress.getByName(device.getIpAddress());
            return address.isReachable(getPingTimeout());
        } catch (final IOException exc) {
            LOG.error("Error on device ping by IP {}", device.getIpAddress(), exc);
        }
        return false;
    }

    @Override
    protected String extractHostName(final String line) {
        List<String> lineWords = Splitter.on(" = ").splitToList(line);
        return lineWords.get(lineWords.size() - 1);
    }

    @Override
    protected Pattern getDnsIpPattern() {
        return getIpMatchPattern();
    }

    @Override
    protected Pattern getNslookupPattern() {
        return NSLOOKUP_RESULT_LINE_PATTERN;
    }

    @Override
    protected Pattern getMacAddressPattern() {
        return MAC_ADDRESS_PATTERN;
    }

    @Override
    protected String getDnsIpGettingCmd() {
        return GET_DNS_IP_CMD;
    }
}
