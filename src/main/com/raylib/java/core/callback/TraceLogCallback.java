package com.raylib.java.core.callback;

import com.raylib.java.core.tracelog.TraceLog;
import org.jetbrains.annotations.Nullable;

import static com.raylib.java.Config.SUPPORT_TRACELOG;
import static com.raylib.java.Config.SUPPORT_TRACELOG_DEBUG;
import static com.raylib.java.core.tracelog.TraceLog.TracelogType.*;

public class TraceLogCallback implements TraceLog {

    TracelogType logTypeLevel = LOG_INFO;
    int EXIT_FAILURE = 255;

    /**
     * Set the current threshold (minimum) log level
     * @param logTypeLevel Minimum log type to be shown
     */
    public void SetTraceLogLevel(TracelogType logTypeLevel){
        this.logTypeLevel = logTypeLevel;
    }

    /**
     * Show trace log messages (LOG_INFO, LOG_WARNING, LOG_ERROR, LOG_DEBUG)
     * @param tracelogType TracelogType enum type that specifies what kind of trace log is to be called.
     * @param text Message to be printed.
     * @param args Arguments to be passed for string formatting
     */
    @Override
    public void TRACELOG(TracelogType tracelogType, String text, @Nullable Object... args) {
        if(SUPPORT_TRACELOG){
            // Message has level below current threshold, don't emit
            if (tracelogType == null || tracelogType.GetLevel() < logTypeLevel.GetLevel()){
                return;
            }

            StringBuilder buffer = new StringBuilder();

            switch (tracelogType){
                case LOG_TRACE:
                    buffer.append("TRACE: ");
                    break;
                case LOG_DEBUG:
                    buffer.append("DEBUG: ");
                    break;
                case LOG_INFO:
                    buffer.append("INFO: ");
                    break;
                case LOG_WARNING:
                    buffer.append("WARNING: ");
                    break;
                case LOG_ERROR:
                    buffer.append("ERROR: ");
                    break;
                case LOG_FATAL:
                    buffer.append("FATAL: ");
                    break;
                default:
                    break;
            }

            buffer.append(text).append("\n");
            if(tracelogType != LOG_DEBUG){
                System.out.printf(buffer.toString(), args);
            }

            if (tracelogType == LOG_DEBUG && SUPPORT_TRACELOG_DEBUG){
                System.out.printf(buffer.toString(), args);
            }

            if (tracelogType == LOG_FATAL){
                System.exit(EXIT_FAILURE);  // If fatal logging, exit program
            }

        }
    }
}
