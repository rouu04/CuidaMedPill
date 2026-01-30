package com.pastillerodigital.cuidamedpill.controlador;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.modelo.usuario.Usuario;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnDataLoadedCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.UsuarioDAO;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.Mensajes;
import com.pastillerodigital.cuidamedpill.utils.UiUtils;
import com.pastillerodigital.cuidamedpill.utils.Utils;

public class WelcomeActivity extends AppCompatActivity {

    //ELEMENTOS DE DISEÑO
    private LinearLayout formLayout; //el formulario en si
    private TextInputLayout layoutUsername, layoutPassword;
    private TextInputEditText edtUsername, edtPassword;
    private MaterialButton btnLogin, btnRegister;
    private CircularProgressIndicator progressIndicator;


    //ELEMENTOS LÓGICOS
    private UsuarioDAO usuarioDAO;

    /**
    Funcion que se llama cuando la activity se crea
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        borrarSesion(); //todo borrar cuando esté log out
        setContentView(R.layout.activity_welcome); //carga layout correspondiente con los componentes

        // Componentes de diseño:
        formLayout = findViewById(R.id.formLayout);
        layoutUsername = findViewById(R.id.layoutUsername);
        layoutPassword = findViewById(R.id.layoutPassword);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        progressIndicator = findViewById(R.id.progressIndicator);

        usuarioDAO = new UsuarioDAO();

        if(sesionActiva()){ //persistencia de sesión
            gotoMainActivity();
        }

        UiUtils.setupPasswordToggle(layoutPassword, edtPassword, this);

        // Botón login
        btnLogin.setOnClickListener(v -> login());

        // Botón registro
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegistroActivity.class);
            startActivity(intent);
        });

    }


    private void login(){
        UiUtils.limpiarErroresLayouts(formLayout);

        //Sacamos información input
        String nombreUsuario = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString();

        // Validaciones
        if(TextUtils.isEmpty(nombreUsuario)){
            layoutUsername.setError(Mensajes.REG_VAL_PUTNOMBREUSR);
            return;
        }
        if(TextUtils.isEmpty(password)){
            layoutPassword.setError(Mensajes.REG_VAL_PUTPASSW);
            return;
        }

        // Mostrar progreso
        progressIndicator.setVisibility(View.VISIBLE);

        //Verificamos que exite un usuario con ese nombre
        //todo aclarar en layout que nombre de usuario no es alias
        usuarioDAO.getBasicWithParameter(Constantes.USUARIO_NOMBREUSUARIO, nombreUsuario, new OnDataLoadedCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario usuario) {
                progressIndicator.setVisibility(View.GONE);
                if(usuario == null){
                    layoutUsername.setError(Mensajes.ERROR_USUARIO_NOEXISTE);
                    return;
                }

                // Verificar hash
                String hashIntroducido = Utils.hashPassword(password, usuario.getSalt());
                if(hashIntroducido.equals(usuario.getPasswordHash())){
                    //La contraseña coincide con la que teníamos, se inicia sesión
                    guardarSesion(usuario);
                    gotoMainActivity();
                } else {
                    layoutPassword.setError(Mensajes.ERROR_USUARIO_CONTRASEÑAINCORRECTA);
                }
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
    Función que permitirá la persistencia de sesión
     SharedPreferences es un mecanismo de android para guardar datos pequeños en forma de clave
     valor de forma persistente.
     */
    private void guardarSesion(Usuario u){
        //modo privado para que solo la aplicación pueda acceder a esos datos, perfs representan las
        //preferencias
        SharedPreferences prefs = getSharedPreferences(Constantes.PERSIST_NOMBREARCHIVOPREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit(); //editor necesario para modificar archivo preferencias
        editor.putString(Constantes.PERSIST_KEYUSERID, u.getId());
        editor.putString(Constantes.PERSIST_KEYTIPOUSR, u.getTipoUsuarioStr());
        editor.putBoolean(Constantes.PERSIST_KEYSESIONACTIVA, true);
        editor.apply(); //aplica los cambios de forma asíncrona
    }

    /**
    Comprobar si ya hay sesión
    */
    private boolean sesionActiva(){
        SharedPreferences prefs = getSharedPreferences(Constantes.PERSIST_NOMBREARCHIVOPREF, MODE_PRIVATE);
        //Si es la primera vez devuelve el valor por defecto false
        return prefs.getBoolean(Constantes.PERSIST_KEYSESIONACTIVA, false);
    }

    /**
     Ir a MainActivity
     */
    private void gotoMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        //flags para no poder volver atrás y decir que main tiene el flujo
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    //todo borrar cuando ya esté puesto en el log out
    private void borrarSesion(){
        SharedPreferences prefs = getSharedPreferences(Constantes.PERSIST_NOMBREARCHIVOPREF, MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

}
