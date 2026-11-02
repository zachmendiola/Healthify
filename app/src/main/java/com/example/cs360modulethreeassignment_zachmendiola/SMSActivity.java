package com.example.cs360modulethreeassignment_zachmendiola;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SMSActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 101;
    private boolean smsPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms);

        Button btnAccept = findViewById(R.id.button3);
        Button btnReject = findViewById(R.id.button4);
        Button btnGoal = findViewById(R.id.button6);

        btnAccept.setOnClickListener(v -> requestSMSPermission());

        btnReject.setOnClickListener(v -> {
            smsPermissionGranted = false;
            Toast.makeText(this, "SMS permission denied. Notifications will be app-only.", Toast.LENGTH_SHORT).show();
        });

        btnGoal.setOnClickListener(v -> {
            if (smsPermissionGranted) {
                sendSMS("5551234567", "Congratulations! You've reached your goal weight!");
            } else {
                Toast.makeText(this, "Goal reached! (SMS disabled)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestSMSPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        } else {
            smsPermissionGranted = true;
            Toast.makeText(this, "SMS permission already granted!", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSMS(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "SMS sent to " + phoneNumber, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "SMS failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                smsPermissionGranted = true;
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                smsPermissionGranted = false;
                Toast.makeText(this, "Permission denied. SMS alerts will be disabled.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
