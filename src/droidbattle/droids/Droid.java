package droidbattle.droids;
import droidbattle.util.ConsoleColors;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors; // Для зручного вибору живих ворогів


public abstract class Droid {

    // Основні характеристики
    protected String name;
    protected int health;
    protected int maxHealth; // Важливо для лікування та скидання стану
    protected String droidType; // Тип (Warrior, Sniper...)

    // Бойові характеристики
    protected int baseDamage;
    protected double baseCritChance; // Шанс криту
    protected double baseCritDamage; // Множник криту
    protected double baseMissChance; // Шанс промаху

    //Система перезарядки + скільки раундів триває перезарядка
    protected int attackCooldown;
    protected int skill1Cooldown;
    protected int skill2Cooldown;
    protected int skill3Cooldown; // Додамо 3-й слот про всяк випадок (для Хілера)

    // Лічильники поточної перезарядки. (0 = готово)
    protected int currentAttackCooldown;
    protected int currentSkill1Cooldown;
    protected int currentSkill2Cooldown;
    protected int currentSkill3Cooldown;

    //Система ефектів, enum (перелік) для всіх можливих ефектів у грі
    public enum Effect {
        STUNNED,         // Оглушення (пропуск ходу)
        SNIPER_AIM,      // Баф снайпера (S1)
        CAMOUFLAGE,      // Баф воїна (S1)
        ROCKET_COOLDOWN, // Дебаф воїна (не може атакувати після ракети)
        POISONED,        // Отруєння (Healer)
        SILENCED,        // Мовчання (не можна використовувати скіли) (Healer S1)
        ACCURACY_DEBUFF  // Дебаф на промах (Healer S2)
    }

    // "Карта" активних ефектів на цьому дроїді
    // Ключ: Ефект (напр., STUNNED)
    // Значення: Тривалість (в раундах)
    protected Map<Effect, Integer> activeEffects;

    // Окреме поле для отрути (бо вона має унікальну логіку)
    protected int poisonDamage;

    // Генератор випадковості
    protected static Random random = new Random();

    // Конструктор
    public Droid(String name, int maxHealth, String droidType, int baseDamage,
                 double baseCritChance, double baseCritDamage, double baseMissChance,
                 int attackCooldown, int skill1Cooldown, int skill2Cooldown, int skill3Cooldown) {

        this.name = name;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.droidType = droidType;
        this.baseDamage = baseDamage;
        this.baseCritChance = baseCritChance;
        this.baseCritDamage = baseCritDamage;
        this.baseMissChance = baseMissChance;

        // Встановлюємо максимальні кулдауни
        this.attackCooldown = attackCooldown;
        this.skill1Cooldown = skill1Cooldown;
        this.skill2Cooldown = skill2Cooldown;
        this.skill3Cooldown = skill3Cooldown;

        // При створенні дроїд готовий до бою (всі кулдауни = 0)
        this.currentAttackCooldown = 0;
        this.currentSkill1Cooldown = 0;
        this.currentSkill2Cooldown = 0;
        this.currentSkill3Cooldown = 0;

        // Створення порожньої карти ефектів
        this.activeEffects = new HashMap<>();
        this.poisonDamage = 0;
    }

    //Гетери
    public String getName() { return name; }
    public int getHealth() { return health; }
    public boolean isAlive() { return health > 0; }
    public String getDroidType() { return droidType; }
    public int getMaxHealth() { return maxHealth; }
    public boolean hasEffect(Effect effect) {
        return activeEffects.containsKey(effect);
    }


    // Ці методи рахують фінальний шанс, враховуючи всі бафи/дебафи
    // Отримання шансу промаху
    public double getEffectiveMissChance() {
        double miss = this.baseMissChance;
        // Перевірка, чи є на дроїді баф снайпера (зменшує промах)
        if (hasEffect(Effect.SNIPER_AIM)) {
            miss /= 2.0;
        }
        // Перевірка, чи є на дроїді дебаф (збільшує промах)
        if (hasEffect(Effect.ACCURACY_DEBUFF)) {
            miss += 40.0;
        }
        return miss;
    }

    // Отримання шансу криту
    public double getEffectiveCritChance() {
        double crit = this.baseCritChance;
        // Баф снайпера
        if (hasEffect(Effect.SNIPER_AIM)) {
            crit += 5.0;
        }
        return crit;
    }

    // Отримання сили криту
    public double getEffectiveCritDamage() {
        double critDmg = this.baseCritDamage;
        // Баф снайпера
        if (hasEffect(Effect.SNIPER_AIM)) {
            critDmg += 50.0;
        }
        return critDmg;
    }


    // Отримання шкоди (тепер враховує пасиви і бафи)
    public void TakeDamage(int damage) {
        int finalDamage = damage;

        // Пасивний скіл цілителя (завжди -25% шкоди)
        if (this instanceof HealerDroid) {
            finalDamage *= 0.75; // 75% від вхідної шкоди
        }
        // Активний скіл воїна (поки діє баф -10% шкоди)
        if (hasEffect(Effect.CAMOUFLAGE)) {
            finalDamage *= 0.90; // 90% від вхідної шкоди
        }

        this.health -= finalDamage;
        if (this.health < 0) {
            this.health = 0;
        }
    }

    // Лікування
    public void Heal(int amount) {
        this.health += amount;
        if (this.health > this.maxHealth) {
            this.health = this.maxHealth;
        }
    }

    // Накладання ефекту
    public void ApplyEffect(Effect effect, int duration) {
        // Задання ефекту в карту, або оновлення тривалості, якщо він вже є
        activeEffects.put(effect, duration);
    }

    // Скидання стану дроїда ("відновлення після бою")
    public void Reset() {
        this.health = this.maxHealth;
        this.currentAttackCooldown = 0;
        this.currentSkill1Cooldown = 0;
        this.currentSkill2Cooldown = 0;
        this.currentSkill3Cooldown = 0;
        this.activeEffects.clear(); // Очищення всіх ефектів
        this.poisonDamage = 0;
        // maxHealth  не скидається, бо він має бути бафнутим на початку бою
    }

    // Баф здоров'я від пасивки цілителя
    public void BuffMaxHealth(double multiplier) {
        // Ми не хочемо, щоб цей баф складався нескінченно,
        // тому спершу скинемо (якщо треба), а потім бафнемо.
        // Але в нашій логіці reset() не чіпає maxHealth, тож все ок.
        this.maxHealth = (int) (this.maxHealth * multiplier);
        this.health = this.maxHealth; // Повністю вилікувати на початку
    }


    //Цей метод викликається на початку ходу дроїда. Він перевіряє всі таймери і ефекти.
    public void TickTurn(StringBuilder log) {
        // 1. Зменшуємо всі кулдауни, що > 0
        if (currentAttackCooldown > 0) currentAttackCooldown--;
        if (currentSkill1Cooldown > 0) currentSkill1Cooldown--;
        if (currentSkill2Cooldown > 0) currentSkill2Cooldown--;
        if (currentSkill3Cooldown > 0) currentSkill3Cooldown--;

        // 2. Обробляємо отруту
        if (poisonDamage > 0) {
            this.TakeDamage(poisonDamage); // Нанесення шкоди до обробки бафів
            log.append(String.format("%s%s%s отримує %s%d%s шкоди від отрути! (Залишилось %d HP)\n",
                    ConsoleColors.ANSI_CYAN, this.name, ConsoleColors.ANSI_RESET,
                    ConsoleColors.ANSI_GREEN, poisonDamage, ConsoleColors.ANSI_RESET,
                    this.health));
            this.poisonDamage -= 5; // Зменшення сили отрути
            if (this.poisonDamage <= 0) {
                this.poisonDamage = 0;
                // Видаляння самого ефекту, щоб можна було отруїти знову
                activeEffects.remove(Effect.POISONED);
                log.append(String.format("%sЯд%s на %s%s%s розсіявся.\n",
                        ConsoleColors.ANSI_GREEN, ConsoleColors.ANSI_RESET,
                        ConsoleColors.ANSI_CYAN, this.name, ConsoleColors.ANSI_RESET));
            }
        }

        // Перевірка всіх інших ефектів (зменшуапння тривалісті)
        // Використовуємо .entrySet() для безпечного видалення під час ітерації
        Map<Effect, Integer> updatedEffects = new HashMap<>();
        for (Map.Entry<Effect, Integer> entry : activeEffects.entrySet()) {
            int duration = entry.getValue() - 1; // Зменшення тривалості
            if (duration > 0) {
                updatedEffects.put(entry.getKey(), duration); // Оновлення, якщо ще діє
            } else {
                // Ефект закінчився
                log.append(String.format("Ефект %s%s%s на %s%s%s закінчився.\n",
                        ConsoleColors.ANSI_YELLOW, entry.getKey().name(), ConsoleColors.ANSI_RESET,
                        ConsoleColors.ANSI_CYAN, this.name, ConsoleColors.ANSI_RESET));
            }
        }
        this.activeEffects = updatedEffects; // Замінення старої карти - новою
    }


    // Кожен дроїд сам керує своїм ходом, показуючи меню дій і питаючи користувача, що робити.
    public abstract void PerformTurn(List<Droid> allies, List<Droid> enemies, Scanner scanner, StringBuilder log);

    // Допоміжний метод для вибору цілі користувачем.
    protected Droid ChooseTarget(List<Droid> enemies, Scanner scanner) {
        // Спершу фільтрування списоку, залишаючи тільки живих
        List<Droid> aliveEnemies = enemies.stream()
                .filter(Droid::isAlive)
                .collect(Collectors.toList());

        if (aliveEnemies.isEmpty()) {
            return null; // Немає кого атакувати
        }

        // Якщо живий ворог тільки один, обирається його автоматично
        if (aliveEnemies.size() == 1) {
            return aliveEnemies.get(0);
        }

        // Показання меню вибору
        System.out.println("Оберіть ціль:");
        for (int i = 0; i < aliveEnemies.size(); i++) {
            System.out.printf("%d. %s (%d/%d HP)\n", (i + 1), aliveEnemies.get(i).getName(), aliveEnemies.get(i).getHealth(), aliveEnemies.get(i).getMaxHealth());
        }

        int choice = -1;
        while (choice < 1 || choice > aliveEnemies.size()) {
            System.out.print(ConsoleColors.ANSI_YELLOW + "Введіть номер цілі: " + ConsoleColors.ANSI_RESET);
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); //
            } catch (Exception e) {
                scanner.nextLine(); // Очищення буферу
                System.out.println(ConsoleColors.ANSI_RED + "Невірний ввід." + ConsoleColors.ANSI_RESET);
            }
        }
        return aliveEnemies.get(choice - 1);
    }

    // Обирається випадкова жива ціль
    protected Droid ChooseRandomTarget(List<Droid> enemies) {
        List<Droid> aliveEnemies = enemies.stream()
                .filter(Droid::isAlive)
                .collect(Collectors.toList());
        if (aliveEnemies.isEmpty()) {
            return null;
        }
        return aliveEnemies.get(random.nextInt(aliveEnemies.size()));
    }

    // toString() для красивого виводу
    public String toString() {
        // Додавання показу ефектів
        String effects = activeEffects.keySet().stream()
                .map(Enum::name)
                .collect(Collectors.joining(", "));
        if (effects.isEmpty()) {
            effects = "немає";
        }

        return String.format("[%s] %s (HP: %d/%d, Dmg: %d, Ефекти: %s)",
                this.droidType,
                this.name,
                this.health,
                this.maxHealth,
                this.baseDamage,
                effects
        );
    }
}