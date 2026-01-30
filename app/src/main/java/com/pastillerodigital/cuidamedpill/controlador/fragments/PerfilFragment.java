package com.pastillerodigital.cuidamedpill.controlador.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.controlador.adapters.AsistidosAdapter;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnDataLoadedCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.UsuarioDAO;
import com.pastillerodigital.cuidamedpill.modelo.usuario.Usuario;
import com.pastillerodigital.cuidamedpill.modelo.usuario.UsuarioEstandar;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.UiUtils;

/**
 * Fragmento que se muestra al darle al icono de la persona. Los usuarios asistidos no tienen acceso a
 * esta pantalla.
 */
public class PerfilFragment extends Fragment {

    private android.widget.ImageView imgFotoPerfil;
    private TextView tvAlias, tvNombreUsr;
    private RecyclerView rvUsrsAsist;
    private Button btnAddAsist;
    private LinearLayout layoutNotis;

    private UsuarioEstandar usrSelf;


    public static PerfilFragment newInstance(String userIdSelf) {
        PerfilFragment fragment = new PerfilFragment();
        Bundle args = new Bundle();
        args.putString(Constantes.ARG_UIDSELF, userIdSelf);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Elementos del layout
        imgFotoPerfil = view.findViewById(R.id.imgFotoPerfil);
        tvAlias = view.findViewById(R.id.tvAlias);
        tvNombreUsr = view.findViewById(R.id.tvNombreUsuario);
        rvUsrsAsist = view.findViewById(R.id.rvPersonasAsistidas);
        btnAddAsist = view.findViewById(R.id.btnAddAsistido);
        layoutNotis = view.findViewById(R.id.layoutNotificaciones);

        String uidSelf = getArguments().getString(Constantes.ARG_UIDSELF);

        btnAddAsist.setOnClickListener(v -> {
            RegistroAsistidoFragment fragment = RegistroAsistidoFragment.newInstance(usrSelf.getId());

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentApp, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        cargarUsuario(uidSelf);
    }

    private void cargarUsuario(String uid) {
        UsuarioDAO dao = new UsuarioDAO();
        dao.getBasic(uid, new OnDataLoadedCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario usuario) {
                if (usuario instanceof UsuarioEstandar) {
                    usrSelf = (UsuarioEstandar) usuario;
                    tvAlias.setText(usrSelf.getAliasU());
                    tvNombreUsr.setText(usrSelf.getNombreUsuario());

                    String nombreDrawable = usrSelf.getFotoPerfil();
                    int resId = getResources().getIdentifier(nombreDrawable, Constantes.RES_TIPO, requireContext().getPackageName());
                    if(resId != 0){
                        imgFotoPerfil.setImageResource(resId);
                    } else {//por si el drawable no existe
                        imgFotoPerfil.setImageResource(R.drawable.usuario_fotoperfil_default);
                    }

                    setupRecyclerView();
                }
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
    }

    private void setupRecyclerView() {
        AsistidosAdapter adapter = new AsistidosAdapter(usrSelf.getUsrAsistidoAsig(), asistido -> {
            // todo lo que pase al supervisar
        });

        rvUsrsAsist.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUsrsAsist.setAdapter(adapter);
    }
}
