package com.example.cs360modulethreeassignment_zachmendiola;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Patterns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Application logic layer.
// The activities only handle the screen, this class handles validation and talks to DatabaseHelper.
public class WeightManager {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US);

    private DatabaseHelper db;
    private SharedPreferences prefs;

    public WeightManager(Context context) {
        db = new DatabaseHelper(context);
        prefs = context.getSharedPreferences("healthify_prefs", Context.MODE_PRIVATE);
    }

    // ---------- Validation (returns an error message, or null if the input is ok) ----------

    public String validateLogin(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            return "Email and password are required";
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Please enter a valid email address";
        }
        return null;
    }

    public String validateRegistration(String email, String password) {
        String error = validateLogin(email, password);
        if (error != null) {
            return error;
        }
        if (password.length() < 8) {
            return "Password must be at least 8 characters";
        }
        return null;
    }

    public String validateEntry(String weight, String date) {
        if (weight.isEmpty() || date.isEmpty()) {
            return "Weight and date are required";
        }

        double value;
        try {
            value = Double.parseDouble(weight);
        } catch (NumberFormatException e) {
            return "Weight must be a number";
        }
        if (value <= 0 || value > 1000) {
            return "Weight must be between 1 and 1000 lbs";
        }

        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        format.setLenient(false);
        try {
            if (!date.matches("\\d{2}/\\d{2}/\\d{4}")) {
                throw new ParseException(date, 0);
            }
            format.parse(date);
        } catch (ParseException e) {
            return "Date must be a real date in MM/DD/YYYY format";
        }
        return null;
    }

    // ---------- Users ----------

    public boolean login(String email, String password) {
        return db.checkUser(email, password);
    }

    public boolean userExists(String email) {
        return db.userExists(email);
    }

    public boolean register(String email, String password) {
        return db.registerUser(email, password);
    }

    // ---------- Weight entries ----------

    public boolean addWeight(String weight, String date) {
        return db.insertEvent(weight, date);
    }

    public boolean updateWeight(int id, String weight, String date) {
        return db.updateEvent(id, weight, date);
    }

    public boolean deleteWeight(int id) {
        return db.deleteEvent(id);
    }

    public Cursor getWeights() {
        return db.getAllEvents();
    }

    // Converts the database rows into WeightEntry objects, ignoring rows that have invalid numbers or dates
    private List<WeightEntry> getEntries() {
        List<WeightEntry> entries = new ArrayList<>();
        Cursor cursor = db.getAllEvents();
        while (cursor.moveToNext()) {
            try {
                double weight = Double.parseDouble(cursor.getString(1));
                LocalDate date = LocalDate.parse(cursor.getString(2), DATE_FORMAT);
                entries.add(new WeightEntry(weight, date));
            } catch (RuntimeException e) {
                // invalid rows passthru
            }
        }
        cursor.close();
        return entries;
    }

    // Goal weight setter
    public void setGoalWeight(double goal) {
        prefs.edit().putFloat("goal_weight", (float) goal).apply();
    }

    // Progress summary getter, string builder for the progress

    public String getProgressSummary() {
        WeightStats stats = new WeightStats(getEntries());
        if (stats.isEmpty()) {
            return "No weight entries yet. Add one above to start tracking!";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.US, "Start: %.1f lbs   Current: %.1f lbs   Change: %+.1f lbs\n",
                stats.start().weight, stats.current().weight, stats.totalChange()));

        if (prefs.contains("goal_weight")) {
            double goal = prefs.getFloat("goal_weight", 0f);
            sb.append(String.format(Locale.US, "Goal: %.1f lbs (%.0f%% there)\n", goal, stats.percentToGoal(goal)));
        }

        // Highest + lowest, last 30 days
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(30);
        WeightEntry high = stats.highest(from, to);
        WeightEntry low = stats.lowest(from, to);
        if (high != null) {
            sb.append(String.format(Locale.US, "Last 30 days: high %.1f lbs, low %.1f lbs\n", high.weight, low.weight));
        }

        // Weekly avg
        sb.append("Weekly averages:\n");
        for (Map.Entry<LocalDate, Double> week : stats.weeklyAverages().entrySet()) {
            sb.append(String.format(Locale.US, "  Week of %s: %.1f lbs\n", week.getKey().format(DATE_FORMAT), week.getValue()));
        }

        // Monthly avg
        sb.append("Monthly averages:\n");
        for (Map.Entry<YearMonth, Double> month : stats.monthlyAverages().entrySet()) {
            sb.append(String.format(Locale.US, "  %s: %.1f lbs\n", month.getKey(), month.getValue()));
        }
        return sb.toString().trim();
    }
}
