package org.example.red;

import org.example.juego.GestorPartida;
import org.example.red.gestor.ControladorPartida;
import org.example.red.modelo.EstadoPartida;
import java.util.List;

public class EmisorEstados {
    private static final int ESTADOS_POR_SEGUNDO = 30;
    private static final long INTERVALO_NANOS = 1_000_000_000L / ESTADOS_POR_SEGUNDO;
    private static final int INTERVALO_HUD_EN_ESTADOS = 6;
    private final GestorPartida partida;
    private final ControladorPartida controlador;
    private final EmisorPaquetesUDP emisor;
    private volatile Thread hilo;

    public EmisorEstados(GestorPartida partida, ControladorPartida controlador,
                         EmisorPaquetesUDP emisor) {
        this.partida = partida;
        this.controlador = controlador;
        this.emisor = emisor;
    }

    public synchronized void iniciar() {
        if (hilo != null) return;
        Thread nuevo = new Thread("HiloEnvioEstados") {
            @Override
            public void run() {
                bucle();
            }
        };
        nuevo.setDaemon(true);
        hilo = nuevo;
        nuevo.start();
        System.out.println("Envio de estados iniciado a 30 Hz.");
    }

    private void bucle() {
        Thread actual = Thread.currentThread();
        long secuencia = 0;
        while (hilo == actual && !actual.isInterrupted()) {
            long inicio = System.nanoTime();
            enviar(++secuencia, actual);
            long restante = INTERVALO_NANOS - (System.nanoTime() - inicio);
            if (restante > 0) {
                try {
                    Thread.sleep(restante / 1_000_000L, (int) (restante % 1_000_000L));
                } catch (InterruptedException e) {
                    actual.interrupt();
                    break;
                }
            }
        }
    }

    private void enviar(long secuencia, Thread actual) {
        if (hilo != actual || controlador.getEstado() != EstadoPartida.EN_CURSO
                || !partida.hayPartidaActiva()) return;

        String jugadores = partida.obtenerEstado(secuencia);
        if (jugadores != null && hilo == actual) emisor.enviarATodos(jugadores);
        if (hilo != actual) return;

        if ((secuencia - 1) % INTERVALO_HUD_EN_ESTADOS == 0) {
            String hud = partida.obtenerEstadoHud(secuencia);
            if (hud != null && hilo == actual) emisor.enviarATodos(hud);
        }
        if (hilo != actual) return;

        // Repetir la selección por si se perdió el mensaje del lobby.
        if ((secuencia - 1) % ESTADOS_POR_SEGUNDO == 0) {
            String personajes = partida.obtenerPersonajes();
            if (personajes != null && hilo == actual) emisor.enviarATodos(personajes);
        }
        if (hilo != actual) return;

        List<String> enemigos = partida.obtenerEstadosEnemigos(secuencia);
        if (enemigos != null) {
            for (String bloque : enemigos) {
                if (hilo != actual) return;
                emisor.enviarATodos(bloque);
            }
        }
    }

    public synchronized void detener() {
        Thread anterior = hilo;
        hilo = null;
        if (anterior != null) {
            anterior.interrupt();
            System.out.println("Envio de estados detenido.");
        }
    }
}
