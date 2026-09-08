package tradex.util;

import java.util.Scanner;


public class InputValidator {

    public static int readInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("Invalid input. Please enter a number between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.printf("Invalid input format '%s'. Please enter an integer.%n", line);
            }
        }
    }

    public static double readPositiveDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(line);
                if (value > 0.0) {
                    return value;
                }
                System.out.println("Invalid input. Value must be strictly greater than zero.");
            } catch (NumberFormatException e) {
                System.out.printf("Invalid input format '%s'. Please enter a valid decimal number.%n", line);
            }
        }
    }

    public static String readNonEmptyString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("Input cannot be empty. Please enter a value.");
        }
    }

    public static boolean readYesNo(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String line = scanner.nextLine().trim().toLowerCase();
            if (line.equals("y") || line.equals("yes")) return true;
            if (line.equals("n") || line.equals("no")) return false;
            System.out.println("Please enter 'y' for yes or 'n' for no.");
        }
    }
}

