package com.pastillerodigital.cuidamedpill.controlador.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.controlador.adapters.FotoPerfilAdapter;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnDataLoadedCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnOperationCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.UsuarioDAO;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoUsuario;
import com.pastillerodigital.cuidamedpill.modelo.usuario.UsuarioAsistido;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.Mensajes;
import com.pastillerodigital.cuidamedpill.utils.UiUtils;
import com.pastillerodigital.cuidamedpill.utils.Utils;

public class RegistroAsistidoFragment extends Fragment {

    // Elementos interfaz
    private LinearLayout formLayout;
    private TextInputLayout layoutAlias, layoutUsername, layoutPassword, layoutConfirmPassword;
    private TextInputEditText edtAlias, edtUsername, edtPassword, edtConfirmPassword;
    private ImageView imgUserPhoto;
    private MaterialButton btnGuardar;
    private CircularProgressIndicator progressIndicator;

    // Lógica
    private UsuarioDAO usuarioDAO;
    private int fotoPerfilSel;
    private String uidTutor;

    public static RegistroAsistidoFragment newInstance(String uidTutor) {
        RegistroAsistidoFragment fragment = new RegistroAsistidoFragment();
        Bundle args = new Bundle();
        args.putString(Constantes.ARG_UIDSELF, uidTutor);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registro_asistido, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        uidTutor = getArguments() != null? getArguments().getString(Constantes.ARG_UIDSELF): null;

        usuarioDAO = new UsuarioDAO();

        // Inicializamos elementos de la interfaz
        formLayout = view.findViewById(R.id.formLayout);
        layoutAlias = view.findViewById(R.id.layoutAlias);
        layoutUsername = view.findViewById(R.id.layoutUsername);
        layoutPassword = view.findViewById(R.id.layoutPassword);
        layoutConfirmPassword = view.findViewById(R.id.layoutConfirmPassword);

        edtAlias = view.findViewById(R.id.edtAlias);
        edtUsername = view.findViewById(R.id.edtUsername);
        edtPassword = view.findViewById(R.id.edtPassword);
        edtConfirmPassword = view.findViewById(R.id.edtConfirmPassword);

        imgUserPhoto = view.findViewById(R.id.imgUserPhoto);
        btnGuardar = view.findViewById(R.id.btnRegister);
        progressIndicator = view.findViewById(R.id.progressIndicator);

        //Para poder ocultar y ver la contraseña
        UiUtils.setupPasswordToggle(layoutPassword, edtPassword, requireContext());
        UiUtils.setupPasswordToggle(layoutConfirmPassword, edtConfirmPassword, requireContext());

        // Avatar por defecto
        fotoPerfilSel = R.drawable.usuario_fotoperfil_default;
        imgUserPhoto.setImageResource(fotoPerfilSel);

        imgUserPhoto.setOnClickListener(v -> mostrarSelectorAvatares());
        btnGuardar.setOnClickListener(v -> registrarAsistido());
    }

    private void registrarAsistido() {
        UiUtils.limpiarErroresLayouts(formLayout);

        String alias = edtAlias.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString();
        String confirm = edtConfirmPassword.getText().toString();

        boolean error = false;

        if (TextUtils.isEmpty(alias)) {
            layoutAlias.setError(Mensajes.REG_VAL_PUTALIAS);
            error = true;
        }

        if (TextUtils.isEmpty(username)) {
            layoutUsername.setError(Mensajes.REG_VAL_PUTNOMBREUSR);
            error = true;
        }

        if (TextUtils.isEmpty(password)) {
            layoutPassword.setError(Mensajes.REG_VAL_PUTPASSW);
            error = true;
        }

        if (!password.equals(confirm)) {
            layoutConfirmPassword.setError(Mensajes.REG_VAL_PASSWDNOCOINCIDEN);
            error = true;
        }

        if (error) return;

        progressIndicator.setVisibility(View.VISIBLE);

        String salt = Utils.generarSalt();
        String hash = Utils.hashPassword(password, salt);

        UsuarioAsistido asistido = new UsuarioAsistido();
        asistido.setAliasU(alias);
        asistido.setNombreUsuario(username);
        asistido.setPasswordHash(hash);
        asistido.setSalt(salt);

        String nombreDrawable = getResources().getResourceEntryName(fotoPerfilSel); // obtiene el nombre
        asistido.setFotoPerfil(nombreDrawable);

        usuarioDAO.getIdWithParameter(Constantes.USUARIO_NOMBREUSUARIO,username,new OnDataLoadedCallback<String>() {
            @Override
            public void onSuccess(String data) {
                if (data != null) {
                    progressIndicator.setVisibility(View.GONE);
                    layoutUsername.setError(Mensajes.ERROR_USUARIO_EXISTE);
                    return;
                }

                usuarioDAO.add(asistido, uidTutor, new OnOperationCallback() {
                    @Override
                    public void onSuccess() {
                        //todo habrá que actualizar el usuario tutor para que lo muestre en la lista
                        progressIndicator.setVisibility(View.GONE);
                        requireActivity()
                                .getSupportFragmentManager()
                                .popBackStack();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        progressIndicator.setVisibility(View.GONE);
                        UiUtils.mostrarErrorYReiniciar(requireActivity());
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                progressIndicator.setVisibility(View.GONE);
                UiUtils.mostrarErrorYReiniciar(requireActivity());
            }
        });
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
            imgUserPhoto.setImageResource(fotoPerfilSel);
            dialog.dismiss();
        });

        dialog.show();
    }

}
