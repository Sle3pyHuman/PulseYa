package com.examen.pulseya;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.examen.pulseya.helper.GeocordHelper;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PublicarEventoActivity extends AppCompatActivity {
    private EditText editTextNombreEvento, editTextFechaEvento, editTextHoraInicioEvento, editTextDescripcion, editTextDireccion;
    private RadioGroup radioGroupTipoEvento;
    private Button btnPublicarEvento, btnSubirImagen;
    private ImageView imageViewFlyer;
    private Uri flyerUri;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private String UsuarioID, placeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_publicar_evento);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configuracion del Cloud y Storage
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        editTextNombreEvento = findViewById(R.id.editTextNombreEvento);
        editTextFechaEvento = findViewById(R.id.editTextFechaEvento);
        editTextHoraInicioEvento = findViewById(R.id.editTextHoraInicioEvento);
        editTextDescripcion = findViewById(R.id.editTextDescripcion);
        editTextDireccion = findViewById(R.id.editTextDireccion);
        radioGroupTipoEvento = findViewById(R.id.radioGroupTipoEvento);
        btnPublicarEvento = findViewById(R.id.btnPublicarEvento);
        btnSubirImagen = findViewById(R.id.btnSubirImagen);
        imageViewFlyer = findViewById(R.id.imageViewFlyer);

        // Conseguir id del usuario
        UsuarioID = getIntent().getStringExtra(UsuarioID);

        if (UsuarioID == null) {
            Log.e("PublicarEventoActivity", "UsuarioID esta nulo");
        } else {
            Log.d("PublicarEventoActivity", "UsuarioID: " + UsuarioID);
        }

        // Selecionar fecha y hora
        editTextFechaEvento.setOnClickListener(v -> showDatePickerDialog());
        editTextHoraInicioEvento.setOnClickListener(v -> showTimePickerDialog());

        // Subir imagen
        btnSubirImagen.setOnClickListener(v -> elegirImagen());

        // Publicar evento
        btnPublicarEvento.setOnClickListener(v -> publicarEvento());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> editTextFechaEvento.setText(year + "-" + (month + 1) + "-" + dayOfMonth),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> editTextHoraInicioEvento.setText(hourOfDay + ":" + minute),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        timePickerDialog.show();
    }

    private void elegirImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            flyerUri = data.getData();
            imageViewFlyer.setImageURI(flyerUri);
        }
    }

    private void publicarEvento() {
        String nombreEvento = editTextNombreEvento.getText().toString().trim();
        String fechaEvento = editTextFechaEvento.getText().toString().trim();
        String horaInicioEvento = editTextHoraInicioEvento.getText().toString().trim();
        String descripcion = editTextDescripcion.getText().toString().trim();
        String direccion = editTextDireccion.getText().toString().trim();

        if (TextUtils.isEmpty(nombreEvento) || TextUtils.isEmpty(fechaEvento) || TextUtils.isEmpty(horaInicioEvento) || TextUtils.isEmpty(descripcion) || TextUtils.isEmpty(direccion)) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        GeocordHelper.getCoordenadasAtravesDireccion(direccion, "@string/google_maps_key", new GeocordHelper.GeocodeCallback() {
            @Override
            public void onCoordinatesFetched(String latitude, String longitude) {
                guardarLugarAFirestore(direccion, latitude, longitude);
            }
        });
    }

    private void guardarLugarAFirestore(String direccion, String latitude, String longitude) {
        Map<String, Object> placeData = new HashMap<>();
        placeData.put("Direccion", direccion);
        placeData.put("Latitud", Double.parseDouble(latitude));
        placeData.put("Longitud", Double.parseDouble(longitude));

        db.collection("Lugares").add(placeData)
                .addOnSuccessListener(documentReference -> {
                    placeId = documentReference.getId();
                    guardarEventoAFirestore();
                })
                .addOnFailureListener(e -> Toast.makeText(PublicarEventoActivity.this, "Error guardando lugar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void guardarEventoAFirestore() {
        String nombreEvento = editTextNombreEvento.getText().toString().trim();
        String fechaEvento = editTextFechaEvento.getText().toString().trim();
        String horaInicioEvento = editTextHoraInicioEvento.getText().toString().trim();
        String descripcion = editTextDescripcion.getText().toString().trim();

        int itemSeleccionado = radioGroupTipoEvento.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(itemSeleccionado);
        String tipoEvento = selectedRadioButton != null ? selectedRadioButton.getText().toString() : "";

        Map<String, Object> evento = new HashMap<>();
        evento.put("Nombre_Evento", nombreEvento);
        evento.put("Fecha_Evento", Timestamp.now());
        evento.put("Hora_Inicio", Timestamp.now());
        evento.put("Descripcion", descripcion);
        evento.put("FlyerUrl", "");
        evento.put("LugarID", placeId);
        evento.put("Tipo_Evento", tipoEvento);
        evento.put("UsuarioID", UsuarioID);

        db.collection("Evento").add(evento)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(PublicarEventoActivity.this, "Evento publicado con éxito", Toast.LENGTH_SHORT).show();
                    subitFlyerImagen(documentReference.getId());
                })
                .addOnFailureListener(e -> Toast.makeText(PublicarEventoActivity.this, "Error publicando evento: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void subitFlyerImagen(String eventoId) {
        if (flyerUri != null) {
            StorageReference flyerRef = storageRef.child("flyers/" + eventoId + ".jpg");
            flyerRef.putFile(flyerUri).addOnSuccessListener(taskSnapshot -> flyerRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Map<String, Object> flyerUpdate = new HashMap<>();
                        flyerUpdate.put("FlyerUrl", uri.toString());
                        db.collection("Evento").document(eventoId).update(flyerUpdate).addOnSuccessListener(aVoid -> Toast.makeText(PublicarEventoActivity.this, "Flyer subido", Toast.LENGTH_SHORT).show());
                    })).addOnFailureListener(e -> Toast.makeText(PublicarEventoActivity.this, "ERROR subiendo flyer: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}