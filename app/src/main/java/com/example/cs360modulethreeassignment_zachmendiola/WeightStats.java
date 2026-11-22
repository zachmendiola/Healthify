package com.example.cs360modulethreeassignment_zachmendiola;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// Trend analysis
// Entries are sorted by date once, then each statistic is calculated from sorted list
public class WeightStats {

    private final List<WeightEntry> entries;

    public WeightStats(List<WeightEntry> entries) {
        this.entries = new ArrayList<>(entries);
        this.entries.sort(Comparator.comparing(e -> e.date));
    }

    // Empty check, prevents issues with start() and current() calls when no entries exist
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    //Starting weight
    public WeightEntry start() {
        return entries.get(0);
    }

    // Current weight
    public WeightEntry current() {
        return entries.get(entries.size() - 1);
    }

    // Current weight minus starting weight
    public double totalChange() {
        return current().weight - start().weight;
    }

    // Percent of the way from the starting weight to the goal
    public double percentToGoal(double goal) {
        double needed = goal - start().weight;
        if (needed == 0) {
            return 100;
        }
        return totalChange() / needed * 100;
    }

    // Average weight per week
    public Map<LocalDate, Double> weeklyAverages() {
        Map<LocalDate, List<Double>> groups = new TreeMap<>();
        for (WeightEntry e : entries) {
            LocalDate week = e.date.with(DayOfWeek.MONDAY);
            groups.computeIfAbsent(week, k -> new ArrayList<>()).add(e.weight);
        }
        return averages(groups);
    }

    // Average weight, calendar month
    public Map<YearMonth, Double> monthlyAverages() {
        Map<YearMonth, List<Double>> groups = new TreeMap<>();
        for (WeightEntry e : entries) {
            groups.computeIfAbsent(YearMonth.from(e.date), k -> new ArrayList<>()).add(e.weight);
        }
        return averages(groups);
    }

    // Averages values in groups
    private <K> Map<K, Double> averages(Map<K, List<Double>> groups) {
        Map<K, Double> result = new TreeMap<>();
        for (Map.Entry<K, List<Double>> group : groups.entrySet()) {
            double sum = 0;
            for (double w : group.getValue()) {
                sum += w;
            }
            result.put(group.getKey(), sum / group.getValue().size());
        }
        return result;
    }

    // Heaviest entry between dates
    public WeightEntry highest(LocalDate from, LocalDate to) {
        WeightEntry best = null;
        for (WeightEntry e : entries) {
            if (!e.date.isBefore(from) && !e.date.isAfter(to) && (best == null || e.weight > best.weight)) {
                best = e;
            }
        }
        return best;
    }

    // Lightest entry between dates
    public WeightEntry lowest(LocalDate from, LocalDate to) {
        WeightEntry best = null;
        for (WeightEntry e : entries) {
            if (!e.date.isBefore(from) && !e.date.isAfter(to) && (best == null || e.weight < best.weight)) {
                best = e;
            }
        }
        return best;
    }
}
