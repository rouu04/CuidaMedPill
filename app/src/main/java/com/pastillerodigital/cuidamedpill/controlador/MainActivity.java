package com.pastillerodigital.cuidamedpill.controlador;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.controlador.fragments.CalendarioFragment;
import com.pastillerodigital.cuidamedpill.controlador.fragments.HomeFragment;
import com.pastillerodigital.cuidamedpill.controlador.fragments.MedicamentosFragment;
import com.pastillerodigital.cuidamedpill.controlador.fragments.PerfilFragment;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoUsuario;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView navInferior;
    private TipoUsuario tipoUsuario;

    private String uidSelf; //id del usuario

    private HomeFragment homeFragment;
    private MedicamentosFragment medicamentosFragment;
    private CalendarioFragment calendarioFragment;
    private PerfilFragment perfilFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        //borrarSesion();//todo borrar cuando esté log out

        navInferior = findViewById(R.id.bottomNavigation);
        navInferior.setItemIconTintList(null);


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

        uidSelf = prefs.getString(Constantes.PERSIST_KEYUSERID, null);
        String tipoStr = prefs.getString(Constantes.PERSIST_KEYTIPOUSR, TipoUsuario.ESTANDAR.name());
        tipoUsuario = TipoUsuario.tipoUsrFromString(tipoStr);

        homeFragment = HomeFragment.newInstance(uidSelf);
        medicamentosFragment = MedicamentosFragment.newInstance(uidSelf);
        calendarioFragment = CalendarioFragment.newInstance(uidSelf);
        perfilFragment = PerfilFragment.newInstance(uidSelf);

        if(tipoUsuario == TipoUsuario.ASISTIDO){ //los usuarios asistidos no tienen acceso al perfil
            Menu menu = navInferior.getMenu();
            MenuItem perfilItem = menu.findItem(R.id.nav_perfil);
            perfilItem.setVisible(false);
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

}