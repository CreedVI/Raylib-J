package com.raylib.java.core.tracelog;

import org.jetbrains.annotations.Nullable;

public interface TraceLog {
    enum TracelogType{
        LOG_ALL(0),        // Display all logs
        LOG_TRACE(1),
        LOG_DEBUG(2),
        LOG_INFO(3),
        LOG_WARNING(4),
        LOG_ERROR(5),
        LOG_FATAL(6),
        LOG_NONE(7);     // Disable logging

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
