package utils;

import models.User;

public class KCalCalculator {

    public static double calculateDailyNorm(User user) {
        if (user == null) {
            return 2000; // значение по умолчанию
        }

        double bmr;

        // Расчет BMR (базального метаболизма) по формуле Миффлина-Сан Жеора
        if ("male".equalsIgnoreCase(user.getGender())) {
            bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() + 5;
        } else {
            bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() - 161;
        }

        // Коэффициент активности
        double activityFactor;
        switch (user.getActivityLevel().toLowerCase()) {
            case "low":
                activityFactor = 1.2;
                break;
            case "medium":
                activityFactor = 1.55;
                break;
            case "high":
                activityFactor = 1.725;
                break;
            default:
                activityFactor = 1.2;
        }

        return bmr * activityFactor;
    }

    public static double calculateBMI(double weight, double height) {
        if (height <= 0) return 0;
        double heightInMeters = height / 100;
        return weight / (heightInMeters * heightInMeters);
    }

    public static String getBMICategory(double bmi) {
        if (bmi < 18.5) return "Недостаточный вес";
        if (bmi < 25) return "Нормальный вес";
        if (bmi < 30) return "Избыточный вес";
        return "Ожирение";
    }
}