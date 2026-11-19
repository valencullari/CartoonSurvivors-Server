package org.example.red.gestor;

import org.example.juego.GestorPartida;
import org.example.red.EmisorEstados;
import org.example.red.EmisorPaquetesUDP;
import org.example.red.modelo.ClienteConectado;
import org.example.red.modelo.EstadoPartida;
import org.example.red.protocolo.Protocolo;
import java.net.InetAddress;

public class ControladorLobby {
    private final GestorConexiones conexiones;
    private final GestorLobby lobby;
    private final ControladorPartida estado;
    private final GestorPartida partida;
    private final EmisorPaquetesUDP emisor;
    private final EmisorEstados estados;

    public ControladorLobby(GestorConexiones conexiones, GestorLobby lobby,
                           ControladorPartida estado, GestorPartida partida,
                           EmisorPaquetesUDP emisor, EmisorEstados estados) {
        this.conexiones = conexiones;
        this.lobby = lobby;
        this.estado = estado;
        this.partida = partida;
        this.emisor = emisor;
        this.estados = estados;
    }

    public void conectar(InetAddress ip, int puerto) {
        if (estado.getEstado() != EstadoPartida.ESPERANDO_JUGADORES) {
            emisor.enviarACliente(Protocolo.SERVIDOR_LLENO, ip, puerto);
            return;
        }
        ClienteConectado cliente = conexiones.registrarCliente(ip, puerto);
        if (cliente == null) {
            emisor.enviarACliente(Protocolo.SERVIDOR_LLENO, ip, puerto);
            return;
        }
        emisor.enviarACliente(cliente.getId() == 1 ? Protocolo.JUGADOR_1 : Protocolo.JUGADOR_2,
                ip, puerto);
        if (conexiones.partidaCompleta()) {
            estado.establecerEstado(EstadoPartida.LOBBY);
            emisor.enviarATodos(Protocolo.JUGADOR_CONECTADO);
        }
    }

    public void seleccionarPersonaje(InetAddress ip, int puerto, String personaje) {
        EstadoPartida actual = estado.getEstado();
        if (actual != EstadoPartida.ESPERANDO_JUGADORES && actual != EstadoPartida.LOBBY)
            return;
        ClienteConectado cliente = conexiones.buscarCliente(ip, puerto);
        if (cliente == null || cliente.isListo()) return;
        if (!"MORDECAI".equals(personaje)) {
            System.out.println("Personaje no valido: " + personaje);
            return;
        }
        cliente.setPersonajeSeleccionado(personaje);
        cliente.actualizarActividad();
        notificarPersonajes();
    }

    public void marcarListo(InetAddress ip, int puerto) {
        if (estado.getEstado() != EstadoPartida.LOBBY) return;
        ClienteConectado cliente = conexiones.buscarCliente(ip, puerto);
        if (cliente == null || cliente.getPersonajeSeleccionado() == null) return;
        boolean todosListos = lobby.marcarListo(cliente.getId());
        emisor.enviarATodos(Protocolo.JUGADOR_LISTO);
        if (!todosListos) return;

        ClienteConectado uno = conexiones.buscarCliente(1);
        ClienteConectado dos = conexiones.buscarCliente(2);
        if (uno == null || dos == null || uno.getPersonajeSeleccionado() == null
                || dos.getPersonajeSeleccionado() == null) return;
        partida.iniciarPartida(uno.getPersonajeSeleccionado(), dos.getPersonajeSeleccionado());
        estado.establecerEstado(EstadoPartida.EN_CURSO);
        notificarPersonajes();
        emisor.enviarATodos(Protocolo.EMPIEZA);
        estados.iniciar();
    }

    public void desmarcarListo(InetAddress ip, int puerto) {
        if (estado.getEstado() != EstadoPartida.LOBBY) return;
        ClienteConectado cliente = conexiones.buscarCliente(ip, puerto);
        if (cliente != null) lobby.desmarcarListo(cliente.getId());
    }

    private void notificarPersonajes() {
        StringBuilder mensaje = new StringBuilder(Protocolo.PERSONAJES);
        for (ClienteConectado cliente : conexiones.getClientes()) {
            String seleccionado = cliente.getPersonajeSeleccionado();
            mensaje.append('|').append(cliente.getId()).append(':')
                    .append(seleccionado == null ? "NINGUNO" : seleccionado);
        }
        emisor.enviarATodos(mensaje.toString());
    }
}
