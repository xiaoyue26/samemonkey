package com.mengqifeng.www.utils;


import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Logger {
    private final DateTimeFormatter formatter;

    public Logger() {
        formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
    }

    public void info(String line, Object... args) {
        String dateTime = LocalDateTime.now(ZoneOffset.of("+8")).format(formatter);
        System.out.println(dateTime + " " + String.format(line, args));
    }

    public void debug(String line, Object... args) {
        // do nothing
        /*String dateTime = LocalDateTime.now(ZoneOffset.of("+8")).format(formatter);
        System.out.println(dateTime + " " + String.format(line, args));*/
    }

    public static void main(String[] args) {
        new Logger().info("a%s%d", "b", 1);
    }
}
