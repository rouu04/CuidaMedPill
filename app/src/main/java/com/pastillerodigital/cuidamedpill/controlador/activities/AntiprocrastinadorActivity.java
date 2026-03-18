package com.pastillerodigital.cuidamedpill.controlador.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.Timestamp;
import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.modelo.dao.IngestaDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.MedicamentoDAO;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnDataLoadedCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnOperationCallback;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.EstadoIngesta;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoNotificacion;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Ingesta;
import com.pastillerodigital.cuidamedpill.modelo.medicamento.Medicamento;
import com.pastillerodigital.cuidamedpill.modelo.notificaciones.medicacion.NotificationHelper;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.UiUtils;
import com.pastillerodigital.cuidamedpill.utils.Utils;

import java.util.Calendar;
import java.util.List;

public class AntiprocrastinadorActivity extends AppCompatActivity {

    private TextView txtTimer, txtNombreMed;
    private MaterialButton btnConfirmar, btnIgnorar;
    private ImageView imgMedicamento;
    private CountDownTimer timer;
    private CircularProgressIndicator circularTimer;

    private static final long DURACION_TIMER = 3 * 60 * 1000; // 3 minutos en milisegundos
    private String medId, uid;
    private Timestamp fechaProgramada, fechaIngesta;
    private Medicamento med;
    private MedicamentoDAO medDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_antiprocrastinador);

        txtTimer = findViewById(R.id.txtTimer);
        txtNombreMed = findViewById(R.id.txtNombreMedAntiproc);
        circularTimer = findViewById(R.id.circularTimer);
        circularTimer.setMax(180);
        circularTimer.setProgress(180);
        imgMedicamento = findViewById(R.id.imgMedicamentoAntiproc);
        btnConfirmar = findViewById(R.id.btnConfirmar);
        btnIgnorar = findViewById(R.id.btnIgnorar);


        leerArgumentosYConsec();
        setButtonListeners();
        iniciarTimer();
    }

    private void leerArgumentosYConsec(){
        medId = getIntent().getStringExtra(Constantes.ARG_MEDID);

        SharedPreferences prefs = getSharedPreferences(Constantes.PERSIST_NOMBREARCHIVOPREF, Context.MODE_PRIVATE);
        String uidSelf = prefs.getString(Constantes.PERSIST_KEYUSERSELFID, null);
        uid = prefs.getString(Constantes.PERSIST_KEYUSERID, uidSelf);

        medDAO = new MedicamentoDAO(uid);

        Calendar ahora = Calendar.getInstance();
        fechaIngesta = new Timestamp(ahora.getTime());

        cargaMed(medId);
    }

    private void setButtonListeners(){
        btnConfirmar.setOnClickListener(v -> {
            registrarIngesta();
            cancelarTimer();
            volverHome();
            finish();
        });

        btnIgnorar.setOnClickListener(v -> {
            cancelarTimer();
            volverHome();
            finish();
        });
    }

    private void cargaMed(String idMed){
        medDAO.getBasic(idMed, new OnDataLoadedCallback<Medicamento>() {
            @Override
            public void onSuccess(Medicamento data) {
                med = data;
                txtNombreMed.setText(med.getNombreMed());
                fechaProgramada = getFechaProgramada();

                UiUtils.setMedicamentoIcon(AntiprocrastinadorActivity.this, imgMedicamento, med.getTipoMed(), med.getColorSimb());
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(AntiprocrastinadorActivity.this);
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

    private void iniciarTimer() {
        timer = new CountDownTimer(DURACION_TIMER, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long segundosTotales = millisUntilFinished / 1000;
                long minutos = segundosTotales / 60;
                long segundos = segundosTotales % 60;
                txtTimer.setText(String.format("%02d:%02d", minutos, segundos));
                circularTimer.setProgress((int) segundosTotales);
            }

            @Override
            public void onFinish() {
                // Timer terminado, se vuelve a la alarma
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
        // Llamar a la notificación para que vuelva a sonar
        NotificationHelper.mostrarNotificacion(
                this,
                "Hora de tu medicamento",
                "Toca tomar tu " + med.getNombreMed(),
                TipoNotificacion.ALARMA,
                true,    // antiprocrastinador
                medId,
                med.getTipoMedStr(),
                med.getColorSimb()
        );

        // Abrir la Activity de alarma (opcional, para que el usuario la vea)
        Intent intent = new Intent(this, AlarmaMedicacionActivity.class);
        intent.putExtra(Constantes.ARG_MEDID, medId);
        intent.putExtra(Constantes.ARG_ANTIPROCRASTINADOR, true);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelarTimer();
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
                            }
                            UiUtils.mostrarConfirmacion(AntiprocrastinadorActivity.this, "Ingesta registrada");
                        }

                        @Override
                        public void onFailure(Exception e) {
                            UiUtils.mostrarErrorYReiniciar(AntiprocrastinadorActivity.this);
                        }
                    });

                } else {
                    //fallback
                    Ingesta nueva = new Ingesta(fechaProgramada, fechaIngesta, estado.toString(), med, "");

                    ingestaDAO.add(nueva, new OnOperationCallback() {
                        @Override
                        public void onSuccess() {
                            if (med != null) med.ingestaTomada(nueva);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            UiUtils.mostrarErrorYReiniciar(AntiprocrastinadorActivity.this);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(AntiprocrastinadorActivity.this);
            }
        });
    }

    private void volverHome(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}