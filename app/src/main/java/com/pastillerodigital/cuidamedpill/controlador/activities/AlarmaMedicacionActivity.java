package com.pastillerodigital.cuidamedpill.controlador.activities;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.firebase.Timestamp;
import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.modelo.dao.IngestaDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.MedicamentoDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnDataLoadedCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnOperationCallback;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.EstadoIngesta;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Ingesta;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.UiUtils;

import java.util.Calendar;
import java.util.List;

public class AlarmaMedicacionActivity extends AppCompatActivity {

    private TextView txtMed;
    private Button btnTomado, btnVoyAhora, btnNoTomado;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;

    private boolean antiprocrastinador;
    private Medicamento med;
    private MedicamentoDAO medDAO;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Mostrar incluso con pantalla bloqueada
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        setContentView(R.layout.activity_alarma_medicacion);

        txtMed = findViewById(R.id.txtMedicamento);
        btnTomado = findViewById(R.id.btnTomado);
        btnVoyAhora = findViewById(R.id.btnVoyAhora);
        btnNoTomado = findViewById(R.id.btnNoTomado);

        iniciarAlarma();
        desbloquearPantalla();
        iniciarVibracion();

        leerArgumentosYConsec();
        setButtonListeners();
    }

    private void leerArgumentosYConsec(){
        antiprocrastinador = getIntent().getBooleanExtra(Constantes.ARG_ANTIPROCRASTINADOR, false);
        String medId = getIntent().getStringExtra(Constantes.ARG_MEDID);

        SharedPreferences prefs = getSharedPreferences(Constantes.PERSIST_NOMBREARCHIVOPREF, Context.MODE_PRIVATE);
        String uidSelf = prefs.getString(Constantes.PERSIST_KEYUSERSELFID, null);
        uid = prefs.getString(Constantes.PERSIST_KEYUSERID, uidSelf);
        medDAO = new MedicamentoDAO(uid);

        cargaMed(medId);
    }

    private void setButtonListeners() {

        btnTomado.setOnClickListener(v -> {
            callaAlarma();
            registrarIngesta();
            cerrarAlarmaYVolverHome();
        });

        btnVoyAhora.setOnClickListener(v -> {
            callaAlarma();
            if (antiprocrastinador) {

                Intent intent = new Intent(
                        AlarmaMedicacionActivity.this,
                        AntiprocrastinadorActivity.class
                );

                //intent.putExtra("nombreMed", nombreMed); todo poner id med

                startActivity(intent);
            }
            cerrarAlarmaYVolverHome();
        });

        btnNoTomado.setOnClickListener(v -> {
            //Ignorar, se queda pendiente
            cerrarAlarmaYVolverHome();
        });
    }

    //
    private void cargaMed(String idMed){
        medDAO.getBasic(idMed, new OnDataLoadedCallback<Medicamento>() {
            @Override
            public void onSuccess(Medicamento data) {
                med = data;
                txtMed.setText(med.getNombreMed());
                btnVoyAhora.setEnabled(antiprocrastinador);
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(AlarmaMedicacionActivity.this);
            }
        });
    }

    private void registrarIngesta(){
        Calendar ahora = Calendar.getInstance();
        Timestamp fechaIngesta = new Timestamp(ahora.getTime());

        Timestamp fechaProgramada = null;
        List<Timestamp> horasHoy = med.getFechaHorasDia(Calendar.getInstance());
        if (horasHoy != null && !horasHoy.isEmpty()) { //selecciono la ultima hora programada
            for (Timestamp ts : horasHoy) {
                if (!ts.toDate().before(fechaIngesta.toDate())) { // ts >= fechaIngesta
                    fechaProgramada = ts;
                    break;
                }
            }
            // Si no encontramos ninguna hora >= ahora, usamos la última del día
            if (fechaProgramada == null) {
                fechaProgramada = horasHoy.get(horasHoy.size() - 1);
            }
        }

        // Determinar estado de ingesta según retraso
        EstadoIngesta estado;
        if (fechaProgramada == null) {
            estado = EstadoIngesta.NO_PROGRAMADA;
        } else {
            long diffMinutos = (fechaIngesta.toDate().getTime() - fechaProgramada.toDate().getTime()) / 60000;
            if (diffMinutos <= Constantes.MINS_RETRASO) estado = EstadoIngesta.TOMADA;
            else estado = EstadoIngesta.RETRASO;
        }

        Ingesta ingesta = new Ingesta(fechaProgramada, fechaIngesta, estado.toString(), med, "");

        // Guardar en la base de datos
        IngestaDAO ingestaDAO = new IngestaDAO(uid, med.getId());
        ingestaDAO.add(ingesta, new OnOperationCallback() {
            @Override
            public void onSuccess() {
                // Actualizar ingestas locales del medicamento
                if (med != null) med.ingestaTomada(ingesta);

                // Mensaje corto opcional
                UiUtils.mostrarConfirmacion(AlarmaMedicacionActivity.this, "Ingesta registrada");
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(AlarmaMedicacionActivity.this);
            }
        });
    }


    //------------FUNCIONES ALARMA

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

    private void cerrarAlarmaYVolverHome() {
        callaAlarma();

        // 1. Preparamos el intent para ir a MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // 2. Cerramos esta actividad de alarma
        finish();

        // 3. ¡FUERZA EL CIERRE!
        // Esto mata el proceso actual de la app. La próxima vez que el usuario
        // abra la app, Android se verá obligado a crear una instancia totalmente nueva.
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
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