package droidbattle.droids;

import droidbattle.util.ConsoleColors;
import java.util.List;
import java.util.Scanner;

public class SniperDroid extends Droid {

    public SniperDroid(String name) {
        super(name, 100, "Sniper", 50, 15.0, 150.0,
                10.0, 1, 3, 2, 99);
        // s3CD = 99 (не використовується)
    }


    public void PerformTurn(List<Droid> allies, List<Droid> enemies, Scanner scanner, StringBuilder log) {
        System.out.println(ConsoleColors.ANSI_PURPLE + "--- Хід снайпера " + this.name + " ---" + ConsoleColors.ANSI_RESET);

        // Показання меню дій з поточними кулдаунами
        System.out.println("Оберіть дію:");
        System.out.printf("1. Звичайний постріл (Перезарядка: %d/%d)\n", this.currentAttackCooldown, this.attackCooldown);
        System.out.printf("2. Прицілювання (S1) (Перезарядка: %d/%d)\n", this.currentSkill1Cooldown, this.skill1Cooldown);
        System.out.printf("3. Подвійний постріл (S2) (Перезарядка: %d/%d)\n", this.currentSkill2Cooldown, this.skill2Cooldown);
        System.out.println("0. Пропустити хід (зменшити час відновлення)");

        int choice = -1;
        try {
            choice = scanner.nextInt();
            scanner.nextLine();
        } catch (Exception e) {
            scanner.nextLine();
            choice = -1;
        }

        switch (choice) {
            case 1:
                // --- Звичайна атака ---
                if (currentAttackCooldown > 0) {
                    System.out.println("Зброя ще перезаряджається!");
                    PerformTurn(allies, enemies, scanner, log); // Ще одна спроба
                    return;
                }
                Droid target = ChooseTarget(enemies, scanner); // Запит, в кого стріляти
                if (target != null) {
                    BasicAttack(target, log);
                }
                break;

            case 2:
                // Навичка 1: Прицілювання
                if (currentSkill1Cooldown > 0) {
                    System.out.println("Навичка 1 ще перезаряджається!");
                    PerformTurn(allies, enemies, scanner, log); // Ще раз
                    return;
                }
                UseSkill1(log);
                break;

            case 3:
                // Навичка 2: Подвійний постріл
                if (currentSkill2Cooldown > 0) {
                    System.out.println("Навичка 2 ще перезаряджається!");
                    PerformTurn(allies, enemies, scanner, log); // Ще раз
                    return;
                }
                UseSkill2(enemies, log);
                break;

            case 0:
                System.out.println(this.name + " пропускає хід.");
                log.append(this.name + " пропускає хід.\n");
                break;

            default:
                System.out.println(ConsoleColors.ANSI_RED + "Неправильний вибір." + ConsoleColors.ANSI_RESET);
                PerformTurn(allies, enemies, scanner, log); // Ще раз
                return;
        }
    }

    // Логіка базової атаки
    private void BasicAttack(Droid target, StringBuilder log) {
        // Встановлення час відновлення одразу
        this.currentAttackCooldown = this.attackCooldown;

        System.out.println(this.name + " стріляє в " + target.getName() + "...");

        // Перевірка на промах
        // 1. Обрання ефективного шансу промаху
        double missChance = getEffectiveMissChance();
        // 2. Додаємо пасивку (якщо ціль - Снайпер, наш промах росте)
        // (Ти написав "ворога збільшується шанс промаху", це можна трактувати
        // і як "ворог частіше маже", і як "по ньому частіше мажуть".
        // Я реалізую "по ньому частіше мажуть", це пасивка снайпера-цілі.)
        if (target instanceof SniperDroid) {
            missChance += 10.0; // Бонусна пасивна навичка цілі (умовно 10%)
        }

        if (random.nextDouble() * 100 < missChance) {
            String logEntry = String.format("%s %s %s%s по %s!",
                    this.name, ConsoleColors.ANSI_YELLOW, ConsoleColors.ANSI_RESET, "ПРОМАХНУВСЯ", target.getName());
            System.out.println(logEntry);
            log.append(logEntry).append("\n");
            return; // Атака закінчена
        }

        //  Перевірка на критичний удар
        int finalDamage = this.baseDamage; // Починання з базової шкоди
        if (random.nextDouble() * 100 < getEffectiveCritChance()) {
            // Критичний удар
            finalDamage = (int) (finalDamage * (getEffectiveCritDamage() / 100.0));
            target.TakeDamage(finalDamage);

            String logEntry = String.format("%s%s %s%s завдає %s%s по %s на %s%d%s шкоди! (Залишилось %d HP)",
                    ConsoleColors.ANSI_CYAN, this.name, ConsoleColors.ANSI_RESET,
                    ConsoleColors.ANSI_RED, ConsoleColors.ANSI_RESET, "КРИТИЧНОГО УДАРУ",  target.getName(),
                    ConsoleColors.ANSI_RED, finalDamage, ConsoleColors.ANSI_RESET, target.getHealth());
            System.out.println(logEntry);
            log.append(logEntry).append("\n");

        } else {
            // Звичайна атака
            target.TakeDamage(finalDamage);
            String logEntry = String.format("%s%s %s%s завдає %s%s %s по %s на %d%s шкоди! (Залишилось %d HP)",
                    ConsoleColors.ANSI_CYAN, this.name, ConsoleColors.ANSI_RESET,
                    ConsoleColors.ANSI_RED, "КРИТИЧНОГО УДАРУ", ConsoleColors.ANSI_RESET,
                    target.getName(), ConsoleColors.ANSI_RED, finalDamage, ConsoleColors.ANSI_RESET,
                    target.getHealth());
            System.out.println(logEntry);
            log.append(logEntry).append("\n");
        }
    }

    // Логіка навички 1 (Баф на себе)
    private void UseSkill1(StringBuilder log) {
        this.currentSkill1Cooldown = this.skill1Cooldown; // Вмикаємо час відновлення

        // Накладання ефекту. Тривалість 3 раунди.
        this.ApplyEffect(Effect.SNIPER_AIM, 3);

        String logEntry = String.format("%s%s %s%s вмикає %sПРИЦІЛЮВАННЯ! (Точність і крит збільшені на 3 ходи)",
                ConsoleColors.ANSI_CYAN, this.name, ConsoleColors.ANSI_RESET,
                ConsoleColors.ANSI_YELLOW, ConsoleColors.ANSI_RESET);
        System.out.println(logEntry);
        log.append(logEntry).append("\n");
    }

    // Логіка навика 2 (2 постріли + стан)
    private void UseSkill2(List<Droid> enemies, StringBuilder log) {
        this.currentSkill2Cooldown = this.skill2Cooldown; // Вмикаємо КД

        String logEntry = String.format("%s%s %s%s використовує %sПОДВІЙНИЙ ПОСТРІЛ!",
                ConsoleColors.ANSI_CYAN, this.name, ConsoleColors.ANSI_RESET,
                ConsoleColors.ANSI_YELLOW, ConsoleColors.ANSI_RESET);
        System.out.println(logEntry);
        log.append(logEntry).append("\n");

        // Постріл 1
        Droid target1 = ChooseRandomTarget(enemies);
        if (target1 != null) {
            AttackWithStun(target1, log);
        }

        // Постріл 2
        Droid target2 = ChooseRandomTarget(enemies);
        if (target2 != null) {
            AttackWithStun(target2, log);
        }
    }

    // Допоміжний метод для навика 2
    private void AttackWithStun(Droid target, StringBuilder log) {
        // "Точний вистріл" - 100% влучання, але базова шкода (без критичного удару)
        int damage = this.baseDamage;
        target.TakeDamage(damage);

        String logEntry = String.format("...%sТочний постріл%s по %s на %d шкоди! (Залишилось %d HP)",
                ConsoleColors.ANSI_YELLOW, ConsoleColors.ANSI_RESET,
                target.getName(), damage, target.getHealth());
        System.out.println(logEntry);
        log.append(logEntry).append("\n");

        // Шанс 10% на стан
        if (random.nextDouble() * 100 < 10.0) {
            // Накладання ефекту на 2 (1 хід ворога + 1 хід наш = 1 раунд)
            target.ApplyEffect(Effect.STUNNED, 2);
            String stunLog = String.format("%s%s%s %s%s%s на 1 раунд!",
                    ConsoleColors.ANSI_RED, target.getName(), ConsoleColors.ANSI_RESET,
                    ConsoleColors.ANSI_RED,"ПРИГОЛОМШЕНИЙ", ConsoleColors.ANSI_RESET);
            System.out.println(stunLog);
            log.append(stunLog).append("\n");
        }
    }
}
