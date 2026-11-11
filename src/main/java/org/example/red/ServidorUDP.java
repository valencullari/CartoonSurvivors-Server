
package org.example.red;

import org.example.red.gestor.*;
import org.example.red.modelo.*;
import org.example.red.protocolo.Protocolo;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ServidorUDP {

    private final int puerto;

    private DatagramSocket socket;

    private final GestorConexiones gestorConexiones;
    private final GestorLobby gestorLobby;
    private final GestorDesconexiones gestorDesconexiones;
    private final ControladorPartida controladorPartida;

    private volatile boolean ejecutando;

    public ServidorUDP(int puerto) {

        this.puerto = puerto;

        gestorConexiones = new GestorConexiones();
        gestorLobby = new GestorLobby(gestorConexiones);
        gestorDesconexiones = new GestorDesconexiones();
        controladorPartida = new ControladorPartida();
    }

    public void iniciar() {

        try {

            socket = new DatagramSocket(puerto);
            ejecutando = true;

            System.out.println("Servidor iniciado.");
            System.out.println("Escuchando en el puerto: " + puerto);

            iniciarControlDesconexiones();

            while (ejecutando && !socket.isClosed()) {

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

                procesarMensaje(mensaje, paquete);
            }

        } catch (SocketException e) {

            if (ejecutando) {
                System.out.println("Error del socket: " + e.getMessage());
            }

        } catch (IOException e) {

            if (ejecutando) {
                System.out.println("Error: " + e.getMessage());
            }

        } finally {

            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    private void procesarMensaje(
            String mensaje,
            DatagramPacket paquete) {

        InetAddress ip = paquete.getAddress();
        int puertoCliente = paquete.getPort();

        System.out.println(
                "Mensaje recibido: " + mensaje + " desde " + ip
        );

        switch (mensaje) {

            case Protocolo.BUSCAR_SERVIDOR:

                enviarRespuesta(
                        Protocolo.SERVIDOR_ENCONTRADO,
                        ip,
                        puertoCliente
                );

                break;

            case Protocolo.CONECTAR:

                conectarCliente(ip, puertoCliente);

                break;

            case Protocolo.KEEP_ALIVE:

                actualizarActividad(ip, puertoCliente);

                break;

            case Protocolo.LISTO:

                marcarListo(ip, puertoCliente);

                break;

            case Protocolo.PARTIDA_FINALIZADA:

                finalizarPartida();

                break;

            case Protocolo.VOLVER_AL_MENU:

            case Protocolo.DESCONECTAR:

                finalizarSesion(
                        ip,
                        puertoCliente,
                        Protocolo.SESION_FINALIZADA
                );

                break;

            default:

                System.out.println("Mensaje desconocido: " + mensaje);

                break;
        }
    }

    private synchronized void conectarCliente(
            InetAddress ip,
            int puertoCliente) {

        if (controladorPartida.getEstado()
                != EstadoPartida.ESPERANDO_JUGADORES) {

            enviarRespuesta(
                    Protocolo.SERVIDOR_LLENO,
                    ip,
                    puertoCliente
            );

            return;
        }

        ClienteConectado cliente =
                gestorConexiones.registrarCliente(ip, puertoCliente);

        if (cliente == null) {

            enviarRespuesta(
                    Protocolo.SERVIDOR_LLENO,
                    ip,
                    puertoCliente
            );

            return;
        }

        String respuesta = cliente.getId() == 1
                ? Protocolo.JUGADOR_1
                : Protocolo.JUGADOR_2;

        enviarRespuesta(respuesta, ip, puertoCliente);

        if (gestorConexiones.cantidadClientes() == 1) {

            controladorPartida.establecerEstado(
                    EstadoPartida.ESPERANDO_JUGADORES
            );

        } else if (gestorConexiones.partidaCompleta()) {

            controladorPartida.establecerEstado(
                    EstadoPartida.LOBBY
            );

            notificarJugadores(Protocolo.JUGADOR_CONECTADO);
        }
    }

    private synchronized void marcarListo(
            InetAddress ip,
            int puertoCliente) {

        if (controladorPartida.getEstado() != EstadoPartida.LOBBY) {
            return;
        }

        ClienteConectado cliente =
                gestorConexiones.buscarCliente(ip, puertoCliente);

        if (cliente == null) {
            return;
        }

        boolean todosListos = gestorLobby.marcarListo(cliente.getId());

        notificarJugadores(Protocolo.JUGADOR_LISTO);

        if (todosListos) {

            controladorPartida.establecerEstado(
                    EstadoPartida.EN_CURSO
            );

            notificarJugadores(Protocolo.EMPIEZA);
        }
    }

    private synchronized void actualizarActividad(
            InetAddress ip,
            int puertoCliente) {

        ClienteConectado cliente =
                gestorConexiones.buscarCliente(ip, puertoCliente);

        if (cliente != null) {
            cliente.actualizarActividad();
        }
    }

    private synchronized void finalizarPartida() {

        if (!controladorPartida.estaEnCurso()) {
            return;
        }

        controladorPartida.establecerEstado(
                EstadoPartida.FINALIZADA
        );

        notificarJugadores(Protocolo.PARTIDA_FINALIZADA);

        System.out.println("Partida finalizada.");
    }

    private synchronized void finalizarSesion(
            InetAddress ip,
            int puertoCliente,
            String mensajeAviso) {

        ClienteConectado cliente =
                gestorConexiones.buscarCliente(ip, puertoCliente);

        if (cliente == null) {
            return;
        }

        System.out.println(
                "Finalizando sesión por salida del jugador "
                        + cliente.getId()
        );

        List<ClienteConectado> jugadores =
                gestorConexiones.getClientes();

        // Avisar a todos los demás jugadores antes de limpiar.
        for (ClienteConectado jugador : jugadores) {

            if (jugador.getId() != cliente.getId()) {

                enviarRespuesta(
                        mensajeAviso,
                        jugador.getDireccionIP(),
                        jugador.getPuerto()
                );
            }
        }

        limpiarSesion();
    }

    private synchronized void limpiarSesion() {

        gestorConexiones.eliminarTodos();
        gestorLobby.reiniciar();
        controladorPartida.reiniciar();

        System.out.println(
                "Servidor disponible para una nueva partida."
        );
    }

    private void iniciarControlDesconexiones() {

        Thread hilo = new Thread(new Runnable() {

            @Override
            public void run() {

                while (ejecutando) {

                    try {

                        Thread.sleep(1000);

                        List<ClienteConectado> desconectados =
                                gestorDesconexiones.detectarDesconexiones(
                                        gestorConexiones
                                );

                        if (!desconectados.isEmpty()) {

                            synchronized (ServidorUDP.this) {

                                if (ejecutando
                                        && !desconectados.isEmpty()) {

                                    ClienteConectado desconectado =
                                            desconectados.get(0);

                                    System.out.println(
                                            "Jugador "
                                                    + desconectado.getId()
                                                    + " desconectado inesperadamente."
                                    );

                                    finalizarSesion(
                                            desconectado.getDireccionIP(),
                                            desconectado.getPuerto(),
                                            Protocolo.SESION_FINALIZADA
                                    );
                                }
                            }
                        }

                    } catch (InterruptedException e) {

                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

        });

        hilo.setDaemon(true);
        hilo.start();
    }

    private void notificarJugadores(String mensaje) {

        for (ClienteConectado cliente :
                gestorConexiones.getClientes()) {

            enviarRespuesta(
                    mensaje,
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

            System.out.println("Respuesta enviada: " + mensaje);

        } catch (IOException e) {

            System.out.println(
                    "Error enviando respuesta: " + e.getMessage()
            );
        }
    }

    public void detener() {

        ejecutando = false;

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
