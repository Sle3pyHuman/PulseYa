package com.examen.pulseya;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.examen.pulseya.helper.BaseDatosHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private BaseDatosHelper dbHelper;
    private String usuarioRol;

    private String UsuarioID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        UsuarioID = getIntent().getStringExtra("userId");
        Log.d("PublicarEventoActivity", "UsuarioID: " + UsuarioID);

        if (UsuarioID == null) {
            Log.e("PublicarEventoActivity", "UsuarioID is null");
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            UsuarioID = sharedPreferences.getString("userId", null);
            Log.d("PublicarEventoActivity", "UsuarioID: " + UsuarioID);
        } else {
            Log.d("PublicarEventoActivity", "UsuarioID: " + UsuarioID);
        }

        // Initialize NavigationView after the content view is set
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Make sure navigationView is not null before accessing it
        if (navigationView != null) {
            // Configurar el Toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            // Conseguir el rol del usuario
            dbHelper = new BaseDatosHelper(this);
            usuarioRol = getUsuarioRol();

            // Log the role of the user
            Log.d(TAG, "Usuario Rol: " + usuarioRol);

            // Ocultar la opción "Publicar Eventos" si el usuario no es "Publicista"
            MenuItem publicarEventos = navigationView.getMenu().findItem(R.id.nav_publicar);
            if (!"Publicista".equals(usuarioRol)) {
                publicarEventos.setVisible(false);
                Log.d(TAG, "Publicar Eventos option hidden for non-Publicista role");
            }

            // Configurar el DrawerLayout
            setupNavigationDrawer();

            // Configurar el BottomNavigationView
            setupBottomNavigation();

            // Cargar el HomeFragment por defecto
            if (savedInstanceState == null) {
                HomeFragment homeFragment = new HomeFragment();
                Bundle bundle = new Bundle();
                bundle.putString("UsuarioID", UsuarioID); // Pasar UsuarioID al fragmento
                homeFragment.setArguments(bundle);

                // Reemplazar el contenedor con el fragmento configurado
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, homeFragment)
                        .commit();

                Log.d(TAG, "HomeFragment loaded by default with UsuarioID: " + UsuarioID);
            }
        } else {
            Log.e(TAG, "NavigationView is null. Make sure the layout is correctly loaded.");
        }
    }

    private void setupNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            Log.d(TAG, "Item seleccionado: " + item.getTitle() + " ID: " + item.getItemId());

            int itemId = item.getItemId();

            if (itemId == R.id.nav_ver_eventos) {
                Log.d(TAG, "Cargando EventosGuardadosActivity");
                Intent intent = new Intent(HomeActivity.this, EventosGuardadosActivity.class);
                startActivity(intent);
            } else if (itemId == R.id.nav_publicar) {
                if ("Publicista".equals(usuarioRol)) {
                    Log.d(TAG, "Cargando PublicarEventoActivity");
                    Intent intent = new Intent(HomeActivity.this, PublicarEventoActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Acceso denegado: Solo Publicistas pueden publicar eventos.", Toast.LENGTH_SHORT).show();
                }
            } else if (itemId == R.id.nav_filtrar) {
                Log.d(TAG, "Cargando HomeFragment con UsuarioID");

                // Crear el fragmento y pasar UsuarioID en un Bundle
                UserFragment userFragment = new UserFragment();
                Bundle bundle = new Bundle();
                bundle.putString("UsuarioID", UsuarioID); // Pasar UsuarioID al fragmento
                userFragment.setArguments(bundle);

                // Reemplazar el contenedor con el HomeFragment configurado
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, userFragment)
                        .commit();

                Log.d(TAG, "HomeFragment cargado con UsuarioID: " + UsuarioID);
            } else if (itemId == R.id.nav_logout) {
                Log.d(TAG, "Cargando Logout");
                logout();
            } else {
                Log.w(TAG, "Opción no reconocida: " + item.getTitle());
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }


    private void menejoNavigationItemSeleccionado(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_ver_eventos) {
            Log.d(TAG, "Cargando EventosGuardadosActivity");
            Intent intent = new Intent(HomeActivity.this, EventosGuardadosActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.nav_publicar) {
            if ("Publicista".equals(usuarioRol)) {
                Log.d(TAG, "Cargando PublicarEventoActivity");
                Intent intent = new Intent(HomeActivity.this, PublicarEventoActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Acceso denegado: Solo Publicistas pueden publicar eventos.", Toast.LENGTH_SHORT).show();
            }
        } else if (itemId == R.id.nav_filtrar) {
            Log.d(TAG, "Cargando FiltrarEventosFragment");
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new UserFragment()).commit();
        } else if (itemId == R.id.nav_logout) {
            Log.d(TAG, "Cargando Logout");
            logout();
        } else {
            Log.w(TAG, "Opción no reconocida: " + item.getTitle());
        }

        drawerLayout.closeDrawers();
    }

    private String getUsuarioRol() {
        Cursor cursor = dbHelper.getUsuario();
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex("tipo_cuenta");
            if (columnIndex != -1) { // Segura columna existe
                String rol = cursor.getString(columnIndex);
                cursor.close();
                Log.d(TAG, "Rol del usuario: " + rol);
                return rol;
            }
            cursor.close();
        }
        Log.d(TAG, "ERROR al obtener el rol del usuario");
        return null;
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_home) {
                // Load HomeFragment
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
                return true;
            } else if (item.getItemId() == R.id.bottom_map) {
                // Load MapaFragment
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MapaFragment()).commit();
                return true;
            } else {
                return false;
            }
        });
    }

    private void logout() {
        dbHelper.eliminarUsuario();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
        finish();
    }

    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "PulseYaCanal";
            String description = "Canal para notificaciones programadas";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("mi_canal_id", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}