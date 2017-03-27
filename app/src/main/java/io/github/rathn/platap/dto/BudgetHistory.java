package io.github.rathn.platap.dto;

import java.util.Calendar;

public class BudgetHistory {
    private double mBudgetSum;
    private Calendar mDate;
    private double mSpentSum;

    public Calendar getDate() {
        return this.mDate;
    }

    public void setDate(Calendar date) {
        this.mDate = date;
    }

    public double getSpentSum() {
        return this.mSpentSum;
    }

    public void setSpentSum(double spentSum) {
        this.mSpentSum = spentSum;
    }

    public double getBudgetSum() {
        return this.mBudgetSum;
    }

    public void setBudgetSum(double budgetSum) {
        this.mBudgetSum = budgetSum;
    }
}
