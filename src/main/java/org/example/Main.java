package org.example;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        AuthenticationService authService = new AuthenticationService();
        FinanceManager financeManager = new FinanceManager();
        DataStorage dataStorage = new FileDataStorage();
        Output output = new ConsoleOutput();
        Scanner scanner = new Scanner(System.in);
        String currentUserName = null;
        Wallet currentWallet = null;

        // Register default users
        authService.registerUser(new User("user1", "pass1"));
        authService.registerUser(new User("user2", "pass2"));

        while (true) {
            if (currentUserName == null) {
                output.print("Welcome to the Personal Finance Manager!");
                output.print("Please login or register (login/register)");
                String action = scanner.nextLine();

                if ("register".equalsIgnoreCase(action)) {
                    output.print("Enter new user login:");
                    String newLogin = scanner.nextLine();
                    output.print("Enter new user password:");
                    String newPassword = scanner.nextLine();
                    authService.registerUser(new User(newLogin, newPassword));
                    output.print("User register successfully");
                } else if ("login".equalsIgnoreCase(action)) {
                    output.print("Enter login:");
                    String login = scanner.nextLine();
                    output.print("Enter password:");
                    String password = scanner.nextLine();
                    User user = authService.authenticateUser(login, password);
                    if (user != null) {
                        currentUserName = user.getLogin();
                        try {
                            currentWallet = dataStorage.loadWallet(currentUserName + ".json");
                        } catch (IOException e) {
                            output.print("Error load wallet: " + e.getMessage());
                            currentWallet = new Wallet();
                        }
                        output.print("Login successful, Welcome " + currentUserName);
                    } else {
                        output.print("Invalid login or password");
                    }
                }
                else{
                    output.print("Invalid action. Please enter 'login' or 'register'.");
                }
            }else{
                output.print("Available command:\n" +
                        "add_income <category> <amount>\n" +
                        "add_expense <category> <amount>\n" +
                        "set_budget <category> <amount>\n" +
                        "print_report\n" +
                        "print_report <filename>\n" +
                        "calculate_total <categories separated by comma>\n" +
                        "calculate_income <categories separated by comma>\n" +
                        "calculate_expense <categories separated by comma>\n" +
                        "logout\n" +
                        "exit");

                String command = scanner.nextLine();
                if(command.startsWith("add_income")){
                    String[] parts = command.split(" ");
                    if(parts.length == 3){
                        try{
                            Category category = new Category(parts[1]);
                            double amount = Double.parseDouble(parts[2]);
                            financeManager.addTransaction(currentWallet, new Transaction(category, amount, Transaction.TransactionType.INCOME));
                            output.print("Income added successfully");
                        } catch (NumberFormatException e) {
                            output.print("Invalid amount format. Please enter a valid number.");
                        }
                    }
                    else{
                        output.print("Invalid format. Please enter 'add_income <category> <amount>'");
                    }

                } else if (command.startsWith("add_expense")) {
                    String[] parts = command.split(" ");
                    if(parts.length == 3){
                        try{
                            Category category = new Category(parts[1]);
                            double amount = Double.parseDouble(parts[2]);
                            financeManager.addTransaction(currentWallet, new Transaction(category, amount, Transaction.TransactionType.EXPENSE));
                            output.print("Expense added successfully");
                        } catch (NumberFormatException e) {
                            output.print("Invalid amount format. Please enter a valid number.");
                        }
                    }
                    else{
                        output.print("Invalid format. Please enter 'add_expense <category> <amount>'");
                    }
                } else if (command.startsWith("set_budget")) {
                    String[] parts = command.split(" ");
                    if(parts.length == 3){
                        try{
                            Category category = new Category(parts[1]);
                            double amount = Double.parseDouble(parts[2]);
                            currentWallet.setBudget(category, amount);
                            output.print("Budget setted for category: " + category.getName() + ", budget: " + amount);
                        }  catch (NumberFormatException e) {
                            output.print("Invalid amount format. Please enter a valid number.");
                        }
                    }
                    else{
                        output.print("Invalid format. Please enter 'set_budget <category> <amount>'");
                    }
                }
                else if (command.startsWith("print_report")) {
                    String[] parts = command.split(" ");
                    Output reportOutput;
                    if (parts.length == 2) {
                        reportOutput = new FileOutput(parts[1]);
                    } else {
                        reportOutput = output;
                    }

                    reportOutput.print("Total income: " + financeManager.getTotalIncome(currentWallet));
                    reportOutput.print("Income by category:");
                    financeManager.getIncomeByCategory(currentWallet).forEach((category, amount) ->
                            reportOutput.print(category.getName() + ": " + amount)
                    );
                    reportOutput.print("Total expenses: " + financeManager.getTotalExpenses(currentWallet));
                    reportOutput.print("Budget by category:");

                    Output finalReportOutput = reportOutput;
                    Wallet finalCurrentWallet = currentWallet;
                    currentWallet.getBudgetByCategory().forEach((category, amount) -> {
                        double remainingBudget = financeManager.calculateRemainingBudget(finalCurrentWallet, category);
                        finalReportOutput.print(category.getName() + ": " + amount + ", remaining budget: " + remainingBudget);
                        if (remainingBudget < 0) {
                            finalReportOutput.print("Warning: Budget exceeded for category: " + category.getName());
                        }
                    });

                    if (financeManager.getTotalExpenses(currentWallet) > financeManager.getTotalIncome(currentWallet)) {
                        reportOutput.print("Warning: Total expenses exceed total income");
                    }
                    if(reportOutput instanceof  FileOutput){
                        output.print("Report created in file:" + parts[1]);
                    }
                }
                else if(command.startsWith("calculate_total")){
                    String[] parts = command.split(" ");
                    if (parts.length > 1) {
                        List<Category> categories = Arrays.stream(parts[1].split(","))
                                .map(String::trim)
                                .map(Category::new)
                                .collect(Collectors.toList());
                        double total = financeManager.calculateRemainingBudget(currentWallet, categories);
                        output.print("Total remaining budget for selected categories: " + total);
                    } else {
                        output.print("Invalid command. Usage: calculate_total <category1,category2,...>");
                    }
                }
                else if (command.startsWith("calculate_expense")) {
                    String[] parts = command.split(" ");
                    if (parts.length > 1) {
                        List<Category> categories = Arrays.stream(parts[1].split(","))
                                .map(String::trim)
                                .map(Category::new)
                                .collect(Collectors.toList());
                        double totalExpense = financeManager.calculateTotalExpenseForCategories(currentWallet, categories);
                        output.print("Total expenses for selected categories: " + totalExpense);
                    } else {
                        output.print("Invalid command. Usage: calculate_expense <category1,category2,...>");
                    }
                }
                else if(command.startsWith("calculate_income")){
                    String[] parts = command.split(" ");
                    if(parts.length > 1){
                        List<Category> categories = Arrays.stream(parts[1].split(","))
                                .map(String::trim)
                                .map(Category::new)
                                .collect(Collectors.toList());
                        double totalIncome = financeManager.calculateTotalIncomeForCategories(currentWallet, categories);
                        output.print("Total income for selected categories: " + totalIncome);
                    }
                    else{
                        output.print("Invalid command. Usage: calculate_income <category1,category2,...>");
                    }
                }
                else if ("logout".equalsIgnoreCase(command)) {
                    currentUserName = null;
                    try {
                        dataStorage.saveWallet(currentUserName + ".json", currentWallet);
                    } catch (IOException e) {
                        output.print("Error save data:" + e.getMessage());
                    }
                    currentWallet = null;
                    output.print("Logged out successfully");
                }
                else if ("exit".equalsIgnoreCase(command)) {
                    try {
                        dataStorage.saveWallet(currentUserName + ".json", currentWallet);
                    } catch (IOException e) {
                        output.print("Error save data:" + e.getMessage());
                    }
                    output.print("Exiting application");
                    break;
                }
                else{
                    output.print("Unknown command");
                }
            }
        }
        scanner.close();
    }
}