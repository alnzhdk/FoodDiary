package views;

import database.DatabaseHelper;
import models.FoodEntry;
import models.Product;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class AddMealDialog extends JDialog {
    private JComboBox<Product> productCombo;
    private JTextField gramsField;
    private LocalDate date;
    private Runnable onAdd;

    public AddMealDialog(Frame parent, LocalDate date, Runnable onAdd) {
        super(parent, "Добавить приём пищи", true);
        this.date = date;
        this.onAdd = onAdd;
        setSize(400, 200);
        setLocationRelativeTo(parent);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Продукт
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Продукт:"), gbc);
        gbc.gridx = 1;
        List<Product> products = DatabaseHelper.getAllProducts();
        productCombo = new JComboBox<>(products.toArray(new Product[0]));
        productCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Product) {
                    value = ((Product) value).getName();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        add(productCombo, gbc);

        // Кнопка добавить новый продукт
        JButton newProductButton = new JButton("+ Новый продукт");
        gbc.gridx = 2;
        add(newProductButton, gbc);
        newProductButton.addActionListener(e -> {
            new AddProductDialog(this, () -> {
                refreshProducts();
            }).setVisible(true);
        });

        // Вес
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Вес (г):"), gbc);
        gbc.gridx = 1;
        gramsField = new JTextField(10);
        add(gramsField, gbc);

        // Кнопки
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Добавить");
        JButton cancelButton = new JButton("Отмена");
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 3;
        add(buttonPanel, gbc);

        addButton.addActionListener(e -> addMeal());
        cancelButton.addActionListener(e -> dispose());
    }

    private void refreshProducts() {
        List<Product> products = DatabaseHelper.getAllProducts();
        productCombo.removeAllItems();
        for (Product p : products) {
            productCombo.addItem(p);
        }
    }

    private void addMeal() {
        try {
            Product selected = (Product) productCombo.getSelectedItem();
            double grams = Double.parseDouble(gramsField.getText().trim());
            if (grams <= 0) {
                JOptionPane.showMessageDialog(this, "Вес должен быть больше 0");
                return;
            }
            FoodEntry entry = new FoodEntry(0, date, selected, grams);
            DatabaseHelper.addMeal(entry);
            if (onAdd != null) onAdd.run();
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Введите корректный вес");
        }
    }
}