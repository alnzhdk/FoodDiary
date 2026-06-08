package database;

import models.User;
import models.Product;
import models.FoodEntry;
import utils.Logger;
import utils.PerformanceMonitor;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:food_diary.db";

    static {
        Logger.info("DatabaseHelper", "Инициализация базы данных");
        PerformanceMonitor.startOperation("DatabaseInit");

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            Logger.debug("DatabaseHelper", "Создание таблиц");

            // таблица пользователя
            stmt.execute("CREATE TABLE IF NOT EXISTS user (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "age INTEGER," +
                    "weight REAL," +
                    "height REAL," +
                    "gender TEXT," +
                    "activity_level TEXT)");

            // таблица продуктов
            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT UNIQUE," +
                    "calories REAL," +
                    "protein REAL," +
                    "fat REAL," +
                    "carbs REAL)");

            // таблица приёмов пищи
            stmt.execute("CREATE TABLE IF NOT EXISTS meals (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "date TEXT," +
                    "product_id INTEGER," +
                    "grams REAL," +
                    "FOREIGN KEY(product_id) REFERENCES products(id))");

            Logger.info("DatabaseHelper", "Таблицы успешно созданы/проверены");
            initDefaultProducts();

            PerformanceMonitor.endOperation("DatabaseInit");
        } catch (SQLException e) {
            Logger.error("DatabaseHelper", "Ошибка инициализации базы данных", e);
            PerformanceMonitor.endOperation("DatabaseInit");
        }
    }

    private static void initDefaultProducts() {
        Logger.debug("DatabaseHelper", "Инициализация продуктов по умолчанию");
        addProductIfNotExists(new Product(0, "Куриная грудка", 165, 31, 3.6, 0));
        addProductIfNotExists(new Product(0, "Рис", 130, 2.7, 0.3, 28));
        addProductIfNotExists(new Product(0, "Гречка", 132, 4.5, 2.3, 25));
        addProductIfNotExists(new Product(0, "Хлеб", 265, 9, 3, 50));
        addProductIfNotExists(new Product(0, "Яйцо", 155, 13, 11, 1.1));
        addProductIfNotExists(new Product(0, "Сыр", 350, 25, 27, 1.3));
        addProductIfNotExists(new Product(0, "Молоко", 42, 3.4, 1, 4.7));
        addProductIfNotExists(new Product(0, "Овсянка", 68, 2.4, 1.4, 12));
        Logger.debug("DatabaseHelper", "Продукты по умолчанию добавлены");
    }

    private static void addProductIfNotExists(Product p) {
        String sql = "INSERT OR IGNORE INTO products(name, calories, protein, fat, carbs) VALUES(?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getName());
            stmt.setDouble(2, p.getCalories());
            stmt.setDouble(3, p.getProtein());
            stmt.setDouble(4, p.getFat());
            stmt.setDouble(5, p.getCarbs());
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.error("DatabaseHelper", "Ошибка добавления продукта: " + p.getName(), e);
        }
    }

    // ==================== USER METHODS ====================

    public static void saveUser(User user) {
        PerformanceMonitor.startOperation("saveUser");
        Logger.info("DatabaseHelper", "Сохранение профиля пользователя: " + user.getName());

        String sql = "DELETE FROM user";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            Logger.debug("DatabaseHelper", "Старый профиль удалён");
        } catch (SQLException e) {
            Logger.error("DatabaseHelper", "Ошибка удаления старого профиля", e);
        }

        sql = "INSERT INTO user(name, age, weight, height, gender, activity_level) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getName());
            pstmt.setInt(2, user.getAge());
            pstmt.setDouble(3, user.getWeight());
            pstmt.setDouble(4, user.getHeight());
            pstmt.setString(5, user.getGender());
            pstmt.setString(6, user.getActivityLevel());
            int rows = pstmt.executeUpdate();
            Logger.info("DatabaseHelper", "Профиль сохранён, затронуто строк: " + rows);
            PerformanceMonitor.endOperation("saveUser");
        } catch (SQLException e) {
            Logger.error("DatabaseHelper", "Ошибка сохранения профиля", e);
            PerformanceMonitor.endOperation("saveUser");
        }
    }

    public static User getUser() {
        PerformanceMonitor.startOperation("getUser");
        Logger.debug("DatabaseHelper", "Загрузка профиля пользователя");

        String sql = "SELECT * FROM user LIMIT 1";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                User user = new User(
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getDouble("weight"),
                        rs.getDouble("height"),
                        rs.getString("gender"),
                        rs.getString("activity_level")
                );
                Logger.info("DatabaseHelper", "Профиль загружен: " + user.getName());
                PerformanceMonitor.endOperation("getUser");
                return user;
            } else {
                Logger.warning("DatabaseHelper", "Профиль не найден");
                PerformanceMonitor.endOperation("getUser");
                return null;
            }
        } catch (SQLException e) {
            Logger.error("DatabaseHelper", "Ошибка загрузки профиля", e);
            PerformanceMonitor.endOperation("getUser");
            return null;
        }
    }

    // ==================== PRODUCT METHODS ====================

    public static List<Product> getAllProducts() {
        PerformanceMonitor.startOperation("getAllProducts");
        Logger.debug("DatabaseHelper", "Загрузка всех продуктов");

        List<Product> list = new ArrayList<>();
        String sql = "SELECT id, name, calories, protein, fat, carbs FROM products ORDER BY name";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Product product = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("calories"),
                        rs.getDouble("protein"),
                        rs.getDouble("fat"),
                        rs.getDouble("carbs")
                );
                list.add(product);
            }
            Logger.info("DatabaseHelper", "Загружено продуктов: " + list.size());
            PerformanceMonitor.endOperation("getAllProducts");
        } catch (SQLException e) {
            Logger.error("DatabaseHelper", "Ошибка загрузки продуктов", e);
            PerformanceMonitor.endOperation("getAllProducts");
        }
        return list;
    }

    public static void addProduct(Product p) {
        PerformanceMonitor.startOperation("addProduct");
        Logger.info("DatabaseHelper", "Добавление продукта: " + p.getName());

        String sql = "INSERT INTO products(name, calories, protein, fat, carbs) VALUES(?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getName());
            pstmt.setDouble(2, p.getCalories());
            pstmt.setDouble(3, p.getProtein());
            pstmt.setDouble(4, p.getFat());
            pstmt.setDouble(5, p.getCarbs());
            int rows = pstmt.executeUpdate();
            Logger.info("DatabaseHelper", "Продукт добавлен, затронуто строк: " + rows);
            PerformanceMonitor.endOperation("addProduct");
        } catch (SQLException e) {
            Logger.error("DatabaseHelper", "Ошибка добавления продукта: " + p.getName(), e);
            PerformanceMonitor.endOperation("addProduct");
        }
    }

    public static Product getProductById(int id) {
        PerformanceMonitor.startOperation("getProductById");
        Logger.debug("DatabaseHelper", "Поиск продукта по ID: " + id);

        String sql = "SELECT id, name, calories, protein, fat, carbs FROM products WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Product product = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("calories"),
                        rs.getDouble("protein"),
                        rs.getDouble("fat"),
                        rs.getDouble("carbs")
                );
                Logger.info("DatabaseHelper", "Продукт найден: " + product.getName());
                PerformanceMonitor.endOperation("getProductById");
                return product;
            } else {
                Logger.warning("DatabaseHelper", "Продукт с ID " + id + " не найден");
                PerformanceMonitor.endOperation("getProductById");
                return null;
            }
        } catch (SQLException e) {
            Logger.error("DatabaseHelper", "Ошибка поиска продукта", e);
            PerformanceMonitor.endOperation("getProductById");
            return null;
        }
    }

    // ==================== MEAL METHODS ====================

    public static void addMeal(FoodEntry entry) {
        PerformanceMonitor.startOperation("addMeal");
        Logger.info("DatabaseHelper", "Добавление приёма пищи: продукт=" +
                entry.getProduct().getName() + ", вес=" + entry.getGrams() + "г, дата=" + entry.getDate());

        String sql = "INSERT INTO meals(date, product_id, grams) VALUES(?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, entry.getDate().toString());
            pstmt.setInt(2, entry.getProduct().getId());
            pstmt.setDouble(3, entry.getGrams());
            int rows = pstmt.executeUpdate();
            Logger.info("DatabaseHelper", "Приём пищи добавлен, затронуто строк: " + rows);
            PerformanceMonitor.endOperation("addMeal");
        } catch (SQLException e) {
            Logger.error("DatabaseHelper", "Ошибка добавления приёма пищи", e);
            PerformanceMonitor.endOperation("addMeal");
        }
    }

    public static List<FoodEntry> getMealsForDate(LocalDate date) {
        PerformanceMonitor.startOperation("getMealsForDate");
        Logger.debug("DatabaseHelper", "Загрузка приёмов пищи за дату: " + date);

        List<FoodEntry> meals = new ArrayList<>();
        String sql = "SELECT m.id, m.date, m.grams, " +
                "p.id, p.name, p.calories, p.protein, p.fat, p.carbs " +
                "FROM meals m " +
                "INNER JOIN products p ON m.product_id = p.id " +
                "WHERE m.date = ? " +
                "ORDER BY m.id";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                // Получаем данные по индексам колонок
                int mealId = rs.getInt(1);
                String mealDate = rs.getString(2);
                double grams = rs.getDouble(3);

                int productId = rs.getInt(4);
                String productName = rs.getString(5);
                double calories = rs.getDouble(6);
                double protein = rs.getDouble(7);
                double fat = rs.getDouble(8);
                double carbs = rs.getDouble(9);

                Product product = new Product(productId, productName, calories, protein, fat, carbs);
                FoodEntry entry = new FoodEntry(mealId, LocalDate.parse(mealDate), product, grams);
                meals.add(entry);
            }
            Logger.info("DatabaseHelper", "Загружено приёмов пищи: " + meals.size());
            PerformanceMonitor.endOperation("getMealsForDate");
        } catch (SQLException e) {
            Logger.error("DatabaseHelper", "Ошибка загрузки приёмов пищи", e);
            PerformanceMonitor.endOperation("getMealsForDate");
        }
        return meals;
    }

    public static void updateMeal(FoodEntry entry) {
        PerformanceMonitor.startOperation("updateMeal");
        Logger.info("DatabaseHelper", "Обновление приёма пищи ID=" + entry.getId() +
                ", новый вес=" + entry.getGrams() + "г");

        String sql = "UPDATE meals SET grams = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, entry.getGrams());
            pstmt.setInt(2, entry.getId());
            int rows = pstmt.executeUpdate();
            Logger.info("DatabaseHelper", "Приём пищи обновлён, затронуто строк: " + rows);
            PerformanceMonitor.endOperation("updateMeal");
        } catch (SQLException e) {
            Logger.error("DatabaseHelper", "Ошибка обновления приёма пищи", e);
            PerformanceMonitor.endOperation("updateMeal");
        }
    }

    public static void deleteMeal(int mealId) {
        PerformanceMonitor.startOperation("deleteMeal");
        Logger.info("DatabaseHelper", "Удаление приёма пищи ID=" + mealId);

        String sql = "DELETE FROM meals WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mealId);
            int rows = pstmt.executeUpdate();
            Logger.info("DatabaseHelper", "Приём пищи удалён, затронуто строк: " + rows);
            PerformanceMonitor.endOperation("deleteMeal");
        } catch (SQLException e) {
            Logger.error("DatabaseHelper", "Ошибка удаления приёма пищи", e);
            PerformanceMonitor.endOperation("deleteMeal");
        }
    }

    public static void deleteAllMealsForDate(LocalDate date) {
        PerformanceMonitor.startOperation("deleteAllMealsForDate");
        Logger.info("DatabaseHelper", "Удаление всех приёмов пищи за дату: " + date);

        String sql = "DELETE FROM meals WHERE date = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date.toString());
            int rows = pstmt.executeUpdate();
            Logger.info("DatabaseHelper", "Удалено приёмов пищи: " + rows);
            PerformanceMonitor.endOperation("deleteAllMealsForDate");
        } catch (SQLException e) {
            Logger.error("DatabaseHelper", "Ошибка удаления приёмов пищи за дату", e);
            PerformanceMonitor.endOperation("deleteAllMealsForDate");
        }
    }

    // ==================== STATISTICS METHODS ====================

    public static double getTotalCaloriesForDate(LocalDate date) {
        PerformanceMonitor.startOperation("getTotalCaloriesForDate");
        Logger.debug("DatabaseHelper", "Расчёт калорий за дату: " + date);

        double total = 0;
        String sql = "SELECT SUM(p.calories * m.grams / 100) as total " +
                "FROM meals m " +
                "INNER JOIN products p ON m.product_id = p.id " +
                "WHERE m.date = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                total = rs.getDouble("total");
            }
            Logger.info("DatabaseHelper", "Всего калорий за " + date + ": " + total);
            PerformanceMonitor.endOperation("getTotalCaloriesForDate");
        } catch (SQLException e) {
            Logger.error("DatabaseHelper", "Ошибка расчёта калорий", e);
            PerformanceMonitor.endOperation("getTotalCaloriesForDate");
        }
        return total;
    }

    public static double getTotalCaloriesForDateRange(LocalDate startDate, LocalDate endDate) {
        PerformanceMonitor.startOperation("getTotalCaloriesForDateRange");
        Logger.debug("DatabaseHelper", "Расчёт калорий за период: " + startDate + " - " + endDate);

        double total = 0;
        String sql = "SELECT SUM(p.calories * m.grams / 100) as total " +
                "FROM meals m " +
                "INNER JOIN products p ON m.product_id = p.id " +
                "WHERE m.date BETWEEN ? AND ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                total = rs.getDouble("total");
            }
            Logger.info("DatabaseHelper", "Всего калорий за период: " + total);
            PerformanceMonitor.endOperation("getTotalCaloriesForDateRange");
        } catch (SQLException e) {
            Logger.error("DatabaseHelper", "Ошибка расчёта калорий за период", e);
            PerformanceMonitor.endOperation("getTotalCaloriesForDateRange");
        }
        return total;
    }

    public static List<Object[]> getDailyStatsForWeek(LocalDate startDate) {
        PerformanceMonitor.startOperation("getDailyStatsForWeek");
        Logger.debug("DatabaseHelper", "Расчёт статистики за неделю с " + startDate);

        List<Object[]> stats = new ArrayList<>();
        String sql = "SELECT m.date, SUM(p.calories * m.grams / 100) as daily_total " +
                "FROM meals m " +
                "INNER JOIN products p ON m.product_id = p.id " +
                "WHERE m.date BETWEEN ? AND ? " +
                "GROUP BY m.date " +
                "ORDER BY m.date";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, startDate.plusDays(6).toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Object[] row = new Object[2];
                row[0] = rs.getString("date");
                row[1] = rs.getDouble("daily_total");
                stats.add(row);
            }
            Logger.info("DatabaseHelper", "Загружено дней со статистикой: " + stats.size());
            PerformanceMonitor.endOperation("getDailyStatsForWeek");
        } catch (SQLException e) {
            Logger.error("DatabaseHelper", "Ошибка расчёта недельной статистики", e);
            PerformanceMonitor.endOperation("getDailyStatsForWeek");
        }
        return stats;
    }
}