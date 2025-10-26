package droidbattle.util;
import java.io.BufferedReader; // Для читання файлу по рядках
import java.io.FileReader;    // Для читання символів з файлу
import java.io.FileWriter;    // Для запису символів у файл
import java.io.PrintWriter;   // Для зручного запису рядків у файл
import java.io.IOException;   // Для обробки помилок (наприклад, "файл не знайдено")

public class BattleLogger {
    // Цей клас відповідає за дві речі: зберегти лог бою у файл і прочитати його.
        // final - константа. Назва файлу, куди будемо писати.
        private static final String LOG_FILE_NAME = "battle_log.txt";

        // Метод для збереження логу. Він приймає готовий рядок (String) з усім логом бою
        public void saveLog(String logContent) {
            // "try-with-resources" - це спеціальна конструкція в Java.
            // Вона автоматично закриє файли (writer, printer),
            // навіть якщо під час запису станеться помилка. Це *дуже* важливо.
            // FileWriter - "відкриває" файл для запису.
            // PrintWriter - "обгортка" над FileWriter, яка дозволяє зручно писати цілі рядки (println).
            try (FileWriter writer = new FileWriter(LOG_FILE_NAME);
                 PrintWriter printer = new PrintWriter(writer)) {

                printer.print(logContent); // Запискання всього рядока з логом у файл

                // Повідомлення для користувача, що все добре
                System.out.println(ConsoleColors.ANSI_GREEN + "Бій успішно записано у файл: " + LOG_FILE_NAME + ConsoleColors.ANSI_RESET);

            } catch (IOException e) {
                // catch - "ловимо" помилку, якщо вона сталася (наприклад, немає прав на запис).
                // IOException - Input/Output Exception (помилка Вводу/Виводу).
                System.err.println(ConsoleColors.ANSI_RED + "Помилка під час запису бою у файл: " + e.getMessage() + ConsoleColors.ANSI_RESET);
                // e.getMessage() - покаже, що саме пішло не так.
            }
        }

        // Метод для відтворення логу з файлу
        public void replayLog() {
            // Знову "try-with-resources" для автоматичного закриття файлів
            // FileReader - "відкриває" файл для читання.
            // BufferedReader - "обгортка", яка дозволяє зручно читати файл по рядках (readLine).
            try (FileReader reader = new FileReader(LOG_FILE_NAME);
                 BufferedReader bufferedReader = new BufferedReader(reader)) {

                String line; // Змінна для зберігання одного рядка

                System.out.println(ConsoleColors.ANSI_YELLOW + "--- Початок відтворення бою з файлу " + LOG_FILE_NAME +
                        " ---" + ConsoleColors.ANSI_RESET);

                // while-цикл: "Поки bufferedReader.readLine() повертає не null (не кінець файлу)..."
                // readLine() - читає один рядок і пересуває "курсор" на наступний.
                while ((line = bufferedReader.readLine()) != null) {
                    System.out.println(line); // ...друкуємо цей рядок в консоль

                    // Додавання невеликої паузи, щоб бій було комфортно читати
                    try {
                        Thread.sleep(300); // "Приспати" програму на 300 мілісекунд
                    } catch (InterruptedException ie) {
                        // Якщо щось "розбудить" потік - обробляємо
                        Thread.currentThread().interrupt();
                    }
                }

                System.out.println(ConsoleColors.ANSI_YELLOW + "--- Кінець відтворення бою ---" + ConsoleColors.ANSI_RESET);

            } catch (IOException e) {
                // Ловлення помилки, якщо, наприклад, файл "battle_log.txt" ще не створено
                System.err.println(ConsoleColors.ANSI_RED + "Помилка читання файлу: " + e.getMessage() + ConsoleColors.ANSI_RESET);
                System.err.println(ConsoleColors.ANSI_YELLOW + "Можливо, ви ще не зберегли жодного бою?" + ConsoleColors.ANSI_RESET);
            }
        }
}