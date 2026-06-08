package utils;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DebugConsole extends JFrame {
    private JTextArea logArea;
    private JButton refreshButton, clearButton, exportButton;
    private Timer refreshTimer;

    public DebugConsole() {
        setTitle("Отладочная консоль - Дневник питания");
        setSize(800, 600);
        setLocationRelativeTo(null);

        initComponents();
        loadLogs();

        // Автообновление каждые 2 секунды
        refreshTimer = new Timer(2000, e -> loadLogs());
        refreshTimer.start();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Панель с кнопками
        JPanel buttonPanel = new JPanel(new FlowLayout());
        refreshButton = new JButton("🔄 Обновить");
        clearButton = new JButton("🗑 Очистить лог");
        exportButton = new JButton("💾 Экспорт");

        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(exportButton);

        // Область для логов
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);

        // Панель статуса
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel statusLabel = new JLabel("Уровень логирования: " + Logger.Level.DEBUG.getName());
        statusPanel.add(statusLabel);

        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        // Обработчики
        refreshButton.addActionListener(e -> loadLogs());
        clearButton.addActionListener(e -> clearLogs());
        exportButton.addActionListener(e -> exportLogs());

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (refreshTimer != null) {
                    refreshTimer.stop();
                }
            }
        });
    }

    private void loadLogs() {
        try {
            File logFile = new File("food_diary.log");
            if (logFile.exists()) {
                String content = new String(Files.readAllBytes(Paths.get("food_diary.log")));
                logArea.setText(content);
                // Прокрутка вниз
                logArea.setCaretPosition(logArea.getDocument().getLength());
            } else {
                logArea.setText("Лог-файл не найден. Приложение будет создавать его при записи.");
            }
        } catch (IOException e) {
            logArea.setText("Ошибка загрузки логов: " + e.getMessage());
        }
    }

    private void clearLogs() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите очистить весь лог-файл?",
                "Подтверждение", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Logger.clearLog();
            loadLogs();
            JOptionPane.showMessageDialog(this, "Лог-файл очищен");
        }
    }

    private void exportLogs() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Сохранить лог-файл");
        fileChooser.setSelectedFile(new File("food_diary_log_" + System.currentTimeMillis() + ".txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File source = new File("food_diary.log");
                File dest = fileChooser.getSelectedFile();
                Files.copy(source.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(this, "Лог экспортирован: " + dest.getName());
                Logger.info("DebugConsole", "Лог экспортирован в " + dest.getName());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Ошибка экспорта: " + e.getMessage());
                Logger.error("DebugConsole", "Ошибка экспорта лога", e);
            }
        }
    }
}