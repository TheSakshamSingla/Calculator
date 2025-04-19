package com.example.calculator;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.calculator.databinding.ActivityMainBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private CalculatorViewModel viewModel;
    private HistoryAdapter historyAdapter;
    private BottomSheetBehavior<View> historyBottomSheet;
    private boolean isScientificMode = false;
    private DecimalFormat decimalFormat = new DecimalFormat("#.##########");
    private SharedPreferences preferences;
    private static final String THEME_PREF = "theme_preference";
    private static final String HISTORY_PREF = "history_preference";
    private static final String MODE_PREF = "mode_preference";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Set up ViewModel
        viewModel = new ViewModelProvider(this).get(CalculatorViewModel.class);
        
        // Set up shared preferences
        preferences = getPreferences(MODE_PRIVATE);
        applyThemePreference();
        
        // Set up toolbar
        setSupportActionBar(binding.topAppBar);
        
        // Set up history recycler view
        setupHistorySheet();
        
        // Load saved mode
        isScientificMode = preferences.getBoolean(MODE_PREF, false);
        updateCalculatorMode();
        
        // Set up buttons
        setupButtons();
        
        // Observe display value changes
        viewModel.getCurrentDisplay().observe(this, this::updateDisplay);
        
        // Observe calculation history changes
        viewModel.getHistory().observe(this, history -> {
            historyAdapter.setHistoryItems(history);
            saveHistory(history);
        });
        
        // Load saved history
        loadHistory();
    }

    private void setupHistorySheet() {
        historyBottomSheet = BottomSheetBehavior.from(findViewById(R.id.historyBottomSheetBehavior));
        historyBottomSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
        
        historyAdapter = new HistoryAdapter(item -> {
            // When a history item is clicked, set the calculation
            viewModel.setCurrentCalculation(item);
            historyBottomSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
        });
        
        binding.historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.historyRecyclerView.setAdapter(historyAdapter);
    }
    
    private void setupButtons() {
        // Number buttons
        binding.btn0.setOnClickListener(v -> appendToDisplay("0"));
        binding.btn1.setOnClickListener(v -> appendToDisplay("1"));
        binding.btn2.setOnClickListener(v -> appendToDisplay("2"));
        binding.btn3.setOnClickListener(v -> appendToDisplay("3"));
        binding.btn4.setOnClickListener(v -> appendToDisplay("4"));
        binding.btn5.setOnClickListener(v -> appendToDisplay("5"));
        binding.btn6.setOnClickListener(v -> appendToDisplay("6"));
        binding.btn7.setOnClickListener(v -> appendToDisplay("7"));
        binding.btn8.setOnClickListener(v -> appendToDisplay("8"));
        binding.btn9.setOnClickListener(v -> appendToDisplay("9"));
        binding.btnDecimal.setOnClickListener(v -> appendToDisplay("."));
        
        // Operator buttons
        binding.btnAdd.setOnClickListener(v -> appendToDisplay("+"));
        binding.btnSubtract.setOnClickListener(v -> appendToDisplay("-"));
        binding.btnMultiply.setOnClickListener(v -> appendToDisplay("*"));
        binding.btnDivide.setOnClickListener(v -> appendToDisplay("/"));
        binding.btnPercent.setOnClickListener(v -> handlePercent());
        binding.btnOpenParenthesis.setOnClickListener(v -> appendToDisplay("("));
        binding.btnCloseParenthesis.setOnClickListener(v -> appendToDisplay(")"));
        
        // Control buttons
        binding.btnClear.setOnClickListener(v -> clearDisplay());
        binding.btnBackspace.setOnClickListener(v -> backspace());
        binding.fabEquals.setOnClickListener(v -> calculateResult());
        
        // Scientific calculator buttons
        binding.btnScientificZero.setOnClickListener(v -> appendToDisplay("0"));
        binding.btnScientific1.setOnClickListener(v -> appendToDisplay("1"));
        binding.btnScientific2.setOnClickListener(v -> appendToDisplay("2"));
        binding.btnScientific3.setOnClickListener(v -> appendToDisplay("3"));
        binding.btnScientific4.setOnClickListener(v -> appendToDisplay("4"));
        binding.btnScientific5.setOnClickListener(v -> appendToDisplay("5"));
        binding.btnScientific6.setOnClickListener(v -> appendToDisplay("6"));
        binding.btnScientific7.setOnClickListener(v -> appendToDisplay("7"));
        binding.btnScientific8.setOnClickListener(v -> appendToDisplay("8"));
        binding.btnScientific9.setOnClickListener(v -> appendToDisplay("9"));
        binding.btnScientificDecimal.setOnClickListener(v -> appendToDisplay("."));
        
        // Scientific operators
        binding.btnScientificAdd.setOnClickListener(v -> appendToDisplay("+"));
        binding.btnScientificSubtract.setOnClickListener(v -> appendToDisplay("-"));
        binding.btnScientificMultiply.setOnClickListener(v -> appendToDisplay("*"));
        binding.btnScientificDivide.setOnClickListener(v -> appendToDisplay("/"));
        binding.btnScientificClear.setOnClickListener(v -> clearDisplay());
        binding.btnScientificBackspace.setOnClickListener(v -> backspace());
        binding.btnScientificEquals.setOnClickListener(v -> calculateResult());
        
        // Scientific functions
        binding.btnSin.setOnClickListener(v -> appendFunction("sin"));
        binding.btnCos.setOnClickListener(v -> appendFunction("cos"));
        binding.btnTan.setOnClickListener(v -> appendFunction("tan"));
        binding.btnLn.setOnClickListener(v -> appendFunction("ln"));
        binding.btnLog.setOnClickListener(v -> appendFunction("log10"));
        binding.btnSquare.setOnClickListener(v -> appendToDisplay("^2"));
        binding.btnPower.setOnClickListener(v -> appendToDisplay("^"));
        binding.btnPi.setOnClickListener(v -> appendToDisplay("π"));
        binding.btnE.setOnClickListener(v -> appendToDisplay("e"));
    }
    
    private void appendFunction(String function) {
        String current = viewModel.getCurrentDisplay().getValue();
        viewModel.setCurrentDisplay(current + function + "(");
    }
    
    private void appendToDisplay(String value) {
        String current = viewModel.getCurrentDisplay().getValue();
        
        // Handle special cases
        if (value.equals("π")) {
            value = "3.14159265359";
        } else if (value.equals("e")) {
            value = "2.71828182846";
        }
        
        if (current.equals("0") && !value.equals(".")) {
            viewModel.setCurrentDisplay(value);
        } else {
            viewModel.setCurrentDisplay(current + value);
        }
    }
    
    private void clearDisplay() {
        viewModel.setCurrentDisplay("0");
        binding.calculationDisplay.setText("");
    }
    
    private void backspace() {
        String current = viewModel.getCurrentDisplay().getValue();
        if (current.length() > 1) {
            viewModel.setCurrentDisplay(current.substring(0, current.length() - 1));
        } else {
            viewModel.setCurrentDisplay("0");
        }
    }
    
    private void handlePercent() {
        try {
            String current = viewModel.getCurrentDisplay().getValue();
            double value = Double.parseDouble(current);
            value = value / 100.0;
            viewModel.setCurrentDisplay(decimalFormat.format(value));
        } catch (Exception e) {
            Toast.makeText(this, "Invalid operation", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void calculateResult() {
        try {
            String calculation = viewModel.getCurrentDisplay().getValue();
            
            // Replace display-friendly operators with actual operators
            String mathExpression = calculation.replace("×", "*").replace("÷", "/");
            
            // Create custom functions
            Function sin = new Function("sin") {
                @Override
                public double apply(double... args) {
                    return Math.sin(Math.toRadians(args[0]));
                }
            };
            
            Function cos = new Function("cos") {
                @Override
                public double apply(double... args) {
                    return Math.cos(Math.toRadians(args[0]));
                }
            };
            
            Function tan = new Function("tan") {
                @Override
                public double apply(double... args) {
                    return Math.tan(Math.toRadians(args[0]));
                }
            };
            
            Function ln = new Function("ln") {
                @Override
                public double apply(double... args) {
                    return Math.log(args[0]);
                }
            };
            
            Function log10 = new Function("log10") {
                @Override
                public double apply(double... args) {
                    return Math.log10(args[0]);
                }
            };
            
            // Build and evaluate the expression
            Expression expression = new ExpressionBuilder(mathExpression)
                    .functions(sin, cos, tan, ln, log10)
                    .build();
            
            double result = expression.evaluate();
            
            // Format result and update displays
            String formattedResult = formatResult(result);
            binding.calculationDisplay.setText(calculation);
            viewModel.setCurrentDisplay(formattedResult);
            
            // Add to history
            String historyItem = calculation + " = " + formattedResult;
            viewModel.addHistoryItem(historyItem);
            
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private String formatResult(double result) {
        if (result == (long) result) {
            return String.valueOf((long) result);
        } else {
            return decimalFormat.format(result);
        }
    }
    
    private void updateDisplay(String value) {
        binding.display.setText(value);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar, menu);
        updateMenuItems(menu);
        return true;
    }
    
    private void updateMenuItems(Menu menu) {
        MenuItem scientificModeItem = menu.findItem(R.id.scientific_mode);
        scientificModeItem.setTitle(isScientificMode ? R.string.standard_mode : R.string.scientific_mode);
        scientificModeItem.setIcon(isScientificMode ? R.drawable.ic_science : R.drawable.ic_science);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.scientific_mode) {
            toggleCalculatorMode();
            return true;
        } else if (id == R.id.history) {
            toggleHistorySheet();
            return true;
        } else if (id == R.id.theme) {
            toggleTheme();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void toggleCalculatorMode() {
        isScientificMode = !isScientificMode;
        updateCalculatorMode();
        
        // Save mode preference
        preferences.edit().putBoolean(MODE_PREF, isScientificMode).apply();
        
        // Update menu items
        invalidateOptionsMenu();
    }
    
    private void updateCalculatorMode() {
        binding.standardCalculator.setVisibility(isScientificMode ? View.GONE : View.VISIBLE);
        binding.scientificCalculator.setVisibility(isScientificMode ? View.VISIBLE : View.GONE);
        binding.fabEquals.setVisibility(isScientificMode ? View.GONE : View.VISIBLE);
    }
    
    private void toggleHistorySheet() {
        if (historyBottomSheet.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            historyBottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            historyBottomSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }
    
    private void toggleTheme() {
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        int newMode = (currentMode == AppCompatDelegate.MODE_NIGHT_YES) ?
                AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;
        
        // Save theme preference
        preferences.edit().putInt(THEME_PREF, newMode).apply();
        
        // Apply new theme
        AppCompatDelegate.setDefaultNightMode(newMode);
    }
    
    private void applyThemePreference() {
        int themeMode = preferences.getInt(THEME_PREF, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }
    
    private void saveHistory(List<String> history) {
        SharedPreferences.Editor editor = preferences.edit();
        
        // Convert list to string set
        StringBuilder historyBuilder = new StringBuilder();
        for (String item : history) {
            historyBuilder.append(item).append("|||");
        }
        
        editor.putString(HISTORY_PREF, historyBuilder.toString());
        editor.apply();
    }
    
    private void loadHistory() {
        String historyString = preferences.getString(HISTORY_PREF, "");
        if (!historyString.isEmpty()) {
            String[] items = historyString.split("\\|\\|\\|");
            List<String> history = new ArrayList<>();
            for (String item : items) {
                if (!item.isEmpty()) {
                    history.add(item);
                }
            }
            viewModel.setHistory(history);
        }
    }
}