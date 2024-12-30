package org.example;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FinanceManager {
    public void addTransaction(Wallet wallet, Transaction transaction) {
        wallet.addTransaction(transaction);
    }

    public double getTotalIncome(Wallet wallet) {
        return wallet.getTransactions().stream()
                .filter(transaction -> transaction.getType() == Transaction.TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalExpenses(Wallet wallet) {
        return wallet.getTransactions().stream()
                .filter(transaction -> transaction.getType() == Transaction.TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public Map<Category, Double> getIncomeByCategory(Wallet wallet) {
        return  wallet.getTransactions().stream()
                .filter(transaction -> transaction.getType() == Transaction.TransactionType.INCOME)
                .collect(Collectors.groupingBy(Transaction::getCategory, Collectors.summingDouble(Transaction::getAmount)));
    }

    public Map<Category, Double> getExpensesByCategory(Wallet wallet) {
        return  wallet.getTransactions().stream()
                .filter(transaction -> transaction.getType() == Transaction.TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(Transaction::getCategory, Collectors.summingDouble(Transaction::getAmount)));
    }


    public double calculateRemainingBudget(Wallet wallet, Category category) {
        double budget = wallet.getBudgetByCategory().getOrDefault(category, 0.0);
        double expenses = getExpensesByCategory(wallet).getOrDefault(category, 0.0);
        return budget - expenses;
    }
    public double calculateRemainingBudget(Wallet wallet, List<Category> categories) {
        return categories.stream()
                .mapToDouble(category -> calculateRemainingBudget(wallet, category))
                .sum();
    }
    public double calculateTotalExpenseForCategories(Wallet wallet, List<Category> categories) {
        return categories.stream()
                .mapToDouble(category -> getExpensesByCategory(wallet).getOrDefault(category, 0.0))
                .sum();
    }

    public double calculateTotalIncomeForCategories(Wallet wallet, List<Category> categories) {
        return categories.stream()
                .mapToDouble(category -> getIncomeByCategory(wallet).getOrDefault(category, 0.0))
                .sum();
    }
}