package com.examen.pulseya;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.Timestamp;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventoList = new ArrayList<>();
        eventoAdapter = new EventoAdapter(getContext(), eventoList); // Pass context here
        recyclerView.setAdapter(eventoAdapter);

        // obtener eventos
        obtenerEventos();

        return view;
    }

    private void obtenerEventos() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        SimpleDateFormat fechaFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat tiempoFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        db.collection("Evento").get().addOnSuccessListener(queryDocumentSnapshots -> {
                    eventoList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {

                        String titulo = doc.getString("Nombre_Evento");
                        String descripcion = doc.getString("Descripcion");

                        Timestamp fechaTimestamp = doc.getTimestamp("Fecha_Evento");
                        String fecha = fechaTimestamp != null ? fechaFormat.format(fechaTimestamp.toDate()) : "Fecha no disponible";

                        Timestamp horaInicioTimestamp = doc.getTimestamp("Hora_Inicio");
                        String horaInicio = horaInicioTimestamp != null ? tiempoFormat.format(horaInicioTimestamp.toDate()) : "Hora no disponible";

                        // manejo de URL de Firebase Storage
                        String storagePath = doc.getString("FlyerUrl"); // Path in Firebase Storage
                        if (storagePath != null) {
                            FirebaseStorage.getInstance().getReferenceFromUrl(storagePath).getDownloadUrl().addOnSuccessListener(uri -> {
                                        // agrega evento con la URL resuelta
                                        eventoList.add(new Evento(titulo, descripcion, fecha, horaInicio, uri.toString()));
                                        eventoAdapter.notifyDataSetChanged();
                                    })
                                    .addOnFailureListener(e -> Log.e("HomeFragment", "Error a obtener imgagen URL", e));
                        } else {
                            // Add the event without an image
                            eventoList.add(new Evento(titulo, descripcion, fecha, horaInicio, null));
                            eventoAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("HomeFragment", "Error obteniendo eventos", e));
    }
}