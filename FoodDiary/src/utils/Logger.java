package utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String LOG_FILE = "food_diary.log";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public enum Level {
        DEBUG("DEBUG"),
        INFO("INFO"),
        WARNING("WARNING"),
        ERROR("ERROR");

        private String name;

        Level(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private static Level currentLevel = Level.DEBUG;

    static {
        // Установка кодировки для консоли
        System.setProperty("console.encoding", "UTF-8");
        try {
            PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8.name());
            System.setOut(out);

            PrintStream err = new PrintStream(System.err, true, StandardCharsets.UTF_8.name());
            System.setErr(err);
        } catch (UnsupportedEncodingException e) {
            System.err.println("Ошибка установки кодировки: " + e.getMessage());
        }
    }

    public static void setLevel(Level level) {
        currentLevel = level;
        info("Logger", "Уровень логирования установлен на " + level.getName());
    }

    public static void debug(String tag, String message) {
        if (currentLevel.ordinal() <= Level.DEBUG.ordinal()) {
            log(Level.DEBUG, tag, message);
        }
    }

    public static void info(String tag, String message) {
        if (currentLevel.ordinal() <= Level.INFO.ordinal()) {
            log(Level.INFO, tag, message);
        }
    }

    public static void warning(String tag, String message) {
        if (currentLevel.ordinal() <= Level.WARNING.ordinal()) {
            log(Level.WARNING, tag, message);
        }
    }

    public static void error(String tag, String message) {
        log(Level.ERROR, tag, message);
    }

    public static void error(String tag, String message, Exception e) {
        log(Level.ERROR, tag, message + " - " + e.getMessage());
        log(Level.ERROR, tag, getStackTraceAsString(e));
    }

    private static void log(Level level, String tag, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logMessage = String.format("[%s] [%s] [%s] %s", timestamp, level.getName(), tag, message);

        // Вывод в консоль с правильной кодировкой
        try {
            if (level == Level.ERROR) {
                System.err.println(logMessage);
            } else {
                System.out.println(logMessage);
            }
        } catch (Exception e) {
            System.err.println("Ошибка вывода в консоль: " + e.getMessage());
        }

        // Запись в файл с UTF-8
        try (OutputStreamWriter osw = new OutputStreamWriter(
                new FileOutputStream(LOG_FILE, true), StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(osw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(logMessage);
        } catch (IOException e) {
            System.err.println("Не удалось записать в лог-файл: " + e.getMessage());
        }
    }

    private static String getStackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    public static void clearLog() {
        try (FileWriter fw = new FileWriter(LOG_FILE)) {
            fw.write("");
            info("Logger", "Лог-файл очищен");
        } catch (IOException e) {
            error("Logger", "Не удалось очистить лог-файл", e);
        }
    }
}