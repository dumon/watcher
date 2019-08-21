package com.dumon.watcher.service.watcher;

import com.dumon.watcher.dto.DeviceData;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class DeviceWatcher implements Watcher {

    protected static final Logger LOG = LoggerFactory.getLogger(DeviceWatcher.class);
    private static final int DEFAULT_PING_TIMEOUT = 2000;
    private static final String DEFAULT_IP_ADDRESS = "10.0.0.1";
    private static final String LOCAL_SUBNET_IP_PARAM = "-Dip";
    private static final ForkJoinPool THREAD_POOL = new ForkJoinPool(150);
    private static final Pattern IP_MATCH_PATTERN = Pattern.compile("([0-9]{1,3}.?){4}");

    private final String[] arpCmd;
    private final String nslookupCmd = "nslookup %s %s";
    private InetAddress localhost;
    private final String dnsIp;
    private int pingTimeout = DEFAULT_PING_TIMEOUT;

    public DeviceWatcher(final String[] arpCmd) {
        this.arpCmd = arpCmd;
        localhost = getIp();
        dnsIp = Optional.ofNullable(getDnsServerIp()).orElse(DEFAULT_IP_ADDRESS);
    }

    @Override
    public List<DeviceData> scanNetwork() {
        List<DeviceData> result = Lists.newArrayList();
        getAllReachableIps().stream().parallel()
                .forEach(ip -> determineDevice(ip).ifPresent(result::add));
        return result;
    }

    @Override
    public Optional<DeviceData> getDeviceByIp(final String ipAddress) {
        if (pingIp(ipAddress)) {
            return determineDevice(ipAddress);
        }
        return Optional.empty();
    }

    private Optional<DeviceData> determineDevice(final String ip) {
        return Optional.ofNullable(getMacForIp(ip)).
                map(mac -> {
                    String hostName = resolveHostName(ip);
                    return DeviceData.builder().ipAddress(ip).macAddress(mac).hostname(hostName).build();
                });
    }

    /**
     * Returns list of all reachable IPs in current sub-network
     * Async process
     */
    private List<String> getAllReachableIps() {
        byte[] ip = localhost.getAddress(); // this code assumes IPv4 is used

        List<CompletableFuture<String>> futures = Lists.newArrayList();
        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 1; i <= 254; i++) {
            final byte ipPath = (byte) counter.incrementAndGet();
            futures.add(CompletableFuture.supplyAsync(() -> tryResolveIp(ip, ipPath), THREAD_POOL));
        }

        return futures.stream().map(CompletableFuture::join).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private String tryResolveIp(byte[] ip, byte i) {
        String reachableIp = null;
        try {
            ip[3] = i;
            InetAddress address = InetAddress.getByAddress(ip);
            if (address.isReachable(pingTimeout)) {
                LOG.info("{} Address is reachable", address );
                reachableIp = address.getHostAddress();
            }
            else {
               LOG.info("{} Address is unreachable", address);
            }
        } catch (final IOException exc) {
            LOG.error("Cannot check ip {}", ip, exc);
        }
        return reachableIp;
    }

    private boolean pingIp(final String ip) {
        boolean result = false;
        try {
            result = InetAddress.getByName(ip).isReachable(pingTimeout);
        } catch (IOException exc) {
            LOG.info("Cannot identify determine device by IP {}", ip);
        }
        return result;
    }

    private String getMacForIp(final String ip) {
        String cmd = buildPingCommand(ip);
        Pattern resultParsePattern = getMacAddressPattern();
        String mac = null;
        try {
            mac = executeCmd(cmd, resultParsePattern);
        } catch (IOException | InterruptedException e1) {
            LOG.error("Cannot get MAC fo IP " + ip);
        }
        LOG.info("For IP {} MAC identified: {}", ip, mac);
        return mac;
    }

    private String resolveHostName(final String ip) {
        String cmd = buildNslookupCommand(ip);
        Pattern resultParsePattern = getNslookupPattern();
        String hostName = null;
        try {
            hostName = executeCmd(cmd, resultParsePattern);
        } catch (IOException | InterruptedException e1) {
            LOG.error("Cannot resolve Hostname fo IP " + ip);
        }

        LOG.info("For IP {} Hostname identified: {}", ip, hostName);

        return hostName;
    }

    private String getDnsServerIp() {
        String cmd = getDnsIpGettingCmd();
        Pattern resultParsePattern = getDnsIpPattern();
        String dnsIp = null;
        try {
            dnsIp = executeCmd(cmd, resultParsePattern);
        } catch (IOException | InterruptedException exc) {
            LOG.error("Cannot resolve DNS IP", exc);
        }

        return dnsIp;
    }

    private String executeCmd(final String cmd, final Pattern pattern) throws IOException, InterruptedException {
        String result = null;
        Process process = Runtime.getRuntime().exec(cmd); // Run command
        process.waitFor(); // read output with BufferedReader
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = reader.readLine();

        while (line != null) {  // Loop trough lines
            Matcher m = pattern.matcher(line);

            if (m.find()) { // when Matcher finds a Line then return it as result
                result = extractHostName(m.group(0));
            }

            line = reader.readLine();
        }
        return result;
    }

    protected abstract String extractHostName(final String line);

    protected abstract Pattern getMacAddressPattern();

    protected abstract Pattern getNslookupPattern();

    protected abstract Pattern getDnsIpPattern();

    protected abstract String getDnsIpGettingCmd();

    protected Pattern getIpMatchPattern() {
        return IP_MATCH_PATTERN;
    }

    private String buildPingCommand(final String ip) {
        String[] cmd = new String[arpCmd.length + 1];
        System.arraycopy(arpCmd, 0, cmd, 0, arpCmd.length);
        cmd[arpCmd.length] = ip;
        return Joiner.on(" ").join(cmd);
    }

    private String buildNslookupCommand(final String ip) {
        Preconditions.checkNotNull(ip);
        return String.format(nslookupCmd, ip , dnsIp);
    }

    @Override
    public void setPingTimeout(int pingTimeout) {
        this.pingTimeout = pingTimeout;
    }

    protected int getPingTimeout() {
        return pingTimeout;
    }

    private InetAddress getIp() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        List<String> listOfArguments = runtimeMXBean.getInputArguments();

        InetAddress result = null;
        String localIp = listOfArguments.stream()
                .filter(arg -> arg.contains(LOCAL_SUBNET_IP_PARAM))
                .findFirst()
                .map(String::toLowerCase)
                .map(arg -> arg.substring(LOCAL_SUBNET_IP_PARAM.length() + 1))
                .orElseGet(this::getLocalIp);
        try {
            result = InetAddress.getByName(localIp);
        } catch (UnknownHostException exc) {
            LOG.trace("Cannot resolve IP {}", localIp, exc);
        }
        return result;
    }

    private String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (final UnknownHostException exc) {
            LOG.error("Not identified local IP, default {} will used", DEFAULT_IP_ADDRESS, exc);
            return DEFAULT_IP_ADDRESS;
        }
    }
}
