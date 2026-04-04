package com.pastillerodigital.cuidamedpill.utils;

/**
Clase que contará con mensajes constantes relacionados con la interfaz
 */
public class Mensajes {

    //-------------BÁSICOS DE INTERACCIONES
    public static final String BASIC_SI = "Si";
    public static final String BASIC_NO = "No";
    public static final String BASIC_ACEPTAR = "Aceptar";
    public static final String BASIC_CANCELAR = "Cancelar";
    public static final String BASIC_ELIMINAR = "Eliminar";
    public static final String BASIC_GUARDAR = "Guardar";
    public static final String BASIC_CONFIRMACION = "Acción realizada correctamente";
    public static final String BASIC_NEG_CONFIRMACION = "La acción no se ha podido realizar";


    //-------------ERROR
    public static final String ERROR_HAYERROR = "Ha ocurrido un problema inesperado";
    public static final String ERROR_REINTENTAR = "No se ha podido realizar la operación. Por favor, inténtalo otra vez";

        //USUARIO
    public static final String ERROR_USUARIO_NOEXISTE = "No existe ningún usuario con ese nombre de usuario";
    public static final String ERROR_USUARIO_EXISTE = "Ya existe un usuario con ese nombre, por favor prueba con otro";
    public static final String ERROR_USUARIO_CONTRASEÑAINCORRECTA = "La contraseña es incorrecta, inténtelo de nuevo";

    //REGISTRO
    public static final String ERROR_TUTORNOVALIDO = "La persona que te ayudará con la medicación tiene que estar registrada en la aplicación y ser un usuario de tipo estándar";


    //-------------ELEMENTOS INTERFAZ
    // REGISTRO
    public static final String REG_PUTFOTO = "Selecciona la foto de perfil";
    public static final String REG_PUTTIPOUSR = "Selecciona el tipo de experiencia que tendrás con la aplicación";
        //VALIDACIONES
    public static final String REG_VAL_PUTNOMBREUSR = "Introduce el nombre de usuario";
    public static final String REG_VAL_PUTPASSW = "Introduce la contraseña";
    public static final String REG_VAL_PASSWDNOCOINCIDEN = "Las contraseñas no coinciden, tienen que ser iguales";
    public static final String REG_VAL_PUTALIAS= "Introduce un alias, será como la aplicación se diriga a ti";
    public static final String REG_VAL_PUTTIPOUSR= "Selecciona tu caso, obtendrás distintos servicios de la aplicación";

    public static final String REG_VAL_PUTUSUARIOTUTOR= "Introduce el nombre de usuario de la persona que te ayudará con la medicación";
    public static final String REG_VAL_PUTUSUARIOTUTORPASSWD = "Introduce la contraseña de la persona que te ayudará con la aplicación";
    public static final String USR_VAL_PUTCONFPASSWD = "Confirma la contraseña, vuelve a escribirla";

    //PERFIL-----
    public static final String PERF_ASIST_SUPERVISANDO = "Supervisando a: %s";
    public static final String PERF_BORRARCUENTA = "Borrar cuenta";
    public static final String PERF_PREG_BORRARCUENTA = "¿Deseas borrar la cuenta?";
    public static final String PERF_PREG_ASIST_BORRARCUENTA = "¿Deseas borrar la cuenta de %s ?";
    public static final String PERF_EDIT_ASIST = "Editar perfil de %s";

    public static final String PERF_CONF_BORRARCUENTA = "El perfil ha sido eliminado correctamente";
    public static final String PERF_NEG_BORRARCUENTA_TUTOR = "El perfil no puede eliminarse si tiene usuarios asistidos que cuentan solo con su ayuda";
    public static final String PERF_NEG_DESVINCULAR = "No puedes desvincularte del usuario asistido si eres su único tutor";
    public static final String PERF_EDITPASSWD = "Puede cambiar su contraseña";
    public static final String PERF_ERROR_UNOUA = "El usuario tiene que ser un usuario asistido";
    public static final String PERF_CONF_DESVINCULAR = "Usuarios desvinculados correctamente";
    public static final String PERF_CONF_EDITAR = "Perfil editado correctamente";

    //-MEDICAMENTOS-----
    public static final String MEDS_TITULO_SUPERV = "Medicamentos de %s";
    public static final String MED_ADD_SUPERV = "Añadir medicamento para %s";
    public static final String MED_EDIT_SUPERV = "Editar medicamento de %s";

        //LISTA
    public static final String MEDS_TITLE = "Tus medicamentos";

        //EDITAR Y AÑADIR
    public static final String MED_EDITADD_SEL_INTERVALO = "Selecciona un intervalo";
    public static final String MED_EDITADD_SEL_TIPO = "Selecciona un tipo de medicamento";
    public static final String MED_EDITADD_SEL_HORA_ESPEC = "Seleccionar hora específica";
    public static final String MED_EDITADD_SEL_HORA_TITLE = "Selecciona una hora";
    public static final String MED_EDITADD_SEL_HORA_MOMENT = "Seleccionar momento del día";
    public static final String MED_EDITADD_SEL_HORA_MOMENT_TITLE = "Selecciona un momento del día";
    public static final String MED_EDITADD_SEL_COLOR = "Selecciona un color";
    public static final String MED_EDITADD_ADD_HORA = "Añadir hora";

    public static final String MED_EDITADD_VAL_NOMBRE = "No puedes tener dos medicamentos con el mismo nombre";
    public static final String MED_EDITADD_ERR_NOMBRE = "El medicamento necesita tener un nombre";
    public static final String MED_EDITADD_ERR_FECHA_INVALIDA= "Fecha inválida";
    public static final String MED_EDITADD_ERR_FECHA_MALFORMATO= "Formato de fecha inválido";
    public static final String MED_EDITADD_ERR_FECHA_ANTIGUALHOY = "Esta fecha no puede ser anterior o igual a hoy";
    public static final String MED_EDITADD_ERR_FECHA_ANTHOY= "Esta fecha no puede ser anterior a hoy";
    public static final String MED_EDITADD_ERR_NUM_MAYORO0= "Tiene que ser mayor o igual a 0";
    public static final String MED_EDITADD_ERR_NUM_MAYOR0= "Tiene que ser mayor que 0";
    public static final String MED_EDITADD_ERR_NUM_INVALIDO= "Número inválido";
    public static final String MED_EDITADD_ERR_HORARIO_INTERVALO= "Con el horario activo se necesita saber la frecuencia del intervalo";
    public static final String MED_EDITADD_ERR_HORARIO_HORA= "Con el horario activo se necesita al menos una hora";

    //DETALLES MED
    public static final String MED_DET_CONF_ELIMINADO= "Medicamento eliminado con éxito";
    public static final String MED_DET_NOTISLIKEGEN= "Se aplican las notificaciones por defecto del perfil";
    public static final String MED_DET_ELIM= "Eliminar medicamento";
    public static final String MED_DET_PREG_ELIM= "¿Deseas borrar este medicamento?";

    //CALENDARIO
    public static final String CAL_VISTA_SEMANAL= "Vista semanal";
    public static final String CAL_VISTA_MENSUAL= "Vista mensual";
    public static final String CAL_TITLE= "Tu calendario";
    public static final String CAL_TITLE_SUPERVISOR= "Calendario de %s";
    public static final String CAL_DIA_SEL= "Medicamentos del día: %02d/%02d/%04d";
    public static final String CAL_DIA_SEL_HOY= "Medicamentos de hoy";

    //HOME
    public static final String HOME_TITLE= "Inicio";
    public static final String HOME_TITLE_SUPERVISOR= "Inicio de %s";
    public static final String HOME_TITLE_AVISOS= "Avisos";
    public static final String HOME_TITLE_AVISOS_SUPERVISOR= "Sus avisos";
    public static final String HOME_MEDS_HOY= "Medicamentos hoy";
    public static final String HOME_MEDS_HOY_SUPERVISOR= "Sus medicamentos hoy";
    public static final String HOME_MEDS_AYER= "AYER ";
    public static final String HOME_CONFING_TITULO= "¿Te has tomado este medicamento?";
    public static final String HOME_CONFING_TITULO_SUPERVISOR= "¿%s se ha tomado este medicamento?";
    public static final String HOME_SELMED_TITULO= "Selecciona el medicamento no programado tomado";
    public static final String HOME_SELMED_TITULO_SUPERVISOR= "Selecciona el medicamento de %s no programado tomado";

    //NOTIFICACIONES
    public static final String NOTI_HORAMED = "Hora de tu medicación";
    public static final String NOTI_TOMARMED = "Es momento de tomar %s";


    //------------EXCEPCIONES
    public static final String EX_EXISTE = "Error al comprobar existencia";



}
