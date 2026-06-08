package views;

import database.DatabaseHelper;
import models.FoodEntry;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class WeekCalendarPanel extends JPanel {
    private LocalDate currentWeekStart;
    private DayPanel[] dayPanels;
    private Runnable onDateSelected;

    public WeekCalendarPanel(Runnable onDateSelected) {
        this.onDateSelected = onDateSelected;
        setLayout(new GridLayout(1, 7, 5, 5));
        setBorder(BorderFactory.createTitledBorder("Календарь недели"));

        dayPanels = new DayPanel[7];
        for (int i = 0; i < 7; i++) {
            dayPanels[i] = new DayPanel();
            add(dayPanels[i]);
        }

        refreshWeek();
    }

    public void refreshWeek() {
        LocalDate today = LocalDate.now();
        currentWeekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);

        for (int i = 0; i < 7; i++) {
            LocalDate date = currentWeekStart.plusDays(i);
            dayPanels[i].updateDate(date);
        }
    }

    private class DayPanel extends JPanel {
        private JLabel dateLabel;
        private JLabel kcalLabel;
        private LocalDate date;

        public DayPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createLineBorder(Color.GRAY));
            setBackground(Color.WHITE);

            dateLabel = new JLabel();
            dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            dateLabel.setFont(new Font("Arial", Font.BOLD, 12));

            kcalLabel = new JLabel();
            kcalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            kcalLabel.setFont(new Font("Arial", Font.PLAIN, 11));

            add(Box.createVerticalStrut(5));
            add(dateLabel);
            add(Box.createVerticalStrut(5));
            add(kcalLabel);
            add(Box.createVerticalStrut(5));

            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    if (onDateSelected != null && date != null) {
                        // Здесь можно вызвать метод для выбора даты
                        setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
                    }
                }
            });
        }

        public void updateDate(LocalDate date) {
            this.date = date;
            dateLabel.setText(date.format(DateTimeFormatter.ofPattern("dd.MM")));

            List<FoodEntry> meals = DatabaseHelper.getMealsForDate(date);
            double totalKcal = meals.stream().mapToDouble(FoodEntry::getTotalCalories).sum();

            kcalLabel.setText(String.format("%.0f ккал", totalKcal));

            if (date.equals(LocalDate.now())) {
                setBackground(new Color(255, 255, 200));
            } else if (totalKcal > 0) {
                setBackground(new Color(200, 255, 200));
            } else {
                setBackground(Color.WHITE);
            }
        }
    }
}