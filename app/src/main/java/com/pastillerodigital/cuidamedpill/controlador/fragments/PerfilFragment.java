package com.pastillerodigital.cuidamedpill.controlador.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.pastillerodigital.cuidamedpill.utils.Constantes;

public class PerfilFragment extends Fragment {


    public static PerfilFragment newInstance(String userIdSellf) {
        PerfilFragment fragment = new PerfilFragment();
        Bundle args = new Bundle();
        args.putString(Constantes.ARG_UIDSELF, userIdSellf);
        fragment.setArguments(args);
        return fragment;
    }
}
