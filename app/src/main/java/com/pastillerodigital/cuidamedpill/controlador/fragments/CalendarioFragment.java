package com.pastillerodigital.cuidamedpill.controlador.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.pastillerodigital.cuidamedpill.modelo.enumerados.Modo;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

public class CalendarioFragment extends Fragment {

    public static CalendarioFragment newInstance(String userIdSelf, Modo modo) {
        CalendarioFragment fragment = new CalendarioFragment();
        Bundle args = new Bundle();
        args.putString(Constantes.ARG_UIDSELF, userIdSelf);
        args.putString(Constantes.ARG_MODO, modo.toString());
        fragment.setArguments(args);
        return fragment;
    }

    public static CalendarioFragment newInstance(String userIdSelf,String userId, Modo modo) {
        CalendarioFragment fragment = new CalendarioFragment();
        Bundle args = new Bundle();
        args.putString(Constantes.ARG_UIDSELF, userIdSelf);
        args.putString(Constantes.ARG_UID, userId);
        args.putString(Constantes.ARG_MODO, modo.toString());
        fragment.setArguments(args);
        return fragment;
    }
}
