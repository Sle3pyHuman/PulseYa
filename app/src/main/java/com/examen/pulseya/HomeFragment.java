package com.examen.pulseya;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private EventoAdapter eventoAdapter;
    private List<Evento> eventoList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventoList = new ArrayList<>();
        eventoAdapter = new EventoAdapter(getContext(), eventoList);
        recyclerView.setAdapter(eventoAdapter);

        db = FirebaseFirestore.getInstance();

        // obtener eventos
        obtenerEventos();

        return view;
    }

    private void obtenerEventos() {
        Log.d("HomeFragment", "Fetching events...");

        SimpleDateFormat fechaFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat tiempoFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        db.collection("Evento").get().addOnSuccessListener(queryDocumentSnapshots -> {
            Log.d("HomeFragment", "Events fetched successfully");
            eventoList.clear();

            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                String titulo = doc.getString("Nombre_Evento");
                String descripcion = doc.getString("Descripcion");
                Timestamp fechaTimestamp = doc.getTimestamp("Fecha_Evento");
                String fecha = fechaTimestamp != null ? fechaFormat.format(fechaTimestamp.toDate()) : "Fecha no disponible";
                Timestamp horaInicioTimestamp = doc.getTimestamp("Hora_Inicio");
                String horaInicio = horaInicioTimestamp != null ? tiempoFormat.format(horaInicioTimestamp.toDate()) : "Hora no disponible";
                String storagePath = doc.getString("FlyerUrl");

                // Obtener el usuario
                DocumentReference usuarioRef = doc.getDocumentReference("UsuarioID");
                if (usuarioRef != null) {
                    usuarioRef.get().addOnSuccessListener(userDoc -> {
                        String usuarioNombre = userDoc.getString("Nombre");

                        // Obtener la referencia de ubicación
                        DocumentReference lugarRef = doc.getDocumentReference("LugarID");
                        if (lugarRef != null) {
                            lugarRef.get().addOnSuccessListener(lugarDoc -> {
                                if (lugarDoc.exists()) {
                                    double latitud = lugarDoc.getDouble("Latitud");
                                    double longitud = lugarDoc.getDouble("Longitud");

                                    if (storagePath != null) {
                                        FirebaseStorage.getInstance().getReferenceFromUrl(storagePath).getDownloadUrl().addOnSuccessListener(uri -> {
                                            eventoList.add(new Evento(titulo, descripcion, fecha, horaInicio, uri.toString(), usuarioNombre, latitud, longitud));
                                            eventoAdapter.notifyDataSetChanged();
                                        }).addOnFailureListener(e -> Log.e("HomeFragment", "Error obtaining flyer image URL: " + e.getMessage()));
                                    } else {
                                        eventoList.add(new Evento(titulo, descripcion, fecha, horaInicio, null, usuarioNombre, latitud, longitud));
                                        eventoAdapter.notifyDataSetChanged();
                                    }
                                } else {
                                    Log.e("HomeFragment", "Lugar document does not exist");
                                }
                            }).addOnFailureListener(e -> Log.e("HomeFragment", "Error fetching location: " + e.getMessage()));
                        } else {
                            Log.e("HomeFragment", "Lugar reference is null");
                        }
                    }).addOnFailureListener(e -> Log.e("HomeFragment", "Error fetching user name from reference: " + e.getMessage()));
                } else {
                    Log.e("HomeFragment", "Usuario reference is null");
                }
            }
        }).addOnFailureListener(e -> Log.e("HomeFragment", "Error fetching events: " + e.getMessage()));
    }
}