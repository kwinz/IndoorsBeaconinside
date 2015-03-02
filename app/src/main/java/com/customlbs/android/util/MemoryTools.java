package com.customlbs.android.util;

import java.text.DecimalFormat;

import android.os.Debug;

public final class MemoryTools {
    private static final double MEGABYTE = 1048576.0;
    private static final Runtime runtime = Runtime.getRuntime();
    private static final DecimalFormat f = new DecimalFormat("#0.00");

    private MemoryTools() {
    }

    public static String getDebugOutput() {
	final double heapAllocated = getNativeMemoryUsage();
	// final double heapAvailable = Debug.getNativeHeapSize() / MEGABYTE;
	// final double heapFree = Debug.getNativeHeapFreeSize() / MEGABYTE;

	final double rtAllocated = getRuntimeMemoryUsage();
	final double rtAvailable = getMaxMemoryPerApp();
	// final double rtFree = runtime.freeMemory() / MEGABYTE;

	return "N:" + f.format(heapAllocated) + "+RT:" + f.format(rtAllocated) + '/'
		+ (int) (rtAvailable) + " MB";
    }

    public static double getRuntimeMemoryUsage() {
	return runtime.totalMemory() / MEGABYTE;
    }

    public static double getNativeMemoryUsage() {
	return Debug.getNativeHeapAllocatedSize() / MEGABYTE;
    }

    private static double getMaxMemoryPerApp() {
	return runtime.maxMemory() / MEGABYTE;
    }

}