package com.pastillerodigital.cuidamedpill.utils;

public class Constantes {

    //FIREBASE
    public static final String COLLECTION_USUARIOS = "usuarios";
    public static final String COLLECTION_MEDICAMENTOS = "medicamentos";
        //PERSISTENCIA SESIÓN
    public static final String PERSIST_NOMBREARCHIVOPREF = "CUIDAMEDPILL_PREFS";
    public static final String PERSIST_KEYUSERID = "USERID";
    public static final String PERSIST_KEYNOMBREUSER = "NOMBREUSER";
    public static final String PERSIST_KEYSESIONACTIVA = "SESIONACTIVA";
    public static final String PERSIST_KEYFOTOPERFIL = "FOTOPERFIL";

    //----------STORAGE (fotos)
    public static final String STOR_FN_USUARIOS = "usuarios/";
    public static final String STOR_FN_FORMATO = ".jpg";

    //-----------TABLA USUARIOS
    public static final String USUARIO_NOMBREUSUARIO = "nombreUsuario";
    public static final String USUARIO_ALIAS = "aliasU";
    public static final String USUARIO_TIPOUSR = "tipoUsuarioStr";
    public static final String USUARIO_FOTO = "fotoPerfil";
    public static final String USUARIO_MEDLISTSTR = "medAsigId";
    public static final String USUARIO_PASSWORDHASH = "passwordHash";
    public static final String USUARIO_SALT = "salt";

    public static final String USUARIO_ESTANDAR_IDUSRASIST = "idUsrAsistAsig";
    public static final String USUARIO_ASIST_IDUSRTUTORESASIG = "idUsrTutoresAsig";




    //ENUMERADOS
    public static final String TIPOUSR_ESTANDAR = "Estándar";
    public static final String TIPOUSR_ASISTIDO = "Asistido";

    //ARGUMENTOS
    public static final String ARG_UID = "uid";



}
