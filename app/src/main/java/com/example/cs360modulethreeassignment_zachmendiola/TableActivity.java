package com.example.cs360modulethreeassignment_zachmendiola;

import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View; // REVIEW [Structure | Design] Unused import.
import android.widget.*;
import android.view.ViewGroup;

// REVIEW [Structure | Design] Unreachable code
// TableActivity (the weight-entry grid) is not declared in AndroidManifest.xml and nothing starts it.
// Enhancement: register it and navigate here after login.
public class TableActivity extends AppCompatActivity {

    // REVIEW [Defensive Programming | Design] tableLayout is never assigned.
    // <TableLayout> in database.xml has no android:id, so this pointer is null and loadEvents() throws a NullPointerException the moment this screen opens.
    TableLayout tableLayout;
    TextView tvProgress;
    WeightManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // REVIEW [Documentation | Design] Leftover tutorial comment below. 
        // [Structure] database.xml also has five leftover rows that reuse the same ids (checkBox2, editTextText).
        setContentView(R.layout.database);

        EditText etEventName = findViewById(R.id.editTextText5);
        EditText etEventDate = findViewById(R.id.editTextDate2);
        Button btnAddEvent = findViewById(R.id.button);
        tableLayout = findViewById(R.id.tableLayout);
        tvProgress = findViewById(R.id.textViewProgress);
        manager = new WeightManager(this);

        loadEvents();

        btnAddEvent.setOnClickListener(v -> {
            String name = etEventName.getText().toString().trim();
            String date = etEventDate.getText().toString().trim();

            // [Variables] Both stay untyped strings
            // Enhancement: WeightEntry model with double weight + LocalDate date.
            String error = manager.validateEntry(name, date);
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                return;
            }

            if (manager.addWeight(name, date)) {
                Toast.makeText(this, "Added " + name + " lbs on " + date, Toast.LENGTH_SHORT).show();
                etEventName.setText("");
                etEventDate.setText("");
                loadEvents();
            } else {
                Toast.makeText(this, "Could not add entry, please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // REVIEW [Loops and Branches | Algorithms] Work that belongs outside the loop
    // Every add/delete clears the table and re-creates all rows on the main thread. 
    // Enhancement: RecyclerView + ListAdapter/DiffUtil so only changed rows redraw, with data loaded off the UI thread.
    private void loadEvents() {
        tableLayout.removeViews(1, Math.max(0, tableLayout.getChildCount() - 1)); // Clear previous rows (keep header)

        Cursor cursor = manager.getWeights();
        while (cursor.moveToNext()) {
            // REVIEW [Defensive Programming | Algorithms] Columns read by hardcoded index with no bound check. 
            // Enhancement: Use a method to get column indices safely.
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String date = cursor.getString(2);

            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            CheckBox cbDelete = new CheckBox(this);
            // REVIEW [Loops and Branches | Algorithms] Ticking the box deletes immediately.
            // Calling loadEvents() from a listener that loadEvents() created destroys the view that fired the event.
            cbDelete.setOnClickListener(view -> {
                if (cbDelete.isChecked()) {
                    if (manager.deleteWeight(id)) {
                        Toast.makeText(this, "Deleted entry: " + name + " lbs on " + date, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Could not delete entry", Toast.LENGTH_SHORT).show();
                    }
                    loadEvents();
                }
            });

            EditText etName = new EditText(this);
            etName.setText(name);
            etName.setHint("lbs");

            EditText etDate = new EditText(this);
            etDate.setText(date);

            Button btnUpdate = new Button(this);
            btnUpdate.setText("Update");
            btnUpdate.setOnClickListener(view -> {
                String newName = etName.getText().toString().trim();
                String newDate = etDate.getText().toString().trim();
                String error = manager.validateEntry(newName, newDate);
                if (error != null) {
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    return;
                }
                if (manager.updateWeight(id, newName, newDate)) {
                    Toast.makeText(this, "Updated entry to " + newName + " lbs on " + newDate, Toast.LENGTH_SHORT).show();
                    loadEvents();
                } else {
                    Toast.makeText(this, "Could not update entry", Toast.LENGTH_SHORT).show();
                }
            });

            row.addView(cbDelete);
            row.addView(etName);
            row.addView(etDate);
            row.addView(btnUpdate);

            tableLayout.addView(row);
        }
        cursor.close();

        tvProgress.setText(manager.getProgressSummary());
    }
}
