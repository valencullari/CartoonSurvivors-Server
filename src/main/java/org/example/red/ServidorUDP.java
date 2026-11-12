package org.example.red;

import org.example.juego.GestorPartida;
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
    private final GestorPartida gestorPartida;
    private Thread hiloEstados;
    private boolean enviandoEstados = false;
    private long secuenciaEstado = 0;


    private volatile boolean ejecutando;

    public ServidorUDP(int puerto) {

        this.puerto = puerto;

        gestorPartida = new GestorPartida();
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

                DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);

                socket.receive(paquete);

                String mensaje = new String(paquete.getData(), paquete.getOffset(), paquete.getLength(), StandardCharsets.UTF_8).trim();

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
            detenerEnvioEstados();
            gestorPartida.detenerPartida();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    private void procesarMensaje(String mensaje, DatagramPacket paquete) {

        InetAddress ip = paquete.getAddress();
        int puertoCliente = paquete.getPort();

        if (!mensaje.equals(Protocolo.KEEP_ALIVE)
                && !mensaje.startsWith(Protocolo.MOVER + "|")) {

            System.out.println(
                    "Mensaje recibido: " + mensaje + " desde " + ip
            );
        }

        if (mensaje.startsWith(Protocolo.PERSONAJE + "|")) {

            String[] partes = mensaje.split("\\|", -1);

            if (partes.length == 2) {

                seleccionarPersonaje(ip, puertoCliente, partes[1]);

            } else {
                System.out.println("Formato de selección de personaje inválido.");
            }

            return;
        }

        if (mensaje.startsWith(Protocolo.MOVER + "|")) {

            procesarMovimiento(
                    mensaje,
                    ip,
                    puertoCliente
            );

            return;
        }

        switch (mensaje) {

            case Protocolo.BUSCAR_SERVIDOR:

                enviarRespuesta(Protocolo.SERVIDOR_ENCONTRADO, ip, puertoCliente);

                break;

            case Protocolo.CONECTAR:

                conectarCliente(ip, puertoCliente);

                break;


            case Protocolo.KEEP_ALIVE:

                responderKeepAlive(ip, puertoCliente);

                break;


            case Protocolo.LISTO:

                marcarListo(ip, puertoCliente);

                break;

            case Protocolo.NO_LISTO:

                desmarcarListo(ip, puertoCliente);

                break;

            case Protocolo.PARTIDA_FINALIZADA:

                finalizarPartida();

                break;

            case Protocolo.VOLVER_AL_MENU:

            case Protocolo.DESCONECTAR:

                finalizarSesion(ip, puertoCliente, Protocolo.SESION_FINALIZADA);

                break;

            default:

                System.out.println("Mensaje desconocido: " + mensaje);

                break;
        }
    }

    private synchronized void conectarCliente(InetAddress ip, int puertoCliente) {

        if (controladorPartida.getEstado() != EstadoPartida.ESPERANDO_JUGADORES) {

            enviarRespuesta(Protocolo.SERVIDOR_LLENO, ip, puertoCliente);

            return;
        }

        ClienteConectado cliente = gestorConexiones.registrarCliente(ip, puertoCliente);

        if (cliente == null) {

            enviarRespuesta(Protocolo.SERVIDOR_LLENO, ip, puertoCliente);

            return;
        }

        String respuesta = cliente.getId() == 1 ? Protocolo.JUGADOR_1 : Protocolo.JUGADOR_2;

        enviarRespuesta(respuesta, ip, puertoCliente);

        if (gestorConexiones.cantidadClientes() == 1) {

            controladorPartida.establecerEstado(EstadoPartida.ESPERANDO_JUGADORES);

        } else if (gestorConexiones.partidaCompleta()) {

            controladorPartida.establecerEstado(EstadoPartida.LOBBY);

            notificarJugadores(Protocolo.JUGADOR_CONECTADO);
        }
    }


    private synchronized void procesarMovimiento(
            String mensaje,
            InetAddress ip,
            int puertoCliente) {

        // Solo permitir movimiento durante la partida.
        if (controladorPartida.getEstado()
                != EstadoPartida.EN_CURSO) {
            return;
        }

        // Identificar al jugador que envió el paquete.
        ClienteConectado cliente =
                gestorConexiones.buscarCliente(
                        ip,
                        puertoCliente
                );

        if (cliente == null) {
            return;
        }

        // Ejemplo: MOVER|1|0
        String[] partes = mensaje.split("\\|", -1);

        if (partes.length != 3) {
            return;
        }

        try {

            float direccionX =
                    Float.parseFloat(partes[1]);

            float direccionY =
                    Float.parseFloat(partes[2]);

            if (!Float.isFinite(direccionX)
                    || !Float.isFinite(direccionY)) {
                return;
            }

            // Actualizar la actividad del cliente.
            cliente.actualizarActividad();

            // Enviar el movimiento a la partida.
            gestorPartida.moverJugador(
                    cliente.getId(),
                    direccionX,
                    direccionY
            );

        } catch (NumberFormatException e) {

            System.out.println(
                    "Movimiento invalido: " + mensaje
            );
        }
    }

    private synchronized void seleccionarPersonaje(InetAddress ip, int puertoCliente, String personaje) {

        // Solo se puede seleccionar mientras
        // esperamos jugadores o estamos en el lobby.

        EstadoPartida estado = controladorPartida.getEstado();

        if (estado != EstadoPartida.ESPERANDO_JUGADORES && estado != EstadoPartida.LOBBY) {
            return;
        }

        // Buscar al cliente que envió el paquete.

        ClienteConectado cliente = gestorConexiones.buscarCliente(ip, puertoCliente);

        if (cliente == null) {
            return;
        }

        // No permitimos cambiar el personaje
        // después de confirmar LISTO.

        if (cliente.isListo()) {
            return;
        }

        // Por ahora solo existe Mordecai.
        // Después agregaremos más personajes.

        if (!personaje.equals("MORDECAI")) {

            System.out.println("Personaje no válido: " + personaje);

            return;
        }

        // Guardar la selección del jugador.

        cliente.setPersonajeSeleccionado(personaje);
        cliente.actualizarActividad();

        System.out.println("Jugador " + cliente.getId() + " seleccionó: " + personaje);

        // Avisar a los clientes qué personajes
        // están seleccionados.

        notificarPersonajes();
    }


    private synchronized void iniciarEnvioEstados() {

        if (enviandoEstados) {
            return;
        }

        enviandoEstados = true;
        secuenciaEstado = 0;

        hiloEstados = new Thread(new Runnable() {

            @Override
            public void run() {

                while (estaEnviandoEstados()) {

                    // Guardar el momento de inicio.
                    long inicio = System.nanoTime();

                    // Enviar posiciones de los jugadores.
                    enviarEstadoJugadores();

                    // Calcular cuánto tardó el envío.
                    long duracion = System.nanoTime() - inicio;

                    // Queremos un envío cada 33,33 ms.
                    long intervalo = 1_000_000_000L / 30;

                    long tiempoRestante = intervalo - duracion;

                    try {

                        if (tiempoRestante > 0) {

                            long milisegundos = tiempoRestante / 1_000_000;

                            int nanosegundos = (int) (tiempoRestante % 1_000_000);

                            Thread.sleep(milisegundos, nanosegundos);
                        }

                    } catch (InterruptedException e) {

                        // Detener el hilo al finalizar la partida.
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

        }, "HiloEnvioEstados");

        hiloEstados.setDaemon(true);
        hiloEstados.start();

        System.out.println("Envio de estados iniciado a 30 Hz.");
    }


    private synchronized void notificarPersonajes() {

        StringBuilder mensaje = new StringBuilder(Protocolo.PERSONAJES);

        for (ClienteConectado cliente : gestorConexiones.getClientes()) {

            String personaje = cliente.getPersonajeSeleccionado();

            if (personaje == null) {
                personaje = "NINGUNO";
            }

            mensaje.append("|").append(cliente.getId()).append(":").append(personaje);
        }

        notificarJugadores(mensaje.toString());
    }

    private synchronized void desmarcarListo(
            InetAddress ip,
            int puertoCliente) {

        if (controladorPartida.getEstado()
                != EstadoPartida.LOBBY) {
            return;
        }

        ClienteConectado cliente =
                gestorConexiones.buscarCliente(ip, puertoCliente);

        if (cliente == null) {
            return;
        }

        gestorLobby.desmarcarListo(cliente.getId());
    }


    private synchronized void marcarListo(InetAddress ip, int puertoCliente) {

        if (controladorPartida.getEstado() != EstadoPartida.LOBBY) {
            return;
        }

        ClienteConectado cliente = gestorConexiones.buscarCliente(ip, puertoCliente);

        if (cliente == null) {
            return;
        }

        if (cliente.getPersonajeSeleccionado() == null) {
            return;
        }

        boolean todosListos = gestorLobby.marcarListo(cliente.getId());

        notificarJugadores(Protocolo.JUGADOR_LISTO);


        if (todosListos) {

            ClienteConectado jugador1 =
                    gestorConexiones.buscarCliente(1);

            ClienteConectado jugador2 =
                    gestorConexiones.buscarCliente(2);

            if (jugador1 == null || jugador2 == null) {
                return;
            }

            String personaje1 =
                    jugador1.getPersonajeSeleccionado();

            String personaje2 =
                    jugador2.getPersonajeSeleccionado();

            if (personaje1 == null || personaje2 == null) {
                return;
            }

            // Crear la partida con los dos jugadores.
            gestorPartida.iniciarPartida(
                    personaje1,
                    personaje2
            );

            // Cambiar el estado del servidor.
            controladorPartida.establecerEstado(
                    EstadoPartida.EN_CURSO
            );

            // Enviar las selecciones de personajes.
            notificarPersonajes();

            // Avisar que comienza la partida.
            notificarJugadores(Protocolo.EMPIEZA);

            // Iniciar el hilo que envía posiciones.
            iniciarEnvioEstados();
        }

    }

    private synchronized void finalizarPartida() {


        if (!controladorPartida.estaEnCurso()) {
            return;
        }

        controladorPartida.establecerEstado(EstadoPartida.FINALIZADA);
        detenerEnvioEstados();
        gestorPartida.detenerPartida();


        notificarJugadores(Protocolo.PARTIDA_FINALIZADA);

        System.out.println("Partida finalizada.");
    }

    private synchronized void finalizarSesion(InetAddress ip, int puertoCliente, String mensajeAviso) {

        ClienteConectado cliente = gestorConexiones.buscarCliente(ip, puertoCliente);

        if (cliente == null) {
            return;
        }

        System.out.println("Finalizando sesión por salida del jugador " + cliente.getId());

        List<ClienteConectado> jugadores = gestorConexiones.getClientes();

        // Avisar a todos los demás jugadores antes de limpiar.
        for (ClienteConectado jugador : jugadores) {

            if (jugador.getId() != cliente.getId()) {

                enviarRespuesta(mensajeAviso, jugador.getDireccionIP(), jugador.getPuerto());
            }
        }

        limpiarSesion();
    }

    private synchronized void limpiarSesion() {

        detenerEnvioEstados();
        gestorPartida.detenerPartida();

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

                        List<ClienteConectado> desconectados = gestorDesconexiones.detectarDesconexiones(gestorConexiones);

                        if (!desconectados.isEmpty()) {

                            synchronized (ServidorUDP.this) {

                                if (ejecutando && !desconectados.isEmpty()) {

                                    ClienteConectado desconectado = desconectados.get(0);

                                    System.out.println("Jugador " + desconectado.getId() + " desconectado inesperadamente.");

                                    finalizarSesion(desconectado.getDireccionIP(), desconectado.getPuerto(), Protocolo.SESION_FINALIZADA);
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

    private synchronized void responderKeepAlive(InetAddress ip, int puertoCliente) {

        ClienteConectado cliente = gestorConexiones.buscarCliente(ip, puertoCliente);

        if (cliente == null) {
            return;
        }

        // Actualizar actividad, como antes.
        cliente.actualizarActividad();

        // Confirmar que el servidor sigue funcionando.
        enviarRespuesta(Protocolo.KEEP_ALIVE_OK, ip, puertoCliente);
    }


    private synchronized void enviarEstadoJugadores() {

        if (controladorPartida.getEstado() != EstadoPartida.EN_CURSO) {
            return;
        }

        if (!gestorPartida.hayPartidaActiva()) {
            return;
        }

        secuenciaEstado++;

        String mensaje = gestorPartida.obtenerEstado(secuenciaEstado);

        if (mensaje == null) {
            return;
        }

        notificarJugadores(mensaje);
    }


    private synchronized void detenerEnvioEstados() {

        enviandoEstados = false;

        if (hiloEstados != null) {

            hiloEstados.interrupt();

            hiloEstados = null;
        }

        System.out.println("Envio de estados detenido.");
    }


    private void notificarJugadores(String mensaje) {

        for (ClienteConectado cliente : gestorConexiones.getClientes()) {

            enviarRespuesta(mensaje, cliente.getDireccionIP(), cliente.getPuerto());
        }
    }

    private void enviarRespuesta(String mensaje, InetAddress direccion, int puertoDestino) {

        try {

            byte[] datos = mensaje.getBytes(StandardCharsets.UTF_8);

            DatagramPacket paquete = new DatagramPacket(datos, datos.length, direccion, puertoDestino);

            socket.send(paquete);

            if (!mensaje.equals(Protocolo.KEEP_ALIVE_OK)
                    && !mensaje.startsWith(Protocolo.ESTADO + "|")) {

                System.out.println(
                        "Respuesta enviada: " + mensaje
                );
            }

        } catch (IOException e) {

            System.out.println("Error enviando respuesta: " + e.getMessage());
        }
    }

    private synchronized boolean estaEnviandoEstados() {
        return enviandoEstados;
    }

    public void detener() {

        detenerEnvioEstados();
        gestorPartida.detenerPartida();
        ejecutando = false;

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
