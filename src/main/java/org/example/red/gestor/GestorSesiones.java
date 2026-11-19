package org.example.red.gestor;

import org.example.juego.GestorPartida;
import org.example.red.EmisorEstados;
import org.example.red.EmisorPaquetesUDP;
import org.example.red.modelo.ClienteConectado;
import org.example.red.modelo.EstadoPartida;
import org.example.red.protocolo.Protocolo;
import java.net.InetAddress;

public class GestorSesiones {
    private final GestorConexiones conexiones;
    private final GestorLobby lobby;
    private final ControladorPartida estado;
    private final GestorPartida partida;
    private final EmisorEstados estados;
    private final EmisorPaquetesUDP emisor;

    public GestorSesiones(GestorConexiones conexiones, GestorLobby lobby,
                         ControladorPartida estado, GestorPartida partida,
                         EmisorEstados estados, EmisorPaquetesUDP emisor) {
        this.conexiones = conexiones;
        this.lobby = lobby;
        this.estado = estado;
        this.partida = partida;
        this.estados = estados;
        this.emisor = emisor;
    }

    public void finalizarPartida(InetAddress ip, int puerto) {
        // Solo se aceptan peticiones de clientes realmente conectados.
        if (conexiones.buscarCliente(ip, puerto) == null || !estado.estaEnCurso()) return;
        estado.establecerEstado(EstadoPartida.FINALIZADA);
        estados.detener();
        partida.detenerPartida();
        emisor.enviarATodos(Protocolo.PARTIDA_FINALIZADA);
        System.out.println("Partida finalizada.");
    }

    public void finalizarSesion(InetAddress ip, int puerto) {
        ClienteConectado saliente = conexiones.buscarCliente(ip, puerto);
        if (saliente == null) return;
        System.out.println("Finalizando sesion por salida del jugador " + saliente.getId());
        for (ClienteConectado cliente : conexiones.getClientes()) {
            if (cliente.getId() != saliente.getId()) {
                emisor.enviarACliente(Protocolo.SESION_FINALIZADA,
                        cliente.getDireccionIP(), cliente.getPuerto());
            }
        }
        limpiarSesion();
    }

    public void limpiarSesion() {
        estados.detener();
        partida.detenerPartida();
        lobby.reiniciar();
        conexiones.eliminarTodos();
        estado.reiniciar();
        System.out.println("Servidor disponible para una nueva partida.");
    }

    public void detenerServidor() {
        estados.detener();
        partida.detenerPartida();
    }
}
