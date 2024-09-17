package com.javarush.telegram;

import com.javarush.telegram.ChatGPTService;
import com.javarush.telegram.DialogMode;
import com.javarush.telegram.MultiSessionTelegramBot;
import com.javarush.telegram.UserInfo;
import com.plexpt.chatgpt.ChatGPT;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = System.getenv("TINDERBOLT_BOT_NAME");
    public static final String TELEGRAM_BOT_TOKEN = System.getenv("TINDERBOLT_BOT_TOKEN");
    public static final String OPEN_AI_TOKEN = System.getenv("TINDERBOLT_OPEN_AI_TOKEN");

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    final private ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode currentMode = null;
    final private ArrayList<String> list = new ArrayList<>();

    @Override
    public void onUpdateEventReceived(Update update) {
        //TODO: основной функционал бота будем писать здесь
        String message = getMessageText();
        switch (message) {
            case "/start":
                currentMode = DialogMode.MAIN;
                sendPhotoMessage("main");
                sendTextMessage(loadMessage("main"));

                showMainMenu("главное меню бота", "/start",
                        "генерация Tinder-профля \uD83D\uDE0E", "/profile",
                        "сообщение для знакомства \uD83E\uDD70", "/opener",
                        "переписка от вашего имени \uD83D\uDE08", "/message",
                        "переписка со звездами \uD83D\uDD25", "/date",
                        "задать вопрос чату GPT \uD83E\uDDE0", "/gpt");
                return;
            case "/gpt":
                currentMode = DialogMode.GPT;
                sendPhotoMessage("gpt");
                sendTextMessage(loadMessage("gpt"));
                return;
            case "/date":
                currentMode = DialogMode.DATE;
                sendPhotoMessage("date");
                sendTextButtonsMessage(loadMessage("date"),
                        "Ариана Гранде", "date_grande",
                        "Марго Робби", "date_robbie",
                        "Зендея", "date_zendaya",
                        "Райн Гослинг", "date_gosling",
                        "Том Харди", "date_hardy");
                return;
            case "/message":
                currentMode = DialogMode.MESSAGE;
                sendPhotoMessage("message");
                sendTextButtonsMessage(loadMessage("message"),
                        "Следующее сообщение", "message_next",
                        "Пригласить на свидание", "message_date");
                return;
        }

        String query;
        Message msg;

        switch (currentMode) {
            case GPT:
                msg = sendTextMessage("Подождите пару секунд - ChatGPT думает...");
                updateTextMessage(msg, chatGPT.sendMessage(loadPrompt("gpt"), message));
                break;
            case DATE:
                query = getCallbackQueryButtonKey();
                if (query.startsWith("date_")) {
                    sendPhotoMessage(query);
                    sendTextMessage("Отличный выбор!\n Твоя задача пригласить девушку/парня на свидание за ❤\uFE0F 5 сообщений.");
                    chatGPT.setPrompt(loadPrompt(query));
                } else {
                    msg = sendTextMessage("Подождите, девушка набирает текст...");
                    updateTextMessage(msg, chatGPT.addMessage(message));
                }
                break;
            case MESSAGE:
                query = getCallbackQueryButtonKey();
                if (query.startsWith("message")) {
                    msg = sendTextMessage("Подождите пару секунд - ChatGPT думает...");
                    updateTextMessage(msg, chatGPT.sendMessage(loadPrompt(query), String.join("\n\n", list)));
                } else {
                    list.add(message);
                }
                break;
            default:
                sendTextMessage("*Упс...* что-то пошло не так");
        }
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
