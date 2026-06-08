package views;

import database.DatabaseHelper;
import models.FoodEntry;

import javax.swing.*;
import java.awt.*;

public class EditMealDialog extends JDialog {
    private FoodEntry entry;
    private JTextField gramsField;
    private Runnable onEdit;

    public EditMealDialog(Frame parent, FoodEntry entry, Runnable onEdit) {
        super(parent, "Редактировать приём пищи", true);
        this.entry = entry;
        this.onEdit = onEdit;
        setSize(350, 150);
        setLocationRelativeTo(parent);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Продукт: " + entry.getProduct().getName()), gbc);
        gbc.gridx = 1;
        add(new JLabel(String.format("%.1f ккал/100г", entry.getProduct().getCalories())), gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Вес (г):"), gbc);
        gbc.gridx = 1;
        gramsField = new JTextField(String.valueOf(entry.getGrams()), 10);
        add(gramsField, gbc);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Сохранить");
        JButton cancelButton = new JButton("Отмена");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(buttonPanel, gbc);

        saveButton.addActionListener(e -> saveEdit());
        cancelButton.addActionListener(e -> dispose());
    }

    private void saveEdit() {
        try {
            double grams = Double.parseDouble(gramsField.getText().trim());
            if (grams <= 0) {
                JOptionPane.showMessageDialog(this, "Вес должен быть больше 0");
                return;
            }
            entry.setGrams(grams);
            DatabaseHelper.updateMeal(entry);
            if (onEdit != null) onEdit.run();
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Введите корректный вес");
        }
    }
}