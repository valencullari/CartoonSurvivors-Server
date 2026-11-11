
package org.example.red;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ServidorUDP {

    private static final long TIEMPO_MAXIMO = 6000;

    private final int puerto;
    private DatagramSocket socket;

    private final GestorConexiones gestorConexiones;

    private boolean partidaIniciada = false;
    private boolean partidaFinalizada = false;

    public ServidorUDP(int puerto) {
        this.puerto = puerto;
        this.gestorConexiones = new GestorConexiones();
    }

    public void iniciar() {

        try {

            socket = new DatagramSocket(puerto);
            socket.setSoTimeout(1000);

            System.out.println("Servidor iniciado.");
            System.out.println("Escuchando en el puerto: " + puerto);

            while (!socket.isClosed()) {

                try {

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

                } catch (SocketTimeoutException e) {
                    // No llegó ningún paquete.
                }

                revisarDesconexiones();
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

        InetAddress ip = paqueteRecibido.getAddress();
        int puertoCliente = paqueteRecibido.getPort();

        switch (mensaje) {

            case Protocolo.BUSCAR_SERVIDOR:

                enviarRespuesta(
                        Protocolo.SERVIDOR_ENCONTRADO,
                        ip,
                        puertoCliente
                );

                System.out.println("Servidor encontrado por un cliente.");

                break;

            case Protocolo.CONECTAR:

                if (partidaFinalizada) {

                    // No permitir entrar a una partida ya finalizada.
                    enviarRespuesta(
                            Protocolo.PARTIDA_FINALIZADA,
                            ip,
                            puertoCliente
                    );

                    break;
                }

                ClienteConectado cliente =
                        gestorConexiones.registrarCliente(ip, puertoCliente);

                if (cliente == null) {

                    enviarRespuesta(
                            Protocolo.SERVIDOR_LLENO,
                            ip,
                            puertoCliente
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

                    if (gestorConexiones.partidaCompleta()
                            && !partidaIniciada) {

                        iniciarPartida();
                    }
                }

                break;

            case Protocolo.KEEP_ALIVE:

                ClienteConectado clienteActivo =
                        gestorConexiones.buscarCliente(ip, puertoCliente);

                if (clienteActivo != null) {
                    clienteActivo.actualizarActividad();
                }

                break;

            case Protocolo.DESCONECTAR:

                ClienteConectado desconectado =
                        gestorConexiones.buscarCliente(ip, puertoCliente);

                if (desconectado != null) {

                    desconectado.desconectar();

                    System.out.println(
                            "Jugador " + desconectado.getId()
                                    + " desconectado normalmente."
                    );

                    finalizarPorDesconexion(desconectado);
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

        partidaIniciada = true;

        System.out.println("Los dos jugadores están conectados.");
        System.out.println("Iniciando partida...");

        for (ClienteConectado cliente :
                gestorConexiones.getClientes()) {

            if (cliente.isConectado()) {

                enviarRespuesta(
                        Protocolo.EMPIEZA,
                        cliente.getDireccionIP(),
                        cliente.getPuerto()
                );
            }
        }
    }

    private void revisarDesconexiones() {

        List<ClienteConectado> desconectados =
                gestorConexiones.detectarDesconectados(TIEMPO_MAXIMO);

        for (ClienteConectado cliente : desconectados) {

            System.out.println(
                    "Jugador " + cliente.getId()
                            + " desconectado inesperadamente."
            );

            finalizarPorDesconexion(cliente);
        }
    }

    private void finalizarPorDesconexion(
            ClienteConectado desconectado) {

        if (!partidaIniciada || partidaFinalizada) {
            return;
        }

        partidaFinalizada = true;

        System.out.println("Partida finalizada por desconexión.");

        for (ClienteConectado cliente :
                gestorConexiones.getClientes()) {

            if (cliente.isConectado()) {

                enviarRespuesta(
                        Protocolo.OTRO_JUGADOR_DESCONECTADO,
                        cliente.getDireccionIP(),
                        cliente.getPuerto()
                );
            }
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
