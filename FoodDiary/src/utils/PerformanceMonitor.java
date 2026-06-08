package utils;

import java.util.HashMap;
import java.util.Map;

public class PerformanceMonitor {
    private static Map<String, Long> startTimes = new HashMap<>();
    private static Map<String, Integer> callCounts = new HashMap<>();
    private static Map<String, Long> totalTimes = new HashMap<>();

    public static void startOperation(String operationName) {
        startTimes.put(operationName, System.currentTimeMillis());
        Logger.debug("Performance", "Начало операции: " + operationName);
    }

    public static void endOperation(String operationName) {
        Long startTime = startTimes.get(operationName);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            Logger.debug("Performance", "Операция '" + operationName + "' выполнена за " + duration + " мс");

            // Статистика
            callCounts.put(operationName, callCounts.getOrDefault(operationName, 0) + 1);
            totalTimes.put(operationName, totalTimes.getOrDefault(operationName, 0L) + duration);

            if (duration > 100) {
                Logger.warning("Performance", "Медленная операция: " + operationName + " - " + duration + " мс");
            }

            startTimes.remove(operationName);
        } else {
            Logger.warning("Performance", "Операция '" + operationName + "' не была запущена");
        }
    }

    public static void printStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========== СТАТИСТИКА ПРОИЗВОДИТЕЛЬНОСТИ ==========\n");
        for (String op : callCounts.keySet()) {
            int count = callCounts.get(op);
            long totalTime = totalTimes.get(op);
            long avgTime = totalTime / count;
            sb.append(String.format("%-30s | Вызовов: %4d | Среднее: %5d мс | Всего: %6d мс\n",
                    op, count, avgTime, totalTime));
        }
        sb.append("==================================================\n");
        Logger.info("PerformanceMonitor", sb.toString());
    }

    public static void reset() {
        startTimes.clear();
        callCounts.clear();
        totalTimes.clear();
        Logger.info("PerformanceMonitor", "Статистика сброшена");
    }
}