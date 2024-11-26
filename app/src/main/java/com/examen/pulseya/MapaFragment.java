    package com.examen.pulseya;

    import android.Manifest;
    import android.content.pm.PackageManager;
    import android.location.Address;
    import android.location.Geocoder;
    import android.location.Location;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.core.app.ActivityCompat;
    import androidx.fragment.app.Fragment;

    import com.google.android.gms.maps.CameraUpdateFactory;
    import com.google.android.gms.maps.GoogleMap;
    import com.google.android.gms.maps.OnMapReadyCallback;
    import com.google.android.gms.maps.SupportMapFragment;
    import com.google.android.gms.maps.model.LatLng;
    import com.google.android.gms.maps.model.MapStyleOptions;
    import com.google.android.gms.maps.model.MarkerOptions;
    import com.google.android.gms.location.FusedLocationProviderClient;
    import com.google.android.gms.location.LocationServices;
    import com.google.android.gms.tasks.OnSuccessListener;
    import com.google.firebase.firestore.DocumentReference;
    import com.google.firebase.firestore.DocumentSnapshot;
    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.firestore.QuerySnapshot;

    import java.io.IOException;
    import java.util.List;
    import java.util.Locale;

    public class MapaFragment extends Fragment implements OnMapReadyCallback {
        private static final String TAG = "MapaFragment"; // Tag para los logs
        private GoogleMap mMap;
        private FusedLocationProviderClient fusedLocationClient;
        private FirebaseFirestore db;
        private Geocoder geocoder;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            db = FirebaseFirestore.getInstance();
            geocoder = new Geocoder(getContext(), Locale.getDefault());
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_mapa, container, false);

            SupportMapFragment mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().replace(R.id.map_container, mapFragment).commit();

            mapFragment.getMapAsync(this);

            return view;
        }

        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            mMap = googleMap;

            // Configurar el estilo del mapa
            try {
                boolean success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.estilo_mapa));
                if (!success) {
                    Log.e(TAG, "No se pudo cargar el estilo del mapa.");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al cargar el estilo del mapa: " + e.getMessage());
            }

            // habilitar ubicación si se concedieron los permisos
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);

                // obtener la ubicación actual y agregar un marcador
                fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                                }
                            }
                        });
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }

            // marcar los eventos en el mapa
            obtenerEventos();
        }

        private void obtenerEventos() {
            db.collection("Evento").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    QuerySnapshot eventos = task.getResult();
                    if (eventos != null) {
                        Log.d(TAG, "Encontrados: " + eventos.size() + " eventos.");
                        for (DocumentSnapshot evento : eventos) {
                            Log.d(TAG, "Evento data: " + evento.getData());

                            String eventoName = evento.getString("Nombre_Evento");
                            String eventDescripcion = evento.getString("Descripcion");

                            Object lugarIdObject = evento.get("LugarID");

                            if (lugarIdObject instanceof DocumentReference) {
                                DocumentReference lugarRef = (DocumentReference) lugarIdObject;
                                String lugarID = lugarRef.getId();
                                Log.d(TAG, "Lugar ID: " + lugarID);
                                obtenerLugar(eventoName, eventDescripcion, lugarID);
                            } else if (lugarIdObject instanceof String) {
                                // Si LugarID es un String, puedes manejarlo de forma diferente (por ejemplo, logs o ignorar).
                                Log.w(TAG, "LugarID no es un DocumentReference, es un String: " + lugarIdObject);
                            } else {
                                Log.w(TAG, "LugarID no es válido o está ausente en el evento.");
                            }
                        }
                    }
                } else {
                    Log.e(TAG, "Error obteniendo eventos: " + task.getException());
                }
            });
        }

        private void obtenerLugar(String eventoName, String eventDescripcion, String lugarID) {
            db.collection("Lugar").document(lugarID).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot placeDoc = task.getResult();
                    if (placeDoc != null && placeDoc.exists()) {
                        // Try to get latitude and longitude
                        double latitude = placeDoc.contains("Latitud") ? placeDoc.getDouble("Latitud") : 0.0;
                        double longitude = placeDoc.contains("Longitud") ? placeDoc.getDouble("Longitud") : 0.0;
                        String address = placeDoc.getString("Direccion");

                        if (latitude == 0.0 && longitude == 0.0 && address == null) {
                            Log.w(TAG, "LugarID: " + lugarID + " no tiene coordenadas ni dirección disponible.");
                        } else if (latitude == 0.0 && longitude == 0.0 && address != null) {
                            Log.w(TAG, "LugarID: " + lugarID + " tiene dirección pero no coordenadas: " + address);
                        }

                        if (latitude == 0.0 && longitude == 0.0) {
                            // Use Geocoder if coordinates are missing and address is available
                            if (address != null && !address.isEmpty()) {
                                try {
                                    List<Address> addresses = geocoder.getFromLocationName(address, 1);
                                    if (addresses != null && !addresses.isEmpty()) {
                                        Address location = addresses.get(0);
                                        latitude = location.getLatitude();
                                        longitude = location.getLongitude();
                                    } else {
                                        Log.w(TAG, "No se encontraron coordenadas para la dirección: " + address);
                                    }
                                } catch (IOException e) {
                                    Log.e(TAG, "Error con Geocoder: " + e.getMessage());
                                }
                            } else {
                                Log.w(TAG, "LugarID: " + lugarID + " no tiene coordenadas ni dirección.");
                            }
                        }

                        if (latitude != 0.0 && longitude != 0.0) {
                            // Add the marker to the map
                            LatLng placeLocation = new LatLng(latitude, longitude);
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(placeLocation)
                                    .title(eventoName)
                                    .snippet(eventDescripcion);

                            mMap.addMarker(markerOptions);
                            Log.d(TAG, "Marcador agregado para evento: " + eventoName + " en ubicación: " + placeLocation);
                        } else {
                            Log.w(TAG, "Coordenadas inválidas para LugarID: " + lugarID);
                        }
                    } else {
                        Log.w(TAG, "Lugar no encontrado para LugarID: " + lugarID);
                    }
                } else {
                    Log.e(TAG, "Error obteniendo datos del lugar: ", task.getException());
                }
            });
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            if (requestCode == 1) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onMapReady(mMap);
                } else {
                    Log.e("MapaFragment", "permiso ubicacion denegado");
                }
            }
        }

        public void showEventLocation(double latitud, double longitud) {
            if (mMap != null) {
                LatLng eventLocation = new LatLng(latitud, longitud);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, 15));
            }
        }
    }