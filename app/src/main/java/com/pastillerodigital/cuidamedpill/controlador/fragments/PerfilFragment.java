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
import com.pastillerodigital.cuidamedpill.modelo.enumerados.Modo;
import com.pastillerodigital.cuidamedpill.modelo.usuario.Usuario;
import com.pastillerodigital.cuidamedpill.modelo.usuario.UsuarioAsistido;
import com.pastillerodigital.cuidamedpill.modelo.usuario.UsuarioEstandar;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.Mensajes;
import com.pastillerodigital.cuidamedpill.utils.UiUtils;

import java.util.List;

/**
 * Fragmento que se muestra al darle al icono de la persona. Los usuarios asistidos no tienen acceso a
 * esta pantalla.
 */
public class PerfilFragment extends Fragment {

    private android.widget.ImageView imgFotoPerfil;
    private TextView tvAlias, tvNombreUsr, tvSupervisando;
    private RecyclerView rvUsrsAsist;
    private Button btnAddAsist;
    private LinearLayout layoutNotis;

    private String uidSelf;
    private String uid;

    private UsuarioEstandar usrSelf;
    private Modo modo;


    //CREACIONES DEL FRAGMENT

    /**
     * Será para el modo supervisión, donde userIdSelf observa los datos de userId
     * @param userIdSelf
     * @param userId
     * @return
     */
    public static PerfilFragment newInstance(String userIdSelf,String userId) {
        PerfilFragment fragment = new PerfilFragment();
        Bundle args = new Bundle();
        args.putString(Constantes.ARG_UIDSELF, userIdSelf);
        args.putString(Constantes.ARG_UID, userId);
        fragment.setArguments(args);
        return fragment;
    }

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
        tvSupervisando = view.findViewById(R.id.tvSupervisando);
        rvUsrsAsist = view.findViewById(R.id.rvPersonasAsistidas);
        btnAddAsist = view.findViewById(R.id.btnAddAsistido);
        layoutNotis = view.findViewById(R.id.layoutNotificaciones);

        modo = Modo.ESTANDAR;
        tvSupervisando.setVisibility(View.GONE);
        lecturaArgumentos();

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

    private void lecturaArgumentos(){
        if(getArguments() != null){
            uidSelf = getArguments().getString(Constantes.ARG_UIDSELF);
            uid = getArguments().getString(Constantes.ARG_UID);
            if(uid == null){
                uid = uidSelf;
            }
            else modo = Modo.SUPERVISOR;
        }
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
        AsistidosAdapter adapter = new AsistidosAdapter(usrSelf.getUsrAsistidoAsig(), new AsistidosAdapter.OnClickListener() {
            @Override
            public void onSupervisar(UsuarioAsistido asistido) {
                modo = Modo.SUPERVISOR;
                uid = asistido.getId();
                tvSupervisando.setText(String.format(Mensajes.PERF_ASIST_SUPERVISANDO, asistido.getAliasU()));
                tvSupervisando.setVisibility(View.VISIBLE);
            }

            @Override
            public void onDejarDeSupervisar() {
                modo = Modo.ESTANDAR;
                uid = uidSelf;
                tvSupervisando.setVisibility(View.GONE);
            }

            @Override
            public void onEditarPerfil(UsuarioAsistido asistido) {
                // TODO: abrir fragmento de edición del asistido
            }

            @Override
            public void onCerrarSesion(UsuarioAsistido asistido) {
                // TODO: cerrar sesión asistido
            }

            @Override
            public void onBorrarCuenta(UsuarioAsistido asistido) {
                //todo borrar cuenta
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle(Mensajes.PERF_BORRARCUENTA)
                        .setMessage(String.format(Mensajes.PERF_PREG_ASIST_BORRARCUENTA, asistido.getAliasU()))
                        .setPositiveButton(Mensajes.BASIC_SI, (dialog, which) -> {
                            new UsuarioDAO().delete(asistido.getId(), new com.pastillerodigital.cuidamedpill.modelo.dao.OnOperationCallback() {
                                @Override
                                public void onSuccess() {
                                    //todo
                                    setupRecyclerView();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    UiUtils.mostrarErrorYReiniciar(requireActivity());
                                }
                            });
                        })
                        .setNegativeButton(Mensajes.BASIC_NO, null)
                        .show();
            }

        });

        rvUsrsAsist.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUsrsAsist.setAdapter(adapter);
    }
}
