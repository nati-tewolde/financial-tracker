package com.pluralsight;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

public class FinancialTracker {
    private static final ArrayList<Transaction> transactions = new ArrayList<>();
    private static final String FILE_NAME = "transactions.csv";

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";
    private static final String DATETIME_PATTERN = DATE_PATTERN + " " + TIME_PATTERN;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern(TIME_PATTERN);
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    public static void main(String[] args) {
        loadTransactions(FILE_NAME);

        Scanner scanner = new Scanner(System.in);
        boolean isRunning = true;

        while (isRunning) {
            System.out.println("Welcome to TransactionApp");
            System.out.println("Choose an option: ");
            System.out.println("D) Add Deposit");
            System.out.println("P) Make Payment (Debit)");
            System.out.println("L) Ledger");
            System.out.println("X) Exit");

            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "D" -> addDeposit(scanner);
                case "P" -> addPayment(scanner);
                case "L" -> ledgerMenu(scanner);
                case "X" -> isRunning = false;
                default -> System.out.println("Invalid option");
            }
        }
        scanner.close();
    }

    public static void loadTransactions(String fileName) {
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length != 5) {
                    continue;
                }

                LocalDate date = parseDate(parts[0]);
                LocalTime time = parseTime(parts[1]);
                String description = parts[2];
                String vendor = parts[3];
                Double amount = parseDouble(parts[4]);

                if (date == null || time == null || amount == null) {
                    // Is condition redundant (parsing exception is already handled)?
                    // Check for empty description/vendor values?
                    continue;
                }

                transactions.add(new Transaction(date, time, description, vendor, amount));
            }
            reader.close();
        } catch (Exception ex) {
            System.err.println("Error reading file.");
        }
    }

    private static void addDeposit(Scanner scanner) {
        addTransaction(scanner, false, "deposit");
    }

    private static void addPayment(Scanner scanner) {
        addTransaction(scanner, true, "payment");
    }

    private static void ledgerMenu(Scanner scanner) {
        boolean isRunning = true;
        while (isRunning) {
            System.out.println("Ledger");
            System.out.println("Choose an option:");
            System.out.println("A) All");
            System.out.println("D) Deposits");
            System.out.println("P) Payments");
            System.out.println("R) Reports");
            System.out.println("H) Home");

            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "A" -> displayLedger();
                case "D" -> displayDeposits();
                case "P" -> displayPayments();
                case "R" -> reportsMenu(scanner);
                case "H" -> isRunning = false;
                default -> System.out.println("\nInvalid option.\n");
            }
        }
    }

    private static void displayLedger() {
        displayTransaction("all");
    }

    private static void displayDeposits() {
        displayTransaction("deposit");
    }

    private static void displayPayments() {
        displayTransaction("payment");
    }


    private static void reportsMenu(Scanner scanner) {
        boolean isRunning = true;
        while (isRunning) {
            System.out.println("Reports");
            System.out.println("Choose an option:");
            System.out.println("1) Month To Date");
            System.out.println("2) Previous Month");
            System.out.println("3) Year To Date");
            System.out.println("4) Previous Year");
            System.out.println("5) Search by Vendor");
            System.out.println("6) Custom Search");
            System.out.println("0) Back");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> {/* TODO – month-to-date report */ }
                case "2" -> {/* TODO – previous month report */ }
                case "3" -> {/* TODO – year-to-date report   */ }
                case "4" -> {/* TODO – previous year report  */ }
                case "5" -> {/* TODO – prompt for vendor then report */ }
                case "6" -> customSearch(scanner);
                case "0" -> isRunning = false;
                default -> System.out.println("Invalid option");
            }
        }
    }


    private static void filterTransactionsByDate(LocalDate start, LocalDate end) {
        // TODO – iterate transactions, print those within the range
    }

    private static void filterTransactionsByVendor(String vendor) {
        // TODO – iterate transactions, print those with matching vendor
    }

    private static void customSearch(Scanner scanner) {
        // TODO – prompt for any combination of date range, description,
        //        vendor, and exact amount, then display matches
    }

    private static void addTransaction(Scanner scanner, boolean isPayment, String transactionType) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true));
            // Pass second string to differentiate between Making payment vs Adding payment?
            System.out.print("Enter transaction date & time to begin adding " + transactionType + " (yyyy-MM-dd HH:mm:ss), enter N to set it to the current date/time: ");
            String dateTime = scanner.nextLine();

            LocalDate date;
            LocalTime time;
            try {
                if (dateTime.equalsIgnoreCase("n")) {
                    date = LocalDate.now();
                    time = LocalTime.now();
                } else {
                    String[] dateTimeParts = dateTime.split(" ");
                    date = LocalDate.parse(dateTimeParts[0]);
                    time = LocalTime.parse(dateTimeParts[1]);
                }
            } catch (DateTimeParseException ex) {
                System.out.println("\nInvalid transaction date/time format, please use yyyy-MM-dd HH:mm:ss.\n");
                return;
            }
            System.out.print("\nEnter " + transactionType + " description: ");
            String description = scanner.nextLine();

            System.out.print("\nEnter " + transactionType + " vendor: ");
            String vendor = scanner.nextLine();

            System.out.print("\nEnter " + transactionType + " amount: ");
            double amount = scanner.nextDouble();
            scanner.nextLine();

            if (amount < 0) {
                System.out.println("\nEntered " + transactionType + " amounts must be greater than $0.\n");
                return;
            }
            if (isPayment) {
                amount = -amount;
            }
            transactions.add(new Transaction(date, time, description, vendor, amount));
            writer.write(date.format(DATE_FMT) + "|" + time.format(TIME_FMT) + "|" + description + "|" + vendor + "|" + amount + "\n");

            System.out.println("\nYou have successfully added your " + transactionType + ".\n");

            writer.close();
        } catch (Exception ex) {
            System.out.println("\nError: invalid input and/or reading file.\n"); //Nest try/catch so catch error message is more specific?
        }
    }

    private static void displayTransaction(String displayType) {
        // Dynamically scale column width based on length of description and vendor?
        transactions.sort(Comparator.comparing(Transaction::getDate)
                .thenComparing(Transaction::getTime)
                .reversed());

        switch (displayType.toLowerCase()) {
            case "deposit":
                System.out.printf("%50s%n", "--Deposits--");
                break;
            case "payment":
                System.out.printf("%50s%n", "--Payments--");
                break;
            default:
                System.out.printf("%50s%n", "--All Transactions--");
        }

        if (transactions.isEmpty()) {
            System.out.println("Ledger is currently empty.");
            return;
        }

        System.out.printf("%n%-10s | %-8s | %-30s | %-20s | %10s%n",
                "Date", "Time", "Description", "Vendor", "Amount");
        System.out.println("-".repeat(90));

        for (Transaction transaction : transactions) {
            boolean isDeposit = transaction.getAmount() > 0;
            boolean isPayment = transaction.getAmount() < 0;

            if (displayType.equalsIgnoreCase("deposit") && !isDeposit) {
                continue;
            }
            if (displayType.equalsIgnoreCase("payment") && !isPayment) {
                continue;
            }

            System.out.printf("%-10s | %-7s | %-30s | %-20s | %10.2f%n",
                    transaction.getDate().format(DATE_FMT),
                    transaction.getTime().format(TIME_FMT),
                    transaction.getDescription(),
                    transaction.getVendor(),
                    transaction.getAmount());
        }
        System.out.println();
    }

    private static LocalDate parseDate(String s) {
        try {
            return LocalDate.parse(s);
        } catch (Exception ex) {
            return null;
        }
    }

    private static LocalTime parseTime(String s) {
        try {
            return LocalTime.parse(s);
        } catch (Exception ex) {
            return null;
        }
    }

    private static Double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception ex) {
            return null;
        }
    }
}
