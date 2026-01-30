package com.pastillerodigital.cuidamedpill.controlador;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.controlador.adapters.FotoPerfilAdapter;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnDataLoadedCallback;
import com.pastillerodigital.cuidamedpill.modelo.usuario.Usuario;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnOperationCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.UsuarioDAO;
import com.pastillerodigital.cuidamedpill.modelo.enumerados.TipoUsuario;
import com.pastillerodigital.cuidamedpill.modelo.usuario.UsuarioAsistido;
import com.pastillerodigital.cuidamedpill.modelo.usuario.UsuarioEstandar;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.Mensajes;
import com.pastillerodigital.cuidamedpill.utils.UiUtils;
import com.pastillerodigital.cuidamedpill.utils.Utils;

import java.util.Arrays;

public class RegistroActivity extends AppCompatActivity {

    //Elementos del layout
    private LinearLayout formLayout;
    private TextInputLayout layoutAlias, layoutUsername, layoutPassword, layoutConfirmPassword, layoutTutorUsername, layoutTutorPassword;
    private TextInputEditText edtAlias, edtUsername, edtPassword, edtConfirmPassword, edtTutorUsername, edtTutorPassword;
    private TextView tvTipousrSel, tvTipousrErr;
    private android.widget.ImageView imgUserPhoto;
    private MaterialButton btnRegister, btnTipoUsuario;
    private CircularProgressIndicator progressIndicator;

    //Elementos lógica
    private UsuarioDAO usuarioDAO;
    private int fotoPerfilSel; //foto de perfil seleccionada
    private TipoUsuario tipoUsrSel = null; // selecciontipousr

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        borrarSesion();//todo borrar cuando esté echo el log out
        setContentView(R.layout.activity_registro);

        //Inicialización de componentes de diseño
        formLayout = findViewById(R.id.formLayout);
        layoutAlias = findViewById(R.id.layoutAlias);
        layoutUsername = findViewById(R.id.layoutUsername);
        layoutPassword = findViewById(R.id.layoutPassword);
        layoutConfirmPassword = findViewById(R.id.layoutConfirmPassword);
        layoutTutorUsername = findViewById(R.id.layoutTutorUsername);
        layoutTutorPassword = findViewById(R.id.layoutTutorPassword);


        edtAlias = findViewById(R.id.edtAlias);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        edtTutorUsername = findViewById(R.id.edtTutorUsername);
        edtTutorPassword = findViewById(R.id.edtTutorPassword);

        imgUserPhoto = findViewById(R.id.imgUserPhoto);
        tvTipousrSel = findViewById(R.id.tvTipoUsuarioSeleccionado);
        tvTipousrErr = findViewById(R.id.tvTTipoUsrError);
        btnTipoUsuario = findViewById(R.id.btnTipoUsuario);
        btnRegister = findViewById(R.id.btnRegister);
        progressIndicator = findViewById(R.id.progressIndicator);

        //Lógica
        usuarioDAO = new UsuarioDAO();

        //Contraseñas que se pueden ocultar y mostrar
        UiUtils.setupPasswordToggle(layoutPassword, edtPassword, this);
        UiUtils.setupPasswordToggle(layoutConfirmPassword, edtConfirmPassword, this);
        UiUtils.setupPasswordToggle(layoutTutorPassword, edtTutorPassword, this);

        btnTipoUsuario.setOnClickListener(v -> {
            // Creamos un array de nombres de los tipos
            String[] tipos = Arrays.stream(TipoUsuario.values())
                    .map(Enum::name)
                    .toArray(String[]::new);

            // Mostramos diálogo de selección
            new MaterialAlertDialogBuilder(this)
                    .setTitle(Mensajes.REG_PUTTIPOUSR)
                    .setItems(tipos, (dialog, which) -> {
                        tipoUsrSel = TipoUsuario.values()[which];
                        tvTipousrSel.setText(
                                getString(R.string.text_tipousr_sel, tipoUsrSel.name())
                        );
                        if(tipoUsrSel != null){
                            tvTipousrErr.setVisibility(View.GONE);
                        }

                        // Los usuarios asistidos necesitan más elementos
                        if(tipoUsrSel == TipoUsuario.ASISTIDO){
                            layoutTutorUsername.setVisibility(View.VISIBLE);
                            layoutTutorPassword.setVisibility(View.VISIBLE);
                        } else { //necesario por si cambia de opción
                            layoutTutorUsername.setVisibility(View.GONE);
                            layoutTutorPassword.setVisibility(View.GONE);
                        }
                    })
                    .show();
        });

        // Selección de foto
        // Set avatar por defecto
        fotoPerfilSel = R.drawable.usuario_fotoperfil_default;
        imgUserPhoto.setImageResource(fotoPerfilSel);

        imgUserPhoto.setOnClickListener(v -> mostrarSelectorAvatares());
        btnRegister.setOnClickListener(v -> registrarUsuario());
    }


    private void registrarUsuario(){
        // Limpiamos errores
        UiUtils.limpiarErroresLayouts(formLayout);

        String alias = edtAlias.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString();
        String confirmPassword = edtConfirmPassword.getText().toString();
        String tipoUsuario;

        boolean error = false;
        //Validaciones
        if(TextUtils.isEmpty(alias)){
            layoutAlias.setError(Mensajes.REG_VAL_PUTALIAS);
            error = true;
        }
        if(TextUtils.isEmpty(username)){
            layoutUsername.setError(Mensajes.REG_VAL_PUTNOMBREUSR);
            error = true;
        }
        if(TextUtils.isEmpty(password)){
            layoutPassword.setError(Mensajes.REG_VAL_PUTPASSW);
            error = true;
        }
        if(!password.equals(confirmPassword)){
            layoutConfirmPassword.setError(Mensajes.REG_VAL_PASSWDNOCOINCIDEN);
            error = true;
        }
        if(tipoUsrSel == null){
            tvTipousrErr.setVisibility(View.VISIBLE);
            error = true;
        }
        if(error) return;

        tipoUsuario = tipoUsrSel.toString();
        progressIndicator.setVisibility(View.VISIBLE);
        // Hash de la contraseña
        String salt = Utils.generarSalt();
        String hash = Utils.hashPassword(password, salt);

        //Creamos el usuario
        Usuario u;
        if (TipoUsuario.tipoUsrFromString(tipoUsuario) == TipoUsuario.ASISTIDO) {
            u = new UsuarioAsistido();
        } else {
            u = new UsuarioEstandar();
        }
        u.setAliasU(alias);
        u.setNombreUsuario(username);
        u.setPasswordHash(hash);
        u.setSalt(salt);
        u.setTipoUsuarioStr(tipoUsuario);
        u.setTipoUsuario(TipoUsuario.tipoUsrFromString(tipoUsuario));

        String nombreDrawable = getResources().getResourceEntryName(fotoPerfilSel); // obtiene el nombre
        u.setFotoPerfil(nombreDrawable);

        //Primero hay que comprobar que no haya ningún usuario con ese nombre
        usuarioDAO.getIdWithParameter(Constantes.USUARIO_NOMBREUSUARIO, u.getNombreUsuario(), new OnDataLoadedCallback<String>() {
            @Override
            public void onSuccess(String data) {
                progressIndicator.setVisibility(View.GONE);
                if(data != null){ //nombre de usuario ya existe
                    layoutUsername.setError(Mensajes.ERROR_USUARIO_EXISTE);
                }
                else{
                    if(u.getTipoUsuario().equals(TipoUsuario.ASISTIDO)){
                        registroAsistido((UsuarioAsistido) u);
                    }
                    else{
                        addUsuarioEstandar((UsuarioEstandar) u);
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                progressIndicator.setVisibility(View.GONE);
                UiUtils.mostrarErrorYReiniciar(RegistroActivity.this);
            }
        });

    }

    /**
     * Registra un usuario asistido
     * @param u
     */
    private void registroAsistido(UsuarioAsistido u){
        //Si es un usuario asistido, necesitará definir un usuario tutor
        String tutorUsername = edtTutorUsername.getText().toString().trim();
        String tutorPassword = edtTutorPassword.getText().toString();

        if (TextUtils.isEmpty(tutorUsername)) {
            layoutTutorUsername.setError(Mensajes.REG_VAL_PUTUSUARIOTUTOR);
            return;
        }

        if (TextUtils.isEmpty(tutorPassword)) {
            layoutTutorPassword.setError(Mensajes.REG_VAL_PUTUSUARIOTUTORPASSWD);
            return;
        }

        progressIndicator.setVisibility(View.VISIBLE);

        usuarioDAO.getBasicWithParameter(Constantes.USUARIO_NOMBREUSUARIO, tutorUsername, new OnDataLoadedCallback<Usuario>() {
                    @Override
                    public void onSuccess(Usuario tutor) {
                        if (tutor == null || tutor.getTipoUsuario() != TipoUsuario.ESTANDAR) {
                            progressIndicator.setVisibility(View.GONE);
                            layoutTutorUsername.setError(Mensajes.ERROR_TUTORNOVALIDO);
                            return;
                        }

                        // Verificación de la contraseña
                        String hashInput = Utils.hashPassword(
                                tutorPassword,
                                tutor.getSalt()
                        );

                        if (!hashInput.equals(tutor.getPasswordHash())) {
                            progressIndicator.setVisibility(View.GONE);
                            layoutTutorPassword.setError(Mensajes.ERROR_USUARIO_CONTRASEÑAINCORRECTA);
                            return;
                        }

                        addAsistido(u, tutor.getId());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        progressIndicator.setVisibility(View.GONE);
                        UiUtils.mostrarErrorYReiniciar(RegistroActivity.this);
                    }
                }
        );
    }


    /**
     * Registra un usuario estándar
     * @param u
     */
    private void addUsuarioEstandar(UsuarioEstandar u) {
        usuarioDAO.add(u, new OnOperationCallback() {
            @Override
            public void onSuccess() {
                progressIndicator.setVisibility(View.GONE);
                guardarSesion(u); //guardamos la sesión
                gotoMainActivity();
            }

            @Override
            public void onFailure(Exception e) {
                progressIndicator.setVisibility(View.GONE);
                layoutUsername.setError(Mensajes.ERROR_REINTENTAR);
                e.printStackTrace();
            }
        });
    }

    /**
     * Añade un usuario asistido a la base de datos
     * @param ua
     * @param idue
     */
    private void addAsistido(UsuarioAsistido ua, String idue) {
        usuarioDAO.add(ua, idue, new OnOperationCallback() {
            @Override
            public void onSuccess() {
                guardarSesion(ua);
                gotoMainActivity();
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(RegistroActivity.this);
            }
        });

    }


    /**
     * Muestra un diálogo con los 10 avatares
     */
    private void mostrarSelectorAvatares() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(Mensajes.REG_PUTFOTO);

        GridView gridView = new GridView(this);
        gridView.setNumColumns(3);
        gridView.setAdapter(new FotoPerfilAdapter(this, UiUtils.fotosPerfil));

        builder.setView(gridView);

        AlertDialog dialog = builder.create();

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            fotoPerfilSel = UiUtils.fotosPerfil[position];
            imgUserPhoto.setImageResource(fotoPerfilSel);
            dialog.dismiss();
        });

        dialog.show();
    }


    private void guardarSesion(Usuario u){
        SharedPreferences prefs = getSharedPreferences(Constantes.PERSIST_NOMBREARCHIVOPREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constantes.PERSIST_KEYUSERID, u.getId());
        editor.putString(Constantes.PERSIST_KEYTIPOUSR, u.getTipoUsuarioStr());
        editor.putBoolean(Constantes.PERSIST_KEYSESIONACTIVA, true);
        editor.apply();
    }

    //todo borrar cuando ya esté puesto en el log out
    private void borrarSesion(){
        SharedPreferences prefs = getSharedPreferences(Constantes.PERSIST_NOMBREARCHIVOPREF, MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    private void gotoMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
