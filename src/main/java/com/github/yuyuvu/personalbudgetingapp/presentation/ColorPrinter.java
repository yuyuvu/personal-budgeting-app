package com.github.yuyuvu.personalbudgetingapp.presentation;

public class ColorPrinter {
    private static final String RESET = "\033[0m";  // Text Reset

    private static final String YELLOW = "\033[0;33m";  // YELLOW
    private static final String GREEN = "\033[0;32m";   // GREEN
    private static final String RED = "\033[0;31m";     // RED
    private static final String CYAN = "\033[0;36m";    // CYAN
    private static final String PURPLE = "\033[0;35m";  // PURPLE

    // вывод без новой строки

    public static void printYellow(String message) {
        System.out.print(YELLOW + message + RESET);
    }

    public static void printGreen(String message) {
        System.out.print(GREEN + message + RESET);
    }

    public static void printRed(String message) {
        System.out.print(RED + message + RESET);
    }

    public static void printCyan(String message) {
        System.out.print(CYAN + message + RESET);
    }

    public static void printPurple(String message) {
        System.out.print(PURPLE + message + RESET);
    }

    public static void print(String message) {
        System.out.print(message);
    }

    // вывод с новой строкой

    public static void printlnYellow(String message) {
        System.out.println(YELLOW + message + RESET);
    }

    public static void printlnGreen(String message) {
        System.out.println(GREEN + message + RESET);
    }

    public static void printlnRed(String message) {
        System.out.println(RED + message + RESET);
    }

    public static void printlnCyan(String message) {
        System.out.println(CYAN + message + RESET);
    }

    public static void printlnPurple(String message) {
        System.out.println(PURPLE + message + RESET);
    }

    public static void println(String message) {
        System.out.println(message);
    }

    // пропуск строки

    public static void skipLine() {
        System.out.println();
    }
}
