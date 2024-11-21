package com.examen.pulseya;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrarActivity extends AppCompatActivity {

    private EditText eTUsuario, eTCorreo, etContraseña;
    private Button btnRegistrar;
    private TextView irALogin;
    private CheckBox tipoDeCuenta;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registrar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        eTUsuario = findViewById(R.id.eTNombre);
        eTCorreo = findViewById(R.id.eTCorreo);
        etContraseña = findViewById(R.id.eTContraseña);
        tipoDeCuenta = findViewById(R.id.tipoCuenta);

        firestore = FirebaseFirestore.getInstance();

        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarUsuario();
            }
        });

        irALogin = findViewById(R.id.txtNoCuenta);
        irALogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegistrarActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void registrarUsuario() {
        String usuario = eTUsuario.getText().toString().trim();
        String correo = eTCorreo.getText().toString().trim();
        String contraseña = etContraseña.getText().toString().trim();

        if (usuario.isEmpty() || correo.isEmpty() || contraseña.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        String tipoCuenta = tipoDeCuenta.isChecked() ? "Publicista" : "Cliente";

        firestore.collection("Usuarios").whereEqualTo("Correo", correo).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "El correo ya está registrado", Toast.LENGTH_SHORT).show();
                    } else {
                        Map<String, Object> usuarios = new HashMap<>();
                        usuarios.put("Nombre", usuario);
                        usuarios.put("Correo", correo);
                        usuarios.put("Contrasena", contraseña);
                        usuarios.put("Tipo_Cuenta", tipoCuenta);

                        firestore.collection("Usuarios").add(usuarios).addOnSuccessListener(documentReference -> {
                                    Toast.makeText(this, "Usuario registrado exitosamente!", Toast.LENGTH_SHORT).show();

                                    startActivity(new Intent(RegistrarActivity.this, LoginActivity.class));
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error al registrar usuario: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al verificar correo: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}