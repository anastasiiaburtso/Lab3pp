package droidbattle.util;

public class ConsoleColors {
    // Це "escape-коди" для терміналу. Вони кажуть консолі змінити колір.
    // final - це константа, її не можна змінити.
    public static final String ANSI_RESET = "\u001B[0m";  // Скидає колір на стандартний
    public static final String ANSI_RED = "\u001B[31m";    // Червоний текст
    public static final String ANSI_GREEN = "\u001B[32m";  // Зелений текст
    public static final String ANSI_YELLOW = "\u001B[33m"; // Жовтий текст
    public static final String ANSI_BLUE = "\u001B[34m";   // Синій текст
    public static final String ANSI_PURPLE = "\u001B[35m"; // Фіолетовий
    public static final String ANSI_CYAN = "\u001B[36m";   // Бірюзовий
}
