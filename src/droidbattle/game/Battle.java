package droidbattle.game;

import droidbattle.droids.Droid;
import droidbattle.droids.HealerDroid;
import droidbattle.util.ConsoleColors;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Battle {

    private StringBuilder battleLog;
    private Scanner scanner; // Битві потрібен Scanner для вибору дій

    // Конструктор приймає Scanner з Main
    public Battle(Scanner scanner) {
        this.scanner = scanner;
        this.battleLog = new StringBuilder();
    }

    // Метод для очищення логу
    private void startNewLog() {
        this.battleLog = new StringBuilder();
        battleLog.append("--- Бій розпочато! ---\n");
    }

    // НОВИЙ ГОЛОВНИЙ МЕТОД БОЮ. Він обробляє і 1v1, і Команда-на-Команду.
    public String startBattle(List<Droid> team1, List<Droid> team2) {
        startNewLog();

        // 1а. Скидання стану всіх дроїдів (це відновлює HP після минулих боїв)
        team1.forEach(Droid::Reset);
        team2.forEach(Droid::Reset);

        // 1б. Застосування пасивних навиків цілителя (+10% HP)
        applyHealerAura(team1);
        applyHealerAura(team2);

        System.out.println(ConsoleColors.ANSI_PURPLE + "--- БІЙ РОЗПОЧАТО! ---" + ConsoleColors.ANSI_RESET);
        printBattleState(team1, team2);

        int round = 1;

        // ГОЛОВНИЙ ЦИКЛ БОЮ
        while (isTeamAlive(team1) && isTeamAlive(team2)) {
            System.out.println(ConsoleColors.ANSI_YELLOW + "--- Раунд " + round + " ---" + ConsoleColors.ANSI_RESET);

            // Хід команди 1
            doTeamTurn(ConsoleColors.ANSI_BLUE + "Команда 1" + ConsoleColors.ANSI_RESET, team1, team2);

            if (!isTeamAlive(team2)) {
                break; // Команда 2 переможена
            }

            pause(1);

            // Хід команди 2
            doTeamTurn(ConsoleColors.ANSI_RED + "Команда 2" + ConsoleColors.ANSI_RESET, team2, team1);

            if (!isTeamAlive(team1)) {
                break; // Команда 1 переможена
            }

            printSeparator();
            System.out.println("Стан після " + round + " раунду:");
            printBattleState(team1, team2);
            pause(2);
            round++;
        }

        // ЗАВЕРШЕННЯ БОЮ
        printSeparator();
        String winnerMessage;
        if (isTeamAlive(team1)) {
            winnerMessage = "Перемогла КОМАНДА 1!";
        } else {
            winnerMessage = "Перемогла КОМАНДА 2!";
        }

        System.out.println(ConsoleColors.ANSI_GREEN + winnerMessage + ConsoleColors.ANSI_RESET);
        battleLog.append(winnerMessage).append("\n--- Бій завершено! ---\n");

        return battleLog.toString();
    }

    // Метод, що обробляє хід цілої команди
    private void doTeamTurn(String teamName, List<Droid> allies, List<Droid> enemies) {
        System.out.println(ConsoleColors.ANSI_PURPLE + "Хід: " + teamName + ConsoleColors.ANSI_RESET);
        battleLog.append("--- Хід ").append(teamName).append(" ---\n");

        // Кожен дроїд в команді ходить по черзі
        for (Droid droid : allies) {
            if (!droid.isAlive()) {
                continue; // Мертві не ходять
            }

            // 1. Перевірка всіх ефектів (отрута, кулдауни, бафи)
            droid.TickTurn(battleLog);

            // 2. Перевіряємо, чи дроїд не помер від отрути
            if (!droid.isAlive()) {
                System.out.println(ConsoleColors.ANSI_RED + droid.getName() + " загинув від отрути!" + ConsoleColors.ANSI_RESET);
                continue;
            }

            // 3. Перевіряємо на СТАН (пропуск ходу)
            if (droid.hasEffect(Droid.Effect.STUNNED)) {
                String stunLog = String.format("%s%s%s %sПРИГОЛОМШЕНИЙ%s і пропускає хід!",
                        ConsoleColors.ANSI_CYAN, droid.getName(), ConsoleColors.ANSI_RESET,
                        ConsoleColors.ANSI_RED, ConsoleColors.ANSI_RESET);
                System.out.println(stunLog);
                battleLog.append(stunLog).append("\n");
                continue; // Пропускання ходу
            }

            // 4. Показання стану перед ходом
            printBattleState(allies, enemies);

            // 5. Передання керування дроїду, щоб він показав меню і зробив хід
            droid.PerformTurn(allies, enemies, scanner, battleLog);

            pause(1);

            // 6. Перевірка, чи не закінчився бій після цього ходу
            if (!isTeamAlive(enemies)) {
                return; // Ворожа команда переможена
            }
        }
    }

    // Допоміжний метод для пасивного навику цілителя
    private void applyHealerAura(List<Droid> team) {
        // Перевірка, чи є в команді хоча б один цілитель
        boolean hasHealer = team.stream().anyMatch(d -> d instanceof HealerDroid);

        if (hasHealer) {
            System.out.println(ConsoleColors.ANSI_GREEN + "Аура цілителя: +10% HP для команди!" + ConsoleColors.ANSI_RESET);
            battleLog.append("Аура цілителя: +10% HP для команди!\n");
            for (Droid droid : team) {
                // Бафання всіх, крім самого цілителя
                if (!(droid instanceof HealerDroid)) {
                    droid.BuffMaxHealth(1.10); // +10%
                }
            }
        }
    }

    // Допоміжний метод перевірки, чи жива команда
    private boolean isTeamAlive(List<Droid> team) {
        return team.stream().anyMatch(Droid::isAlive);
    }

    // Друкування поточного стану обох команд
    private void printBattleState(List<Droid> team1, List<Droid> team2) {
        printSeparator();
        System.out.println(ConsoleColors.ANSI_BLUE + "КОМАНДА 1:" + ConsoleColors.ANSI_RESET);
        team1.forEach(d -> System.out.println(d.toString())); // .toString() покаже ефекти
        System.out.println(ConsoleColors.ANSI_RED + "КОМANDA 2:" + ConsoleColors.ANSI_RESET);
        team2.forEach(d -> System.out.println(d.toString()));
        printSeparator();
    }

    private void pause(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void printSeparator() {
        System.out.println(ConsoleColors.ANSI_PURPLE + "=========================================================" + ConsoleColors.ANSI_RESET);
    }
}