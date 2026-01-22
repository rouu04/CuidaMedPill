package com.pastillerodigital.cuidamedpill.controlador;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.modelo.Usuario;
import com.pastillerodigital.cuidamedpill.modelo.dao.OnDataLoadedCallback;
import com.pastillerodigital.cuidamedpill.modelo.dao.UsuarioDAO;
import com.pastillerodigital.cuidamedpill.utils.Constantes;
import com.pastillerodigital.cuidamedpill.utils.Mensajes;
import com.pastillerodigital.cuidamedpill.utils.UiUtils;

import java.util.concurrent.TimeUnit;

public class WelcomeActivity extends AppCompatActivity {

    private TextInputLayout layoutTelefono, layoutCodigo; //contenedores de los campos de texto
    private TextInputEditText edtTelefono, edtCodigo;
    private MaterialButton btnEnviarCodigo, btnVerificar;
    private CircularProgressIndicator progressIndicator; //indicador de carga

    private FirebaseAuth auth; //instancia autenticación firebase para autenticar telefono
    private UsuarioDAO usuarioDAO;

    private String verificacionId; //id de verificacion que fb envia con el sms

    /*
    Funcion que se llama cuando la activity se crea
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome); //carga layout correspondiente con los componentes

        // Componentes de diseño:
        layoutTelefono = findViewById(R.id.layoutTelefono);
        layoutCodigo = findViewById(R.id.layoutCodigo);
        edtTelefono = findViewById(R.id.edtTelefono);
        edtCodigo = findViewById(R.id.edtCodigo);
        btnEnviarCodigo = findViewById(R.id.btnEnviarCodigo);
        btnVerificar = findViewById(R.id.btnVerificar);
        progressIndicator = findViewById(R.id.progressIndicator);

        auth = FirebaseAuth.getInstance();
        usuarioDAO = new UsuarioDAO();

        //Lógica botones
        btnEnviarCodigo.setOnClickListener(v -> enviarCodigo());
        btnVerificar.setOnClickListener(v -> verificarCodigo());
    }

    /*
    Función que envia sms
     */
    private void enviarCodigo() {
        String telefono = edtTelefono.getText().toString().trim(); //sacamos telefono quitando espacios

        if (telefono.isEmpty()) {
            layoutTelefono.setError(Mensajes.REG_LAYOUTTELEFONO);
            return;
        } else {
            layoutTelefono.setError(null);
        }

        progressIndicator.setVisibility(View.VISIBLE); //mostramos loading mientras firebase envia sms

        //configuramos opciones phone auth --> numero de telefono a verificar, tiempo se espera máximo,
        //y los callbacks tras éxito o fallo
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(telefono)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    /*
                    Si firebase detecta automaticamente el sms, firebase introduce el código y así
                    no lo hace el usuario
                     */
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        progressIndicator.setVisibility(View.GONE);
                        signInWithPhoneAuthCredential(credential);
                    }

                    /*
                    Si falla el envío del sms muestra error al usuario. Es necesario estar conectado
                    a la red
                     */

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        progressIndicator.setVisibility(View.GONE);
                        layoutTelefono.setError(Mensajes.ERROR_REGCODIGO);
                    }

                    /*
                    Cuando firebase envía correctamente el sms:
                    Guarda el verification id, oculta loading, avisa al usuario con un toast
                     */
                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        progressIndicator.setVisibility(View.GONE);
                        WelcomeActivity.this.verificacionId = verificationId;
                        layoutTelefono.setError(null); // limpia cualquier error previo
                        layoutTelefono.setHelperText(Mensajes.REG_CODIGOENVIADO);
                    }
                })
                .build();

        //Inicia proceso de verificacion firebase
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    /*
    Función que comprueba el sms
     */
    private void verificarCodigo() {
        String codigo = edtCodigo.getText().toString().trim(); //obtiene codigo que puso el usuario

        if (codigo.isEmpty()) {
            layoutCodigo.setError(Mensajes.REG_LAYOUTCODIGO);
            return;
        } else {
            layoutCodigo.setError(null);
        }

        if (verificacionId == null || verificacionId.isEmpty()) {
            layoutCodigo.setError(Mensajes.ERROR_REGCODIGONECESARIO);
            return;
        }

        progressIndicator.setVisibility(View.VISIBLE);
        //Crea un credential a partir del codigo ingresado y el verification id
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificacionId, codigo);
        signInWithPhoneAuthCredential(credential);
    }

    /*
    Función que inicia sesión
     */
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        //Firebase intenta iniciar sesión con el credential (y oculta loading al acabar)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    progressIndicator.setVisibility(View.GONE);

                    if (task.isSuccessful()) { //si la verificación es correcta
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        String telefono = firebaseUser.getPhoneNumber();

                        //Podemos registrarnos o iniciar sesión, dependiendo de si ya existe el usuario o no
                        //Si el usuario no existe quiere decir que no hay cuentas con ese teléfono
                        usuarioDAO.usuarioExiste(telefono, new OnDataLoadedCallback<Usuario>() {
                            @Override
                            public void onSuccess(Usuario data) {
                                if(data == null){ //usuario no existe: registro
                                    /*
                                    Establecemos los argumentos y cambiamos pantalla vista de la app
                                     */
                                    Bundle bundle = new Bundle();
                                    bundle.putString(Constantes.ARG_UID, firebaseUser.getUid());
                                    bundle.putString(Constantes.ARG_TELEFONO, telefono);

                                    Intent intent = new Intent(WelcomeActivity.this, RegistroActivity.class);
                                    intent.putExtras(bundle);

                                    startActivity(intent);
                                    finish();
                                }
                                else{ //usuario existe: inicio de sesión
                                    //todo
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                UiUtils.mostrarErrorYReiniciar(WelcomeActivity.this);
                            }
                        });

                    } else {
                        layoutCodigo.setError(Mensajes.ERROR_REGCODIGOINCORRECTO);
                    }
                });
    }
}
