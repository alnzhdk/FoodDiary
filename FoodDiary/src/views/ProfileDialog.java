package views;

import database.DatabaseHelper;
import models.User;

import javax.swing.*;
import java.awt.*;

public class ProfileDialog extends JDialog {
    private JTextField nameField, ageField, weightField, heightField;
    private JComboBox<String> genderCombo, activityCombo;
    private Runnable onSave;

    public ProfileDialog(Frame parent, Runnable onSave) {
        super(parent, "Профиль пользователя", true);
        this.onSave = onSave;
        setSize(400, 350);
        setLocationRelativeTo(parent);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        User user = DatabaseHelper.getUser();

        // Имя
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Имя:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(user != null ? user.getName() : "", 15);
        add(nameField, gbc);

        // Возраст
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Возраст:"), gbc);
        gbc.gridx = 1;
        ageField = new JTextField(user != null ? String.valueOf(user.getAge()) : "", 15);
        add(ageField, gbc);

        // Вес
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Вес (кг):"), gbc);
        gbc.gridx = 1;
        weightField = new JTextField(user != null ? String.valueOf(user.getWeight()) : "", 15);
        add(weightField, gbc);

        // Рост
        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Рост (см):"), gbc);
        gbc.gridx = 1;
        heightField = new JTextField(user != null ? String.valueOf(user.getHeight()) : "", 15);
        add(heightField, gbc);

        // Пол
        gbc.gridx = 0; gbc.gridy = 4;
        add(new JLabel("Пол:"), gbc);
        gbc.gridx = 1;
        genderCombo = new JComboBox<>(new String[]{"male", "female"});
        if (user != null && user.getGender() != null) genderCombo.setSelectedItem(user.getGender());
        add(genderCombo, gbc);

        // Активность
        gbc.gridx = 0; gbc.gridy = 5;
        add(new JLabel("Активность:"), gbc);
        gbc.gridx = 1;
        activityCombo = new JComboBox<>(new String[]{"low", "medium", "high"});
        if (user != null && user.getActivityLevel() != null) activityCombo.setSelectedItem(user.getActivityLevel());
        add(activityCombo, gbc);

        // Кнопки
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Сохранить");
        JButton cancelButton = new JButton("Отмена");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        add(buttonPanel, gbc);

        saveButton.addActionListener(e -> saveProfile());
        cancelButton.addActionListener(e -> dispose());
    }

    private void saveProfile() {
        try {
            String name = nameField.getText().trim();
            int age = Integer.parseInt(ageField.getText().trim());
            double weight = Double.parseDouble(weightField.getText().trim());
            double height = Double.parseDouble(heightField.getText().trim());
            String gender = (String) genderCombo.getSelectedItem();
            String activity = (String) activityCombo.getSelectedItem();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите имя");
                return;
            }

            User user = new User(name, age, weight, height, gender, activity);
            DatabaseHelper.saveUser(user);
            JOptionPane.showMessageDialog(this, "Профиль сохранён");
            if (onSave != null) onSave.run();
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Проверьте ввод чисел");
        }
    }
}