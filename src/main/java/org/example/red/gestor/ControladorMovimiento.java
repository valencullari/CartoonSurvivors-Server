package org.example.red.gestor;

import org.example.juego.GestorPartida;
import org.example.red.modelo.ClienteConectado;
import org.example.red.modelo.EstadoPartida;
import java.net.InetAddress;

public class ControladorMovimiento {
    private final GestorConexiones conexiones;
    private final ControladorPartida estado;
    private final GestorPartida partida;

    public ControladorMovimiento(GestorConexiones conexiones,
                                ControladorPartida estado, GestorPartida partida) {
        this.conexiones = conexiones;
        this.estado = estado;
        this.partida = partida;
    }

    public void procesarMejora(String mensaje, InetAddress ip, int puerto) {
        if (estado.getEstado() != EstadoPartida.EN_CURSO) return;
        ClienteConectado cliente = conexiones.buscarCliente(ip, puerto);
        if (cliente == null) return;
        String[] partes = mensaje.split("\\|", -1);
        if (partes.length != 5) return;
        try {
            int nivel = Integer.parseInt(partes[2]);
            int revision = Integer.parseInt(partes[3]);
            int opcion = Integer.parseInt(partes[4]);
            partida.elegirMejora(cliente.getId(), partes[1], nivel, revision, opcion);
        } catch (NumberFormatException e) {
            System.out.println("Seleccion de mejora invalida: " + mensaje);
        }
    }

    public void procesar(String mensaje, InetAddress ip, int puerto) {
        if (estado.getEstado() != EstadoPartida.EN_CURSO) return;
        ClienteConectado cliente = conexiones.buscarCliente(ip, puerto);
        if (cliente == null) return;
        String[] partes = mensaje.split("\\|", -1);
        if (partes.length != 3) return;
        try {
            float dx = Float.parseFloat(partes[1]);
            float dy = Float.parseFloat(partes[2]);
            if (!Float.isFinite(dx) || !Float.isFinite(dy)) return;
            cliente.actualizarActividad();
            partida.moverJugador(cliente.getId(), dx, dy);
        } catch (NumberFormatException e) {
            System.out.println("Movimiento invalido: " + mensaje);
        }
    }
}
