
package org.example.red.protocolo;

public final class Protocolo {

    private Protocolo() {
    }

    // Descubrimiento
    public static final String BUSCAR_SERVIDOR = "BUSCAR_SERVIDOR";
    public static final String SERVIDOR_ENCONTRADO = "SERVIDOR_ENCONTRADO";
    public static final String SERVIDOR_LLENO = "LLENO";

    // Conexión
    public static final String CONECTAR = "CONECTAR";
    public static final String DESCONECTAR = "DESCONECTAR";
    public static final String KEEP_ALIVE = "KEEP_ALIVE";
    public static final String KEEP_ALIVE_OK = "KEEP_ALIVE_OK";

    // Identificación
    public static final String JUGADOR_1 = "OK-1";
    public static final String JUGADOR_2 = "OK-2";

    // Lobby
    public static final String JUGADOR_CONECTADO = "JUGADOR_CONECTADO";
    public static final String JUGADOR_LISTO = "JUGADOR_LISTO";
    public static final String LISTO = "LISTO";
    public static final String NO_LISTO = "NO_LISTO";
    public static final String PERSONAJE = "PERSONAJE";
    public static final String PERSONAJES = "PERSONAJES";
    public static final String EMPIEZA = "EMPIEZA";



    // Partida
    public static final String PARTIDA_FINALIZADA = "PARTIDA_FINALIZADA";
    public static final String MOVER = "MOVER";
    public static final String ESTADO = "ESTADO";

    // Finalización de sesión
    public static final String VOLVER_AL_MENU = "VOLVER_AL_MENU";
    public static final String SESION_FINALIZADA = "SESION_FINALIZADA";

    // Desconexión inesperada
    public static final String DESCONEXION_INESPERADA =
            "DESCONEXION_INESPERADA";
}
