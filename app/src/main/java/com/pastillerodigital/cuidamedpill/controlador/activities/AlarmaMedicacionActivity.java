package com.pastillerodigital.cuidamedpill.controlador.activities;

import android.app.KeyguardManager;
import android.app.NotificationManager;
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
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
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
import com.pastillerodigital.cuidamedpill.utils.Utils;

import java.util.Calendar;
import java.util.List;

public class AlarmaMedicacionActivity extends AppCompatActivity {

    private TextView txtMed, txtFechaProgramada;
    private MaterialButton btnTomado, btnVoyAhora, btnNoTomado;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private ImageView imgMedicamento;

    private boolean antiprocrastinador;
    private Medicamento med;
    private MedicamentoDAO medDAO;
    private String uid;
    private Timestamp fechaProgramada, fechaIngesta;

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
        txtFechaProgramada = findViewById(R.id.txtFechaProgramadaAlarma);
        imgMedicamento = findViewById(R.id.imgMedicamentoAlarma);
        btnTomado = findViewById(R.id.btnTomado);
        btnVoyAhora = findViewById(R.id.btnVoyAhora);
        btnNoTomado = findViewById(R.id.btnNoTomado);

        desbloquearPantalla();

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

        Calendar ahora = Calendar.getInstance();
        fechaIngesta = new Timestamp(ahora.getTime());

        cargaMed(medId);
    }

    private void setButtonListeners() {

        btnTomado.setOnClickListener(v -> {
            callaAlarma();
            registrarIngesta();
        });

        btnVoyAhora.setOnClickListener(v -> {
            callaAlarma();
            if (antiprocrastinador) {
                // Abrir pantalla antiprocrastinador
                Intent intent = new Intent(
                        AlarmaMedicacionActivity.this,
                        AntiprocrastinadorActivity.class
                );
                intent.putExtra(Constantes.ARG_NOMBREMED, med.getNombreMed());
                intent.putExtra(Constantes.ARG_MEDID, med.getId());
                startActivity(intent);

                // Cerramos esta alarma, pero no matamos el proceso
                finish();
            } else {
                // Si no es antiprocrastinador, volver a home
                cerrarAlarmaYVolverHome();
            }
        });

        btnNoTomado.setOnClickListener(v -> {
            // Ignorar, se queda pendiente
            callaAlarma();
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
                fechaProgramada = getFechaProgramada();
                txtFechaProgramada.setText(Utils.timestampToString(fechaProgramada));
                UiUtils.setMedicamentoIcon(AlarmaMedicacionActivity.this, imgMedicamento, med.getTipoMed(), med.getColorSimb());
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(AlarmaMedicacionActivity.this);
            }
        });
    }

    private void registrarIngesta(){
        EstadoIngesta estado;
        long diffMinutos = (fechaIngesta.toDate().getTime() - fechaProgramada.toDate().getTime()) / 60000;
        if (diffMinutos <= Constantes.MINS_RETRASO) estado = EstadoIngesta.TOMADA;
        else estado = EstadoIngesta.RETRASO;

        IngestaDAO ingestaDAO = new IngestaDAO(uid, med.getId());

        ingestaDAO.getListBasic(new OnDataLoadedCallback<List<Ingesta>>() {
            @Override
            public void onSuccess(List<Ingesta> lista) {

                Ingesta existente = null;

                for (Ingesta ing : lista) {
                    if (ing.getFechaProgramada() != null &&
                            ing.getFechaProgramada().equals(fechaProgramada)) {

                        existente = ing;
                        break;
                    }
                }

                if (existente != null) {
                    existente.setFechaIngesta(fechaIngesta);
                    existente.setEstadoIngesta(estado);
                    existente.setEstadoIngestaStr(estado.toString());

                    final Ingesta ingFinal = existente;
                    ingestaDAO.edit(existente, new OnOperationCallback() {
                        @Override
                        public void onSuccess() {
                            if (med != null) {
                                med.ingestaTomada(ingFinal); //solo deja usar finales
                                medDAO.edit(med, new OnOperationCallback() {
                                    @Override
                                    public void onSuccess() {
                                        cerrarAlarmaYVolverHome();
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        UiUtils.mostrarErrorYReiniciar(AlarmaMedicacionActivity.this);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            UiUtils.mostrarErrorYReiniciar(AlarmaMedicacionActivity.this);
                        }
                    });

                } else {
                    //fallback
                    Ingesta nueva = new Ingesta(fechaProgramada, fechaIngesta, estado.toString(), med, "");

                    ingestaDAO.add(nueva, new OnOperationCallback() {
                        @Override
                        public void onSuccess() {
                            if (med != null){
                                med.ingestaTomada(nueva);
                                medDAO.edit(med, new OnOperationCallback() {
                                    @Override
                                    public void onSuccess() {
                                        cerrarAlarmaYVolverHome();
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        UiUtils.mostrarErrorYReiniciar(AlarmaMedicacionActivity.this);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            UiUtils.mostrarErrorYReiniciar(AlarmaMedicacionActivity.this);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(AlarmaMedicacionActivity.this);
            }
        });
    }

    private Timestamp getFechaProgramada(){
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
        return fechaProgramada;
    }


    //------------FUNCIONES ALARMA

    private void iniciarAlarma() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

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
            KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock(Constantes.KEYGUARDLOCK);
            keyguardLock.disableKeyguard();
        }
    }

    private void callaAlarma() {

        try {
            // 1. CANCELAR LA NOTIFICACIÓN DEL SISTEMA (EL "FANTASMA")
            if (med != null) {
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (nm != null) {
                    nm.cancel(med.getId().hashCode()); // Cancelamos el ID que generamos antes
                }
            }

            // 2. Detener vibración
            if (vibrator != null) {
                vibrator.cancel();
                vibrator = null;
            }

            // 3. Detener  MediaPlayer
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {}
    }

    private void cerrarAlarmaYVolverHome() {
        callaAlarma();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
    }

    /**
     * Para el sonido de la alarma si se cierra la actividad
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        callaAlarma();
    }

    @Override
    protected void onStart() {
        super.onStart();
        iniciarAlarma();
        iniciarVibracion();
    }
}