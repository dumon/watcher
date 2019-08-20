package com.dumon.watcher.helper;

import com.google.common.base.Preconditions;
import lombok.NonNull;

public final class ConversionHelper {

    private static final int ETHER_ADDR_LEN = 6;

    private ConversionHelper() {}

    public static @NonNull String longMacToString(final long addr) {
        return String.format("%02x:%02x:%02x:%02x:%02x:%02x",
                (addr >> 40) & 0xff,
                (addr >> 32) & 0xff,
                (addr >> 24) & 0xff,
                (addr >> 16) & 0xff,
                (addr >> 8) & 0xff,
                addr & 0xff);
    }

    public static long stringMacToLong(final String addr) {
        Preconditions.checkNotNull(addr);
        String[] parts = addr.split(":");
        if (parts.length != ETHER_ADDR_LEN) {
            throw new IllegalArgumentException(addr + " was not a valid MAC address");
        }
        long longAddr = 0;
        for (int i = 0; i < parts.length; i++) {
            int x = Integer.valueOf(parts[i], 16);
            if (x < 0 || 0xff < x) {
                throw new IllegalArgumentException(addr + "was not a valid MAC address");
            }
            longAddr = x + (longAddr << 8);
        }
        return longAddr;
    }

}
