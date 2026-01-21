package com.pastillerodigital.cuidamedpill.controlador;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.pastillerodigital.cuidamedpill.R;
import com.pastillerodigital.cuidamedpill.modelo.Usuario;
import com.pastillerodigital.cuidamedpill.modelo.dao.UsuarioDAO;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    protected FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        db = FirebaseFirestore.getInstance();


        probarAddUsuario();
    }

    private void probarAddUsuario() {
        UsuarioDAO usuarioDAO = new UsuarioDAO();

        Usuario usuario = new Usuario();
        usuario.setNombreU("Ana López");
        usuario.setTelefono("600123456");
        usuario.setFotoURL("https://foto.url/ana.jpg");
        usuario.setTipoUsuarioStr("PACIENTE");
        List<String> medListStr = new LinkedList<>();
        usuario.setMedListStr(medListStr);

        db.collection("usuarios").add(usuario);
        /*
        usuarioDAO.add(usuario, new OnOperationCallback() {
            @Override
            public void onSuccess() {
                Log.d("MainActivity", "Usuario añadido con ID: " + usuario.getId());
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("MainActivity", "Error al añadir usuario: " + e.getMessage());
            }
        });

         */
    }

}