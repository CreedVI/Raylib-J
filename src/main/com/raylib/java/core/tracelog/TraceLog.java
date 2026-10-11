package com.raylib.java.core.tracelog;

import org.jetbrains.annotations.Nullable;

/**
 * Tracelog, system logging interface.
 */
public interface TraceLog {
    /**
     * Logs to display based on severity 
     */
    enum TracelogType{
        /**
         * Display all logs
         */
        LOG_ALL(0), 
        /**
         * Display trace, info, warning, error, and fatal logs  
         */
        LOG_TRACE(1),
        /**
         * Display debug, info, warning, error, and fatal logs 
         */
        LOG_DEBUG(2),
        /**
         * Display info, warning, error, and fatal logs
         */
        LOG_INFO(3),
        /**
         * Display warning, error, and fatal logs
         */
        LOG_WARNING(4),
        /**
         * Display error and fatal logs
         */
        LOG_ERROR(5),
        /**
         * Display only fatal logs
         */
        LOG_FATAL(6),
        /**
         * Disable all logging
         */
        LOG_NONE(7);

        private final int level;

        TracelogType(int level) {
         this.level = level;
        }

        public int GetLevel() {
         return this.level;
        }
    }

    /**
     * Show trace log messages (LOG_INFO, LOG_WARNING, LOG_ERROR, LOG_DEBUG)
     * @param tracelogType TracelogType enum type that specifies what kind of trace log is to be called.
     * @param text Message to be printed.
     * @param args Arguments to be passed for string formatting
     */
    void TRACELOG(TracelogType tracelogType, String text, @Nullable Object... args);
}
