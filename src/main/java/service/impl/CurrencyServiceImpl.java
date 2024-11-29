package service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import io.github.cdimascio.dotenv.Dotenv;
import service.CurrencyService;
import validator.DateValidator;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class CurrencyServiceImpl implements CurrencyService {

    // Загружаем переменные из файла .env
    private static final Dotenv dotenv = Dotenv.load();
    private static final String API_URL = dotenv.get("API_URL");
    private static final String API_URL_TEMPLATE = dotenv.get("API_URL_TEMPLATE");

    // Реализация метода получения текущих курсов валют
    @Override
    public String getCurrencyRates() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(API_URL).openConnection();
            connection.setRequestMethod("GET");

            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> rates = objectMapper.readValue(connection.getInputStream(),
                    new TypeReference<List<Map<String, Object>>>() {});

            StringBuilder result = new StringBuilder("Курси валют ПриватБанку (готівкові):\n");
            for (Map<String, Object> rate : rates) {
                String currency = (String) rate.get("ccy");
                if (currency != null && (currency.equals("USD") || currency.equals("EUR"))) {
                    String buyRate = formatRate(rate.get("buy"));
                    String saleRate = formatRate(rate.get("sale"));
                    result.append(String.format("%s: Купівля - %s, Продаж - %s\n", currency, buyRate, saleRate));
                }
            }
            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "Не вдалося отримати дані про курси валют. Спробуйте пізніше.";
        }
    }

    // Реализация метода получения исторических курсов валют по дате
    @Override
    public String getHistoricalRates(String dateInput) {
        String validationError = DateValidator.validateDate(dateInput);
        if (validationError != null) {
            return validationError;
        }

        try {
            String apiUrl = String.format(API_URL_TEMPLATE, dateInput);
            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("GET");

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> response = objectMapper.readValue(connection.getInputStream(),
                    new TypeReference<Map<String, Object>>() {});

            List<Map<String, Object>> rates = (List<Map<String, Object>>) response.get("exchangeRate");

            StringBuilder result = new StringBuilder("Курси валют ПриватБанку за " + dateInput + ":\n");
            for (Map<String, Object> rate : rates) {
                String currency = (String) rate.get("currency");
                if (currency != null && (currency.equals("USD") || currency.equals("EUR"))) {
                    String buyRate = formatRate(rate.get("purchaseRate"));
                    String saleRate = formatRate(rate.get("saleRate"));
                    result.append(String.format("%s: Купівля - %s, Продаж - %s\n", currency, buyRate, saleRate));
                }
            }

            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "Не вдалося отримати дані про курси валют за вказану дату. Спробуйте пізніше.";
        }
    }

    // Метод для форматирования курсов с двумя знаками после запятой
    private String formatRate(Object rate) {
        if (rate == null) {
            return "N/A";
        }
        return String.format("%.2f", Double.parseDouble(rate.toString()));
    }
}
