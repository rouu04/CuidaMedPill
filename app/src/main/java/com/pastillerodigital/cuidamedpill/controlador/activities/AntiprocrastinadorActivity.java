package com.pastillerodigital.cuidamedpill.controlador.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

public class AntiprocrastinadorActivity extends AppCompatActivity {

    private TextView txtMensaje, txtTimer;
    private Button btnConfirmar, btnIgnorar;
    private CountDownTimer timer;
    private static final long DURACION_TIMER = 3 * 60 * 1000; // 3 minutos en milisegundos
    private String medId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_antiprocrastinador);

        txtMensaje = findViewById(R.id.txtMensaje);
        txtTimer = findViewById(R.id.txtTimer);
        btnConfirmar = findViewById(R.id.btnConfirmar);
        btnIgnorar = findViewById(R.id.btnIgnorar);

        // Obtenemos el nombre y el ID del medicamento desde el Intent
        String nombreMed = getIntent().getStringExtra("nombreMed");
        medId = getIntent().getStringExtra(Constantes.ARG_MEDID);

        txtMensaje.setText("Ve a tomar tu medicamento: " + nombreMed);

        iniciarTimer();

        btnConfirmar.setOnClickListener(v -> {
            // Usuario confirma que tomó el medicamento
            cancelarTimer();
            finish();
        });

        btnIgnorar.setOnClickListener(v -> {
            // Usuario decide ignorar, vuelve a la alarma
            cancelarTimer();
            volverAlarma();
        });
    }

    private void iniciarTimer() {
        timer = new CountDownTimer(DURACION_TIMER, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutos = millisUntilFinished / 60000;
                long segundos = (millisUntilFinished % 60000) / 1000;
                txtTimer.setText(String.format("%02d:%02d", minutos, segundos));
            }

            @Override
            public void onFinish() {
                // Timer terminado → volvemos a la alarma
                volverAlarma();
            }
        }.start();
    }

    private void cancelarTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void volverAlarma() {
        Intent intent = new Intent(this, AlarmaMedicacionActivity.class);
        intent.putExtra(Constantes.ARG_MEDID, medId);
        intent.putExtra(Constantes.ARG_ANTIPROCRASTINADOR, true);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelarTimer();
    }
}