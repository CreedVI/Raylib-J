package com.creedvi.raylib.java.rlj.utils;

import static com.creedvi.raylib.java.rlj.utils.Tracelog.TraceLogType.LOG_ERROR;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.TraceLogType.LOG_INFO;

public class Tracelog{

    final int MAX_TRACELOG_MSG_LENGTH = 128;     // Max length of one trace-log message
    final int MAX_UWP_MESSAGES = 512;           // Max UWP messages to process

    static int logTypeLevel = LOG_INFO.getTraceLogInt();                     // Minimum log type level
    static int logTypeExit = LOG_ERROR.getTraceLogInt();                     // Log type that exits

    public enum TraceLogType {
        LOG_ALL(0),        // Display all logs
        LOG_TRACE(1),
        LOG_DEBUG(2),
        LOG_INFO(3),
        LOG_WARNING(4),
        LOG_ERROR(5),
        LOG_FATAL(6),
        LOG_NONE(7);           // Disable logging

        private final int TraceLogInt;

        TraceLogType(int TraceLogInt){
            this.TraceLogInt = TraceLogInt;
        }

        public int getTraceLogInt(){
            return TraceLogInt;
        }
    }


    public static void Tracelog(TraceLogType logType, String text){
        // Message has level below current threshold, don't emit
        if (logType.getTraceLogInt() < logTypeLevel)
            return;

        /* TODO: figure out logcallback
        if (logCallback){
            logCallback(logType, text, args);
            va_end(args);
            return;
        }*/

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
        System.out.print(buffer.toString());

        if (logType.getTraceLogInt() >= logTypeExit)
            System.exit(1); // If exit message, exit program
    }

    public static void TracelogS(String text){
        // Message has level below current threshold, don't emit

        /* TODO: figure out logcallback
        if (logCallback){
            logCallback(logType, text, args);
            va_end(args);
            return;
        }*/

        System.out.println(text);
    }

    void SetTraceLogLevel(TraceLogType logType){
        logTypeLevel = logType.getTraceLogInt();
    }

}
