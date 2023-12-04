package org.mesdag.exporter;

public class CLIHelper {
    private static final int LENGTH = 30;

    public static void printSchedule(int percent) {
        int now = LENGTH * percent / 100;
        System.out.print("\b".repeat(LENGTH + 10));
        System.out.print("[");
        System.out.print(">".repeat(now));
        System.out.print(" ".repeat(LENGTH - now));
        System.out.print("]");
        System.out.print("  " + percent + "%");
    }
}
