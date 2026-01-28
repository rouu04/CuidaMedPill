package com.pastillerodigital.cuidamedpill.utils;

/**
Clase que contará con mensajes constantes relacionados con la interfaz
 */
public class Mensajes {

    //-------------ERROR
    public static final String ERROR_HAYERROR = "Ha ocurrido un problema inesperado";
    public static final String ERROR_REINTENTAR = "No se ha podido realizar la operación. Por favor, inténtalo otra vez";
    public static final String ERROR_USUARIO_NOEXISTE = "No existe ningún usuario con ese nombre de usuario";
    public static final String ERROR_USUARIO_EXISTE = "Ya existe un usuario con ese nombre, por favor prueba con otro";
    public static final String ERROR_USUARIO_CONTRASEÑAINCORRECTA = "La contraseña es incorrecta, inténtelo de nuevo";

    //REGISTRO
    public static final String ERROR_FOTOURL = "Ha ocurrido un problema al obtener la foto";
    public static final String ERROR_FOTOUPLOAD = "Ha ocurrido un problema al guardar la foto";
    public static final String ERROR_TUTORNOVALIDO = "La persona que te ayudará con la medicación tiene que estar registrada en la aplicación y ser un usuario de tipo estándar";

    //-------------INTERACCIONES
    public static final String ACEPTAR = "Aceptar";

    //-------------ELEMENTOS INTERFAZ
    // REGISTRO
    public static final String REG_PUTFOTO = "Selecciona la foto de perfil";
    public static final String REG_PUTTIPOUSR = "Selecciona el tipo de experiencia que tendrás con la aplicación";
        //VALIDACIONES
    public static final String REG_VAL_PUTNOMBREUSR = "Introduce tu nombre de usuario";
    public static final String REG_VAL_PUTPASSW = "Introduce tu contraseña";
    public static final String REG_VAL_PASSWDNOCOINCIDEN = "Las contraseñas no coinciden, tienen que ser iguales";
    public static final String REG_VAL_PUTALIAS= "Introduce un alias, será como la aplicación se diriga a ti";
    public static final String REG_VAL_PUTTIPOUSR= "Selecciona tu caso, obtendrás distintos servicios de la aplicación";

    public static final String REG_VAL_PUTUSUARIOTUTOR= "Introduce el nombre de usuario de la persona que te ayudará con la medicación";
    public static final String REG_VAL_PUTUSUARIOTUTORPASSWD = "Introduce la contraseña de la persona que te ayudará con la aplicación";
    //------------EXCEPCIONES
    public static final String EX_EXISTE = "Error al comprobar existencia";



}
