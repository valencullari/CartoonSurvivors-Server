
package org.example.red;

import org.example.red.controlador.ControladorPartida;
import org.example.red.gestor.GestorConexiones;
import org.example.red.gestor.GestorDesconexiones;
import org.example.red.gestor.GestorLobby;
import org.example.red.modelo.ClienteConectado;
import org.example.red.protocolo.Protocolo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class ServidorUDP {

    private final int puerto;

    private DatagramSocket socket;

    private final GestorConexiones gestorConexiones;
    private final ControladorPartida controlador;

    public ServidorUDP(int puerto) {

        this.puerto = puerto;

        this.gestorConexiones = new GestorConexiones();

        GestorLobby gestorLobby = new GestorLobby();

        GestorDesconexiones gestorDesconexiones =
                new GestorDesconexiones();

        this.controlador = new ControladorPartida(
                gestorConexiones,
                gestorLobby,
                gestorDesconexiones,
                this::enviarMensaje
        );
    }

    public void iniciar() {

        try {

            socket = new DatagramSocket(puerto);
            socket.setSoTimeout(1000);

            System.out.println("Servidor iniciado.");
            System.out.println(
                    "Escuchando en el puerto: " + puerto
            );

            while (!socket.isClosed()) {

                try {

                    recibirPaquete();

                } catch (SocketTimeoutException e) {

                    // Permite comprobar desconexiones periódicamente.

                }

                controlador.comprobarDesconexiones();
            }

        } catch (IOException e) {

            System.out.println(
                    "Error en el servidor: " + e.getMessage()
            );

        } finally {

            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    private void recibirPaquete() throws IOException {

        byte[] buffer = new byte[1024];

        DatagramPacket paquete =
                new DatagramPacket(buffer, buffer.length);

        socket.receive(paquete);

        String mensaje = new String(
                paquete.getData(),
                paquete.getOffset(),
                paquete.getLength(),
                StandardCharsets.UTF_8
        ).trim();

        InetAddress ip = paquete.getAddress();
        int puertoOrigen = paquete.getPort();

        System.out.println(
                "Mensaje recibido: " + mensaje
                        + " desde " + ip
        );

        if (mensaje.equals(Protocolo.BUSCAR_SERVIDOR)) {

            enviarRespuesta(
                    Protocolo.SERVIDOR_ENCONTRADO,
                    ip,
                    puertoOrigen
            );

            System.out.println(
                    "Servidor encontrado por un cliente."
            );

            return;
        }

        controlador.procesarMensaje(
                mensaje,
                ip,
                puertoOrigen
        );
    }

    private void enviarMensaje(
            ClienteConectado cliente,
            String mensaje) {

        enviarRespuesta(
                mensaje,
                cliente.getDireccionIP(),
                cliente.getPuerto()
        );
    }

    private void enviarRespuesta(
            String mensaje,
            InetAddress direccion,
            int puertoDestino) {

        if (socket == null || socket.isClosed()) {
            return;
        }

        try {

            byte[] datos = mensaje.getBytes(
                    StandardCharsets.UTF_8
            );

            DatagramPacket paquete = new DatagramPacket(
                    datos,
                    datos.length,
                    direccion,
                    puertoDestino
            );

            socket.send(paquete);

            System.out.println(
                    "Respuesta enviada: " + mensaje
            );

        } catch (IOException e) {

            System.out.println(
                    "Error al enviar mensaje: " + e.getMessage()
            );
        }
    }
}
