package com.example.cs360modulethreeassignment_zachmendiola;

import java.time.LocalDate;

// Weight entry class
// Each entry is a weight measurement on a specific date
public class WeightEntry {

    public final double weight;
    public final LocalDate date;

    public WeightEntry(double weight, LocalDate date) {
        this.weight = weight;
        this.date = date;
    }
}
