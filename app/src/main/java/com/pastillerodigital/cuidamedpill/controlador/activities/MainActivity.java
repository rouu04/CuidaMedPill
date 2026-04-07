package com.pastillerodigital.cuidamedpill.controlador.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.controlador.fragments.CalendarioFragment;
import com.pastillerodigital.cuidamedpill.controlador.fragments.HomeFragment;
import com.pastillerodigital.cuidamedpill.controlador.fragments.MedicamentosFragment;
import com.pastillerodigital.cuidamedpill.controlador.fragments.PerfilFragment;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.Modo;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoUsuario;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.MedicamentoRealTimeManager;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.avisos.AvisoManager;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.medicacion.NotificationHelper;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.medicacion.RecordatorioManager;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView navInferior;
    private TipoUsuario tipoUsuario;

    private String uidSelf; //id del usuario identidad
    private String uid; //id del usuario que estamos visualizando (puede ser el self)

    private HomeFragment homeFragment;
    private MedicamentosFragment medicamentosFragment;
    private CalendarioFragment calendarioFragment;
    private PerfilFragment perfilFragment;
    private Modo modo;
    private MedicamentoRealTimeManager realtimeManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        AndroidThreeTen.init(this);
        setContentView(R.layout.activity_main);
        //borrarSesion();//todo borrar cuando esté log out

        View mainView = findViewById(R.id.main);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            // Aplicamos el margen superior para la barra de estado y el inferior para la de navegación
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        NotificationHelper.crearCanales(this); //canales de notificaciones
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        navInferior = findViewById(R.id.bottomNavigation);
        navInferior.setItemIconTintList(null);

        /*
        Menu menuColor = navInferior.getMenu();
        MenuItem medicamentoItem = menuColor.findItem(R.id.nav_medicamentos);
        medicamentoItem.setIcon(R.drawable.ic_pastilla_capsula); // sin tint

         */

        // Cargamos sesión de sharedPreferences
        SharedPreferences prefs = getSharedPreferences(Constantes.PERSIST_NOMBREARCHIVOPREF, MODE_PRIVATE);

        boolean sesionActiva = prefs.getBoolean(Constantes.PERSIST_KEYSESIONACTIVA, false);

        if (!sesionActiva) { //Es la primera vez que entramos o se nos ha borrado la sesión
            Intent intent = new Intent(this, WelcomeActivity.class);
            //Flags para que no pueda volver atrás y ahora welcome tenga el control del flujo
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        uidSelf = prefs.getString(Constantes.PERSIST_KEYUSERSELFID, null);
        uid = prefs.getString(Constantes.PERSIST_KEYUSERID, uidSelf);
        String modoStr = prefs.getString(Constantes.PERSIST_KEYMODO, Modo.ESTANDAR.toString());
        modo = Modo.modoFromString(modoStr);
        initializeFragmentsModo(uidSelf, uid, modo);

        if(modo.equals(Modo.ASISTIDO)){ //los usuarios asistidos no tienen acceso al perfil
            Menu menu = navInferior.getMenu();
            MenuItem perfilItem = menu.findItem(R.id.nav_perfil);
            perfilItem.setVisible(false);

            realtimeManager = new MedicamentoRealTimeManager();
            realtimeManager.iniciarListener(getApplicationContext(), uid);

            RecordatorioManager.sincronizarRecordatorios(this, uid);
            AvisoManager.sincronizarAvisos(this, uid);
        }

        if (savedInstanceState == null) {
            replaceFragment(homeFragment);
        }



        // Listener para cambiar fragments
        navInferior.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                replaceFragment(homeFragment);
            } else if (itemId == R.id.nav_medicamentos) {
                replaceFragment(medicamentosFragment);
            } else if (itemId == R.id.nav_calendario) {
                replaceFragment(calendarioFragment);
            } else if (itemId == R.id.nav_perfil) {
                replaceFragment(perfilFragment);
            }
            return true;
        });

        // Cargar fragment por defecto
        if(savedInstanceState == null){
            navInferior.setSelectedItemId(R.id.nav_home);
        }

    }

    /**
     * Función que cambia el fragment que se ve por el que llega en el parámetro
     * @param fragment
     */
    private void replaceFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentApp, fragment);
            transaction.commit();
        }
    }

    //todo borrar cuando ya esté puesto en el log out
    private void borrarSesion(){
        SharedPreferences prefs = getSharedPreferences(Constantes.PERSIST_NOMBREARCHIVOPREF, MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    /**
     * Métod*o que permite supervisar /dejar de supervisar a un asistido manteniendo el modo en la app.
     * @param nuevoModo
     * @param nuevoUid
     * @param nuevoUidSelf
     */
    public void actualizarSesionModo(Modo nuevoModo, String nuevoUid, String nuevoUidSelf) {
        this.modo = nuevoModo;
        this.uid = nuevoUid;
        this.uidSelf = nuevoUidSelf;

        SharedPreferences prefs = getSharedPreferences(Constantes.PERSIST_NOMBREARCHIVOPREF, MODE_PRIVATE);

        if(modo == Modo.SUPERVISOR){
            prefs.edit()
                    .putString(Constantes.PERSIST_KEYMODO, nuevoModo.toString())
                    .putString(Constantes.PERSIST_KEYUSERID, nuevoUid)
                    .putString(Constantes.PERSIST_KEYUSERSELFID, nuevoUidSelf)
                    .apply();

            initializeFragmentsModo(nuevoUidSelf, uid, modo);
        }
        else if(modo == Modo.ESTANDAR){
            prefs.edit()
                    .putString(Constantes.PERSIST_KEYMODO, nuevoModo.toString())
                    .putString(Constantes.PERSIST_KEYUSERID, nuevoUidSelf)
                    .putString(Constantes.PERSIST_KEYUSERSELFID, nuevoUidSelf)
                    .apply();
            initializeFragmentsModo(nuevoUidSelf, nuevoUidSelf, modo);
        }
    }

    /**
     * Crea la instancia de los fragments en función del modo para mantener el estado en la aplicación
     * @param uidSelf
     * @param uid
     * @param modo
     */
    private void initializeFragmentsModo(String uidSelf, String uid, Modo modo){
        if(modo ==  Modo.SUPERVISOR){
            homeFragment = HomeFragment.newInstance(uidSelf, uid, modo);
            medicamentosFragment = MedicamentosFragment.newInstance(uidSelf, uid, modo);
            calendarioFragment = CalendarioFragment.newInstance(uidSelf, uid, modo);
            perfilFragment = PerfilFragment.newInstance(uidSelf, uid, modo);
        }
        else{
            homeFragment = HomeFragment.newInstance(uidSelf, modo);
            medicamentosFragment = MedicamentosFragment.newInstance(uidSelf, modo);
            calendarioFragment = CalendarioFragment.newInstance(uidSelf, modo);
            perfilFragment = PerfilFragment.newInstance(uidSelf, modo);
        }

    }

    public String getUid() {
        return uid;
    }

    public String getUidSelf() {
        return uidSelf;
    }

    public Modo getModo() {
        return modo;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (realtimeManager != null) {
            realtimeManager.detenerListener();
        }
    }

}