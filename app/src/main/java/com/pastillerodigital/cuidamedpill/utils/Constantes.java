package com.pastillerodigital.cuidamedpill.utils;

public class Constantes {

    //FIREBASE
    public static final String COLLECTION_USUARIOS = "usuarios";
    public static final String COLLECTION_MEDICAMENTOS = "medicamentos";
        //PERSISTENCIA SESIÓN
    public static final String PERSIST_NOMBREARCHIVOPREF = "CUIDAMEDPILL_PREFS";
    public static final String PERSIST_KEYUSERID = "USERID";
    public static final String PERSIST_KEYUSERSELFID = "USERSELFID";
    public static final String PERSIST_KEYSESIONACTIVA = "SESIONACTIVA";
    public static final String PERSIST_KEYMODO = "MODO";

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

    //-------------TABLA MEDICAMENTOS
    public static final String MED_NOMBREMED = "nombreUsuario";
    public static final String MED_COLORSIMB = "colorSimb";
    public static final String MED_PAUTA = "pauta";
    public static final String MED_TIPOMED = "tipoMed";
    public static final String MED_FECHACAD = "fechaCad";
    public static final String MED_FECHAFIN = "fechaFin";
    public static final String MED_NCAJASMED= "nCajasMed";
    public static final String MED_NMEDCAJA = "nMedCaja";
    public static final String MED_NMEDACTCAJA= "nMedActCaja";
    public static final String MED_HORARIO= "horario";


    //-------------ENUMERADOS
    public static final String TIPOUSR_ESTANDAR = "Estándar";
    public static final String TIPOUSR_ASISTIDO = "Asistido";
    public static final String MODO_ESTANDAR = "Modo Estándar";
    public static final String MODO_ASISTIDO = "Modo Asistido";
    public static final String MODO_SUPERVISOR = "Modo Supervisor";

        //TIPO MEDICAMENTO
    public static final String TIPOMED_PASTILLA = "Pastilla";
    public static final String TIPOMED_VACUNA = "Vacuna";
    public static final String TIPOMED_INYECCION = "Inyección";
    public static final String TIPOMED_INHALADOR= "Inhalador";
    public static final String TIPOMED_CREMA= "Crema";
    public static final String TIPOMED_JARABE= "Jarabe";
    public static final String TIPOMED_GOTAS= "Gotas";
    public static final String TIPOMED_AEROSOL= "Aerosol";
    public static final String TIPOMED_PARCHE= "Parche";

        // MOMENTO DEL DIA
    public static final String TIPOMOM_DESAYUNO = "Desayuno";
    public static final String TIPOMOM_ALMUERZO = "Almuerzo";
    public static final String TIPOMOM_COMIDA = "Comida";
    public static final String TIPOMOM_MERIENDA = "Merienda";
    public static final String TIPOMOM_CENA = "Cena";

        //TIPO INTERVALO
    public static final String TIPOINTERVALO_DIARIO = "Diario";
    public static final String TIPOINTERVALO_QUINCENAL = "Quincenal";
    public static final String TIPOINTERVALO_SEMANAL = "Semanal";
    public static final String TIPOINTERVALO_MENSUAL = "Mensual";
    public static final String TIPOINTERVALO_TRIMESTRAL = "Trimestral";
    public static final String TIPOINTERVALO_ANUNAL= "Anual";


    //ARGUMENTOS
    public static final String ARG_UID = "uid";
    public static final String ARG_UIDSELF = "uidSelf";
    public static final String ARG_MODO = "Modo";
    //EXCEPCIONES
    public static final String EX_ID_INVALIDO = "Id inválido o nulo";

    //OTROS
    public static final String RES_TIPO = "drawable";
}
