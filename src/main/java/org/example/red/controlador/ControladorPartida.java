
package org.example.red.controlador;

import org.example.red.gestor.GestorConexiones;
import org.example.red.gestor.GestorDesconexiones;
import org.example.red.gestor.GestorLobby;
import org.example.red.modelo.ClienteConectado;
import org.example.red.protocolo.Protocolo;

import java.net.InetAddress;
import java.util.List;
import java.util.function.BiConsumer;

public class ControladorPartida {

    private final GestorConexiones gestorConexiones;
    private final GestorLobby gestorLobby;
    private final GestorDesconexiones gestorDesconexiones;

    // Permite enviar un mensaje a un cliente sin conocer el socket.
    private final BiConsumer<ClienteConectado, String> emisor;

    public ControladorPartida(
            GestorConexiones gestorConexiones,
            GestorLobby gestorLobby,
            GestorDesconexiones gestorDesconexiones,
            BiConsumer<ClienteConectado, String> emisor) {

        this.gestorConexiones = gestorConexiones;
        this.gestorLobby = gestorLobby;
        this.gestorDesconexiones = gestorDesconexiones;
        this.emisor = emisor;
    }

    public synchronized void procesarMensaje(
            String mensaje,
            InetAddress ip,
            int puerto) {

        switch (mensaje) {

            case Protocolo.CONECTAR:
                conectarJugador(ip, puerto);
                break;

            case Protocolo.LISTO:
                cambiarEstadoListo(ip, puerto, true);
                break;

            case Protocolo.NO_LISTO:
                cambiarEstadoListo(ip, puerto, false);
                break;

            case Protocolo.KEEP_ALIVE:
                actualizarActividad(ip, puerto);
                break;

            case Protocolo.DESCONECTAR:
                desconectarJugador(ip, puerto);
                break;

            default:
                System.out.println(
                        "Mensaje desconocido: " + mensaje
                );
                break;
        }
    }

    private void conectarJugador(InetAddress ip, int puerto) {

        if (!gestorLobby.estaEnLobby()) {
            return;
        }

        ClienteConectado cliente =
                gestorConexiones.registrarCliente(ip, puerto);

        if (cliente == null) {
            return;
        }

        String respuesta = cliente.getId() == 1
                ? Protocolo.JUGADOR_1
                : Protocolo.JUGADOR_2;

        emisor.accept(cliente, respuesta);

        System.out.println(
                "Jugador registrado: " + cliente.getId()
        );

        enviarEstadoLobby();
    }

    private void cambiarEstadoListo(
            InetAddress ip,
            int puerto,
            boolean listo) {

        ClienteConectado cliente =
                gestorConexiones.buscarCliente(ip, puerto);

        if (cliente == null || !gestorLobby.estaEnLobby()) {
            return;
        }

        cliente.actualizarActividad();

        if (!gestorLobby.marcarListo(cliente, listo)) {
            return;
        }

        System.out.println(
                "Jugador " + cliente.getId()
                        + (listo ? " está LISTO." : " ya no está LISTO.")
        );

        enviarEstadoLobby();

        if (gestorLobby.iniciarPartida(gestorConexiones)) {

            System.out.println("Ambos jugadores están listos.");
            System.out.println("Iniciando partida...");

            enviarATodos(Protocolo.EMPIEZA);
        }
    }

    private void actualizarActividad(
            InetAddress ip,
            int puerto) {

        ClienteConectado cliente =
                gestorConexiones.buscarCliente(ip, puerto);

        if (cliente != null) {
            cliente.actualizarActividad();
        }
    }

    private void desconectarJugador(
            InetAddress ip,
            int puerto) {

        ClienteConectado cliente =
                gestorConexiones.buscarCliente(ip, puerto);

        if (cliente != null) {
            gestionarDesconexion(cliente);
        }
    }

    public synchronized void comprobarDesconexiones() {

        List<ClienteConectado> desconectados =
                gestorDesconexiones.detectarDesconexiones(
                        gestorConexiones
                );

        for (ClienteConectado cliente : desconectados) {

            System.out.println(
                    "Jugador " + cliente.getId()
                            + " desconectado inesperadamente."
            );

            gestionarDesconexion(cliente);
        }
    }

    private void gestionarDesconexion(ClienteConectado cliente) {

        boolean partidaEstabaIniciada =
                gestorLobby.estaIniciada();

        gestorConexiones.eliminarCliente(
                cliente.getDireccionIP(),
                cliente.getPuerto()
        );

        if (partidaEstabaIniciada) {

            gestorLobby.finalizarPartida();

            enviarATodos(
                    Protocolo.OTRO_JUGADOR_DESCONECTADO
            );

            System.out.println(
                    "Partida finalizada por desconexión."
            );

        } else {

            gestorLobby.reiniciarConfirmaciones(
                    gestorConexiones
            );

            enviarEstadoLobby();
        }
    }

    private void enviarEstadoLobby() {

        ClienteConectado jugador1 =
                gestorConexiones.buscarCliente(1);

        ClienteConectado jugador2 =
                gestorConexiones.buscarCliente(2);

        String estado1 = jugador1 == null
                ? "0:0"
                : "1:" + (jugador1.estaListo() ? "1" : "0");

        String estado2 = jugador2 == null
                ? "0:0"
                : "1:" + (jugador2.estaListo() ? "1" : "0");

        String mensaje = Protocolo.LOBBY_ACTUALIZADO
                + "|1:" + estado1
                + "|2:" + estado2;

        enviarATodos(mensaje);
    }

    private void enviarATodos(String mensaje) {

        for (ClienteConectado cliente :
                gestorConexiones.getClientes()) {

            emisor.accept(cliente, mensaje);
        }
    }

}
