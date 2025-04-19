package com.example.calculator;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class CalculatorViewModel extends ViewModel {
    
    private final MutableLiveData<String> currentDisplay = new MutableLiveData<>("0");
    private final MutableLiveData<List<String>> history = new MutableLiveData<>(new ArrayList<>());

    public LiveData<String> getCurrentDisplay() {
        return currentDisplay;
    }

    public void setCurrentDisplay(String display) {
        currentDisplay.setValue(display);
    }

    public LiveData<List<String>> getHistory() {
        return history;
    }

    public void addHistoryItem(String item) {
        List<String> currentHistory = history.getValue();
        if (currentHistory != null) {
            // Add new item at the beginning for reverse chronological order
            currentHistory.add(0, item);
            // Limit history to 50 items
            if (currentHistory.size() > 50) {
                currentHistory.remove(currentHistory.size() - 1);
            }
            history.setValue(currentHistory);
        }
    }
    
    public void setHistory(List<String> historyList) {
        history.setValue(historyList);
    }
    
    public void setCurrentCalculation(String calculation) {
        // Extract calculation result if in history format "calculation = result"
        if (calculation.contains("=")) {
            String result = calculation.substring(calculation.lastIndexOf("=") + 1).trim();
            currentDisplay.setValue(result);
        } else {
            currentDisplay.setValue(calculation);
        }
    }

    public void clearHistory() {
        history.setValue(new ArrayList<>());
    }
}