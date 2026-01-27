package com.pastillerodigital.cuidamedpill.controlador.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.pastillerodigital.cuidamedpill.utils.Constantes;

public class HomeFragment extends Fragment {

    public static HomeFragment newInstance(String userIdSellf) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(Constantes.ARG_UIDSELF, userIdSellf);
        fragment.setArguments(args);
        return fragment;
    }
}
