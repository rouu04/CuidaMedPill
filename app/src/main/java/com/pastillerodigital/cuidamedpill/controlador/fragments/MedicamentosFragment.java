package com.pastillerodigital.cuidamedpill.controlador.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.pastillerodigital.cuidamedpill.modelo.enumerados.Modo;
import com.pastillerodigital.cuidamedpill.utils.Constantes;

public class MedicamentosFragment extends Fragment {

    public static MedicamentosFragment newInstance(String userIdSellf) {
        MedicamentosFragment fragment = new MedicamentosFragment();
        Bundle args = new Bundle();
        args.putString(Constantes.ARG_UIDSELF, userIdSellf);
        fragment.setArguments(args);
        return fragment;
    }

    public static MedicamentosFragment newInstance(String userIdSelf,String userId, Modo modo) {
        MedicamentosFragment fragment = new MedicamentosFragment();
        Bundle args = new Bundle();
        args.putString(Constantes.ARG_UIDSELF, userIdSelf);
        args.putString(Constantes.ARG_UID, userId);
        args.putString(Constantes.ARG_MODO, modo.toString());
        fragment.setArguments(args);
        return fragment;
    }
}
