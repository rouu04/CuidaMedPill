package com.pastillerodigital.cuidamedpill.controlador;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        borrarSesion();//todo borrar cuando esté log out
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    //todo borrar cuando ya esté puesto en el log out
    private void borrarSesion(){
        SharedPreferences prefs = getSharedPreferences(Constantes.PERSIST_NOMBREARCHIVOPREF, MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

}