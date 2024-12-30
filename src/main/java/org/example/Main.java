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
                output.print("Добро пожаловать в Менеджер личных финансов!");
                output.print("Пожалуйста, войдите или зарегистрируйтесь (войти/зарегистрироваться)");
                String action = scanner.nextLine();

                if ("зарегистрироваться".equalsIgnoreCase(action)) {
                    output.print("Введите логин нового пользователя:");
                    String newLogin = scanner.nextLine();
                    output.print("Введите пароль нового пользователя:");
                    String newPassword = scanner.nextLine();
                    authService.registerUser(new User(newLogin, newPassword));
                    output.print("Пользователь успешно зарегистрирован");
                } else if ("войти".equalsIgnoreCase(action)) {
                    output.print("Введите логин:");
                    String login = scanner.nextLine();
                    output.print("Введите пароль:");
                    String password = scanner.nextLine();
                    User user = authService.authenticateUser(login, password);
                    if (user != null) {
                        currentUserName = user.getLogin();
                        try {
                            currentWallet = dataStorage.loadWallet(currentUserName + ".json");
                        } catch (IOException e) {
                            output.print("Ошибка загрузки кошелька: " + e.getMessage());
                            currentWallet = new Wallet();
                        }
                        output.print("Вход выполнен, Добро пожаловать " + currentUserName);
                    } else {
                        output.print("Неверный логин или пароль");
                    }
                }
                else{
                    output.print("Неверное действие. Пожалуйста, введите 'войти' или 'зарегистрироваться'.");
                }
            }else{
                output.print("Доступные команды:\n" +
                        "add_income <категория> <сумма>\n" +
                        "add_expense <категория> <сумма>\n" +
                        "set_budget <категория> <сумма>\n" +
                        "print_report\n" +
                        "print_report <имя_файла>\n" +
                        "calculate_total <категории через запятую>\n" +
                        "calculate_income <категории через запятую>\n" +
                        "calculate_expense <категории через запятую>\n" +
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
                            output.print("Доход успешно добавлен");
                        } catch (NumberFormatException e) {
                            output.print("Неверный формат суммы. Пожалуйста, введите корректное число.");
                        }
                    }
                    else{
                        output.print("Неверный формат. Пожалуйста, введите 'add_income <категория> <сумма>'");
                    }

                } else if (command.startsWith("add_expense")) {
                    String[] parts = command.split(" ");
                    if(parts.length == 3){
                        try{
                            Category category = new Category(parts[1]);
                            double amount = Double.parseDouble(parts[2]);
                            financeManager.addTransaction(currentWallet, new Transaction(category, amount, Transaction.TransactionType.EXPENSE));
                            output.print("Расход успешно добавлен");
                        } catch (NumberFormatException e) {
                            output.print("Неверный формат суммы. Пожалуйста, введите корректное число.");
                        }
                    }
                    else{
                        output.print("Неверный формат. Пожалуйста, введите 'add_expense <категория> <сумма>'");
                    }
                } else if (command.startsWith("set_budget")) {
                    String[] parts = command.split(" ");
                    if(parts.length == 3){
                        try{
                            Category category = new Category(parts[1]);
                            double amount = Double.parseDouble(parts[2]);
                            currentWallet.setBudget(category, amount);
                            output.print("Бюджет установлен для категории: " + category.getName() + ", бюджет: " + amount);
                        }  catch (NumberFormatException e) {
                            output.print("Неверный формат суммы. Пожалуйста, введите корректное число.");
                        }
                    }
                    else{
                        output.print("Неверный формат. Пожалуйста, введите 'set_budget <категория> <сумма>'");
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

                    reportOutput.print("Общий доход: " + financeManager.getTotalIncome(currentWallet));
                    reportOutput.print("Доходы по категориям:");
                    financeManager.getIncomeByCategory(currentWallet).forEach((category, amount) ->
                            reportOutput.print(category.getName() + ": " + amount)
                    );
                    reportOutput.print("Общие расходы: " + financeManager.getTotalExpenses(currentWallet));
                    reportOutput.print("Бюджет по категориям:");

                    Output finalReportOutput = reportOutput;
                    Wallet finalCurrentWallet = currentWallet;
                    currentWallet.getBudgetByCategory().forEach((category, amount) -> {
                        double remainingBudget = financeManager.calculateRemainingBudget(finalCurrentWallet, category);
                        finalReportOutput.print(category.getName() + ": " + amount + ", оставшийся бюджет: " + remainingBudget);
                        if (remainingBudget < 0) {
                            finalReportOutput.print("Предупреждение: Бюджет превышен для категории: " + category.getName());
                        }
                    });

                    if (financeManager.getTotalExpenses(currentWallet) > financeManager.getTotalIncome(currentWallet)) {
                        reportOutput.print("Предупреждение: Общие расходы превышают общий доход");
                    }

                    if(reportOutput instanceof  FileOutput){
                        output.print("Отчет создан в файле: " + parts[1]);
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
                        output.print("Общий оставшийся бюджет для выбранных категорий: " + total);
                    } else {
                        output.print("Неверная команда. Использование: calculate_total <категория1,категория2,...>");
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
                        output.print("Общая сумма расходов для выбранных категорий: " + totalExpense);
                    } else {
                        output.print("Неверная команда. Использование: calculate_expense <категория1,категория2,...>");
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
                        output.print("Общая сумма доходов для выбранных категорий: " + totalIncome);
                    }
                    else{
                        output.print("Неверная команда. Использование: calculate_income <категория1,категория2,...>");
                    }
                }
                else if ("logout".equalsIgnoreCase(command)) {
                    currentUserName = null;
                    try {
                        dataStorage.saveWallet(currentUserName + ".json", currentWallet);
                    } catch (IOException e) {
                        output.print("Ошибка сохранения данных:" + e.getMessage());
                    }
                    currentWallet = null;
                    output.print("Выход из пользователя выполнен успешно");
                }
                else if ("exit".equalsIgnoreCase(command)) {
                    try {
                        dataStorage.saveWallet(currentUserName + ".json", currentWallet);
                    } catch (IOException e) {
                        output.print("Ошибка сохранения данных:" + e.getMessage());
                    }
                    output.print("Выход из приложения");
                    break;
                }
                else{
                    output.print("Неизвестная команда");
                }
            }
        }
        scanner.close();
    }
}