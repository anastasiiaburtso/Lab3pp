package droidbattle.droids;

import droidbattle.util.ConsoleColors;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class WarriorDroid extends Droid {

    public WarriorDroid(String name) {
        // Час відновлення атаки 0, S1 (Камуфляж) 2, S2 (Ракета) 1
        super(name, 200, "Warrior", 20, 10.0, 125.0,
                20.0, 0, 2, 1, 99);
    }

    public void PerformTurn(List<Droid> allies, List<Droid> enemies, Scanner scanner, StringBuilder log) {
        System.out.println(ConsoleColors.ANSI_PURPLE + "--- Хід воїна " + this.name + " ---" + ConsoleColors.ANSI_RESET);

        // Перевірка, чи можна взагалі атакувати (через S2)
        if (hasEffect(Effect.ROCKET_COOLDOWN)) {
            System.out.println(ConsoleColors.ANSI_YELLOW + this.name + " перезаряджає ракетну установку і не може атакувати." + ConsoleColors.ANSI_RESET);
        }

        System.out.println("Оберіть дію:");
        // Пасивний навик "x2" відображається в описі
        System.out.printf("1. Подвійна атака (Перезарядка: %d/%d) %s\n",
                this.currentAttackCooldown, this.attackCooldown,
                (hasEffect(Effect.ROCKET_COOLDOWN) ? "(НЕДОСТУПНО)" : ""));

        System.out.printf("2. Камуфляж (S1) (Перезарядка: %d/%d)\n", this.currentSkill1Cooldown, this.skill1Cooldown);
        System.out.printf("3. Ракета (S2) (Перезарядка: %d/%d)\n", this.currentSkill2Cooldown, this.skill2Cooldown);
        System.out.println("0. Пропустити хід");

        int choice = -1;
        try { choice = scanner.nextInt(); scanner.nextLine(); }
        catch (Exception e) { scanner.nextLine(); choice = -1; }

        switch (choice) {
            case 1:
                if (currentAttackCooldown > 0) {
                    System.out.println("Атака ще перезаряджається!");
                    PerformTurn(allies, enemies, scanner, log);
                    return;
                }
                // Додаткова перевірка на дебаф від S2
                if (hasEffect(Effect.ROCKET_COOLDOWN)) {
                    System.out.println("Не можна атакувати після пострілу ракетою!");
                    PerformTurn(allies, enemies, scanner, log);
                    return;
                }
                Droid target = ChooseTarget(enemies, scanner);
                if (target != null) {
                    BasicAttack(target, log); // Викликання подвійної атаки
                }
                break;

            case 2:
                if (currentSkill1Cooldown > 0) {
                    System.out.println("Навичка 1 ще перезаряджається!");
                    PerformTurn(allies, enemies, scanner, log);
                    return;
                }
                UseSkill1(log);
                break;

            case 3:
                if (currentSkill2Cooldown > 0) {
                    System.out.println("Навичка 2 ще перезаряджається!");
                    PerformTurn(allies, enemies, scanner, log);
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
                PerformTurn(allies, enemies, scanner, log);
                return;
        }
    }

    // Пасивка (х2) реалізована тут
    private void BasicAttack(Droid target, StringBuilder log) {
        this.currentAttackCooldown = this.attackCooldown;

        log.append(this.name + " використовує подвійну атаку по " + target.getName() + "!\n");

        // --- Постріл 1 ---
        System.out.print("Постріл 1: ");
        log.append("Постріл 1: ");
        SingleShot(target, log); // Викликаємо допоміжний метод

        // --- Постріл 2 ---
        // Перевіряємо, чи ціль ще жива
        if (target.isAlive()) {
            System.out.print("Постріл 2: ");
            log.append("Постріл 2: ");
            SingleShot(target, log);
        } else {
            log.append("Ціль " + target.getName() + " знищена, другий постріл скасовано.\n");
        }
    }

    // Логіка для одного пострілу
    private void SingleShot(Droid target, StringBuilder log) {
        // Перевірка на промах (у воїна 20% базовий)
        // Пасивний навик снайпера-цілі тут теж можна врахувати
        double missChance = getEffectiveMissChance();
        if (target instanceof SniperDroid) {
            missChance += 10.0; // Пасивний навик снайпера-цілі
        }

        if (random.nextDouble() * 100 < missChance) {
            String logEntry = String.format("%sПРОМАХ!%s\n", ConsoleColors.ANSI_YELLOW, ConsoleColors.ANSI_RESET);
            System.out.print(logEntry);
            log.append(logEntry);
            return;
        }

        // Перевірка на критичний удар (у Воїна 10% базовий)
        int finalDamage = this.baseDamage;
        if (random.nextDouble() * 100 < getEffectiveCritChance()) {
            // Крит!
            finalDamage = (int) (finalDamage * (getEffectiveCritDamage() / 100.0));
            target.TakeDamage(finalDamage);

            String logEntry = String.format("%s%s%s на %s%d%s шкоди! (Залишилось %d HP)\n",
                    ConsoleColors.ANSI_RED, "КРИТ", ConsoleColors.ANSI_RESET,
                    ConsoleColors.ANSI_RED, finalDamage, ConsoleColors.ANSI_RESET, target.getHealth());
            System.out.print(logEntry);
            log.append(logEntry);

        } else {
            // Звичайна атака
            target.TakeDamage(finalDamage);
            String logEntry = String.format("Влучання на %s%d%s шкоди. (Залишилось %d HP)\n",
                    ConsoleColors.ANSI_RED, finalDamage, ConsoleColors.ANSI_RESET, target.getHealth());
            System.out.print(logEntry);
            log.append(logEntry);
        }
    }

    // Навик 1: Камуфляж
    private void UseSkill1(StringBuilder log) {
        this.currentSkill1Cooldown = this.skill1Cooldown;
        // "діє 1 раунд" -> накладаємо на 2
        this.ApplyEffect(Effect.CAMOUFLAGE, 2);

        String logEntry = String.format("%s%s %s%s вмикає %sКАМУФЛЯЖ! (Шкода по ньому зменшена на 1 раунд)",
                ConsoleColors.ANSI_CYAN, this.name, ConsoleColors.ANSI_RESET,
                ConsoleColors.ANSI_YELLOW, ConsoleColors.ANSI_RESET);
        System.out.println(logEntry);
        log.append(logEntry).append("\n");
    }

    // Навик 2: Ракета
    private void UseSkill2(List<Droid> enemies, StringBuilder log) {
        this.currentSkill2Cooldown = this.skill2Cooldown;
        // Накладаємо на "себе" дебаф "не можна атакувати" на 1 раунд (2 тіки)
        this.ApplyEffect(Effect.ROCKET_COOLDOWN, 2);

        String logEntry = String.format("%s%s %s%s запускає %sРАКЕТУ!",
                ConsoleColors.ANSI_CYAN, this.name, ConsoleColors.ANSI_RESET,
                ConsoleColors.ANSI_YELLOW, ConsoleColors.ANSI_RESET);
        System.out.println(logEntry);
        log.append(logEntry).append("\n");

        // Знаходження живих ворогів
        List<Droid> aliveEnemies = enemies.stream()
                .filter(Droid::isAlive)
                .collect(Collectors.toList());

        // Перемішення знайдених живих ворогів, щоб вдарити 3 випадкових
        Collections.shuffle(aliveEnemies);

        int targetsHit = 0;
        for (Droid target : aliveEnemies) {
            if (targetsHit >= 3) {
                break; // Удар у трьох
            }

            target.TakeDamage(35); // Шкода 35
            String hitLog = String.format("...Ракета влучає в %s на %d шкоди! (Залишилось %d HP)\n",
                    target.getName(), 35, target.getHealth());
            System.out.print(hitLog);
            log.append(hitLog);

            targetsHit++;
        }
    }
}