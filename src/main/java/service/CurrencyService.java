package service;

public interface CurrencyService {
    // Метод для получения текущих курсов валют
    String getCurrencyRates();

    // Метод для получения исторических курсов валют по дате
   String getHistoricalRates(String dateInput);
}