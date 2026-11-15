package com.example.cs360modulethreeassignment_zachmendiola;

import android.content.Context;
import android.database.Cursor;
import android.util.Patterns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

// Application logic layer.
// The activities only handle the screen, this class handles validation and talks to DatabaseHelper.
public class WeightManager {

    private DatabaseHelper db;

    public WeightManager(Context context) {
        db = new DatabaseHelper(context);
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

    // Builds a short summary of the user's progress (first entry vs latest entry)
    public String getProgressSummary() {
        Cursor cursor = db.getAllEvents();
        int count = 0;
        double start = 0;
        double current = 0;
        while (cursor.moveToNext()) {
            try {
                double weight = Double.parseDouble(cursor.getString(1));
                if (count == 0) {
                    start = weight;
                }
                current = weight;
                count++;
            } catch (NumberFormatException e) {
                // skip old entries that are not numbers
            }
        }
        cursor.close();

        if (count == 0) {
            return "No weight entries yet. Add one above to start tracking!";
        }
        double change = current - start;
        return String.format(Locale.US, "Start: %.1f lbs   Current: %.1f lbs   Change: %+.1f lbs (%d entries)",
                start, current, change, count);
    }
}
