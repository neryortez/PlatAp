package io.github.rathn.platap;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import io.github.rathn.platap.dto.Balance;
import io.github.rathn.platap.dto.Category;
import io.github.rathn.platap.dto.Transaction;

/**
 * Created by Neri Ortez on 22/11/2016.
 */

public class DatabaseMan {

    private Category cat;

    public DatabaseMan(){
        cat = new Category("0001", "Gasto!");
        cat.setCategoryColor(Color.WHITE);
        cat.setIconIndex(0);
        cat.setIsExpense(true);
    }
    public ArrayList<Transaction> getList(Date date) {
        ArrayList<Transaction> list = new ArrayList<>();
        Transaction transaction = new Transaction();
        for (int i = 0; i < 4; i++) {
            transaction.setCategory(cat);
            Calendar instance = Calendar.getInstance();
            instance.setTime(date);
            transaction.setPrice(instance.get(Calendar.DATE)-i);
            list.add(transaction.copyWithCategory());
        }
        return list;
    }

    public HashMap<String, Integer> getBalances(Date date) {

        HashMap<String, Integer> map = new HashMap<>(15);
        for (int i = 0; i < 15; i++) {
            map.put(String.valueOf(i+1), 100*i);
        }
        return map;
    }


    public static HashMap<String, Double> getBalancesHashMapFromList(ArrayList<Balance> list) {
        HashMap<String, Double> map = new HashMap<>();
//        double prev = -1;
        for (int i = 0; i < list.size(); i++) {
            double totalValue = list.get(i).getTotalValue();
//            if (totalValue = prev){ totalValue =
            map.put(String.valueOf(i+1), totalValue);
//            prev = totalValue;
        }
        return map;
    }
}
