package validator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateValidator {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    // Метод для валидации даты
    public static String validateDate(String dateInput) {
        try {
            Date inputDate = DATE_FORMAT.parse(dateInput);
            Date today = new Date();

            // Проверка на будущую дату
            if (inputDate.after(today)) {
                return "Помилка: Введена дата не може бути у майбутньому.";
            }

            // Проверка на дату более чем 4 года назад
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            calendar.add(Calendar.YEAR, -4);
            Date fourYearsAgo = calendar.getTime();

            if (inputDate.before(fourYearsAgo)) {
                return "Помилка: Дані доступні лише за останні 4 роки.";
            }

        } catch (ParseException e) {
            return "Помилка: Введіть дату у форматі dd.MM.yyyy.";
        }

        return null; // Дата корректная
    }
}

