package utils;

import database.DatabaseHelper;
import models.FoodEntry;
import models.Product;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class DatabaseDebugHelper {

    public static void printDatabaseInfo() {
        Logger.info("DatabaseDebug", "========== ИНФОРМАЦИЯ О БАЗЕ ДАННЫХ ==========");

        // Информация о таблицах
        checkTableStructure("user");
        checkTableStructure("products");
        checkTableStructure("meals");

        // Количество записей
        printTableCount("user", "Пользователи");
        printTableCount("products", "Продукты");
        printTableCount("meals", "Приёмы пищи");

        // Примеры данных
        printSampleProducts();
        printSampleMeals();
    }

    private static void checkTableStructure(String tableName) {
        String sql = "PRAGMA table_info(" + tableName + ")";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:food_diary.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Таблица ").append(tableName).append(": ");
            while (rs.next()) {
                sb.append(rs.getString("name")).append("(").append(rs.getString("type")).append(") ");
            }
            Logger.debug("DatabaseDebug", sb.toString());
        } catch (SQLException e) {
            Logger.error("DatabaseDebug", "Ошибка проверки таблицы " + tableName, e);
        }
    }

    private static void printTableCount(String tableName, String description) {
        String sql = "SELECT COUNT(*) as count FROM " + tableName;
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:food_diary.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int count = rs.getInt("count");
            Logger.info("DatabaseDebug", description + ": " + count);
        } catch (SQLException e) {
            Logger.error("DatabaseDebug", "Ошибка подсчёта в " + tableName, e);
        }
    }

    private static void printSampleProducts() {
        List<Product> products = DatabaseHelper.getAllProducts();
        Logger.debug("DatabaseDebug", "Примеры продуктов (первые 5):");
        for (int i = 0; i < Math.min(5, products.size()); i++) {
            Product p = products.get(i);
            Logger.debug("DatabaseDebug", String.format("  %d. %s - %.0f ккал/100г",
                    i+1, p.getName(), p.getCalories()));
        }
    }

    private static void printSampleMeals() {
        List<FoodEntry> meals = DatabaseHelper.getMealsForDate(LocalDate.now());
        Logger.debug("DatabaseDebug", "Приёмы пищи за сегодня: " + meals.size());
        for (FoodEntry meal : meals) {
            Logger.debug("DatabaseDebug", String.format("  - %s: %.0fг (%.0f ккал)",
                    meal.getProduct().getName(), meal.getGrams(), meal.getTotalCalories()));
        }
    }

    public static void checkDatabaseIntegrity() {
        Logger.info("DatabaseDebug", "Проверка целостности базы данных...");

        String sql = "PRAGMA integrity_check";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:food_diary.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            String result = rs.getString(1);
            if ("ok".equals(result)) {
                Logger.info("DatabaseDebug", "База данных в порядке ✓");
            } else {
                Logger.error("DatabaseDebug", "Проблемы с базой данных: " + result);
            }
        } catch (SQLException e) {
            Logger.error("DatabaseDebug", "Ошибка проверки целостности", e);
        }
    }

    public static void vacuumDatabase() {
        Logger.info("DatabaseDebug", "Оптимизация базы данных...");
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:food_diary.db");
             Statement stmt = conn.createStatement()) {
            stmt.execute("VACUUM");
            Logger.info("DatabaseDebug", "База данных оптимизирована");
        } catch (SQLException e) {
            Logger.error("DatabaseDebug", "Ошибка оптимизации БД", e);
        }
    }
}