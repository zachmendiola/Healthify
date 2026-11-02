package com.example.cs360modulethreeassignment_zachmendiola;

import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.view.ViewGroup;

public class TableActivity extends AppCompatActivity {

    TableLayout tableLayout;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.database); // match your XML file

        EditText etEventName = findViewById(R.id.editTextText5);
        EditText etEventDate = findViewById(R.id.editTextDate2);
        Button btnAddEvent = findViewById(R.id.button);
        db = new DatabaseHelper(this);

        loadEvents();

        btnAddEvent.setOnClickListener(v -> {
            String name = etEventName.getText().toString();
            String date = etEventDate.getText().toString();

            if (this.db.insertEvent(name, date)) {
                Toast.makeText(this, "Event added!", Toast.LENGTH_SHORT).show();
                loadEvents();
            } else {
                Toast.makeText(this, "Insert failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadEvents() {
        tableLayout.removeViews(1, Math.max(0, tableLayout.getChildCount() - 1)); // Clear previous rows (keep header)

        Cursor cursor = this.db.getAllEvents();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String date = cursor.getString(2);

            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            CheckBox cbDelete = new CheckBox(this);
            cbDelete.setOnClickListener(view -> {
                if (cbDelete.isChecked()) {
                    this.db.deleteEvent(id);
                    loadEvents();
                }
            });

            EditText etName = new EditText(this);
            etName.setText(name);

            EditText etDate = new EditText(this);
            etDate.setText(date);

            Button btnUpdate = new Button(this);
            btnUpdate.setText("Update");
            btnUpdate.setOnClickListener(view -> {
                String newName = etName.getText().toString();
                String newDate = etDate.getText().toString();
                db.updateEvent(id, newName, newDate);
                Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
            });

            row.addView(cbDelete);
            row.addView(etName);
            row.addView(etDate);
            row.addView(btnUpdate);

            tableLayout.addView(row);
        }
        cursor.close();
    }
}
