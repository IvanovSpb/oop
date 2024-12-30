package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Wallet {
    private double balance;
    private List<Transaction> transactions;
    private Map<Category, Double> budgetByCategory;


    public Wallet() {
        this.balance = 0;
        this.transactions = new ArrayList<>();
        this.budgetByCategory = new HashMap<>();
    }

    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
        if (transaction.getType() == Transaction.TransactionType.INCOME) {
            balance += transaction.getAmount();
        } else {
            balance -= transaction.getAmount();
        }
    }

    public double getBalance() {
        return balance;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
    public Map<Category, Double> getBudgetByCategory() {
        return budgetByCategory;
    }

    public void setBudget(Category category, double amount) {
        budgetByCategory.put(category, amount);
    }
}