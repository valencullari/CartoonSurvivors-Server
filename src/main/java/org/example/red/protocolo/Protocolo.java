
package org.example.red.protocolo;

public final class Protocolo {

    private Protocolo() {
    }

    // Descubrimiento
    public static final String BUSCAR_SERVIDOR = "BUSCAR_SERVIDOR";
    public static final String SERVIDOR_ENCONTRADO = "SERVIDOR_ENCONTRADO";

    // Conexiones
    public static final String CONECTAR = "CONECTAR";
    public static final String DESCONECTAR = "DESCONECTAR";
    public static final String KEEP_ALIVE = "KEEP_ALIVE";

    // Identificación
    public static final String JUGADOR_1 = "OK-1";
    public static final String JUGADOR_2 = "OK-2";
    public static final String SERVIDOR_LLENO = "LLENO";

    // Lobby
    public static final String LISTO = "LISTO";
    public static final String NO_LISTO = "NO_LISTO";
    public static final String LOBBY_ACTUALIZADO = "LOBBY";

    // Partida
    public static final String EMPIEZA = "EMPIEZA";
    public static final String OTRO_JUGADOR_DESCONECTADO =
            "OTRO_JUGADOR_DESCONECTADO";

    public static final String PARTIDA_FINALIZADA =
            "PARTIDA_FINALIZADA";
}
