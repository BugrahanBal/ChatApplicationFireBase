package co.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    EditText emailText, passwordText;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        emailText = findViewById(R.id.userEmail);
        passwordText = findViewById(R.id.userPassword);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void signIn(View view) {

        mAuth.signInWithEmailAndPassword(emailText.getText().toString().trim(),
                passwordText.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener< AuthResult >() {
            @Override
            public void onComplete(@NonNull Task< AuthResult > task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(SignUpActivity.this, "Failed...", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void signUp(View view) {

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();


        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener< AuthResult >() {
            @Override
            public void onComplete(@NonNull Task< AuthResult > task) {
                if (task.isSuccessful()) {
                    /*FirebaseUser user = mAuth.getCurrentUser();//giriş yapmış kullanıcı kimse
                    String userEmail= user.getEmail().toString().trim();
                    System.out.println("User E mail : " + userEmail);*/
                    Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(SignUpActivity.this, "Failed...", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}