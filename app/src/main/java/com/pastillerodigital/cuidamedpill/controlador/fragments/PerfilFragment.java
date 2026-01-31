package com.pastillerodigital.cuidamedpill.controlador.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.controlador.activities.WelcomeActivity;
import com.pastillerodigital.cuidamedpill.controlador.adapters.AsistidosAdapter;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnDataLoadedCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnOperationCallback;
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

    private View progressPerfil, layoutContenido;
    private android.widget.ImageView imgFotoPerfil;
    private TextView tvAlias, tvNombreUsr, tvSupervisando;
    private RecyclerView rvUsrsAsist;
    private Button btnAddAsist,btnEditarPerfil, btnCerrarSesion, btnEliminarCuenta;
    private LinearLayout layoutNotis;

    //Lógica
    private String uidSelf;
    private String uid;
    UsuarioDAO uDAO;
    private UsuarioEstandar usrSelf;
    private Modo modo;
    private boolean updateNeeded = false;


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

        btnEditarPerfil = view.findViewById(R.id.btnEditarPerfil);
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);
        btnEliminarCuenta = view.findViewById(R.id.btnEliminarCuenta);

        progressPerfil = view.findViewById(R.id.progressPerfil);
        layoutContenido = view.findViewById(R.id.layoutContenido);

        //Lógica
        uDAO = new UsuarioDAO();
        modo = Modo.ESTANDAR;
        tvSupervisando.setVisibility(View.GONE);

        lecturaArgumentos();
        setButtonListeners();
        mostrarCarga(); //muestra icono carga (se quitará cuando el usuario se cargue)

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

    private void setButtonListeners(){
        btnAddAsist.setOnClickListener(v -> {
            AddAndEditAsistidoFragment fragment = AddAndEditAsistidoFragment.newInstance(usrSelf.getId());

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentApp, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        btnEditarPerfil.setOnClickListener(v -> {

        });

        btnCerrarSesion.setOnClickListener(v -> {
            cerrarSesion();
            gotoWelcomeActivity();
        });

        btnEliminarCuenta.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle(Mensajes.PERF_BORRARCUENTA)
                    .setMessage(Mensajes.PERF_PREG_BORRARCUENTA)
                    .setPositiveButton(Mensajes.BASIC_ELIMINAR, (d, w) -> {
                        uDAO.delete((UsuarioEstandar) usrSelf, new OnDataLoadedCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean data) {
                                if(data){
                                    UiUtils.mostrarConfirmacion(requireActivity(), Mensajes.PERF_CONF_BORRARCUENTA);
                                    cerrarSesion();
                                    gotoWelcomeActivity();
                                }
                                else{
                                    UiUtils.mostrarNegConfirmacion(requireActivity(), Mensajes.PERF_NEG_BORRARCUENTA_TUTOR);
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                UiUtils.mostrarErrorYReiniciar(requireActivity());
                            }
                        });
                    })
                    .setNegativeButton(Mensajes.BASIC_CANCELAR, null)
                    .show();
        });
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

                    ocultarCarga();
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
            public void onEditarPerfil(UsuarioAsistido ua) {
                updateNeeded = true;
                AddAndEditAsistidoFragment fragment = AddAndEditAsistidoFragment.newInstance(uidSelf, ua.getId());

                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentApp, fragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onBorrarCuenta(UsuarioAsistido asistido) {
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle(Mensajes.PERF_BORRARCUENTA)
                        .setMessage(String.format(Mensajes.PERF_PREG_ASIST_BORRARCUENTA, asistido.getAliasU()))
                        .setPositiveButton(Mensajes.BASIC_SI, (dialog, which) -> {
                            new UsuarioDAO().delete((UsuarioAsistido) asistido, new OnOperationCallback() {
                                @Override
                                public void onSuccess() {
                                    //Quito lo correspondiente al objeto que se muestra para evitar llamada a base de datos
                                    List<UsuarioAsistido> listua = usrSelf.getUsrAsistidoAsig();
                                    listua.remove(asistido);
                                    List<String> lstrua = usrSelf.getIdUsrAsistAsig();
                                    lstrua.remove(asistido.getId());
                                    usrSelf.setIdUsrAsistAsig(lstrua);
                                    usrSelf.setUsrAsistidoAsig(listua);


                                    UiUtils.mostrarConfirmacion(requireActivity(), Mensajes.PERF_CONF_BORRARCUENTA);
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


    private void mostrarCarga() {
        progressPerfil.setVisibility(View.VISIBLE);
        layoutContenido.setVisibility(View.GONE);
    }

    private void ocultarCarga() {
        progressPerfil.setVisibility(View.GONE);
        layoutContenido.setVisibility(View.VISIBLE);
    }

    private void cerrarSesion(){
        SharedPreferences prefs = requireActivity().getSharedPreferences(Constantes.PERSIST_NOMBREARCHIVOPREF, requireContext().MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    private void gotoWelcomeActivity(){
        Intent intent = new Intent(requireActivity(), WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //para que no pueda volver atrás
        startActivity(intent);
        requireActivity().finish();
    }

    /**
     * Para que se actualicen las listas cuando vuelve a la interfaz
     */
    @Override
    public void onResume() {
        super.onResume();

        if (updateNeeded) {
            updateNeeded = false;
            mostrarCarga();
            cargarUsuario(uidSelf);
        }
    }
}
