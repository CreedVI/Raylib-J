package com.raylib.java.utils;

import java.lang.reflect.Method;

import static com.raylib.java.Config.SUPPORT_TRACELOG;
import static com.raylib.java.Config.SUPPORT_TRACELOG_DEBUG;
import static com.raylib.java.utils.Tracelog.TracelogType.*;

public class Tracelog{

    /**
     * Raylib-j Tracelog
     * If <code>SUPPORT_TRACELOG</code> is defined as <code>true</code> in Config.java trace log output messages will be
     * printed.
     */

    final int MAX_TRACELOG_MSG_LENGTH = 128;     // Max length of one trace-log message

    static int logTypeLevel = LOG_ALL;                     // Minimum log type level
    static int logTypeExit = LOG_ERROR;                     // Log type that exits
    static int EXIT_FAILURE = 255;
    static Method logCallback;

    public static class TracelogType{
        public static final int
        LOG_ALL = 0,        // Display all logs
        LOG_TRACE = 1,
        LOG_DEBUG = 2,
        LOG_INFO = 3,
        LOG_WARNING = 4,
        LOG_ERROR = 5,
        LOG_FATAL = 6,
        LOG_NONE = 7;           // Disable logging
    }

    /**
     * Show trace log messages (LOG_INFO, LOG_WARNING, LOG_ERROR, LOG_DEBUG)
     * @param logType TracelogType enum type that specifies what kind of trace log is to be called.
     * @param text rText to be printed.
     */
    public static void Tracelog(int logType, String text){
        if(SUPPORT_TRACELOG){
            // Message has level below current threshold, don't emit
            if (logType < logTypeLevel){
                return;
            }

            StringBuilder buffer = new StringBuilder();

            switch (logType){
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
            if(logType != LOG_DEBUG){
                System.out.print(buffer.toString());
            }

            if (logType == LOG_DEBUG && SUPPORT_TRACELOG_DEBUG){
                System.out.print(buffer.toString());
            }

            if (logType == LOG_FATAL){
                System.exit(EXIT_FAILURE);  // If fatal logging, exit program
            }

        }
    }

    /**
     * Prints trace log without a log type
     * @param text rText to be printed
     */
    public static void Tracelog(String text){
        if(SUPPORT_TRACELOG && logTypeLevel <= LOG_DEBUG){
            System.out.println(text);
        }
    }

    /**
     * Set the current threshold (minimum) log level
     * @param logType Minimum log type to be shown
     */
    public static void SetTraceLogLevel(int logType){
        logTypeLevel = logType;
    }

    // Set a trace log callback to enable custom logging
    public static void SetTraceLogCallback(Method callback){
        logCallback = callback;
    }


}
