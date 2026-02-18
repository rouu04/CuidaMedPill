package com.pastillerodigital.cuidamedpill.controlador.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.controlador.activities.MainActivity;
import com.pastillerodigital.cuidamedpill.controlador.activities.WelcomeActivity;
import com.pastillerodigital.cuidamedpill.controlador.adapters.AsistidosAdapter;
import com.pastillerodigital.cuidamedpill.controlador.adapters.FotoPerfilAdapter;
import com.pastillerodigital.cuidamedpill.controlador.fragments.extra.AddAndEditUsuarioFragment;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnDataLoadedCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnOperationCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.UsuarioDAO;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.Modo;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoUsuario;
import com.pastillerodigital.cuidamedpill.modelo.usuario.Usuario;
import com.pastillerodigital.cuidamedpill.modelo.usuario.UsuarioAsistido;
import com.pastillerodigital.cuidamedpill.modelo.usuario.UsuarioEstandar;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.Mensajes;
import com.pastillerodigital.cuidamedpill.utils.UiUtils;
import com.pastillerodigital.cuidamedpill.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
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
    private Button btnAddAsist,btnEditarPerfil, btnCerrarSesion, btnEliminarCuenta, btnConfirmAddAsist, btnRegAsist, btnCancelarAddAsist;
    private LinearLayout layoutNotis, layoutAddAsist;
    private TextInputEditText etUa, etPasswdAsist;
    private TextInputLayout layoutNombreUa, layoutPasswdUa;

    //Lógica
    private String uidSelf, uid;
    private UsuarioDAO uDAO;
    private UsuarioEstandar usrSelf;
    private Modo modo;
    private boolean updateNeeded = false;
    private int fotoPerfilSel;



    //CREACIONES DEL FRAGMENT

    /**
     * Será para el modo supervisión, donde userIdSelf observa los datos de userId
     * @param userIdSelf
     * @param userId
     * @return
     */
    public static PerfilFragment newInstance(String userIdSelf,String userId, Modo modo) {
        PerfilFragment fragment = new PerfilFragment();
        Bundle args = new Bundle();
        args.putString(Constantes.ARG_UIDSELF, userIdSelf);
        args.putString(Constantes.ARG_UID, userId);
        args.putString(Constantes.ARG_MODO, modo.toString());
        fragment.setArguments(args);
        return fragment;
    }

    public static PerfilFragment newInstance(String userIdSelf, Modo modo) {
        PerfilFragment fragment = new PerfilFragment();
        Bundle args = new Bundle();
        args.putString(Constantes.ARG_UIDSELF, userIdSelf);
        args.putString(Constantes.ARG_MODO, modo.toString());
        fragment.setArguments(args);
        return fragment;
    }

    //INICIALIZACIONES
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //ELEMENTOS DEL LAYOUT
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

        //Elementos de añadir / registrar usuario asistido
        layoutAddAsist = view.findViewById(R.id.layoutVincularAsistido);
        btnCancelarAddAsist = view.findViewById(R.id.btnCancelarAddAsistido);
        etUa = view.findViewById(R.id.etUsuarioAsistido);
        layoutNombreUa = view.findViewById(R.id.layoutNombreUa);
        etPasswdAsist = view.findViewById(R.id.etPasswordAsistido);
        layoutPasswdUa = view.findViewById(R.id.layoutPasswdUa);
        btnRegAsist = view.findViewById(R.id.btnRegAsist);
        btnConfirmAddAsist = view.findViewById(R.id.btnConfirmarAddAsistido);

        progressPerfil = view.findViewById(R.id.progressPerfil);
        layoutContenido = view.findViewById(R.id.layoutContenido);

        //Lógica
        uDAO = new UsuarioDAO();
        tvSupervisando.setVisibility(View.GONE);

        lecturaArgumentos();
        setButtonListeners();
        mostrarCarga(); //muestra icono carga (se quitará cuando el usuario se cargue)

        cargarUsuario(uidSelf);
    }


    //FUNCIONES BÁSICAS
    private void setButtonListeners(){
        btnAddAsist.setOnClickListener(v -> {
            layoutAddAsist.setVisibility(View.VISIBLE);
            btnAddAsist.setVisibility(View.GONE);

        });

        btnRegAsist.setOnClickListener(v -> {
            AddAndEditUsuarioFragment fragment = AddAndEditUsuarioFragment.newInstance(usrSelf.getId(), Modo.SUPERVISOR);

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentApp, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        btnConfirmAddAsist.setOnClickListener(v ->{
            addAsist();
        });

        btnEditarPerfil.setOnClickListener(v -> {
            AddAndEditUsuarioFragment fragment =
                    AddAndEditUsuarioFragment.newInstance(
                            uidSelf,
                            Modo.ESTANDAR
                    );

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentApp, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        btnCerrarSesion.setOnClickListener(v -> {
            cerrarSesion();
            gotoWelcomeActivity();
        });

        btnEliminarCuenta.setOnClickListener(v -> {
            borrarCuenta();
        });

        btnCancelarAddAsist.setOnClickListener(v -> {
            layoutAddAsist.setVisibility(View.GONE);
            btnAddAsist.setVisibility(View.VISIBLE);

            // Se limpian los campos y errores
            etUa.setText("");
            etPasswdAsist.setText("");
            layoutNombreUa.setError(null);
            layoutPasswdUa.setError(null);
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

                    //Si el usuario estaba supervisando a un asistido, hay que cargar el asistido para poder
                    //indicar a quién supervisa
                    if(modo == Modo.SUPERVISOR){
                        cargarModoSupervisor();
                    }
                    else setVistaModo("");

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

    /**
     * Carga el usuario asistido que estamos supervisando para poder enseñar a quien supervisa
     */
    private void cargarModoSupervisor(){
        uDAO.getBasic(uid, new OnDataLoadedCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario usuario) {
                if (usuario != null) {
                    setVistaModo(usuario.getAliasU());
                }
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
    }

    private void setupRecyclerView() {
        List<UsuarioAsistido> listaOrdenada = new ArrayList<>(usrSelf.getUsrAsistidoAsig());
        listaOrdenada.sort((u1, u2) -> u1.getAliasU().compareToIgnoreCase(u2.getAliasU()));

        AsistidosAdapter adapter = new AsistidosAdapter(listaOrdenada,uid, new AsistidosAdapter.OnClickListener() {
            @Override
            public void onSupervisar(UsuarioAsistido asistido) {
                modo = Modo.SUPERVISOR;
                uid = asistido.getId();
                //Indica al main que tiene que actualizar el estado de la aplicación para que si vamos a
                //otra pestaña se mantenga
                MainActivity ma = (MainActivity) getActivity();
                ma.actualizarSesionModo(modo, uid, uidSelf);
                setVistaModo(asistido.getAliasU());
            }

            @Override
            public void onDejarDeSupervisar() {
                modo = Modo.ESTANDAR;
                uid = uidSelf;
                MainActivity ma = (MainActivity) getActivity();
                ma.actualizarSesionModo(modo, uid, uidSelf);
                setVistaModo("");
            }

            @Override
            public void onEditarPerfil(UsuarioAsistido ua) {
                updateNeeded = true;
                AddAndEditUsuarioFragment fragment = AddAndEditUsuarioFragment.newInstance(uidSelf, ua.getId(), Modo.SUPERVISOR);

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

            /**
             * Usuario actual dejará de ser tutor de este usuario asistido
             * @param ua
             */
            @Override
            public void onDesvincular(UsuarioAsistido ua) {
                if(ua.getIdUsrTutoresAsig().size() == 1){
                    UiUtils.mostrarNegConfirmacion(requireActivity(), Mensajes.PERF_NEG_DESVINCULAR);
                    return;
                }
                uDAO.desvincular(ua.getId(), usrSelf.getId(), new OnOperationCallback() {
                    @Override
                    public void onSuccess() {
                        UiUtils.mostrarConfirmacion(requireActivity(), Mensajes.PERF_CONF_DESVINCULAR);
                        recargarDatos();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        UiUtils.mostrarErrorYReiniciar(requireActivity());
                    }
                });
            }
        });

        rvUsrsAsist.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUsrsAsist.setAdapter(adapter);
    }

    //--------FUNCIONES AUXILIARES CON GRANDES FUNCIONALIDADES

    private void addAsist(){
        String nombreU = etUa.getText().toString().trim();
        String passwd = etPasswdAsist.getText().toString().trim();

        if (nombreU.isEmpty()) {
            layoutNombreUa.setError(Mensajes.REG_VAL_PUTNOMBREUSR);
            return;
        }
        if(passwd.isEmpty()){
            layoutPasswdUa.setError(Mensajes.REG_VAL_PUTPASSW);
            return;
        }

        //Hay que comprobar que el nombre de usuario existe
        uDAO.getBasicWithParameter(Constantes.USUARIO_NOMBREUSUARIO, nombreU, new OnDataLoadedCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario ua) {
                if(ua != null){
                    if(!ua.getTipoUsuario().equals(TipoUsuario.ASISTIDO)){
                        layoutNombreUa.setError(Mensajes.PERF_ERROR_UNOUA);
                        return;
                    }
                    String hashIntroducido = Utils.hashPassword(passwd, ua.getSalt());
                    if(!hashIntroducido.equals(ua.getPasswordHash())){
                        layoutPasswdUa.setError(Mensajes.ERROR_USUARIO_CONTRASEÑAINCORRECTA);
                    }

                    //Existe, actualizamos las listas de ambos
                    uDAO.vincularAsistATutor(ua.getId(), uidSelf, new OnOperationCallback() {
                        @Override
                        public void onSuccess() {
                            layoutAddAsist.setVisibility(View.GONE);
                            btnAddAsist.setVisibility(View.VISIBLE);
                            etUa.setText("");
                            etPasswdAsist.setText("");
                            layoutNombreUa.setError(null);
                            layoutPasswdUa.setError(null);

                            recargarDatos();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            UiUtils.mostrarErrorYReiniciar(requireActivity());
                        }
                    });
                }
                else{
                    layoutNombreUa.setError(Mensajes.ERROR_USUARIO_NOEXISTE);
                    return;
                }
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
    }

    private void borrarCuenta(){
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
    }



    //---------FUNCIONES AUXILIARES
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

    /**
     * Enseña el texto de supervisando o no en función del modo
     * @param uaAlias
     */
    private void setVistaModo(String uaAlias){
        if(modo == Modo.SUPERVISOR){
            tvSupervisando.setText(String.format(Mensajes.PERF_ASIST_SUPERVISANDO, uaAlias));
            tvSupervisando.setVisibility(View.VISIBLE);
        }
        else{
            tvSupervisando.setVisibility(View.GONE);
        }
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
     * Llamado al volver a entrar en la interfaz. Si necesitan actualizarse los datos, se actualizan
     * para mantener coherencia en la vista. Si está supervisando a alguien se actualiza
     */
    @Override
    public void onResume() {
        super.onResume();

        if (updateNeeded) {
            updateNeeded = false;
            mostrarCarga();
            cargarUsuario(uidSelf);
        }

        //Si está supervisando a alguien se actualiza la vista correspondiente.
        MainActivity ma = (MainActivity) requireActivity();
        modo = ma.getModo();
        uidSelf = ma.getUidSelf();

        if(modo == Modo.SUPERVISOR){
            uid = ma.getUid();
            uDAO.getBasic(uid, new OnDataLoadedCallback<Usuario>() {
                @Override
                public void onSuccess(Usuario usuario) {
                    if (usuario != null) {
                        setVistaModo(usuario.getAliasU());
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    UiUtils.mostrarErrorYReiniciar(requireActivity());
                }
            });
        }
        else setVistaModo("");
    }

    /**
     * Función necesaria cuando haya que recargar cuando el usuario no vaya a viajar entre pantallas
     */
    private void recargarDatos(){
        mostrarCarga();
        cargarUsuario(uidSelf);
    }

    private void mostrarSelectorAvatares() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(Mensajes.REG_PUTFOTO);

        GridView gridView = new GridView(requireContext());
        gridView.setNumColumns(3);
        gridView.setAdapter(new FotoPerfilAdapter(requireContext(), UiUtils.fotosPerfil));

        builder.setView(gridView);
        AlertDialog dialog = builder.create();

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            fotoPerfilSel = UiUtils.fotosPerfil[position];
            imgFotoPerfil.setImageResource(fotoPerfilSel);
            dialog.dismiss();
        });

        dialog.show();
    }
}
