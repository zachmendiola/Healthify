package com.example.cs360modulethreeassignment_zachmendiola;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

// REVIEW [Structure | Design] MainActivity is the launcher and the only screen declared in AndroidManifest.xml. 
// It handles login and registration against the users table.
// UI, validation and database calls all live in onCreate, so none of it can be unit-tested. 
// Enhancement: split into View Model and Repository.
public class MainActivity extends AppCompatActivity {

    WeightManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // REVIEW [Variables | Design] View ids are layout-editor defaults (button2, textView8, editTextTextPassword2)
        // No meaningful names, you have to open theme to know them.
        EditText etEmail = findViewById(R.id.editTextTextEmailAddress);
        EditText etPassword = findViewById(R.id.editTextTextPassword2);
        Button btnLogin = findViewById(R.id.button2);
        TextView tvRegister = findViewById(R.id.textView8);

        // REVIEW [Variables | Design] rememberMe is assigned but never used
        CheckBox rememberMe = findViewById(R.id.checkBox);
        manager = new WeightManager(this);

        btnLogin.setOnClickListener(view -> {
            String email = etEmail.getText().toString().trim();
            // REVIEW [Defensive Programming | Databases] trim() alters the password the user typed.
            String password = etPassword.getText().toString().trim();

            String error = manager.validateLogin(email, password);
            if (error != null) {
                // REVIEW [Structure | Design] String constants - should be symbolic resources in a constants .xml file.
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                return;
            }

            // REVIEW [Defensive Programming | Databases] No limit on failed login attempts. Brute force attacks are possible.
            if (manager.login(email, password)) {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, TableActivity.class));
            } else {
                Toast.makeText(this, "Incorrect email or password", Toast.LENGTH_SHORT).show();
            }
        });

        tvRegister.setOnClickListener(view -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            String error = manager.validateRegistration(email, password);
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                return;
            }

            // REVIEW [Structure | Databases] Check then insert takes two queries.
            // A UNIQUE constraint would let the insert alone detect the duplicate.
            if (manager.userExists(email)) {
                Toast.makeText(this, "An account with this email already exists", Toast.LENGTH_SHORT).show();
            } else {
                if (manager.register(email, password)) {
                    Toast.makeText(this, "Registered successfully! You can now log in.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Registration failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
