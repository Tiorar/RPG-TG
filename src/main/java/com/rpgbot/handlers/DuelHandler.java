package com.rpgbot.handlers;

import com.rpgbot.RpgBot;
import com.rpgbot.commands.Command;
import com.rpgbot.database.models.Character;
import com.rpgbot.database.models.DuelChallenge;
import com.rpgbot.database.repositories.CharacterRepository;
import com.rpgbot.database.repositories.DuelRepository;
import com.rpgbot.database.repositories.UserRepository;
import com.rpgbot.entities.Duel;
import com.rpgbot.util.TextFormatter;

import java.util.ArrayList;
import java.util.List;

public class DuelHandler implements Command {
    private final RpgBot bot;
    private final long chatId;
    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;
    private final DuelRepository duelRepository;
    private String[] args;
    private int messageId;

    public DuelHandler(RpgBot bot, long chatId,
                       UserRepository userRepository,
                       CharacterRepository characterRepository,
                       DuelRepository duelRepository) {
        this.bot = bot;
        this.chatId = chatId;
        this.userRepository = userRepository;
        this.characterRepository = characterRepository;
        this.duelRepository = duelRepository;
    }

    @Override
    public void setArgs(String[] args) {
        this.args = args;
    }

    @Override
    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    @Override
    public void execute() {
        if (args == null || args.length == 0) {
            showDuelMenu();
            return;
        }

        switch (args[0]) {
            case "challenge": handleChallenge(); break;
            case "accept": acceptChallenge(); break;
            case "reject": rejectChallenge(); break;
            case "list": showChallenges(); break;
            case "action": handleDuelAction(); break;
            default: showDuelMenu();
        }
    }

    private void showDuelMenu() {
        String message = "⚔️ Дуэльный зал гильдии\n\n" +
                "Здесь вы можете бросить вызов другим игрокам";

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text("🎯 Бросить вызов")
                .callbackData("/duel challenge")
                .build());
        row1.add(InlineKeyboardButton.builder()
                .text("📜 Мои вызовы")
                .callbackData("/duel list")
                .build());

        keyboard.add(row1);
        bot.sendMessageWithKeyboard(chatId, message, bot.createKeyboard(keyboard));
    }

    private void handleChallenge() {
        if (args.length == 1) {
            bot.sendMessage(chatId,
                    "Введите имя соперника и ставку через пробел\n" +
                            "Пример: Герой 50");
            return;
        }

        if (args.length < 3) {
            bot.sendMessage(chatId, "Неверный формат команды");
            return;
        }

        String opponentName = args[1];
        int betAmount = Integer.parseInt(args[2]);

        Character challenger = getCurrentCharacter();
        Character opponent = characterRepository.findByName(opponentName)
                .orElse(null);

        if (opponent == null) {
            bot.sendMessage(chatId, "Персонаж не найден");
            return;
        }

        if (challenger.getGold() < betAmount) {
            bot.sendMessage(chatId, "Недостаточно золота");
            return;
        }

        DuelChallenge challenge = new DuelChallenge();
        challenge.setChallenger(challenger);
        challenge.setOpponent(opponent);
        challenge.setBetAmount(betAmount);
        duelRepository.save(challenge);

        notifyPlayersAboutChallenge(challenge);
    }

    private void notifyPlayersAboutChallenge(DuelChallenge challenge) {
        String challengerMsg = String.format(
                "Вызов отправлен %s на %d золота",
                challenge.getOpponent().getName(),
                challenge.getBetAmount()
        );
        bot.sendMessage(chatId, challengerMsg);

        String opponentMsg = String.format(
                "%s вызывает вас на дуэль!\nСтавка: %d золота\n" +
                        "Принять: /duel accept %d\nОтклонить: /duel reject %d",
                challenge.getChallenger().getName(),
                challenge.getBetAmount(),
                challenge.getId(),
                challenge.getId()
        );
        bot.sendMessage(
                userRepository.findByCharacter(challenge.getOpponent()).get().getChatId(),
                opponentMsg
        );
    }

    private void acceptChallenge() {
        if (args.length < 2) return;

        long challengeId = Long.parseLong(args[1]);
        DuelChallenge challenge = duelRepository.findById(challengeId).orElse(null);
        Character acceptor = getCurrentCharacter();

        if (challenge == null || !challenge.getOpponent().equals(acceptor)) {
            bot.sendMessage(chatId, "Нельзя принять этот вызов");
            return;
        }

        if (acceptor.getGold() < challenge.getBetAmount()) {
            bot.sendMessage(chatId, "Недостаточно золота для принятия вызова");
            return;
        }

        startDuel(challenge);
    }

    private void startDuel(DuelChallenge challenge) {
        Duel duel = new Duel(
                challenge.getChallenger(),
                challenge.getOpponent(),
                challenge.getBetAmount()
        );

        duelRepository.save(duel);
        duelRepository.delete(challenge);

        notifyDuelStart(duel);
        sendNextTurn(duel);
    }

    private void notifyDuelStart(Duel duel) {
        String message = String.format(
                "⚔️ Дуэль началась!\n%s vs %s\nСтавка: %d золота",
                duel.getChallenger().getName(),
                duel.getOpponent().getName(),
                duel.getBetAmount()
        );

        sendToBothPlayers(duel, message);
    }

    private void sendNextTurn(Duel duel) {
        Character activePlayer = duel.isChallengerTurn() ?
                duel.getChallenger() : duel.getOpponent();

        String message = buildDuelStatusMessage(duel, activePlayer);
        InlineKeyboardMarkup keyboard = createDuelKeyboard(duel);

        bot.sendMessage(
                userRepository.findByCharacter(activePlayer).get().getChatId(),
                message,
                keyboard
        );
    }

    private String buildDuelStatusMessage(Duel duel, Character activePlayer) {
        Character opponent = duel.isChallengerTurn() ?
                duel.getOpponent() : duel.getChallenger();

        return String.format(
                "Ваш ход в дуэли против %s\n\n" +
                        "❤️ Ваше здоровье: %d/%d\n" +
                        "❤️ Здоровье соперника: %d/%d",
                opponent.getName(),
                activePlayer.getCurrentHealth(),
                activePlayer.getMaxHealth(),
                opponent.getCurrentHealth(),
                opponent.getMaxHealth()
        );
    }

    private InlineKeyboardMarkup createDuelKeyboard(Duel duel) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text("⚔️ Атака")
                .callbackData("/duel action attack")
                .build());

        keyboard.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder()
                .text("⏹ Завершить ход")
                .callbackData("/duel action end")
                .build());

        keyboard.add(row2);
        return bot.createKeyboard(keyboard);
    }

    private void handleDuelAction() {
        if (args.length < 2) return;

        Character character = getCurrentCharacter();
        Duel duel = duelRepository.findActiveDuel(character).orElse(null);

        if (duel == null) {
            bot.sendMessage(chatId, "Активная дуэль не найдена");
            return;
        }

        switch (args[1]) {
            case "attack":
                duel.performAttack();
                duelRepository.save(duel);
                break;
            case "end":
                duel.endTurn();
                duelRepository.save(duel);
                break;
        }

        checkDuelEnd(duel);
        sendNextTurn(duel);
    }

    private void checkDuelEnd(Duel duel) {
        if (!duel.isCompleted()) return;

        Character winner = duel.getWinner();
        Character loser = duel.getChallenger().equals(winner) ?
                duel.getOpponent() : duel.getChallenger();

        winner.setGold(winner.getGold() + duel.getBetAmount() * 2);
        characterRepository.save(winner);
        characterRepository.save(loser);

        String resultMessage = String.format(
                "🏆 Дуэль завершена! Победитель: %s\n" +
                        "Выигрыш: %d золота",
                winner.getName(),
                duel.getBetAmount() * 2
        );

        sendToBothPlayers(duel, resultMessage);
        duelRepository.delete(duel);
    }

    private void sendToBothPlayers(Duel duel, String message) {
        userRepository.findByCharacter(duel.getChallenger()).ifPresent(user ->
                bot.sendMessage(user.getChatId(), message));
        userRepository.findByCharacter(duel.getOpponent()).ifPresent(user ->
                bot.sendMessage(user.getChatId(), message));
    }

    private Character getCurrentCharacter() {
        return characterRepository.findByUser(
                userRepository.findByChatId(chatId).orElseThrow()
        ).orElseThrow();
    }

    private void rejectChallenge() {
        if (args.length < 2) return;

        long challengeId = Long.parseLong(args[1]);
        DuelChallenge challenge = duelRepository.findById(challengeId).orElse(null);

        if (challenge != null && challenge.getOpponent().equals(getCurrentCharacter())) {
            duelRepository.delete(challenge);
            bot.sendMessage(chatId, "Вы отклонили вызов");

            String rejectMsg = String.format(
                    "%s отклонил ваш вызов",
                    challenge.getOpponent().getName()
            );
            bot.sendMessage(
                    userRepository.findByCharacter(challenge.getChallenger()).get().getChatId(),
                    rejectMsg
            );
        }
    }

    private void showChallenges() {
        Character character = getCurrentCharacter();
        List<DuelChallenge> challenges = duelRepository.findByCharacter(character);

        if (challenges.isEmpty()) {
            bot.sendMessage(chatId, "У вас нет активных вызовов");
            return;
        }

        StringBuilder message = new StringBuilder("Ваши вызовы:\n");
        for (DuelChallenge challenge : challenges) {
            message.append(String.format(
                    "ID: %d | %s -> %s | %d золота\n",
                    challenge.getId(),
                    challenge.getChallenger().getName(),
                    challenge.getOpponent().getName(),
                    challenge.getBetAmount()
            ));
        }

        bot.sendMessage(chatId, message.toString());
    }
}