package com.examen.pulseya;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.examen.pulseya.helper.BaseDatosHelper;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText eTCorreo, eTContraseña;
    private Button btnLogin;
    private TextView registrarCuenta;
    private BaseDatosHelper dbHelper;
    private String UsuarioID;

    private FirebaseFirestore firestore;

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

        eTCorreo = findViewById(R.id.eTCorreo);
        eTContraseña = findViewById(R.id.eTContraseña);

        firestore = FirebaseFirestore.getInstance();
        dbHelper = new BaseDatosHelper(this);

        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUsuario();
            }
        });

        registrarCuenta = findViewById(R.id.txtNoCuenta);
        registrarCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegistrarActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUsuario() {
        String correo = eTCorreo.getText().toString();
        String contraseña = eTContraseña.getText().toString();

        if (TextUtils.isEmpty(correo) || TextUtils.isEmpty(contraseña)) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("Usuarios").whereEqualTo("Correo", correo).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String contraseñaBD = document.getString("Contrasena");

                            if (contraseña.equals(contraseñaBD)) {
                                String tipoDeCuenta = document.getString("Tipo_Cuenta");
                                String nombre = document.getString("Nombre");
                                String UsuarioID = document.getId();

                                Toast.makeText(this, "Bienvenido " + tipoDeCuenta, Toast.LENGTH_SHORT).show();

                                dbHelper.guardarUsuario(nombre, correo, contraseña, tipoDeCuenta);

                                storeUserId(UsuarioID);

                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                intent.putExtra("id", UsuarioID);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(this, "No existe el usuario", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al iniciar sesión: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void storeUserId(String userId) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userId", userId);  // Store the user ID
        editor.apply();
    }

    public String getUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("userId", null);  // Return null if no userId is found
    }
}