package com.pastillerodigital.cuidamedpill.utils;

/**
Clase que contará con mensajes constantes relacionados con la interfaz
 */
public class Mensajes {

    //-------------ERROR
    public static final String ERROR_HAYERROR = "Ha ocurrido un problema inesperado";
    public static final String ERROR_REINTENTAR = "No se ha podido realizar la operación. Por favor, inténtalo otra vez";
    public static final String ERROR_USUARIO_NOEXISTE = "No existe ningún usuario con ese nombre de usuario";
    public static final String ERROR_USUARIO_CONTRASEÑAINCORRECTA = "La contraseña es incorrecta, inténtelo de nuevo";

    //REGISTRO
    public static final String ERROR_REGCODIGO = "No se ha podido enviar el codigo, revisa la conexión y vuelve a intentarlo";
    public static final String ERROR_REGCODIGONECESARIO = "Primero debes enviar el código SMS";
    public static final String ERROR_REGCODIGOINCORRECTO = "Código incorrecto o expirado";
    public static final String ERROR_REGTELFINVALIDO = "El número de teléfono no es válido";
    public static final String ERROR_REGTOOMANYSOLICITUDES = "Se han enviado demasiadas solicitudes, intentalo de nuevo más tarde";

    //-------------INTERACCIONES
    public static final String ACEPTAR = "Aceptar";

    //-------------ELEMENTOS INTERFAZ
    // REGISTRO
    public static final String REG_LAYOUTTELEFONO = "Introduce un teléfono";
    public static final String REG_LAYOUTCODIGO = "Introduce el código";
    public static final String REG_CODIGOENVIADO = "Código enviado correctamente";
        //VALIDACIONES
    public static final String REG_VAL_PUTNOMBREUSR = "Introduce tu nombre de usuario";
    public static final String REG_VAL_PUTPASSW = "Introduce tu contraseña";
    public static final String REG_VAL_PASSWDNOCOINCIDEN = "Las contraseñas no coinciden, tienen que ser iguales";


    //------------EXCEPCIONES
    public static final String EX_EXISTE = "Error al comprobar existencia";



}
