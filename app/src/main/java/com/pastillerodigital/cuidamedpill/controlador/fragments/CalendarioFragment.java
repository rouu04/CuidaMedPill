package com.pastillerodigital.cuidamedpill.controlador.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.pastillerodigital.cuidamedpill.utils.Constantes;

public class CalendarioFragment extends Fragment {

    public static CalendarioFragment newInstance(String userIdSellf) {
        CalendarioFragment fragment = new CalendarioFragment();
        Bundle args = new Bundle();
        args.putString(Constantes.ARG_UIDSELF, userIdSellf);
        fragment.setArguments(args);
        return fragment;
    }
}
