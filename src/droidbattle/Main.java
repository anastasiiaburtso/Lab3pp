package droidbattle;
import droidbattle.droids.*;
import droidbattle.game.Battle;
import droidbattle.util.BattleLogger;
import droidbattle.util.ConsoleColors;

import java.util.ArrayList; // Для *змінного* списку
import java.util.List;
import java.util.Scanner; // Для зчитування вводу з консолі
import java.util.InputMismatchException; // Для обробки помилок вводу

public class Main {

        // final - не можна змінити (Scanner буде один на всю програму).
        private static final Scanner scanner = new Scanner(System.in); // Об'єкт для читання з консолі

        // "склад" дроїдів.
        private static List<Droid> droidStorage = new ArrayList<>(); // ArrayList - це реалізація List, в яку можна додавати/видаляти

        // Об'єкти для логіки гри
        private static Battle battle = new Battle(scanner); // Створення одиного об'єкта "Битва"
        private static BattleLogger logger = new BattleLogger(); // Створення одиного об'єкта "Логгер"

        // Змінна для зберігання останнього бою (для подальшого збереження у файл)
        private static String lastBattleLog = null;


        public static void main(String[] args) {
            boolean isRunning = true; // "Прапорець", що контролює головний цикл гри

            // Головний цикл гри (Game Loop)
            // "Поки isRunning == true, повторювати..."
            while (isRunning) {
                printMenu(); // Викликання методу, що малює меню
                int choice = getUserChoice(); // Викликання методу, що отримує вибір користувача


                switch (choice) {
                    case 1:
                        createDroid(); // Викликання методу створення дроїда
                        break; // break - вийти зі switch
                    case 2:
                        showAllDroids(); // показ дроїдів
                        break;
                    case 3:
                        run1v1Battle(); // бій 1 на 1
                        break;
                    case 4:
                        runTeamBattle(); // бій команда на команду
                        break;
                    case 5:
                        saveLastBattle(); // збереження бою
                        break;
                    case 6:
                        replayLastBattle(); // відтворення бою
                        break;
                    case 7:
                        isRunning = false; // "Опускання прапореця" - цикл завершиться
                        System.out.println("Дякую за гру! Вихід...");
                        break;
                    default:
                        // default - виконується, якщо choice не дорівнює 1-7
                        System.out.println(ConsoleColors.ANSI_RED + "Неправильний вибір. Будь ласка, введіть число від 1 до 7." + ConsoleColors.ANSI_RESET);
                }

                // Невелика пауза перед повторним показом меню (якщо не вихід)
                if (isRunning) {
                    System.out.println("\n(Натисніть Enter, щоб продовжити.)");
                    scanner.nextLine(); // Очікування, поки користувач натисне Enter
                }
            }

            scanner.close(); // Закриваємо Scanner, коли програма завершується
        }


        // Метод для друку меню
        private static void printMenu() {
            System.out.println(ConsoleColors.ANSI_PURPLE + "\n---- ( БИТВА ДРОЇДІВ ) ----" + ConsoleColors.ANSI_RESET);
            System.out.println("1. Створити дроїда");
            System.out.println("2. Показати список моїх дроїдів");
            System.out.println("3. Запустити бій (1 на 1)");
            System.out.println("4. Запустити бій (Команда на Команду)");
            System.out.println("5. Зберегти останній бій у файл");
            System.out.println("6. Відтворити останній бій з файлу");
            System.out.println("7. Вийти з програми");
            System.out.print(ConsoleColors.ANSI_YELLOW + "Ваш вибір: " + ConsoleColors.ANSI_RESET);
        }

        // Метод для отримання вводу від користувача (з обробкою помилок)
        private static int getUserChoice() {
            try {
                int choice = scanner.nextInt(); // Спроба прочитати число
                scanner.nextLine(); // "З'їдаємо" символ нового рядка (\n)
                return choice;
            } catch (InputMismatchException e) {
                // "Зловити" помилку, якщо користувач ввів не число (напр., "abc")
                scanner.nextLine(); // Очищуємо буфер сканера від невірного вводу
                return -1; // Повертаємо -1 (неправильний вибір), щоб спрацював "default" у switch
            }
        }

        // 1. Створення дроїда
        private static void createDroid() {
            System.out.println("\n--- Створення дроїда ---");
            System.out.println("Оберіть тип дроїда:");
            System.out.println("1. Warrior (Багато HP, шанс критичного удару)");
            System.out.println("2. Sniper (Мало HP, велике нанесення пошкодження, шанс промаху)");
            System.out.println("3. Healer (Лікує союзників, слабка атака)");
            System.out.print(ConsoleColors.ANSI_YELLOW + "Тип (1-3): " + ConsoleColors.ANSI_RESET);

            int type = getUserChoice(); // Використання нашого захищеного методу

            System.out.print(ConsoleColors.ANSI_YELLOW + "Введіть ім'я дроїда: " + ConsoleColors.ANSI_RESET);
            String name = scanner.nextLine(); // Читання рядока

            Droid newDroid; // Оголошення змінну типу Droid

            // Створення конкретного типу дроїда залежно від вибору
            switch (type) {
                case 1:
                    newDroid = new WarriorDroid(name); // Створення Воїна
                    break;
                case 2:
                    newDroid = new SniperDroid(name); // Створюємо Снайпера
                    break;
                case 3:
                    newDroid = new HealerDroid(name); // Створюємо Цілителя
                    break;
                default:
                    System.out.println(ConsoleColors.ANSI_RED + "Неправильний тип. Створення скасовано." + ConsoleColors.ANSI_RESET);
                    return;
            }

            droidStorage.add(newDroid);
            System.out.println(ConsoleColors.ANSI_GREEN + "Створено дроїда: " + newDroid + ConsoleColors.ANSI_RESET);
        }

        // Показ всіх дроїдів
        private static void showAllDroids() {
            System.out.println("\n--- Список ваших дроїдів ---");
            if (droidStorage.isEmpty()) { // .isEmpty() - перевіряє, чи список порожній
                System.out.println(ConsoleColors.ANSI_YELLOW + "У вас ще немає дроїдів. Створіть їх (пункт 1)." + ConsoleColors.ANSI_RESET);
            } else {
                // Проходимо по списку і друкуємо кожного дроїда з його індексом
                // (i + 1) - щоб нумерація була з 1, а не з 0.
                for (int i = 0; i < droidStorage.size(); i++) {
                    System.out.printf("%d. %s\n", (i + 1), droidStorage.get(i));
                    // .get(i) - отримати елемент зі списку за індексом i
                }
            }
        }

        // Бій 1 на 1
        private static void run1v1Battle() {
            System.out.println("\n--- Бій 1 на 1 ---");
            if (droidStorage.size() < 2) { // Перевірка, чи є хоча б 2 дроїди
                System.out.println(ConsoleColors.ANSI_RED + "Недостатньо дроїдів для бою. Потрібно хоча б 2." + ConsoleColors.ANSI_RESET);
                return;
            }

            showAllDroids();

            // Вибір першого бійця
            System.out.print(ConsoleColors.ANSI_YELLOW + "Оберіть першого бійця (введіть номер): " + ConsoleColors.ANSI_RESET);
            Droid d1 = SelectDroidFromStorage();
            if (d1 == null) return; // Якщо вибір невдалий - виходимо

            // Вибір другого бійця
            System.out.print(ConsoleColors.ANSI_YELLOW + "Оберіть другого бійця (введіть номер): " + ConsoleColors.ANSI_RESET);
            Droid d2 = SelectDroidFromStorage();
            if (d2 == null) return;

            if (d1 == d2) { // Перевірка, чи не вибрали одного й того ж
                System.out.println(ConsoleColors.ANSI_RED + "Дроїд не може битися сам з собою!" + ConsoleColors.ANSI_RESET);
                return;
            }

            // Створення "команди" з одного дроїда
            List<Droid> team1 = new ArrayList<>();
            team1.add(d1);
            List<Droid> team2 = new ArrayList<>();
            team2.add(d2);

            // Запускаємо бій і ЗБЕРІГАЄМО результат (лог) у нашу змінну
            lastBattleLog = battle.startBattle(team1, team2);

        }

        // Командний бій
        private static void runTeamBattle() {
            System.out.println("\n--- Командний бій ---");
            if (droidStorage.size() < 2) { // Знову, треба хоча б 2 дроїди
                System.out.println(ConsoleColors.ANSI_RED + "Недостатньо дроїдів для бою. Потрібно хоча б 2." + ConsoleColors.ANSI_RESET);
                return;
            }

            // Створення списків для двох команд
            List<Droid> team1 = new ArrayList<>();
            List<Droid> team2 = new ArrayList<>();

            // Створення копій, щоб можна "викреслювати" вже обраних щоб безпечно видаляти обраних дроїдів
            List<Droid> availableDroids = new ArrayList<>(droidStorage);

            System.out.println(ConsoleColors.ANSI_BLUE + "--- Формування Команди 1 ---" + ConsoleColors.ANSI_RESET);
            // Викликання метод, який заповнить команду 1
            // (true - означає, що можна вибирати дроїдів)
            fillTeam(team1, availableDroids, true);

            if (team1.isEmpty()) {
                System.out.println(ConsoleColors.ANSI_RED + "Команда 1 порожня. Бій скасовано." + ConsoleColors.ANSI_RESET);
                return;
            }

            System.out.println(ConsoleColors.ANSI_RED + "\n--- Формування команди 2 ---" + ConsoleColors.ANSI_RESET);
            // Решту дроїдів можна або додати в команду 2, або вибрати вручну
            System.out.println("1. Додати всіх дроїдів, що залишились, в Команду 2");
            System.out.println("2. Вибрати дроїдів для команди 2 вручну");
            System.out.print(ConsoleColors.ANSI_YELLOW + "Ваш вибір (1-2): " + ConsoleColors.ANSI_RESET);
            int choice = getUserChoice();

            if (choice == 1) {
                if (availableDroids.isEmpty()) {
                    System.out.println(ConsoleColors.ANSI_RED + "Не залишилось дроїдів для команди 2!" + ConsoleColors.ANSI_RESET);
                    return;
                }
                team2.addAll(availableDroids); // .addAll() - додати всі елементи з іншого списку
                System.out.println(ConsoleColors.ANSI_GREEN + "Всі дроїди, що залишились, додані в команду 2." + ConsoleColors.ANSI_RESET);
            } else {
                // (false - означає, що дроїди, які залишились, більше не потрібні)
                fillTeam(team2, availableDroids, false);
                if (team2.isEmpty()) {
                    System.out.println(ConsoleColors.ANSI_RED + "Команда 2 порожня. Бій скасовано." + ConsoleColors.ANSI_RESET);
                    return;
                }
            }

            // Запуск бою і зберігання логу
            lastBattleLog = battle.startBattle(team1, team2);
        }

        // Допоміжний метод для вибору дроїдів у команду
        private static void fillTeam(List<Droid> team, List<Droid> availableDroids, boolean allowSkip) {
            while(true) { // Нескінченний цикл
                System.out.println("\nДоступні дроїди для вибору:");
                if (availableDroids.isEmpty()) {
                    System.out.println(ConsoleColors.ANSI_YELLOW + "Немає доступних дроїдів." + ConsoleColors.ANSI_RESET);
                    break; // Вийти з нескінченного циклу
                }

                // Друкування доступних дроїдів
                for (int i = 0; i < availableDroids.size(); i++) {
                    System.out.printf("%d. %s\n", (i + 1), availableDroids.get(i));
                }
                System.out.println(ConsoleColors.ANSI_YELLOW + "0. Завершити формування команди" + ConsoleColors.ANSI_RESET);
                System.out.print("Оберіть дроїда (введіть номер): ");

                int choice = getUserChoice();
                if (choice == 0) {
                    if (team.isEmpty()) { // Дозволення "пропустити" вибір
                        System.out.println("Команда не може бути порожньою. Оберіть хоча б одного.");
                        continue; // Не даємо вийти, якщо команда порожня
                    }
                    break; // Завершити формування
                }

                int index = choice - 1; // Рахування індексу (бо нумерація з 0)

                // Перевірка коректності індексу
                if (index >= 0 && index < availableDroids.size()) {
                    // .remove(index) - ВИДАЛЯЄ дроїда зі списку availableDroid і одразу повертає його
                    Droid selectedDroid = availableDroids.remove(index);
                    team.add(selectedDroid); // Додаємо його в команду
                    System.out.println(ConsoleColors.ANSI_GREEN + "Додано: " + selectedDroid.getName() + ConsoleColors.ANSI_RESET);
                } else {
                    System.out.println(ConsoleColors.ANSI_RED + "Неправильний номер." + ConsoleColors.ANSI_RESET);
                }
            }
        }

        // Допоміжний метод для вибору одного дроїда (для 1vs1)
        private static Droid SelectDroidFromStorage() {
            int choice = getUserChoice();
            int index = choice - 1; // Рахуємо індекс

            // Перевірка
            if (index >= 0 && index < droidStorage.size()) {
                return droidStorage.get(index); // Повертання дроїда за індексом
            } else {
                System.out.println(ConsoleColors.ANSI_RED + "Неправильний номер. Вибір скасовано." + ConsoleColors.ANSI_RESET);
                return null; // Повертання null (нічого)
            }
        }

        // 5. Зберегти бій
        private static void saveLastBattle() {
            System.out.println("\n--- Збереження останнього бою ---");
            if (lastBattleLog == null) {
                System.out.println(ConsoleColors.ANSI_YELLOW + "Ви ще не провели жодного бою!" + ConsoleColors.ANSI_RESET);
            } else {
                // Викликання методу логгера
                logger.saveLog(lastBattleLog);
            }
        }

        // Відтворити бій
        private static void replayLastBattle() {
            System.out.println("\n--- Відтворення бою з файлу ---");
            // Виклик методу логгера
            logger.replayLog();
        }
}
