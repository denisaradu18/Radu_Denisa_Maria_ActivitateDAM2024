package com.example.aplicatielicenta;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText loginEmail, loginPassword;
    private Button loginButton;
    private TextView signupRedirecting;
    private ProgressBar progressBar;
    private ExecutorService executor;
    private Handler mainHandler;
    private Handler timeoutHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            auth.signOut();
        }
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        timeoutHandler = new Handler(Looper.getMainLooper());

        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signupRedirecting = findViewById(R.id.signup_Redirecting);
        progressBar = findViewById(R.id.login_progress);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            goToMainActivity();
        }

        loginButton.setOnClickListener(v -> {
            String email = loginEmail.getText().toString().trim();
            String pass = loginPassword.getText().toString().trim();

            if (!validateInput(email, pass)) return;

            if (!isNetworkAvailable()) {
                Toast.makeText(LoginActivity.this, "No internet connection. Please check your network settings.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            showLoading(true);

            timeoutHandler.postDelayed(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, "Login timed out. Please try again.",
                            Toast.LENGTH_LONG).show();
                }
            }, 15000);
            performLogin(email, pass);
        });

        signupRedirecting.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            finish();
        });
    }


    private boolean validateInput(String email, String pass) {
        if (email.isEmpty()) {
            loginEmail.setError("Email cannot be empty");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginEmail.setError("Please enter a valid email");
            return false;
        }

        if (pass.isEmpty()) {
            loginPassword.setError("Password cannot be empty");
            return false;
        }

        if (pass.length() < 6) {
            loginPassword.setError("Password must be at least 6 characters");
            return false;
        }

        return true;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private void performLogin(String email, String password) {
        executor.execute(() -> {
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        timeoutHandler.removeCallbacksAndMessages(null); // 🔹 Anulează timeout-ul

                        mainHandler.post(() -> {
                            showLoading(false);
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                goToMainActivity();
                            } else {
                                String errorMessage = "Login Failed";
                                if (task.getException() != null) {
                                    errorMessage += ": " + task.getException().getMessage();
                                }
                                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
        });
    }


    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        loginButton.setEnabled(!isLoading);
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // 🔹 Curăță stiva activităților
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        timeoutHandler.removeCallbacksAndMessages(null); // 🔹 Evităm memory leaks
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        super.onDestroy();
    }
}
