package org.example.red;

import org.example.juego.GestorPartida;
import org.example.red.gestor.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class ServidorUDP {
    private final int puerto;
    private final Object bloqueo = new Object();
    private volatile boolean ejecutando;
    private DatagramSocket socket;
    private GestorSesiones sesiones;
    private MonitorDesconexiones monitor;
    private ProcesadorMensajes procesador;

    public ServidorUDP(int puerto) {
        this.puerto = puerto;
    }

    public void iniciar() {
        try {
            socket = new DatagramSocket(puerto);
            configurarServicios();
            ejecutando = true;
            System.out.println("Servidor iniciado. Escuchando en el puerto: " + puerto);
            monitor.iniciar();
            while (ejecutando && !socket.isClosed()) {
                byte[] buffer = new byte[1024];
                DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
                socket.receive(paquete);
                String mensaje = new String(paquete.getData(), paquete.getOffset(),
                        paquete.getLength(), StandardCharsets.UTF_8).trim();
                procesador.procesar(mensaje, paquete);
            }
        } catch (SocketException e) {
            if (socket == null) {
                throw new IllegalStateException("No se pudo abrir el puerto UDP " + puerto, e);
            }
            if (ejecutando) System.err.println("Error del socket: " + e.getMessage());
        } catch (IOException e) {
            if (ejecutando) System.err.println("Error: " + e.getMessage());
        } finally {
            detener();
        }
    }

    private void configurarServicios() {
        GestorPartida partida = new GestorPartida();
        GestorConexiones conexiones = new GestorConexiones();
        GestorLobby gestorLobby = new GestorLobby(conexiones);
        ControladorPartida estado = new ControladorPartida();
        GestorDesconexiones detector = new GestorDesconexiones();

        EmisorPaquetesUDP emisor = new EmisorPaquetesUDP(socket, conexiones);
        EmisorEstados estados = new EmisorEstados(partida, estado, emisor);
        sesiones = new GestorSesiones(conexiones, gestorLobby, estado, partida, estados, emisor);
        ControladorLobby lobby = new ControladorLobby(
                conexiones, gestorLobby, estado, partida, emisor, estados);
        ControladorMovimiento movimiento = new ControladorMovimiento(conexiones, estado, partida);
        procesador = new ProcesadorMensajes(bloqueo, conexiones, lobby, movimiento, sesiones, emisor);
        monitor = new MonitorDesconexiones(bloqueo, conexiones, detector, sesiones);
    }

    public void detener() {
        ejecutando = false;
        if (monitor != null) monitor.detener();
        synchronized (bloqueo) {
            if (sesiones != null) sesiones.detenerServidor();
            if (socket != null && !socket.isClosed()) socket.close();
        }
    }
}
