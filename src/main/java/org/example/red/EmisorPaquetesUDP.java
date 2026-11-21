package org.example.red;

import org.example.red.gestor.GestorConexiones;
import org.example.red.modelo.ClienteConectado;
import org.example.red.protocolo.Protocolo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class EmisorPaquetesUDP {
    private final DatagramSocket socket;
    private final GestorConexiones conexiones;

    public EmisorPaquetesUDP(DatagramSocket socket, GestorConexiones conexiones) {
        this.socket = socket;
        this.conexiones = conexiones;
    }

    public void enviarACliente(String mensaje, InetAddress ip, int puerto) {
        try {
            byte[] datos = mensaje.getBytes(StandardCharsets.UTF_8);
            socket.send(new DatagramPacket(datos, datos.length, ip, puerto));
            if (!mensaje.equals(Protocolo.KEEP_ALIVE_OK)
                    && !mensaje.startsWith(Protocolo.ESTADO + "|")
                    && !mensaje.startsWith(Protocolo.HUD + "|")
                    && !mensaje.startsWith(Protocolo.ENEMIGOS + "|")) {
                System.out.println("Respuesta enviada: " + mensaje);
            }
        } catch (IOException e) {
            if (!socket.isClosed()) {
                System.out.println("Error enviando respuesta: " + e.getMessage());
            }
        }
    }

    public void enviarATodos(String mensaje) {
        for (ClienteConectado cliente : conexiones.getClientes()) {
            enviarACliente(mensaje, cliente.getDireccionIP(), cliente.getPuerto());
        }
    }
}
