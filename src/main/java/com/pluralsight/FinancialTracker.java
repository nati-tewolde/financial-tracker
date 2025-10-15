package com.pluralsight;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.InputMismatchException;
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
            System.out.println("\nWelcome to TransactionApp");
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
                default -> System.out.println("\nInvalid option");
            }
        }
        scanner.close();
    }

    /**
     * Reads file line-by-line, parsing and storing each field into a new transaction
     *
     */
    public static void loadTransactions(String fileName) {
        try {
            // Creates new file object if file isn't found in path
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

                LocalDate date = LocalDate.parse(parts[0]);
                LocalTime time = LocalTime.parse(parts[1]);
                String description = parts[2];
                String vendor = parts[3];
                double amount = Double.parseDouble(parts[4]);

                /*if (date == null || time == null || amount == null) {
                    continue;
                }*/

                transactions.add(new Transaction(date, time, description, vendor, amount));
            }
            reader.close();
        } catch (Exception ex) {
            System.err.println("Error reading file.");
        }
    }

    /**
     * Method description
     *
     */
    private static void addDeposit(Scanner scanner) {
        addTransaction(scanner, false, "deposit");
    }

    /**
     * Method description
     *
     */
    private static void addPayment(Scanner scanner) {
        addTransaction(scanner, true, "payment");
    }

    /**
     * Method description
     *
     */
    private static void ledgerMenu(Scanner scanner) {
        boolean isRunning = true;
        while (isRunning) {
            System.out.println("\nLedger");
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
                default -> System.out.println("\nInvalid option.");
            }
        }
    }

    /**
     * Method description
     *
     */
    private static void displayLedger() {
        if (transactions.isEmpty()) {
            System.out.println("No transactions available.");
            return;
        }

        sortTransactions();
        displayLedgerTable("--All Transactions--");

        for (Transaction transaction : transactions) {
            printTransaction(transaction);
        }
        System.out.println();
    }

    /**
     * Method description
     *
     */
    private static void displayDeposits() {
        if (transactions.isEmpty()) {
            System.out.println("No transactions available.");
            return;
        }

        sortTransactions();
        displayLedgerTable("--Deposits--");

        for (Transaction transaction : transactions) {
            if (transaction.getAmount() > 0) {
                printTransaction(transaction);
            }
        }
        System.out.println();
    }

    /**
     * Method description
     *
     */
    private static void displayPayments() {
        if (transactions.isEmpty()) {
            System.out.println("No transactions available.");
            return;
        }

        sortTransactions();
        displayLedgerTable("--Payments--");

        for (Transaction transaction : transactions) {
            if (transaction.getAmount() < 0) {
                printTransaction(transaction);
            }
        }
        System.out.println();
    }

    /**
     * Method description
     *
     */
    private static void reportsMenu(Scanner scanner) {
        boolean isRunning = true;
        while (isRunning) {
            System.out.println("\nReports");
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
                case "1" -> {
                    LocalDate start = LocalDate.now().withDayOfMonth(1);
                    LocalDate end = LocalDate.now();
                    filterTransactionsByDate(start, end, "month-to-date");
                }
                case "2" -> {
                    LocalDate start = LocalDate.now().minusMonths(1).withDayOfMonth(1);
                    LocalDate end = LocalDate.now().withDayOfMonth(1).minusDays(1);
                    filterTransactionsByDate(start, end, "previous-month");
                }
                case "3" -> {
                    LocalDate start = LocalDate.now().withDayOfYear(1);
                    LocalDate end = LocalDate.now();
                    filterTransactionsByDate(start, end, "year-to-date");
                }
                case "4" -> {
                    LocalDate start = LocalDate.now().minusYears(1).withDayOfYear(1);
                    LocalDate end = LocalDate.now().withDayOfYear(1).minusDays(1);
                    filterTransactionsByDate(start, end, "previous-year");
                }
                case "5" -> {
                    System.out.print("\nEnter vendor name to filter transactions by vendor: ");
                    String vendor = scanner.nextLine();
                    if (vendor.isEmpty()) {
                        System.out.println("\nVendor name cannot be empty.");
                        break;
                    }
                    filterTransactionsByVendor(vendor.trim());
                }
                case "6" -> customSearch(scanner);
                case "0" -> isRunning = false;
                default -> System.out.println("Invalid option");
            }
        }
    }

    /**
     * Method description
     *
     */
    private static void filterTransactionsByDate(LocalDate start, LocalDate end, String displayType) {
        if (transactions.isEmpty()) {
            System.out.println("Report is currently empty.");
            return;
        }

        sortTransactions();

        switch (displayType.toLowerCase()) {
            case "month-to-date" -> System.out.printf("%50s%n%n", "--Month To Date Report--");
            case "previous-month" -> System.out.printf("%50s%n%n", "--Previous Month Report--");
            case "year-to-date" -> System.out.printf("%50s%n%n", "--Year To Date Report--");
            default -> System.out.printf("%50s%n%n", "--Previous Year Report--");
        }

        System.out.printf("%-10s | %-8s | %-30s | %-20s | %10s%n",
                "Date", "Time", "Description", "Vendor", "Amount");
        System.out.println("-".repeat(90));

        boolean isFound = false;
        for (Transaction transaction : transactions) {
            LocalDate date = transaction.getDate();
            if (!date.isBefore(start) && !date.isAfter(end)) {
                printTransaction(transaction);
                isFound = true;
            }
        }
        if (!isFound) {
            System.out.println("No transactions found in this date range.");
        }
    }

    /**
     * Method description
     *
     */
    private static void filterTransactionsByVendor(String vendor) {
        if (transactions.isEmpty()) {
            System.out.println("No transactions available.");
            return;
        }

        sortTransactions();
        displayLedgerTable("--Transactions by Vendor--");

        boolean isFound = false;
        for (Transaction transaction : transactions) {
            if (transaction.getVendor().equalsIgnoreCase(vendor)) {
                printTransaction(transaction);
                isFound = true;
            }
        }
        if (!isFound) {
            System.out.println("No transactions found for vendor: " + vendor);
        }
    }

    /**
     * Method description
     *
     */
    private static void customSearch(Scanner scanner) {
        // Input validation and re-prompt user
        if (transactions.isEmpty()) {
            System.out.println("No transactions available.");
            return;
        }

        System.out.print("\nEnter start date (yyyy-MM-dd) or leave blank: ");
        String startInput = scanner.nextLine().trim();
        LocalDate start = startInput.isEmpty() ? null : parseDate(startInput);

        System.out.print("Enter end date (yyyy-MM-dd) or leave blank: ");
        String endInput = scanner.nextLine().trim();
        LocalDate end = endInput.isEmpty() ? null : parseDate(endInput);

        System.out.print("Enter description keyword or leave blank: ");
        String desc = scanner.nextLine().trim();

        System.out.print("Enter vendor or leave blank: ");
        String vendor = scanner.nextLine().trim();

        System.out.print("Enter exact amount or leave blank: ");
        String amtInput = scanner.nextLine().trim();
        Double amount = amtInput.isEmpty() ? null : parseDouble(amtInput);

        sortTransactions();
        displayLedgerTable("--Transactions by Custom Search--");

        boolean isFound = false;
        for (Transaction transaction : transactions) {
            boolean isMatched = true;

            if (start != null && transaction.getDate().isBefore(start)) {
                isMatched = false;
            }

            if (end != null && transaction.getDate().isAfter(end)) {
                isMatched = false;
            }

            if (!desc.isEmpty() && !transaction.getDescription().toLowerCase().contains(desc.toLowerCase())) {
                isMatched = false;
            }

            if (!vendor.isEmpty() && !transaction.getVendor().equalsIgnoreCase(vendor)) {
                isMatched = false;
            }

            if (amount != null && transaction.getAmount() != amount) {
                isMatched = false;
            }

            if (isMatched) {
                printTransaction(transaction);
                isFound = true;
            }
        }
        if (!isFound) {
            System.out.println("No transactions found for the given filters");
        }
    }

    /**
     * Method description
     *
     */
    private static void addTransaction(Scanner scanner, boolean isPayment, String transactionType) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true));
            // Input validation and re-prompt user
            System.out.print("Enter transaction date & time to begin adding " + transactionType + " (yyyy-MM-dd HH:mm:ss), enter N to set it to the current date/time: ");
            String dateTime = scanner.nextLine();

            LocalDate date;
            LocalTime time;
            if (dateTime.equalsIgnoreCase("n")) {
                date = LocalDate.now();
                time = LocalTime.now();
            } else {
                String[] dateTimeParts = dateTime.split(" ");
                date = LocalDate.parse(dateTimeParts[0]);
                time = LocalTime.parse(dateTimeParts[1]);
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
            writer.close();

            System.out.println("\nYou have successfully added your " + transactionType + ".\n");

        } catch (DateTimeParseException ex) {
            System.out.println("\nInvalid date/time format. Use yyyy-MM-dd HH:mm:ss.");
        } catch (InputMismatchException ex) {
            System.out.println("\nInvalid input, please enter a valid amount.");
        } catch (IOException ex) {
            System.out.println("\nError writing to file.");
        } catch (Exception ex) {
            System.out.println("\nError: " + ex.getMessage());
        }
    }

    /*private static void displayTransaction(String displayType) {
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
    }*/

    /*private static void filterTransaction(String filterType, LocalDate start, LocalDate end, String vendor, String displayType) {
        transactions.sort(Comparator.comparing(Transaction::getDate)
                .thenComparing(Transaction::getTime)
                .reversed());

        switch (displayType.toLowerCase()) {
            case "month-to-date":
                System.out.printf("%50s%n", "--Month To Date Report--");
                break;
            case "previous-month":
                System.out.printf("%50s%n", "--Previous Month Report--");
                break;
            case "year-to-date":
                System.out.printf("%50s%n", "--Year To Date Report--");
                break;
            case "previous-year":
                System.out.printf("%50s%n", "--Previous Year Report--");
                break;
            default:
                System.out.printf("%n%50s%n", "--Transactions by Vendor--");
                break;
        }

        if (transactions.isEmpty()) {
            System.out.println("There are currently no transactions in the ledger.");
            return;
        }

        System.out.printf("%n%-10s | %-8s | %-30s | %-20s | %10s%n",
                "Date", "Time", "Description", "Vendor", "Amount");
        System.out.println("-".repeat(90));

        boolean isFound = false;
        for (Transaction transaction : transactions) {
            LocalDate date = transaction.getDate();
            LocalTime time = transaction.getTime();
            String desc = transaction.getDescription();
            String vend = transaction.getVendor();
            double amt = transaction.getAmount();

            boolean isMatched = false;

            switch (filterType.toLowerCase()) {
                case "date" -> isMatched = (date.isAfter(start) || date.isEqual(start))
                        && (date.isBefore(end) || date.isEqual(end));
                case "vendor" -> isMatched = vend.equalsIgnoreCase(vendor.trim());
            }

            if (isMatched) {
                System.out.printf("%-10s | %-7s | %-30s | %-20s | %10.2f%n",
                        date.format(DATE_FMT), time.format(TIME_FMT), desc, vend, amt);
                isFound = true;
            }
        }
        if (!isFound) {
            System.out.print("No transactions found for specified filter\n");
        }
    }*/

    /**
     * Method description
     *
     */
    private static void printTransaction(Transaction transaction) {
        System.out.printf("%-10s | %-7s | %-30s | %-20s | %10.2f%n",
                transaction.getDate().format(DATE_FMT),
                transaction.getTime().format(TIME_FMT),
                transaction.getDescription(),
                transaction.getVendor(),
                transaction.getAmount());
    }

    /**
     * Method description
     *
     */
    private static void displayLedgerTable(String title) {
        System.out.printf("%n%50s%n%n", title);
        System.out.printf("%-10s | %-8s | %-30s | %-20s | %10s%n",
                "Date", "Time", "Description", "Vendor", "Amount");
        System.out.println("-".repeat(90));
    }

    /**
     * Method description
     *
     */
    private static void sortTransactions() {
        transactions.sort(Comparator.comparing(Transaction::getDate)
                .thenComparing(Transaction::getTime)
                .reversed());
    }

    /**
     * Method description
     *
     */
    private static LocalDate parseDate(String s) {
        try {
            return LocalDate.parse(s);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Method description
     *
     */
    private static LocalTime parseTime(String s) {
        try {
            return LocalTime.parse(s);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Method description
     *
     */
    private static Double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception ex) {
            return null;
        }
    }
}
