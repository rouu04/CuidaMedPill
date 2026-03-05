package com.pastillerodigital.cuidamedpill.controlador.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.pastillerodigital.cuidamedpill.R;

public class AntiprocrastinadorActivity extends AppCompatActivity {

    private TextView txtMensaje;
    private Button btnConfirmar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_antiprocrastinador);

        String nombreMed = getIntent().getStringExtra("nombreMed");

        txtMensaje = findViewById(R.id.txtMensaje);
        btnConfirmar = findViewById(R.id.btnConfirmar);

        txtMensaje.setText("Ve a tomar tu medicamento: " + nombreMed);

        btnConfirmar.setOnClickListener(v -> {
            // registrar que realmente lo tomó
            finish();
        });
    }
}