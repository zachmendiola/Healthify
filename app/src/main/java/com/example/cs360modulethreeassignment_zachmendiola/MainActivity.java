package com.example.cs360modulethreeassignment_zachmendiola;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.*;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText etEmail = findViewById(R.id.editTextTextEmailAddress);
        EditText etPassword = findViewById(R.id.editTextTextPassword2);
        Button btnLogin = findViewById(R.id.button2);
        TextView tvRegister = findViewById(R.id.textView8);
        CheckBox rememberMe = findViewById(R.id.checkBox);
        db = new DatabaseHelper(this);

        btnLogin.setOnClickListener(view -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (db.checkUser(email, password)) {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Invalid login!", Toast.LENGTH_SHORT).show();
            }
        });

        tvRegister.setOnClickListener(view -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password required for registration", Toast.LENGTH_SHORT).show();
                return;
            }

            if (db.userExists(email)) {
                Toast.makeText(this, "User exists!", Toast.LENGTH_SHORT).show();
            } else {
                if (db.registerUser(email, password)) {
                    Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Registration failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
