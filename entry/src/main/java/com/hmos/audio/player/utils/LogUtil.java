/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.hmos.audio.player.utils;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

import java.util.Locale;

public class LogUtil {
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(HiLog.LOG_APP, 0x00201, "business");

    private static final String LOG_FORMAT = "%{public}s: %{public}s";
    private static final String DEFAULT_STR = "null";

    private LogUtil() {
        /* Do nothing */
    }

    /**
     * Print debug log
     *
     * @param className class name
     * @param msg log message
     */
    public static void debug(String className, String msg) {
        HiLog.debug(LABEL_LOG, LOG_FORMAT, className, msg);
    }

    /**
     * Print info log
     *
     * @param className class name
     * @param msg log message
     */
    public static void info(String className, String msg) {
        HiLog.info(LABEL_LOG, LOG_FORMAT, className, msg);
    }

    /**
     * Print info log
     *
     * @param className class name
     * @param format format
     * @param args args
     */
    public static void info(String className, final String format, Object... args) {
        String buffMsg = String.format(Locale.ROOT, format, args);
        HiLog.info(LABEL_LOG, LOG_FORMAT, className, buffMsg);
    }

    /**
     * Print error log
     *
     * @param className class name
     * @param msg log message
     */
    public static void warn(String className, String msg) {
        HiLog.warn(LABEL_LOG, LOG_FORMAT, className, msg);
    }

    /**
     * Print error log
     *
     * @param className class name
     * @param msg log message
     */
    public static void error(String className, String msg) {
        HiLog.error(LABEL_LOG, LOG_FORMAT, className, msg);
    }

    /**
     * Print error log
     *
     * @param classType class name
     * @param format format
     * @param args args
     */
    public static void error(Class<?> classType, final String format, Object... args) {
        String buffMsg = String.format(Locale.ROOT, format, args);
        HiLog.error(LABEL_LOG, LOG_FORMAT, classType == null ? DEFAULT_STR : classType.getSimpleName(), buffMsg);
    }

    /**
     * Print error log
     *
     * @param tag log tag
     * @param format format
     * @param args args
     */
    public static void error(String tag, final String format, Object... args) {
        String buffMsg = String.format(Locale.ROOT, format, args);
        HiLog.error(LABEL_LOG, LOG_FORMAT, tag, buffMsg);
    }

    /**
     * Print debug log
     *
     * @param classType class name
     * @param format format
     * @param args args
     */
    public static void debug(Class<?> classType, final String format, Object... args) {
        String buffMsg = String.format(Locale.ROOT, format, args);
        HiLog.debug(LABEL_LOG, LOG_FORMAT, classType == null ? DEFAULT_STR : classType.getSimpleName(), buffMsg);
    }

    /**
     * Print info log
     *
     * @param classType class name
     * @param format format
     * @param args args
     */
    public static void info(Class<?> classType, final String format, Object... args) {
        String buffMsg = String.format(Locale.ROOT, format, args);
        HiLog.info(LABEL_LOG, LOG_FORMAT, classType == null ? DEFAULT_STR : classType.getSimpleName(), buffMsg);
    }

    /**
     * Print warn log
     *
     * @param classType class name
     * @param format format
     * @param args args
     */
    public static void warn(Class<?> classType, final String format, Object... args) {
        String buffMsg = String.format(Locale.ROOT, format, args);
        HiLog.warn(LABEL_LOG, LOG_FORMAT, classType == null ? DEFAULT_STR : classType.getSimpleName(), buffMsg);
    }
}
