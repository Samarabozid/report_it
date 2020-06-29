package reportit.reportit.reportit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    EditText email, password;
    TextView forgotPassword;
    Button sign_in, sign_up;
    String email_txt, password_txt;

    FirebaseUser user;
    FirebaseAuth auth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    CheckBox checkBox;
    SharedPreferences loginPreferences;
    SharedPreferences.Editor loginPrefsEditor;
    Boolean saveLogin;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginPreferences = getApplicationContext().getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        email = findViewById(R.id.email_field);
        password = findViewById(R.id.password_field);
        sign_in = findViewById(R.id.login_btn);
        sign_up = findViewById(R.id.register_btn);
        forgotPassword = findViewById(R.id.forgot_password_txt);

        checkBox = findViewById(R.id.remember_me_checkbox);

        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                final String emailAddress = email.getText().toString();

                if (TextUtils.isEmpty(emailAddress)) {
                    Toast.makeText(getApplicationContext(), "please enter your email firstly", Toast.LENGTH_SHORT).show();
                    return;
                }

                auth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "password reset email has been sent to : " + emailAddress, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        saveLogin = loginPreferences.getBoolean("saveLogin", false);

        if (saveLogin) {
            email.setText(loginPreferences.getString("username", ""));
            password.setText(loginPreferences.getString("password", ""));
            checkBox.setChecked(true);
        }

        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email_txt = email.getText().toString();
                password_txt = password.getText().toString();

                if (TextUtils.isEmpty(email_txt)) {
                    Toast.makeText(getApplicationContext(), "please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password_txt)) {
                    Toast.makeText(getApplicationContext(), "please enter your password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (email_txt.equals("admin@admin.com") && password_txt.equals("adminadmin")) {

                    progressDialog = new ProgressDialog(LoginActivity.this);
                    progressDialog.setTitle("Admin Login");
                    progressDialog.setMessage("Please Wait Until Admin Login ...");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.show();
                    progressDialog.setCancelable(false);

                    AdminLogin(email_txt, password_txt);

                    loginPrefsEditor.putBoolean("savepassword", true);
                    loginPrefsEditor.putString("pass", password_txt);
                    loginPrefsEditor.apply();

                    if (checkBox.isChecked()) {
                        loginPrefsEditor.putBoolean("saveLogin", true);
                        loginPrefsEditor.putString("username", email_txt);
                        loginPrefsEditor.putString("password", password_txt);
                        loginPrefsEditor.apply();
                    } else {
                        loginPrefsEditor.clear();
                        loginPrefsEditor.apply();
                    }
                } else {
                    progressDialog = new ProgressDialog(LoginActivity.this);
                    progressDialog.setTitle("User Login");
                    progressDialog.setMessage("Please Wait Until Login ...");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.show();
                    progressDialog.setCancelable(false);

                    UserLogin(email_txt, password_txt);

                    loginPrefsEditor.putBoolean("savepassword", true);
                    loginPrefsEditor.putString("pass", password_txt);
                    loginPrefsEditor.apply();

                    if (checkBox.isChecked()) {
                        loginPrefsEditor.putBoolean("saveLogin", true);
                        loginPrefsEditor.putString("username", email_txt);
                        loginPrefsEditor.putString("password", password_txt);
                        loginPrefsEditor.apply();
                    } else {
                        loginPrefsEditor.clear();
                        loginPrefsEditor.apply();
                    }
                }
            }
        });
    }

    private void UserLogin(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent i = new Intent(getApplicationContext(), UserStartActivity.class);
                            startActivity(i);
                            progressDialog.dismiss();
                        } else {
                            //String taskmessage = task.getException().getMessage();
                            Toast.makeText(getApplicationContext(), "This email and password are not in database", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }

    private void AdminLogin(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().getUser().getUid().equals("cmhI8RFSUDUBn9qd95Cwu7lgP2f2")) {
                                Intent i = new Intent(getApplicationContext(), AdminStartActivity.class);
                                startActivity(i);
                                progressDialog.dismiss();
                            }
                        } else {
                            String taskmessage = task.getException().getMessage();
                            Toast.makeText(getApplicationContext(), taskmessage, Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }


    private String getUID() {
        user = auth.getCurrentUser();
        String UserID = user.getUid();

        return UserID;
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}