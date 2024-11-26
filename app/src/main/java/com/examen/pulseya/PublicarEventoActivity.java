package com.examen.pulseya;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.examen.pulseya.helper.GeocordHelper;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PublicarEventoActivity extends AppCompatActivity {
    private EditText editTextNombreEvento, editTextFechaEvento, editTextHoraInicioEvento, editTextDescripcion, editTextDireccion;
    private RadioGroup grupoTipoEvento;
    private Button botonPublicarEvento, botonSubirImagen;
    private ImageView imagenFlyer;
    private Uri flyerUri;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private String usuarioID, lugarID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publicar_evento);

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        // Inicialización de los elementos de la interfaz
        editTextNombreEvento = findViewById(R.id.editTextNombreEvento);
        editTextFechaEvento = findViewById(R.id.editTextFechaEvento);
        editTextHoraInicioEvento = findViewById(R.id.editTextHoraInicioEvento);
        editTextDescripcion = findViewById(R.id.editTextDescripcion);
        editTextDireccion = findViewById(R.id.editTextDireccion);
        grupoTipoEvento = findViewById(R.id.radioGroupTipoEvento);
        botonPublicarEvento = findViewById(R.id.btnPublicarEvento);
        botonSubirImagen = findViewById(R.id.btnSubirImagen);
        imagenFlyer = findViewById(R.id.imageViewFlyer);

        usuarioID = obtenerUsuarioID();
        if (usuarioID == null) {
            Log.e("PublicarEventoActivity", "UsuarioID es null");
        }

        // Configurar los selectores de fecha, hora e imagen
        editTextFechaEvento.setOnClickListener(v -> mostrarSelectorFecha());
        editTextHoraInicioEvento.setOnClickListener(v -> mostrarSelectorHora());
        botonSubirImagen.setOnClickListener(v -> seleccionarImagen());
        botonPublicarEvento.setOnClickListener(v -> publicarEvento());
    }

    private String obtenerUsuarioID() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("userId", null);
    }

    private void mostrarSelectorFecha() {
        Calendar calendario = Calendar.getInstance();
        DatePickerDialog selectorFecha = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> editTextFechaEvento.setText(String.format("%d-%02d-%02d", year, month + 1, dayOfMonth)),
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH));
        selectorFecha.show();
    }

    private void mostrarSelectorHora() {
        Calendar calendario = Calendar.getInstance();
        TimePickerDialog selectorHora = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> editTextHoraInicioEvento.setText(String.format("%02d:%02d", hourOfDay, minute)),
                calendario.get(Calendar.HOUR_OF_DAY),
                calendario.get(Calendar.MINUTE),
                true);
        selectorHora.show();
    }

    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            flyerUri = data.getData();
            imagenFlyer.setImageURI(flyerUri);
        }
    }

    private void publicarEvento() {
        String nombreEvento = editTextNombreEvento.getText().toString().trim();
        String fechaEvento = editTextFechaEvento.getText().toString().trim();
        String horaInicioEvento = editTextHoraInicioEvento.getText().toString().trim();
        String descripcion = editTextDescripcion.getText().toString().trim();
        String direccion = editTextDireccion.getText().toString().trim();

        if (TextUtils.isEmpty(nombreEvento) || TextUtils.isEmpty(fechaEvento) || TextUtils.isEmpty(horaInicioEvento)
                || TextUtils.isEmpty(descripcion) || TextUtils.isEmpty(direccion)) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (flyerUri == null) {
            Toast.makeText(this, "Por favor, selecciona una imagen para el flyer", Toast.LENGTH_SHORT).show();
            return;
        }

        GeocordHelper.getCoordenadasAtravesDireccion(direccion, getString(R.string.google_maps_key), new GeocordHelper.GeocodeCallback() {
            @Override
            public void onCoordinatesFetched(String latitude, String longitude) {
                guardarLugarEnFirestore(direccion, latitude, longitude);
            }
        });
        finish();
    }

    private void guardarLugarEnFirestore(String direccion, String latitud, String longitud) {
        Map<String, Object> datosLugar = new HashMap<>();
        datosLugar.put("Direccion", direccion);
        datosLugar.put("Latitud", Double.parseDouble(latitud));
        datosLugar.put("Longitud", Double.parseDouble(longitud));

        db.collection("Lugar").add(datosLugar)
                .addOnSuccessListener(documentReference -> {
                    lugarID = documentReference.getId();
                    guardarEventoEnFirestore();
                })
                .addOnFailureListener(e -> Toast.makeText(PublicarEventoActivity.this, "Error al guardar el lugar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void guardarEventoEnFirestore() {
        String nombreEvento = editTextNombreEvento.getText().toString().trim();
        String descripcion = editTextDescripcion.getText().toString().trim();
        String fechaEvento = editTextFechaEvento.getText().toString().trim();
        String horaInicioEvento = editTextHoraInicioEvento.getText().toString().trim();

        Timestamp fechaHoraEvento = convertirAFechaHora(fechaEvento, horaInicioEvento);
        if (fechaHoraEvento == null) {
            Toast.makeText(this, "Error al procesar la fecha y hora del evento", Toast.LENGTH_SHORT).show();
            return;
        }

        int tipoSeleccionadoId = grupoTipoEvento.getCheckedRadioButtonId();
        RadioButton radioSeleccionado = findViewById(tipoSeleccionadoId);
        String tipoEvento = radioSeleccionado != null ? radioSeleccionado.getText().toString() : "";

        DocumentReference referenciaLugar = db.collection("Lugar").document(lugarID);
        DocumentReference referenciaUsuario = db.collection("Usuarios").document(usuarioID);

        Map<String, Object> evento = new HashMap<>();
        evento.put("Nombre_Evento", nombreEvento);
        evento.put("Fecha_Evento", fechaHoraEvento);
        evento.put("Hora_Inicio", fechaHoraEvento);
        evento.put("Descripcion", descripcion);
        evento.put("FlyerUrl", ""); // Inicialmente vacío, se actualizará después
        evento.put("LugarID", referenciaLugar);
        evento.put("Tipo_Evento", tipoEvento);
        evento.put("UsuarioID", referenciaUsuario);

        db.collection("Evento").add(evento)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(PublicarEventoActivity.this, "Evento publicado con éxito", Toast.LENGTH_SHORT).show();
                    uploadFlyerImage(documentReference.getId()); // Subir el flyer y actualizar el evento
                })
                .addOnFailureListener(e -> Toast.makeText(PublicarEventoActivity.this, "Error publicando evento: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private Timestamp convertirAFechaHora(String fecha, String hora) {
        try {
            String fechaHoraStr = fecha + " " + hora;
            SimpleDateFormat formatoFechaHora = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            formatoFechaHora.setTimeZone(Calendar.getInstance().getTimeZone()); // Usa la zona horaria local
            Date fechaHora = formatoFechaHora.parse(fechaHoraStr);
            return new Timestamp(fechaHora);
        } catch (Exception e) {
            Log.e("PublicarEventoActivity", "Error al convertir fecha y hora", e);
            return null;
        }
    }

    private void uploadFlyerImage(String eventId) {
        if (flyerUri != null) {
            StorageReference flyerRef = storageRef.child("flyers/" + eventId + ".jpg");
            flyerRef.putFile(flyerUri)
                    .addOnSuccessListener(taskSnapshot -> flyerRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                Map<String, Object> flyerUpdate = new HashMap<>();
                                flyerUpdate.put("FlyerUrl", uri.toString());
                                db.collection("Evento").document(eventId)
                                        .update(flyerUpdate)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(PublicarEventoActivity.this, "Flyer subido y URL actualizado", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(PublicarEventoActivity.this, HomeActivity.class);
                                            startActivity(intent);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> Log.e("PublicarEventoActivity", "Error actualizando URL del flyer: " + e.getMessage()));
                            })
                            .addOnFailureListener(e -> Log.e("PublicarEventoActivity", "Error obteniendo URL del flyer: " + e.getMessage())))
                    .addOnFailureListener(e -> Log.e("PublicarEventoActivity", "Error subiendo flyer: " + e.getMessage()));
        } else {
            Log.e("PublicarEventoActivity", "Flyer URI es null. No se puede subir la imagen.");
        }
    }
}
