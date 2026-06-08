package views;

import database.DatabaseHelper;
import models.Product;

import javax.swing.*;
import java.awt.*;

public class AddProductDialog extends JDialog {
    private JTextField nameField, caloriesField, proteinField, fatField, carbsField;
    private Runnable onProductAdded;

    public AddProductDialog(Dialog parent, Runnable onProductAdded) {
        super(parent, "Добавить продукт", true);
        this.onProductAdded = onProductAdded;
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Название
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Название:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(15);
        add(nameField, gbc);

        // Калории
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Ккал на 100г:"), gbc);
        gbc.gridx = 1;
        caloriesField = new JTextField(15);
        add(caloriesField, gbc);

        // Белки
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Белки (г):"), gbc);
        gbc.gridx = 1;
        proteinField = new JTextField(15);
        add(proteinField, gbc);

        // Жиры
        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Жиры (г):"), gbc);
        gbc.gridx = 1;
        fatField = new JTextField(15);
        add(fatField, gbc);

        // Углеводы
        gbc.gridx = 0; gbc.gridy = 4;
        add(new JLabel("Углеводы (г):"), gbc);
        gbc.gridx = 1;
        carbsField = new JTextField(15);
        add(carbsField, gbc);

        // Кнопки
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Сохранить");
        JButton cancelButton = new JButton("Отмена");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        add(buttonPanel, gbc);

        saveButton.addActionListener(e -> addProduct());
        cancelButton.addActionListener(e -> dispose());
    }

    private void addProduct() {
        try {
            String name = nameField.getText().trim();
            double calories = Double.parseDouble(caloriesField.getText().trim());
            double protein = Double.parseDouble(proteinField.getText().trim());
            double fat = Double.parseDouble(fatField.getText().trim());
            double carbs = Double.parseDouble(carbsField.getText().trim());

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите название продукта");
                return;
            }

            Product product = new Product(0, name, calories, protein, fat, carbs);
            DatabaseHelper.addProduct(product);
            JOptionPane.showMessageDialog(this, "Продукт добавлен");
            if (onProductAdded != null) onProductAdded.run();
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Проверьте ввод чисел");
        }
    }
}