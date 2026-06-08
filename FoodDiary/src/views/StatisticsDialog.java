package views;

import database.DatabaseHelper;
import models.FoodEntry;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StatisticsDialog extends JDialog {
    private JTextArea statsArea;

    public StatisticsDialog(Frame parent) {
        super(parent, "Статистика", true);
        setSize(600, 500);
        setLocationRelativeTo(parent);

        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        JLabel titleLabel = new JLabel("Статистика питания", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(titleLabel);

        statsArea = new JTextArea();
        statsArea.setEditable(false);
        statsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(statsArea);

        JPanel buttonPanel = new JPanel();
        JButton refreshButton = new JButton("Обновить");
        JButton closeButton = new JButton("Закрыть");
        refreshButton.addActionListener(e -> loadStatistics());
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        loadStatistics();
    }

    private void loadStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                         СТАТИСТИКА ПИТАНИЯ                         ║\n");
        sb.append("╚════════════════════════════════════════════════════════════════════╝\n\n");

        // Статистика за сегодня
        LocalDate today = LocalDate.now();
        double todayKcal = DatabaseHelper.getTotalCaloriesForDate(today);
        List<FoodEntry> todayMeals = DatabaseHelper.getMealsForDate(today);

        sb.append("📅 СЕГОДНЯ:\n");
        sb.append(String.format("   • Калорий: %.1f ккал\n", todayKcal));
        sb.append(String.format("   • Приёмов пищи: %d\n", todayMeals.size()));
        sb.append("\n");

        // Статистика за неделю
        LocalDate weekAgo = today.minusDays(7);
        double weekKcal = DatabaseHelper.getTotalCaloriesForDateRange(weekAgo, today);
        sb.append("📊 ЗА ПОСЛЕДНИЕ 7 ДНЕЙ:\n");
        sb.append(String.format("   • Всего калорий: %.1f ккал\n", weekKcal));
        sb.append(String.format("   • Среднее в день: %.1f ккал\n", weekKcal / 7));
        sb.append("\n");

        // Детальная статистика по дням
        sb.append("┌────────────┬──────────┬────────────┬────────────────────────┐\n");
        sb.append("│    Дата    │ Приёмов  │  Ккал/день │       Прогресс         │\n");
        sb.append("├────────────┼──────────┼────────────┼────────────────────────┤\n");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            List<FoodEntry> meals = DatabaseHelper.getMealsForDate(date);
            double dayKcal = DatabaseHelper.getTotalCaloriesForDate(date);
            int mealsCount = meals.size();

            String dateStr = date.format(DateTimeFormatter.ofPattern("dd.MM"));
            String bar = createSimpleBar(dayKcal, 500, 20);

            sb.append(String.format("│ %10s │ %8d │ %10.1f │ %-22s │\n",
                    dateStr, mealsCount, dayKcal, bar));
        }

        sb.append("└────────────┴──────────┴────────────┴────────────────────────┘\n");

        statsArea.setText(sb.toString());
    }

    private String createSimpleBar(double value, double max, int length) {
        int filled = (int) (value / max * length);
        filled = Math.min(filled, length);
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i < filled) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }
        return bar.toString();
    }
}