import utils.Logger;
import utils.DatabaseDebugHelper;
import views.MainFrame;

public class Main {
    public static void main(String[] args) {
        // Инициализация логирования
        Logger.info("Main", "Запуск приложения 'Дневник питания'");
        Logger.info("Main", "Версия: 1.0");
        Logger.info("Main", "Java версия: " + System.getProperty("java.version"));

        // Отладочная информация о системе
        Logger.debug("Main", "OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        Logger.debug("Main", "Working directory: " + System.getProperty("user.dir"));

        // Запуск GUI в потоке EDT
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                Logger.info("Main", "Запуск графического интерфейса");

                // Проверка базы данных
                DatabaseDebugHelper.checkDatabaseIntegrity();
                DatabaseDebugHelper.printDatabaseInfo();

                MainFrame frame = new MainFrame();
                frame.setVisible(true);

                Logger.info("Main", "Приложение успешно запущено");
            } catch (Exception e) {
                Logger.error("Main", "Критическая ошибка при запуске", e);
                javax.swing.JOptionPane.showMessageDialog(null,
                        "Ошибка запуска приложения:\n" + e.getMessage(),
                        "Критическая ошибка",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}