package com.dumon.watcher.service.watcher;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import java.util.Map;
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
    private static final ForkJoinPool THREAD_POOL = new ForkJoinPool(100);

    private final String[] arpCmd;
    private final Pattern macAddressPattern;
    private InetAddress localhost;
    private int pingTimeout = DEFAULT_PING_TIMEOUT;

    public DeviceWatcher(final String[] arpCmd, final Pattern macAddressPattern) {
        this.arpCmd = arpCmd;
        this.macAddressPattern = macAddressPattern;
        localhost = getIp();
    }

    protected Map<String, String> asyncObtainMacIpMap() {
        Map<String, String> result = Maps.newConcurrentMap();
        getAllReachableIps().stream().parallel().forEach(ip ->
                Optional.ofNullable(getMacForIp(ip)).
                        ifPresent(mac -> result.put(mac, ip))
        );
        return result;
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

    protected final String getMacForIp(final String ip) {
        String[] cmd = buildPingCommand(ip);

        String mac = null;
        try {
            Process process = Runtime.getRuntime().exec(cmd); // Run command
            process.waitFor(); // read output with BufferedReader
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();

            while (line != null) {  // Loop trough lines
                Matcher m = macAddressPattern.matcher(line);

                if (m.find()) { // when Matcher finds a Line then return it as result
                    LOG.info("For IP {} MAC identified: {}", ip, m.group(0));
                    mac = m.group(0);
                }

                line = reader.readLine();
            }
        } catch (IOException | InterruptedException e1) {
            LOG.error("Cannot get MAC fo IP " + ip);
        }

        return mac;
    }

    private String[] buildPingCommand(final String ip) {
        String[] cmd = new String[arpCmd.length + 1];
        System.arraycopy(arpCmd, 0, cmd, 0, arpCmd.length);
        cmd[arpCmd.length] = ip;
        return cmd;
    }

    @Override
    public void setPingTimeout(int pingTimeout) {
        this.pingTimeout = pingTimeout;
    }

    protected int getPingTimeout() {
        return pingTimeout;
    }

    private static InetAddress getIp() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        List<String> listOfArguments = runtimeMXBean.getInputArguments();

        String localIp = listOfArguments.stream()
                .filter(arg -> arg.contains(LOCAL_SUBNET_IP_PARAM))
                .findFirst()
                .map(String::toLowerCase)
                .map(arg -> arg.substring(LOCAL_SUBNET_IP_PARAM.length() + 1))
                .orElse(DEFAULT_IP_ADDRESS);
        try {
            return InetAddress.getByName(localIp);
        } catch (UnknownHostException exc) {
            LOG.trace("Cannot identify local address by IP {}", localIp, exc);
        }
        return null;
    }
}
