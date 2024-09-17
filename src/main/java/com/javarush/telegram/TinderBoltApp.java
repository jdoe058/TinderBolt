package com.javarush.telegram;

import com.javarush.telegram.ChatGPTService;
import com.javarush.telegram.DialogMode;
import com.javarush.telegram.MultiSessionTelegramBot;
import com.javarush.telegram.UserInfo;
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
        }

        switch (currentMode) {
            case GPT:
                String prompt = loadPrompt("gpt");
                String answer = chatGPT.sendMessage(prompt, message);
                sendTextMessage(answer);
                return;
            default:
                sendTextMessage("*Привет!*");
                sendTextMessage("_Привет!_");
                sendTextMessage("Вы написали " + message);
                sendTextButtonsMessage("Выберете режим работы:", "Старт", "start", "Стоп", "stop");
        }



        /*
        if (message.equals("/start")) {
            sendPhotoMessage("main");
            String text = loadMessage("main");
            sendTextMessage(text);
            return;
        }

        if (message.equals("/gpt")) {
            sendPhotoMessage("gpt");
            sendTextMessage("Напишите ваше сообщение *chatGPT*:");
            return;

        }

        sendTextMessage("*Привет!*");
        sendTextMessage("_Привет!_");
        sendTextMessage("Вы написали " + message);
        sendTextButtonsMessage("Выберете режим работы:", "Старт", "start", "Стоп", "stop");

         */
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
