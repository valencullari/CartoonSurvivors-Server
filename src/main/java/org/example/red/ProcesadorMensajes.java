package org.example.red;

import org.example.red.gestor.*;
import org.example.red.modelo.ClienteConectado;
import org.example.red.protocolo.Protocolo;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class ProcesadorMensajes {
    private final Object bloqueo;
    private final GestorConexiones conexiones;
    private final ControladorLobby lobby;
    private final ControladorMovimiento movimiento;
    private final GestorSesiones sesiones;
    private final EmisorPaquetesUDP emisor;

    public ProcesadorMensajes(Object bloqueo, GestorConexiones conexiones,
                              ControladorLobby lobby, ControladorMovimiento movimiento,
                              GestorSesiones sesiones, EmisorPaquetesUDP emisor) {
        this.bloqueo = bloqueo;
        this.conexiones = conexiones;
        this.lobby = lobby;
        this.movimiento = movimiento;
        this.sesiones = sesiones;
        this.emisor = emisor;
    }

    public void procesar(String mensaje, DatagramPacket paquete) {
        synchronized (bloqueo) {
            InetAddress ip = paquete.getAddress();
            int puerto = paquete.getPort();
            if (!mensaje.equals(Protocolo.KEEP_ALIVE)
                    && !mensaje.startsWith(Protocolo.MOVER + "|")) {
                System.out.println("Mensaje recibido: " + mensaje + " desde " + ip);
            }

            if (mensaje.startsWith(Protocolo.PERSONAJE + "|")) {
                String[] partes = mensaje.split("\\|", -1);
                if (partes.length == 2) lobby.seleccionarPersonaje(ip, puerto, partes[1]);
                else System.out.println("Formato de seleccion de personaje invalido.");
                return;
            }
            if (mensaje.startsWith(Protocolo.MOVER + "|")) {
                movimiento.procesar(mensaje, ip, puerto);
                return;
            }

            switch (mensaje) {
                case Protocolo.BUSCAR_SERVIDOR:
                    emisor.enviarACliente(Protocolo.SERVIDOR_ENCONTRADO, ip, puerto);
                    break;
                case Protocolo.CONECTAR:
                    lobby.conectar(ip, puerto);
                    break;
                case Protocolo.KEEP_ALIVE:
                    ClienteConectado cliente = conexiones.buscarCliente(ip, puerto);
                    if (cliente != null) {
                        cliente.actualizarActividad();
                        emisor.enviarACliente(Protocolo.KEEP_ALIVE_OK, ip, puerto);
                    }
                    break;
                case Protocolo.LISTO:
                    lobby.marcarListo(ip, puerto);
                    break;
                case Protocolo.NO_LISTO:
                    lobby.desmarcarListo(ip, puerto);
                    break;
                case Protocolo.PARTIDA_FINALIZADA:
                    sesiones.finalizarPartida(ip, puerto);
                    break;
                case Protocolo.VOLVER_AL_MENU:
                case Protocolo.DESCONECTAR:
                    sesiones.finalizarSesion(ip, puerto);
                    break;
                default:
                    System.out.println("Mensaje desconocido: " + mensaje);
            }
        }
    }
}
