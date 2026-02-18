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


}
