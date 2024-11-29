package bot;

import command.BotCommands;
import keyboard.ButtonCreator;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import service.CurrencyService;
import service.impl.CurrencyServiceImpl;
import io.github.cdimascio.dotenv.Dotenv;

public class CurrencyBot extends TelegramLongPollingBot {

    private final Dotenv dotenv = Dotenv.load();  // Загружаем конфигурацию из .env
    private final String BOT_USERNAME = dotenv.get("BOT_USERNAME");
    private final String BOT_TOKEN = dotenv.get("BOT_TOKEN");
    private final CurrencyService currencyService = new CurrencyServiceImpl(); // Используем интерфейс и имплементацию

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleTextMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery().getData(), update.getCallbackQuery().getMessage());
        }
    }

    private void handleTextMessage(Message message) {
        String chatId = message.getChatId().toString();
        String text = message.getText();

        if (text.equals(BotCommands.START)) {
            sendStartMessage(chatId);
        } else if (text.equals("Введіть дату")) {
            sendMessage(chatId, "Введіть дату у форматі dd.MM.yyyy, щоб отримати курс валют за цю дату:");
        } else if (text.matches("\\d{2}\\.\\d{2}\\.\\d{4}")) {
            String historicalRates = currencyService.getHistoricalRates(text);
            sendMessage(chatId, historicalRates);
        } else {
            sendMessage(chatId, "Помилка: Введіть правильну дату у форматі dd.MM.yyyy.");
        }
    }

    private void handleCallbackQuery(String callbackData, Message message) {
        if (callbackData.equals(BotCommands.GET_RATES)) {
            String rates = currencyService.getCurrencyRates();
            sendMessage(message.getChatId().toString(), rates);
        } else if (callbackData.equals(BotCommands.GET_HISTORICAL_RATES)) {
            sendMessage(message.getChatId().toString(), "Введіть дату у форматі dd.MM.yyyy, щоб отримати курс валют за цю дату:");
        } else if (callbackData.equals(BotCommands.START)) {
            sendStartMessage(message.getChatId().toString());
        }
    }

    private void sendStartMessage(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Привіт! Я допоможу дізнатися курси валют. Натисніть кнопку нижче для отримання поточного курсу або для отримання курсу за певну дату.");

        InlineKeyboardMarkup keyboardMarkup = ButtonCreator.createMainMenu();
        message.setReplyMarkup(keyboardMarkup);

        sendResponse(message);
    }

    private void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        // кнопка для возврата в главное меню
        InlineKeyboardMarkup keyboardMarkup = ButtonCreator.createDateInputMenu();
        message.setReplyMarkup(keyboardMarkup);

        sendResponse(message);
    }

    private void sendResponse(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
