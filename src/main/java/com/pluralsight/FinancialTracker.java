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

    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    public static final String HIGH_INTENSITY = "\u001B[1m";

    public static void main(String[] args) {
        loadTransactions(FILE_NAME);

        Scanner scanner = new Scanner(System.in);
        boolean isRunning = true;

        while (isRunning) {
            System.out.println("\n" + CYAN + HIGH_INTENSITY + """ 
                    ┏━╸╻┏┓╻┏━┓┏┓╻┏━╸╻┏━┓╻  ╺┳╸┏━┓┏━┓┏━╸╻┏ ┏━╸┏━┓
                    ┣╸ ┃┃┗┫┣━┫┃┗┫┃  ┃┣━┫┃   ┃ ┣┳┛┣━┫┃  ┣┻┓┣╸ ┣┳┛
                    ╹  ╹╹ ╹╹ ╹╹ ╹┗━╸╹╹ ╹┗━╸ ╹ ╹┗╸╹ ╹┗━╸╹ ╹┗━╸╹┗╸®""" + RESET);
            System.out.println("D) Add Deposit");
            System.out.println("P) Make Payment (Debit)");
            System.out.println("L) Ledger");
            System.out.println("X) Exit");
            System.out.print(WHITE + "Choose an option: " + RESET);

            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "D" -> addDeposit(scanner);
                case "P" -> addPayment(scanner);
                case "L" -> ledgerMenu(scanner);
                case "X" -> isRunning = false;
                default -> System.out.println(RED + "\nInvalid option." + RESET);
            }
        }
        scanner.close();
    }

    /**
     * Reads file line-by-line, parsing and storing each field into a new transaction
     */
    public static void loadTransactions(String fileName) {
        try {
            // Creates new file object if file isn't found
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }

            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            // Reads each line and splits line based on pipe location
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");

                // Validates line content
                if (parts.length != 5) {
                    System.out.println(RED + "\nError extracting file content, please check " +
                            fileName + " for corrupted data." + RESET);
                    continue;
                }

                // Parses necessary fields
                LocalDate date = LocalDate.parse(parts[0]);
                LocalTime time = LocalTime.parse(parts[1]);
                String description = parts[2];
                String vendor = parts[3];
                double amount = Double.parseDouble(parts[4]);

                /*if (date == null || time == null || amount == null) {
                    continue;
                }*/

                // Stores transaction in arraylist
                transactions.add(new Transaction(date, time, description, vendor, amount));
            }
            reader.close();
        } catch (Exception ex) {
            System.err.println(RED + "\nError reading file." + RESET);
        }
    }

    /**
     * Calls addTransaction method to prompt user to add a new deposit
     */
    private static void addDeposit(Scanner scanner) {
        addTransaction(scanner, false, "deposit");
    }

    /**
     * Calls addTransaction method to prompt user to add a new payment
     */
    private static void addPayment(Scanner scanner) {
        addTransaction(scanner, true, "payment");
    }

    /**
     * Displays ledger options to prompt user
     */
    private static void ledgerMenu(Scanner scanner) {
        boolean isRunning = true;
        while (isRunning) {
            System.out.println(PURPLE + "\nLedger Menu" + RESET);
            System.out.println("A) All");
            System.out.println("D) Deposits");
            System.out.println("P) Payments");
            System.out.println("R) Reports");
            System.out.println("H) Home");
            System.out.print(WHITE + "Choose an option: " + RESET);

            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "A" -> displayLedger();
                case "D" -> displayDeposits();
                case "P" -> displayPayments();
                case "R" -> reportsMenu(scanner);
                case "H" -> isRunning = false;
                default -> System.out.println(RED + "\nInvalid option." + RESET);
            }
        }
    }

    /**
     * Displays all transactions stored in the arraylist
     */
    private static void displayLedger() {
        // Prints message if no transactions are currently stored
        if (transactions.isEmpty()) {
            System.out.println(RED + "\nNo transactions available." + RESET);
            return;
        }

        // Calls method to sort transactions (descending)
        // and display ledger in table format
        sortTransactions();
        displayLedgerTable(PURPLE + "--All Transactions--" + RESET);

        // Iterates through each transaction and calls
        // method to print them
        for (Transaction transaction : transactions) {
            printTransaction(transaction);
        }
        System.out.println();
    }

    /**
     * Displays only deposits or transactions with amounts greater than 0
     */
    private static void displayDeposits() {
        if (transactions.isEmpty()) {
            System.out.println(RED + "\nNo transactions available." + RESET);
            return;
        }

        sortTransactions();
        displayLedgerTable(PURPLE + "--Deposits--" + RESET);

        // Checks if transaction is positive
        for (Transaction transaction : transactions) {
            if (transaction.getAmount() > 0) {
                printTransaction(transaction);
            }
        }
        System.out.println();
    }

    /**
     * Displays only payments or transactions with amounts less than 0
     */
    private static void displayPayments() {
        if (transactions.isEmpty()) {
            System.out.println(RED + "\nNo transactions available." + RESET);
            return;
        }

        sortTransactions();
        displayLedgerTable(PURPLE + "--Payments--" + RESET);

        for (Transaction transaction : transactions) {
            // Checks if transaction is negative
            if (transaction.getAmount() < 0) {
                printTransaction(transaction);
            }
        }
        System.out.println();
    }

    /**
     * Displays report options to prompt user
     * Evaluates date logic and calls methods to display reports
     */
    private static void reportsMenu(Scanner scanner) {
        boolean isRunning = true;
        while (isRunning) {
            System.out.println(GREEN + "\nFinancial Reports" + RESET);
            System.out.println("1) Month To Date");
            System.out.println("2) Previous Month");
            System.out.println("3) Year To Date");
            System.out.println("4) Previous Year");
            System.out.println("5) Search by Vendor");
            System.out.println("6) Custom Search");
            System.out.println("0) Back");
            System.out.print(WHITE + "Choose an option: " + RESET);

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
                    String vendor;
                    while (true) {
                        System.out.print("\nEnter vendor name to filter transactions by vendor: ");
                        vendor = scanner.nextLine();
                        if (vendor.isEmpty()) {
                            System.out.println(RED + "\nVendor name cannot be empty." + RESET);
                            continue;
                        }
                        break;
                    }
                    filterTransactionsByVendor(vendor.trim());
                }
                case "6" -> customSearch(scanner);
                case "0" -> isRunning = false;
                default -> System.out.println(RED + "\nInvalid option" + RESET);
            }
        }
    }

    /**
     * Filters transactions based on provided date range
     * Displays only transactions that meet the condition
     */
    private static void filterTransactionsByDate(LocalDate start, LocalDate end, String displayType) {
        if (transactions.isEmpty()) {
            System.out.println(RED + "\nReport is currently empty." + RESET);
            return;
        }

        sortTransactions();

        // Displays different headings based on parameter
        switch (displayType.toLowerCase()) {
            case "month-to-date" -> System.out.printf(GREEN + "%n%55s%n%n" + RESET, "--Month To Date Report--");
            case "previous-month" -> System.out.printf(GREEN + "%n%55s%n%n" + RESET, "--Previous Month Report--");
            case "year-to-date" -> System.out.printf(GREEN + "%n%55s%n%n" + RESET, "--Year To Date Report--");
            default -> System.out.printf(GREEN + "%n%55s%n%n" + RESET, "--Previous Year Report--");
        }

        System.out.printf("%-10s | %-8s | %-30s | %-20s | %10s%n",
                "Date", "Time", "Description", "Vendor", "Amount");
        System.out.println("-".repeat(90));

        // Checks if dates are within valid range
        // and displays transactions within the given range
        boolean isFound = false;
        for (Transaction transaction : transactions) {
            LocalDate date = transaction.getDate();
            if (!date.isBefore(start) && !date.isAfter(end)) {
                printTransaction(transaction);
                isFound = true;
            }
        }
        if (!isFound) {
            System.out.println(RED + "No transactions found in this date range." + RESET);
        }
        System.out.println();
    }

    /**
     * Filters transactions based on provided vendor
     * Displays only transactions that meet the condition
     */
    private static void filterTransactionsByVendor(String vendor) {
        if (transactions.isEmpty()) {
            System.out.println(RED + "\nReport is currently empty." + RESET);
            return;
        }

        sortTransactions();
        displayLedgerTable(GREEN + "--Transactions by Vendor--" + RESET);

        // Checks if vendor in each transaction matches vendor in method parameter
        boolean isFound = false;
        for (Transaction transaction : transactions) {
            if (transaction.getVendor().equalsIgnoreCase(vendor)) {
                printTransaction(transaction);
                isFound = true;
            }
        }
        if (!isFound) {
            System.out.println(RED + "No transactions found for vendor: " + vendor + RESET);
        }
        System.out.println();
    }

    /**
     * Prompts user for desired filter or choice to skip that filter
     * Assumes all filters are chosen and eliminates each filter if field is left empty or doesn't match transaction values
     */
    private static void customSearch(Scanner scanner) {
        if (transactions.isEmpty()) {
            System.out.println(RED + "\nReport is currently empty." + RESET);
            return;
        }

        // Prompts user for start date, parses string, and validates input (range and format)
        LocalDate start = null, end = null;
        while (true) {
            System.out.print("\nEnter start date (yyyy-MM-dd) or leave blank: ");
            String startInput = scanner.nextLine().trim();
            if (startInput.isEmpty()) {
                break;
            }
            try {
                start = LocalDate.parse(startInput);
                if (start.isBefore(LocalDate.now().minusYears(20)) || start.isAfter(LocalDate.now().plusYears(100))) {
                    System.out.println(RED + "\nEntered date is unrealistic, please enter a more appropriate date." + RESET);
                    continue;
                }
                break;
            } catch (DateTimeParseException ex) {
                System.out.println(RED + "\nInvalid date format, please enter a valid start date." + RESET);
            }
        }


        while (true) {
            System.out.print("\nEnter end date (yyyy-MM-dd) or leave blank: ");
            String endInput = scanner.nextLine().trim();
            if (endInput.isEmpty()) {
                break;
            }
            try {
                end = LocalDate.parse(endInput);
                if (end.isBefore(LocalDate.now().minusYears(20)) || end.isAfter(LocalDate.now().plusYears(100))) {
                    System.out.println(RED + "\nEntered date is unrealistic, please enter a more appropriate date." + RESET);
                    continue;
                }
                break;
            } catch (DateTimeParseException ex) {
                System.out.println(RED + "\nInvalid date format, please enter a valid end date." + RESET);
            }
        }

        System.out.print("\nEnter description keyword or leave blank: ");
        String desc = scanner.nextLine().trim();

        System.out.print("\nEnter vendor or leave blank: ");
        String vendor = scanner.nextLine().trim();

        // Prompts user for amount, parses string, and validates input
        Double amount = null;
        while (true) {
            System.out.print("\nEnter exact amount or leave blank: ");
            String amtInput = scanner.nextLine().trim();
            if (amtInput.isEmpty()) {
                break;
            }
            try {
                amount = Double.parseDouble(amtInput);
                break;
            } catch (NumberFormatException ex) {
                System.out.println(RED + "\nInvalid amount, please enter a valid amount." + RESET);
            }
        }

        sortTransactions();
        displayLedgerTable(GREEN + "--Transactions by Custom Search--" + RESET);

        boolean isFound = false;
        for (Transaction transaction : transactions) {
            // Assumes all filters are chosen and eliminates each filter if conditions aren't met
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

            // Displays transactions based off of remaining filters
            if (isMatched) {
                printTransaction(transaction);
                isFound = true;
            }
        }
        if (!isFound) {
            System.out.println(RED + "No transactions found for the given filters" + RESET);
        }
    }

    /**
     * Prompts user for transactions details to store a new payment or deposit based on parameter transactionType
     * Amounts for payment transactions are converted to negative before storing and writing to file
     */
    private static void addTransaction(Scanner scanner, boolean isPayment, String transactionType) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true));

            // Prompts user for date and time, parses values, and validates input (range and format)
            LocalDate date;
            LocalTime time;
            while (true) {
                System.out.print("\nEnter transaction date & time (yyyy-MM-dd HH:mm:ss) to begin adding " +
                        transactionType + ", enter N to set it to the current date/time: ");
                String dateTime = scanner.nextLine().trim();

                if (dateTime.equalsIgnoreCase("n")) {
                    date = LocalDate.now();
                    time = LocalTime.now();
                    break;

                }
                try {
                    String[] dateTimeParts = dateTime.split(" ");
                    date = LocalDate.parse(dateTimeParts[0]);
                    time = LocalTime.parse(dateTimeParts[1]);

                    if (date.isBefore(LocalDate.now().minusYears(20)) || date.isAfter(LocalDate.now().plusYears(100))) {
                        System.out.println(RED + "\nEntered date is unrealistic, please enter a more appropriate date." + RESET);
                        continue;
                    }
                    break;
                } catch (Exception ex) {
                    System.out.println(RED + "\nInvalid date/time format. Please use yyyy-MM-dd HH:mm:ss." + RESET);
                }
            }

            String description;
            while (true) {
                System.out.print("\nEnter " + transactionType + " description: ");
                description = scanner.nextLine().trim();
                if (description.isEmpty()) {
                    System.out.println(RED + "\nDescription cannot be empty." + RESET);
                    continue;
                }
                break;
            }

            String vendor;
            while (true) {
                System.out.print("\nEnter " + transactionType + " vendor: ");
                vendor = scanner.nextLine().trim();
                if (vendor.isEmpty()) {
                    System.out.println(RED + "\nVendor cannot be empty." + RESET);
                    continue;
                }
                break;
            }

            // Prompts user for amount, parses value, and validates input
            // Converts amount to negative if entered as a payment
            double amount;
            while (true) {
                System.out.print("\nEnter " + transactionType + " amount: ");
                String amountInput = scanner.nextLine().trim();

                if (amountInput.length() > 25) {
                    System.out.println(RED + "\nAmount is too long to store, please enter a more appropriate amount." + RESET);
                    continue;
                }

                try {
                    amount = Double.parseDouble(amountInput);
                    if (amount <= 0) {
                        System.out.println(RED + "\nEntered " + transactionType + " amounts must be greater than $0." + RESET);
                        continue;
                    }
                    break;
                } catch (Exception ex) {
                    System.out.println(RED + "\nInvalid input, please enter a valid amount." + RESET);
                }
            }
            if (isPayment) {
                amount = -amount;
            }

            // Stores and writes transaction to file
            transactions.add(new Transaction(date, time, description, vendor, amount));
            writer.write(date.format(DATE_FMT) + "|" + time.format(TIME_FMT) + "|" + description + "|" + vendor + "|" + amount + "\n");
            writer.close();

            System.out.println(BLUE + "\nYou have successfully added your " + transactionType + "." + RESET);

        } catch (IOException ex) {
            System.out.println(RED + "\nError writing to file." + RESET);
        }
    }

    /**
     * Helper method to iterate through and print transactions in arraylist
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
     * Helper method to format and print ledger table
     */
    private static void displayLedgerTable(String title) {
        System.out.printf("%n%60s%n%n", title);
        System.out.printf("%-10s | %-8s | %-30s | %-20s | %10s%n",
                "Date", "Time", "Description", "Vendor", "Amount");
        System.out.println("-".repeat(90));
    }

    /**
     * Helper method to sort transactions in descending order
     */
    private static void sortTransactions() {
        transactions.sort(Comparator.comparing(Transaction::getDate)
                .thenComparing(Transaction::getTime)
                .reversed());
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

    /*private static LocalDate parseDate(String s) {
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
    }*/
}
