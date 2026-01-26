package com.pastillerodigital.cuidamedpill.controlador;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
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

import java.util.List;

public class RegistroActivity extends AppCompatActivity {

    //Elementos del layout
    private TextInputLayout layoutAlias, layoutUsername, layoutPassword, layoutConfirmPassword, layoutTipoUsuario, layoutTutorUsername, layoutTutorPassword;
    private TextInputEditText edtAlias, edtUsername, edtPassword, edtConfirmPassword, edtTutorUsername, edtTutorPassword;
    private android.widget.ImageView imgUserPhoto;
    private MaterialAutoCompleteTextView actvTipoUsuario;
    private MaterialButton btnRegister;
    private CircularProgressIndicator progressIndicator;

    //Elementos lógica
    private UsuarioDAO usuarioDAO;
    private int fotoPerfilSel; //foto de perfil seleccionada

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        borrarSesion();//todo borrar cuando esté echo el log out
        setContentView(R.layout.activity_registro);

        //Inicialización de componentes de diseño
        layoutAlias = findViewById(R.id.layoutAlias);
        layoutUsername = findViewById(R.id.layoutUsername);
        layoutPassword = findViewById(R.id.layoutPassword);
        layoutConfirmPassword = findViewById(R.id.layoutConfirmPassword);
        layoutTipoUsuario = findViewById(R.id.layoutTipoUsuario);
        layoutTutorUsername = findViewById(R.id.layoutTutorUsername);
        layoutTutorPassword = findViewById(R.id.layoutTutorPassword);


        edtAlias = findViewById(R.id.edtAlias);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        edtTutorUsername = findViewById(R.id.edtTutorUsername);
        edtTutorPassword = findViewById(R.id.edtTutorPassword);

        actvTipoUsuario = findViewById(R.id.actvTipoUsuario);
        imgUserPhoto = findViewById(R.id.imgUserPhoto);
        btnRegister = findViewById(R.id.btnRegister);
        progressIndicator = findViewById(R.id.progressIndicator);

        //Lógica
        usuarioDAO = new UsuarioDAO();

        // Adapter con la lista de tipos de usuarios, que se verán en formato menú markdown
        ArrayAdapter<TipoUsuario> adapter = new ArrayAdapter<>(this, R.layout.lista_items_dropdown, TipoUsuario.values());
        actvTipoUsuario.setAdapter(adapter);
        //Que muestre el menu cuando se le da
        actvTipoUsuario.setOnItemClickListener((parent, view, position, id) -> {
            TipoUsuario tipo = (TipoUsuario) parent.getItemAtPosition(position);

            //En función del tipo de usuario se ven unas cosas u otras.
            if (tipo == TipoUsuario.ASISTIDO) {
                layoutTutorUsername.setVisibility(View.VISIBLE);
                layoutTutorPassword.setVisibility(View.VISIBLE);
            } else {
                layoutTutorUsername.setVisibility(View.GONE);
                layoutTutorPassword.setVisibility(View.GONE);
            }
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
        layoutAlias.setError(null);
        layoutUsername.setError(null);
        layoutPassword.setError(null);
        layoutConfirmPassword.setError(null);
        layoutTipoUsuario.setError(null);

        String alias = edtAlias.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString();
        String confirmPassword = edtConfirmPassword.getText().toString();
        String tipoUsuario = actvTipoUsuario.getText().toString();

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
        if(TextUtils.isEmpty(tipoUsuario)){
            layoutTipoUsuario.setError(Mensajes.REG_VAL_PUTTIPOUSR);
            error = true;
        }
        if(error) return;

        progressIndicator.setVisibility(View.VISIBLE);

        // Hash de la contraseña
        String salt = Utils.generarSalt();
        String hash = Utils.hashPassword(password, salt);

        //Creamos el usuario
        Usuario u = new Usuario();
        u.setAliasU(alias);
        u.setNombreUsuario(username);
        u.setPasswordHash(hash);
        u.setSalt(salt);
        u.setTipoUsuarioStr(tipoUsuario);
        u.setTipoUsuario(TipoUsuario.tipoUsrFromString(tipoUsuario));
        u.setFotoPerfil(fotoPerfilSel);

        //Si es un usuario asistido, necesitará definir un usuario tutor
        if(u.getTipoUsuario().equals(TipoUsuario.ASISTIDO)){
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

            usuarioDAO.getWithParameter(Constantes.USUARIO_NOMBREUSUARIO, tutorUsername, new OnDataLoadedCallback<Usuario>() {
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

                            addAsistido((UsuarioAsistido)u, tutor.getId());
                        }

                        @Override
                        public void onFailure(Exception e) {
                            progressIndicator.setVisibility(View.GONE);
                            layoutTutorUsername.setError(Mensajes.ERROR_REINTENTAR);
                        }
                    }
            );
        }
        else{
            this.addUsuario(u);
        }


    }


    private void addUsuario(Usuario u) {
        usuarioDAO.add(u, new OnOperationCallback() {
            @Override
            public void onSuccess() {
                progressIndicator.setVisibility(View.GONE);
                guardarSesion(u); //guardamos la sesión
                //todo llevar a home
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                progressIndicator.setVisibility(View.GONE);
                layoutUsername.setError(Mensajes.ERROR_REINTENTAR);
                e.printStackTrace();
            }
        });
    }

    private void addAsistido(UsuarioAsistido ua, String idue) {
        usuarioDAO.add(ua, idue, new OnOperationCallback() {
            @Override
            public void onSuccess() {
                guardarSesion(ua);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                UiUtils.mostrarErrorYReiniciar(RegistroActivity.this);
            }
        });

    }

    // Muestra un diálogo con los 10 avatares
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
        editor.putString(Constantes.PERSIST_KEYNOMBREUSER, u.getNombreUsuario());
        editor.putInt(Constantes.PERSIST_KEYFOTOPERFIL, u.getFotoPerfil()); // guardamos la foto
        editor.putBoolean(Constantes.PERSIST_KEYSESIONACTIVA, true);
        editor.apply();
    }

    //todo borrar cuando ya esté puesto en el log out
    private void borrarSesion(){
        SharedPreferences prefs = getSharedPreferences(Constantes.PERSIST_NOMBREARCHIVOPREF, MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

}
