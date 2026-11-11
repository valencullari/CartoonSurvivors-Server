
package org.example.red;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class ServidorUDP {

    private final int puerto;
    private DatagramSocket socket;

    private final GestorConexiones gestorConexiones;

    public ServidorUDP(int puerto) {
        this.puerto = puerto;
        this.gestorConexiones = new GestorConexiones();
    }

    public void iniciar() {

        try {

            socket = new DatagramSocket(puerto);

            System.out.println("Servidor iniciado.");
            System.out.println("Escuchando en el puerto: " + puerto);

            while (!socket.isClosed()) {

                byte[] buffer = new byte[1024];

                DatagramPacket paqueteRecibido =
                        new DatagramPacket(buffer, buffer.length);

                socket.receive(paqueteRecibido);

                String mensaje = new String(
                        paqueteRecibido.getData(),
                        0,
                        paqueteRecibido.getLength(),
                        StandardCharsets.UTF_8
                ).trim();

                System.out.println(
                        "Mensaje recibido: " + mensaje
                                + " desde "
                                + paqueteRecibido.getAddress()
                );

                procesarMensaje(mensaje, paqueteRecibido);
            }

        } catch (SocketException e) {

            System.out.println("Socket cerrado o error de conexión.");

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

    private void procesarMensaje(
            String mensaje,
            DatagramPacket paqueteRecibido) {

        switch (mensaje) {

            // El cliente busca servidores mediante Broadcast
            case Protocolo.BUSCAR_SERVIDOR:

                enviarRespuesta(
                        Protocolo.SERVIDOR_ENCONTRADO,
                        paqueteRecibido.getAddress(),
                        paqueteRecibido.getPort()
                );

                System.out.println("Servidor encontrado por un cliente.");

                break;

            // El cliente solicita conectarse
            case Protocolo.CONECTAR:

                ClienteConectado cliente =
                        gestorConexiones.registrarCliente(
                                paqueteRecibido.getAddress(),
                                paqueteRecibido.getPort()
                        );

                if (cliente == null) {

                    enviarRespuesta(
                            Protocolo.SERVIDOR_LLENO,
                            paqueteRecibido.getAddress(),
                            paqueteRecibido.getPort()
                    );

                } else {

                    String respuesta = cliente.getId() == 1
                            ? Protocolo.JUGADOR_1
                            : Protocolo.JUGADOR_2;

                    enviarRespuesta(
                            respuesta,
                            cliente.getDireccionIP(),
                            cliente.getPuerto()
                    );

                    System.out.println(
                            "Jugador registrado: " + cliente.getId()
                    );

                    if (gestorConexiones.partidaCompleta()) {
                        iniciarPartida();
                    }
                }

                break;

            // El cliente avisa que se desconecta
            case Protocolo.DESCONECTAR:

                ClienteConectado desconectado =
                        gestorConexiones.buscarCliente(
                                paqueteRecibido.getAddress(),
                                paqueteRecibido.getPort()
                        );

                if (desconectado != null) {

                    desconectado.desconectar();

                    System.out.println(
                            "Jugador desconectado: "
                                    + desconectado.getId()
                    );
                }

                break;

            default:

                System.out.println(
                        "Mensaje desconocido: " + mensaje
                );

                break;
        }
    }

    private void iniciarPartida() {

        System.out.println("Los dos jugadores están conectados.");
        System.out.println("Iniciando partida...");

        for (ClienteConectado cliente :
                gestorConexiones.getClientes()) {

            enviarRespuesta(
                    Protocolo.EMPIEZA,
                    cliente.getDireccionIP(),
                    cliente.getPuerto()
            );
        }
    }

    private void enviarRespuesta(
            String mensaje,
            InetAddress direccion,
            int puertoDestino) {

        try {

            byte[] datos = mensaje.getBytes(StandardCharsets.UTF_8);

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
                    "Error al enviar respuesta: " + e.getMessage()
            );
        }
    }
}
