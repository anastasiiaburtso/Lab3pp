package droidbattle.droids;

import droidbattle.util.ConsoleColors;
import java.util.List;
import java.util.Scanner;

public class HealerDroid extends Droid {

    public HealerDroid(String name) {
        // Атака (Яд) CD 0, S1 (Хіл) CD 0, S2 (Мовчання) CD 2, S3 (Дебаф) CD 4
        super(name, 150, "Healer", 10, 0.0, 0.0,
                0.0, 0, 0, 2, 4);
    }

    // Пасивний навик 1: Зменшення шкоди ---
    public void TakeDamage(int damage) {
        // Рахування шкоди з 25% зменшенням
        int finalDamage = (int) (damage * 0.75);

        // Перевірка, чи є ще й Камуфляж (вони можуть складатись)
        if (hasEffect(Effect.CAMOUFLAGE)) {
            finalDamage *= 0.90;
        }

        // Викликання базового методу, але вже зі зменшеною шкодою
        this.health -= finalDamage;
        if (this.health < 0) {
            this.health = 0;
        }
    }


    public void PerformTurn(List<Droid> allies, List<Droid> enemies, Scanner scanner, StringBuilder log) {
        System.out.println(ConsoleColors.ANSI_PURPLE + "--- Хід цілителя " + this.name + " ---" + ConsoleColors.ANSI_RESET);

        // Перевірка на мовчання (Silence)
        if (hasEffect(Effect.SILENCED)) {
            System.out.println(ConsoleColors.ANSI_RED + this.name + " змушений мовчати і не може використовувати навички!" + ConsoleColors.ANSI_RESET);
        }

        System.out.println("Оберіть дію:");
        System.out.printf("1. Отруйна атака (Перезарядка: %d/%d)\n", this.currentAttackCooldown, this.attackCooldown);
        System.out.printf("2. Лікування (S1) (Перезарядка: %d/%d) %s\n",
                this.currentSkill1Cooldown, this.skill1Cooldown,
                (hasEffect(Effect.SILENCED) ? "(НЕДОСТУПНО)" : ""));

        System.out.printf("3. Скасування (S2) (Перезарядка: %d/%d) %s\n",
                this.currentSkill2Cooldown, this.skill2Cooldown,
                (hasEffect(Effect.SILENCED) ? "(НЕДОСТУПНО)" : ""));

        System.out.printf("4. Збій сенсорів (S3) (Перезарядка: %d/%d) %s\n",
                this.currentSkill3Cooldown, this.skill3Cooldown,
                (hasEffect(Effect.SILENCED) ? "(НЕДОСТУПНО)" : ""));

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
                Droid target = ChooseTarget(enemies, scanner);
                if (target != null) {
                    BasicAttack(target, log); // Атака ядом
                }
                break;

            case 2:
                if (hasEffect(Effect.SILENCED)) {
                    System.out.println("Неможливо використати навичку під ефектом мовчання!");
                    PerformTurn(allies, enemies, scanner, log);
                    return;
                }
                // Критичний удар не перевіряється, бо він 0
                UseSkill1(allies, log); // Лікування
                break;

            case 3:
                if (hasEffect(Effect.SILENCED)) {
                    System.out.println("Неможливо використати навичку під ефектом мовчання!");
                    PerformTurn(allies, enemies, scanner, log);
                    return;
                }
                if (currentSkill2Cooldown > 0) {
                    System.out.println("Навичка 2 ще перезаряджається!");
                    PerformTurn(allies, enemies, scanner, log);
                    return;
                }
                UseSkill2(enemies, log); // Мовчання
                break;

            case 4:
                if (hasEffect(Effect.SILENCED)) {
                    System.out.println("Неможливо використати навичку під ефектом мовчання!");
                    PerformTurn(allies, enemies, scanner, log);
                    return;
                }
                if (currentSkill3Cooldown > 0) {
                    System.out.println("Навичка 3 ще перезаряджається!");
                    PerformTurn(allies, enemies, scanner, log);
                    return;
                }
                UseSkill3(enemies, log); // Дебаф
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

    // Атака: Отрута (Пасивний навик: яд наносить урон...)
    private void BasicAttack(Droid target, StringBuilder log) {
        this.currentAttackCooldown = this.attackCooldown;

        // Перевірка, чи на цілі вже є отрута
        if (target.hasEffect(Effect.POISONED)) {
            String logEntry = String.format("%s%s %sвже отруєний! %sАтака не дала ефекту.%s",
                    ConsoleColors.ANSI_CYAN, target.getName(), ConsoleColors.ANSI_RESET,
                    ConsoleColors.ANSI_YELLOW, ConsoleColors.ANSI_RESET);
            System.out.println(logEntry);
            log.append(logEntry).append("\n");
        } else {
            // Накладання ефекту (10, 5, 0 -> 3 тіки)
            target.ApplyEffect(Effect.POISONED, 3);
            // Встановлення початкової шкоди отрути
            target.poisonDamage = 10;

            String logEntry = String.format("%s%s %sотруює %s%s%s! (Отримає 10, 5 шкоди)",
                    ConsoleColors.ANSI_CYAN, this.name, ConsoleColors.ANSI_RESET,
                    ConsoleColors.ANSI_CYAN, target.getName(), ConsoleColors.ANSI_RESET);
            System.out.println(logEntry);
            log.append(logEntry).append("\n");
        }
    }

    // Навик 1: Лікування
    private void UseSkill1(List<Droid> allies, StringBuilder log) {
        this.currentSkill1Cooldown = this.skill1Cooldown;

        // Шукання найбільшого пораненого союзника (але не себе)
        Droid healTarget = null;
        int minHealth = Integer.MAX_VALUE;

        for (Droid ally : allies) {
            // Шукання живого союзника, який НЕ є ним, і який поранений
            if (ally.isAlive() && ally != this && ally.getHealth() < ally.getMaxHealth()) {
                if (ally.getHealth() < minHealth) {
                    minHealth = ally.getHealth();
                    healTarget = ally;
                }
            }
        }

        if (healTarget != null) {
            healTarget.Heal(25);
            String logEntry = String.format("%s%s %s%s%s %s%s%s на %s25 %sHP%s! (Стало %d/%d HP)",
                    ConsoleColors.ANSI_CYAN, this.name, ConsoleColors.ANSI_RESET,
                    ConsoleColors.ANSI_GREEN,"ЛІКУЄ", ConsoleColors.ANSI_RESET,
                    ConsoleColors.ANSI_CYAN, healTarget.getName(), ConsoleColors.ANSI_RESET,
                    ConsoleColors.ANSI_GREEN, ConsoleColors.ANSI_RESET,
                    healTarget.getHealth(), healTarget.getMaxHealth());
            System.out.println(logEntry);
            log.append(logEntry).append("\n");
        } else {
            String logEntry = String.format("%s%s %s хотів полікувати, але %s всі союзники здорові%s (або він один).",
                    ConsoleColors.ANSI_CYAN, this.name, ConsoleColors.ANSI_RESET,
                    ConsoleColors.ANSI_YELLOW, ConsoleColors.ANSI_RESET);
            System.out.println(logEntry);
            log.append(logEntry).append("\n");
        }
    }

    // Навик 2: Скасування (мовчання)
    private void UseSkill2(List<Droid> enemies, StringBuilder log) {
        this.currentSkill2Cooldown = this.skill2Cooldown;

        Droid target = ChooseRandomTarget(enemies);
        if (target != null) {
            // "скасовує ... скілів" -> накладання "мовчання" на 1 раунд
            target.ApplyEffect(Effect.SILENCED, 2);

            String logEntry = String.format("%s%s %sнакладає %s%s%s на %s%s%s на 1 раунд!",
                    ConsoleColors.ANSI_CYAN, this.name, ConsoleColors.ANSI_RESET, "МОВЧАННЯ",
                    ConsoleColors.ANSI_RED, ConsoleColors.ANSI_RESET,
                    ConsoleColors.ANSI_CYAN, target.getName(), ConsoleColors.ANSI_RESET);
            System.out.println(logEntry);
            log.append(logEntry).append("\n");
        }
    }

    // Навик 3: Збій сенсорів (Дебаф на промах)
    private void UseSkill3(List<Droid> enemies, StringBuilder log) {
        this.currentSkill3Cooldown = this.skill3Cooldown;

        Droid target = ChooseRandomTarget(enemies);
        if (target != null) {
            // "збільшує шанс на промах на 40%" -> на 1 раунд
            target.ApplyEffect(Effect.ACCURACY_DEBUFF, 2);

            String logEntry = String.format("%s%s %sвикликає %s%s%s у %s%s%s! (Шанс промаху +40%% на 1 раунд)",
                    ConsoleColors.ANSI_CYAN, this.name, ConsoleColors.ANSI_RESET,
                    ConsoleColors.ANSI_RED, "ЗБІЙ СЕНСОРІВ", ConsoleColors.ANSI_RESET,
                    ConsoleColors.ANSI_CYAN, target.getName(), ConsoleColors.ANSI_RESET);
            System.out.println(logEntry);
            log.append(logEntry).append("\n");
        }
    }
}