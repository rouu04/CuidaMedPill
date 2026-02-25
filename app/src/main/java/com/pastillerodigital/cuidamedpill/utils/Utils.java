package com.pastillerodigital.cuidamedpill.utils;

import static android.content.Context.MODE_PRIVATE;

import android.util.Base64;

import com.google.firebase.Timestamp;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utils {

    /**
     //Para guardar la contraseña de forma segura
     */
    public static String generarSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.decode(salt, Base64.NO_WRAP));
            byte[] hashed = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hashed, Base64.NO_WRAP);
        } catch (Exception e) {
            throw new RuntimeException("Error al hashear contraseña", e);
        }
    }

    public static String calendarToString(Calendar calendar) {
        if (calendar == null) return "";

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    public static Timestamp stringToTimestamp(String fechaStr) {
        if (fechaStr == null || fechaStr.isEmpty()) return null;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = sdf.parse(fechaStr);
            return new Timestamp(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String timestampToString(Timestamp timestamp) {
        if (timestamp == null) return "";
        Date date = timestamp.toDate(); // convierte Timestamp a Date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()); // formato día/mes/año
        return sdf.format(date);
    }

    public static String timestampToTextoSigToma(Timestamp timestamp){
        if(timestamp == null) return "";

        Calendar ahora = Calendar.getInstance();
        Calendar fecha = Calendar.getInstance();
        fecha.setTime(timestamp.toDate());

        // Hora
        SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String horaStr = sdfHora.format(fecha.getTime());

        // Normalizar fechas
        Calendar hoy = (Calendar) ahora.clone();
        limpiarHora(hoy);
        Calendar fechaSoloDia = (Calendar) fecha.clone();
        limpiarHora(fechaSoloDia);

        long diffMillis = fechaSoloDia.getTimeInMillis() - hoy.getTimeInMillis();
        int diffDays = (int)(diffMillis / (1000 * 60 * 60 * 24));

        // SWITCH
        switch (diffDays){
            case 0:
                return horaStr + " " + Constantes.HORARIO_SIGINGESTA_TEXT_HOY;

            case 1:
                return horaStr + " " + Constantes.HORARIO_SIGINGESTA_TEXT_MANANA;

            case 7:
                return horaStr + " " + Constantes.HORARIO_SIGINGESTA_TEXT_UNA_SEMANA;

            case 14:
                return horaStr + " " + Constantes.HORARIO_SIGINGESTA_TEXT_DOS_SEMANAS;
        }

        // Rango días
        if(diffDays > 1 && diffDays <= 21){
            return horaStr + " " +
                    String.format(Locale.getDefault(),
                            Constantes.HORARIO_SIGINGESTA_TEXT_DIAS,
                            diffDays);
        }

        // Mes siguiente aproximado
        if(diffDays >= 28 && diffDays <= 60){
            return horaStr + " " + Constantes.HORARIO_SIGINGESTA_TEXT_MES_SIGUIENTE;
        }

        // Fallback fecha normal
        SimpleDateFormat sdfFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return horaStr + " " + sdfFecha.format(fecha.getTime());
    }

    public static void limpiarHora(Calendar cal){
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    public static Timestamp construirTimestamp(Calendar base, String hora){
        String[] partes = hora.split(":");

        Calendar c = (Calendar) base.clone();
        c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(partes[0]));
        c.set(Calendar.MINUTE, Integer.parseInt(partes[1]));
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return new Timestamp(c.getTime());
    }

    public static String timestampAHoraHome(Timestamp timestamp) {
        if (timestamp == null) return "";

        Calendar ahora = Calendar.getInstance();
        Calendar fecha = Calendar.getInstance();
        fecha.setTime(timestamp.toDate());

        // Formateo de hora
        SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String horaStr = sdfHora.format(fecha.getTime());

        // Solo comparo fechas
        Calendar hoy = (Calendar) ahora.clone();
        limpiarHora(hoy);
        Calendar fechaSoloDia = (Calendar) fecha.clone();
        limpiarHora(fechaSoloDia);

        long diffMillis = fechaSoloDia.getTimeInMillis() - hoy.getTimeInMillis();
        int diffDays = (int) (diffMillis / (1000 * 60 * 60 * 24));

        if (diffDays == 0) {
            return "Hoy " + horaStr;
        } else if (diffDays == -1) {
            return "Ayer " + horaStr;
        } else {// Si no es hoy ni ayer, muestro fecha completa
            SimpleDateFormat sdfFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdfFecha.format(fecha.getTime()) + " " + horaStr;
        }
    }
}
