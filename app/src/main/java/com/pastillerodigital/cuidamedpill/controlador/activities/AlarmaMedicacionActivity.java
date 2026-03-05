package com.pastillerodigital.cuidamedpill.controlador.activities;

import android.app.KeyguardManager;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.pastillerodigital.cuidamedpill.R;

public class AlarmaMedicacionActivity extends AppCompatActivity {

    private TextView txtMed;
    private Button btnTomado, btnVoyAhora, btnNoTomado;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;

    private boolean antiprocrastinador;
    private String nombreMed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Mostrar incluso con pantalla bloqueada
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );
        setContentView(R.layout.activity_alarma_medicacion);

        iniciarAlarma();
        desbloquearPantalla();
        iniciarVibracion();

        antiprocrastinador = getIntent().getBooleanExtra("antiprocrastinador", false);
        nombreMed = getIntent().getStringExtra("nombreMed");


        txtMed = findViewById(R.id.txtMedicamento);
        if (txtMed != null && nombreMed != null) {
            txtMed.setText(nombreMed);
        }

        btnTomado = findViewById(R.id.btnTomado);
        btnVoyAhora = findViewById(R.id.btnVoyAhora);
        btnNoTomado = findViewById(R.id.btnNoTomado);

        btnVoyAhora.setEnabled(antiprocrastinador);

        setButtonListeners();
    }

    private void setButtonListeners() {

        if (btnTomado != null) {
            btnTomado.setOnClickListener(v -> {
                callaAlarma();
                // TODO registrar ingesta tomada
                finish();
            });
        }

        if (btnVoyAhora != null) {
            btnVoyAhora.setOnClickListener(v -> {
                callaAlarma();
                if (antiprocrastinador) {

                    Intent intent = new Intent(
                            AlarmaMedicacionActivity.this,
                            AntiprocrastinadorActivity.class
                    );

                    intent.putExtra("nombreMed", nombreMed);

                    startActivity(intent);
                }
                finish();
            });
        }

        if (btnNoTomado != null) {
            btnNoTomado.setOnClickListener(v -> {
                callaAlarma();
                // TODO registrar olvido
                finish();
            });
        }
    }

    private void iniciarAlarma() {

        setVolumeControlStream(AudioManager.STREAM_ALARM);

        Uri alarmSound = Settings.System.DEFAULT_ALARM_ALERT_URI;
        if (alarmSound == null) alarmSound = Settings.System.DEFAULT_NOTIFICATION_URI;

        mediaPlayer = MediaPlayer.create(this, alarmSound);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    private void iniciarVibracion() {

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        if (vibrator != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(
                        VibrationEffect.createWaveform(
                                new long[]{0, 500, 500},
                                0
                        )
                );
            } else {
                vibrator.vibrate(new long[]{0, 500, 500}, 0);
            }
        }
    }

    private void desbloquearPantalla() {

        KeyguardManager keyguardManager =
                (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

        if (keyguardManager != null) {
            KeyguardManager.KeyguardLock keyguardLock =
                    keyguardManager.newKeyguardLock("MedicationAlarm");
            keyguardLock.disableKeyguard();
        }
    }

    private void callaAlarma() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    /**
     * Para el sonido de la alarma si se cierra la actividad
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        callaAlarma();
    }
}