package com.creedvi.raylib.java.rlj.utils;

import java.lang.reflect.Method;

import static com.creedvi.raylib.java.rlj.Config.SUPPORT_TRACELOG;
import static com.creedvi.raylib.java.rlj.Config.SUPPORT_TRACELOG_DEBUG;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.TracelogType.*;

public class Tracelog{

    /**
     * Raylib-j Tracelog
     * If <code>SUPPORT_TRACELOG</code> is defined as <code>true</code> in Config.java trace log output messages will be
     * printed.
     */

    final int MAX_TRACELOG_MSG_LENGTH = 128;     // Max length of one trace-log message
    final int MAX_UWP_MESSAGES = 512;           // Max UWP messages to process

    static int logTypeLevel = LOG_ALL.getTraceLogInt();                     // Minimum log type level
    static int logTypeExit = LOG_ERROR.getTraceLogInt();                     // Log type that exits
    static Method logCallback;

    public enum TracelogType{
        LOG_ALL(0),        // Display all logs
        LOG_TRACE(1),
        LOG_DEBUG(2),
        LOG_INFO(3),
        LOG_WARNING(4),
        LOG_ERROR(5),
        LOG_FATAL(6),
        LOG_NONE(7);           // Disable logging

        private final int TraceLogInt;

        TracelogType(int TraceLogInt){
            this.TraceLogInt = TraceLogInt;
        }

        public int getTraceLogInt(){
            return TraceLogInt;
        }
    }

    /**
     * Show trace log messages (LOG_INFO, LOG_WARNING, LOG_ERROR, LOG_DEBUG)
     * @param logType TracelogType enum type that specifies what kind of trace log is to be called.
     * @param text Text to be printed.
     */
    public static void Tracelog(TracelogType logType, String text){
        if(SUPPORT_TRACELOG){
            // Message has level below current threshold, don't emit
            if (logType.getTraceLogInt() < logTypeLevel){
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

            if (logType.getTraceLogInt() >= logTypeExit){
                System.exit(1); // If exit message, exit program
            }
        }
    }

    /**
     * Prints trace log without a log type
     * @param text Text to be printed
     */
    public static void TracelogS(String text){
        if(SUPPORT_TRACELOG){
            System.out.println(text);
        }
    }

    /**
     * Set the current threshold (minimum) log level
     * @param logType Minimum log type to be shown
     */
    public static void SetTraceLogLevel(TracelogType logType){
        logTypeLevel = logType.getTraceLogInt();
    }

    // Set the exit threshold (minimum) log level
    public static void SetTraceLogExit(int logType){
        logTypeExit = logType;
    }

    // Set a trace log callback to enable custom logging
    public static void SetTraceLogCallback(Method callback){
        logCallback = callback;
    }


}
