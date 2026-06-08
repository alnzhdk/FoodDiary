package views;

import database.DatabaseHelper;
import models.User;
import models.FoodEntry;
import utils.KCalCalculator;
import utils.Logger;
import utils.PerformanceMonitor;
import utils.DebugConsole;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainFrame extends JFrame {
    private JLabel dateLabel, totalCaloriesLabel, progressLabel;
    private JTable mealsTable;
    private DefaultTableModel tableModel;
    private LocalDate currentDate;
    private User currentUser;
    private JButton prevButton, nextButton, todayButton;
    private JLabel weekDayLabel;

    public MainFrame() {
        Logger.info("MainFrame", "Инициализация главного окна");
        PerformanceMonitor.startOperation("MainFrameInit");

        setTitle("Дневник питания");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        currentDate = LocalDate.now();
        currentUser = DatabaseHelper.getUser();

        Logger.debug("MainFrame", "Текущая дата: " + currentDate);
        Logger.debug("MainFrame", "Пользователь: " + (currentUser != null ? currentUser.getName() : "не задан"));

        initComponents();
        refreshData();

        PerformanceMonitor.endOperation("MainFrameInit");
        Logger.info("MainFrame", "Главное окно инициализировано");
    }

    private void initComponents() {
        Logger.debug("MainFrame", "Создание компонентов интерфейса");

        setLayout(new BorderLayout());

        // Верхняя панель с датой и навигацией
        JPanel topPanel = new JPanel(new BorderLayout());

        // Панель навигации по дням
        JPanel navPanel = new JPanel(new FlowLayout());
        prevButton = new JButton("◀ Предыдущий день");
        nextButton = new JButton("Следующий день ▶");
        todayButton = new JButton("Сегодня");

        JPanel datePanel = new JPanel();
        datePanel.setLayout(new BoxLayout(datePanel, BoxLayout.Y_AXIS));
        dateLabel = new JLabel();
        dateLabel.setFont(new Font("Arial", Font.BOLD, 20));
        dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        weekDayLabel = new JLabel();
        weekDayLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        weekDayLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        datePanel.add(dateLabel);
        datePanel.add(weekDayLabel);

        navPanel.add(prevButton);
        navPanel.add(todayButton);
        navPanel.add(nextButton);

        topPanel.add(navPanel, BorderLayout.WEST);
        topPanel.add(datePanel, BorderLayout.CENTER);

        // Правая панель с кнопками
        JPanel rightPanel = new JPanel(new FlowLayout());
        JButton profileButton = new JButton("⚙ Профиль");
        JButton statsButton = new JButton("📊 Статистика");
        JButton debugButton = new JButton("🐛 Отладка");
        rightPanel.add(profileButton);
        rightPanel.add(statsButton);
        rightPanel.add(debugButton);
        topPanel.add(rightPanel, BorderLayout.EAST);

        prevButton.addActionListener(e -> changeDate(-1));
        nextButton.addActionListener(e -> changeDate(1));
        todayButton.addActionListener(e -> goToToday());
        profileButton.addActionListener(e -> openProfile());
        statsButton.addActionListener(e -> openStatistics());
        debugButton.addActionListener(e -> openDebugConsole());

        add(topPanel, BorderLayout.NORTH);

        // Панель со статистикой за день
        JPanel statsPanel = new JPanel(new GridLayout(2, 1, 10, 5));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Статистика за день"));
        totalCaloriesLabel = new JLabel();
        totalCaloriesLabel.setFont(new Font("Arial", Font.BOLD, 16));
        progressLabel = new JLabel();
        progressLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statsPanel.add(totalCaloriesLabel);
        statsPanel.add(progressLabel);
        add(statsPanel, BorderLayout.SOUTH);

        // Центральная панель с таблицей
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Приёмы пищи"));

        // Таблица
        tableModel = new DefaultTableModel(new String[]{"Продукт", "Вес (г)", "Ккал", "Белки (г)", "Жиры (г)", "Углеводы (г)"}, 0);
        mealsTable = new JTable(tableModel);
        mealsTable.setRowHeight(25);
        mealsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        JScrollPane scrollPane = new JScrollPane(mealsTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Панель кнопок управления записями
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("➕ Добавить приём пищи");
        JButton editButton = new JButton("✏ Редактировать");
        JButton deleteButton = new JButton("🗑 Удалить");
        JButton clearDayButton = new JButton("❌ Очистить день");

        addButton.setPreferredSize(new Dimension(180, 35));
        editButton.setPreferredSize(new Dimension(150, 35));
        deleteButton.setPreferredSize(new Dimension(150, 35));
        clearDayButton.setPreferredSize(new Dimension(150, 35));

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearDayButton);

        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> {
            Logger.debug("MainFrame", "Нажата кнопка 'Добавить приём пищи'");
            openAddMeal();
        });
        editButton.addActionListener(e -> {
            Logger.debug("MainFrame", "Нажата кнопка 'Редактировать'");
            openEditMeal();
        });
        deleteButton.addActionListener(e -> {
            Logger.debug("MainFrame", "Нажата кнопка 'Удалить'");
            deleteMeal();
        });
        clearDayButton.addActionListener(e -> {
            Logger.debug("MainFrame", "Нажата кнопка 'Очистить день'");
            clearDay();
        });

        add(centerPanel, BorderLayout.CENTER);

        Logger.debug("MainFrame", "Интерфейс создан");
    }

    private void changeDate(int delta) {
        Logger.debug("MainFrame", "Изменение даты на " + (delta > 0 ? "+" : "") + delta + " дней");
        currentDate = currentDate.plusDays(delta);
        refreshData();
    }

    private void goToToday() {
        Logger.info("MainFrame", "Переход к сегодняшней дате");
        currentDate = LocalDate.now();
        refreshData();
    }

    private void updateDateDisplay() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        dateLabel.setText(currentDate.format(formatter));

        String weekDay;
        switch (currentDate.getDayOfWeek().getValue()) {
            case 1: weekDay = "Понедельник"; break;
            case 2: weekDay = "Вторник"; break;
            case 3: weekDay = "Среда"; break;
            case 4: weekDay = "Четверг"; break;
            case 5: weekDay = "Пятница"; break;
            case 6: weekDay = "Суббота"; break;
            case 7: weekDay = "Воскресенье"; break;
            default: weekDay = "";
        }
        weekDayLabel.setText(weekDay);

        // Подсветка кнопки "Сегодня"
        if (currentDate.equals(LocalDate.now())) {
            todayButton.setBackground(new Color(144, 238, 144));
        } else {
            todayButton.setBackground(null);
        }
    }

    private void refreshData() {
        PerformanceMonitor.startOperation("refreshData");
        Logger.debug("MainFrame", "Обновление данных для даты: " + currentDate);

        updateDateDisplay();

        List<FoodEntry> meals = DatabaseHelper.getMealsForDate(currentDate);
        tableModel.setRowCount(0);

        double totalKcal = 0, totalP = 0, totalF = 0, totalC = 0;

        if (meals.isEmpty()) {
            tableModel.addRow(new Object[]{"Нет записей", "", "", "", "", ""});
            Logger.debug("MainFrame", "Нет записей за выбранную дату");
        } else {
            for (FoodEntry m : meals) {
                tableModel.addRow(new Object[]{
                        m.getProduct().getName(),
                        String.format("%.1f", m.getGrams()),
                        String.format("%.1f", m.getTotalCalories()),
                        String.format("%.1f", m.getTotalProtein()),
                        String.format("%.1f", m.getTotalFat()),
                        String.format("%.1f", m.getTotalCarbs())
                });
                totalKcal += m.getTotalCalories();
                totalP += m.getTotalProtein();
                totalF += m.getTotalFat();
                totalC += m.getTotalCarbs();
            }
            Logger.debug("MainFrame", "Загружено записей: " + meals.size());
        }

        totalCaloriesLabel.setText(String.format("📊 Итого: %.1f ккал | 🥩 Белки: %.1f г | 🧈 Жиры: %.1f г | 🍚 Углеводы: %.1f г",
                totalKcal, totalP, totalF, totalC));

        if (currentUser != null) {
            double norm = KCalCalculator.calculateDailyNorm(currentUser);
            int percent = (int) (totalKcal / norm * 100);
            String progressBar = createProgressBar(percent);
            progressLabel.setText(String.format("🎯 Норма на день: %.0f ккал | Прогресс: %d%% %s", norm, Math.min(percent, 100), progressBar));

            if (totalKcal > norm) {
                progressLabel.setForeground(Color.RED);
                Logger.debug("MainFrame", "Превышение нормы калорий: " + totalKcal + "/" + norm);
            } else if (totalKcal > norm * 0.8) {
                progressLabel.setForeground(new Color(255, 140, 0));
            } else {
                progressLabel.setForeground(new Color(0, 150, 0));
            }
        } else {
            progressLabel.setText("⚠ Заполните профиль для расчёта нормы калорий");
            progressLabel.setForeground(Color.GRAY);
            Logger.warning("MainFrame", "Профиль пользователя не заполнен");
        }

        PerformanceMonitor.endOperation("refreshData");
    }

    private String createProgressBar(int percent) {
        int barLength = 20;
        int filled = Math.min(barLength, percent * barLength / 100);
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }
        bar.append("]");
        return bar.toString();
    }

    private void clearDay() {
        Logger.info("MainFrame", "Очистка дня: " + currentDate);

        List<FoodEntry> meals = DatabaseHelper.getMealsForDate(currentDate);
        if (meals.isEmpty()) {
            Logger.debug("MainFrame", "За этот день уже нет записей");
            JOptionPane.showMessageDialog(this, "За этот день уже нет записей");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите удалить ВСЕ записи за " + currentDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + "?",
                "Подтверждение", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            DatabaseHelper.deleteAllMealsForDate(currentDate);
            refreshData();
            JOptionPane.showMessageDialog(this, "День очищен");
            Logger.info("MainFrame", "День успешно очищен");
        } else {
            Logger.debug("MainFrame", "Очистка дня отменена");
        }
    }

    private void openProfile() {
        Logger.debug("MainFrame", "Открытие окна профиля");
        new ProfileDialog(this, () -> {
            currentUser = DatabaseHelper.getUser();
            refreshData();
            Logger.info("MainFrame", "Профиль обновлён");
        }).setVisible(true);
    }

    private void openStatistics() {
        Logger.debug("MainFrame", "Открытие окна статистики");
        new StatisticsDialog(this).setVisible(true);
    }

    private void openDebugConsole() {
        Logger.info("MainFrame", "Открытие отладочной консоли");
        DebugConsole debugConsole = new DebugConsole();
        debugConsole.setVisible(true);
    }

    private void openAddMeal() {
        Logger.debug("MainFrame", "Открытие окна добавления приёма пищи");
        new AddMealDialog(this, currentDate, () -> {
            refreshData();
            Logger.debug("MainFrame", "Приём пищи добавлен, данные обновлены");
        }).setVisible(true);
    }

    private void openEditMeal() {
        int row = mealsTable.getSelectedRow();
        if (row == -1) {
            Logger.warning("MainFrame", "Попытка редактирования без выбора записи");
            JOptionPane.showMessageDialog(this, "Выберите запись для редактирования");
            return;
        }

        List<FoodEntry> meals = DatabaseHelper.getMealsForDate(currentDate);
        if (meals.isEmpty()) {
            Logger.warning("MainFrame", "Нет записей для редактирования");
            JOptionPane.showMessageDialog(this, "Нет записей для редактирования");
            return;
        }

        if (row < meals.size()) {
            Logger.debug("MainFrame", "Редактирование записи: " + meals.get(row).getProduct().getName());
            new EditMealDialog(this, meals.get(row), () -> {
                refreshData();
                Logger.debug("MainFrame", "Запись отредактирована");
            }).setVisible(true);
        }
    }

    private void deleteMeal() {
        int row = mealsTable.getSelectedRow();
        if (row == -1) {
            Logger.warning("MainFrame", "Попытка удаления без выбора записи");
            JOptionPane.showMessageDialog(this, "Выберите запись для удаления");
            return;
        }

        List<FoodEntry> meals = DatabaseHelper.getMealsForDate(currentDate);
        if (meals.isEmpty()) {
            Logger.warning("MainFrame", "Нет записей для удаления");
            JOptionPane.showMessageDialog(this, "Нет записей для удаления");
            return;
        }

        if (row < meals.size()) {
            String productName = meals.get(row).getProduct().getName();
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Удалить запись \"" + productName + "\"?",
                    "Подтверждение", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                DatabaseHelper.deleteMeal(meals.get(row).getId());
                refreshData();
                Logger.info("MainFrame", "Запись удалена: " + productName);
            } else {
                Logger.debug("MainFrame", "Удаление записи отменено");
            }
        }
    }
}