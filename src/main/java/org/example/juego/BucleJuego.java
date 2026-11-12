
package org.example.juego;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BucleJuego {

    private static final int ACTUALIZACIONES_POR_SEGUNDO = 60;

    private static final long INTERVALO_NANOSEGUNDOS =
            1_000_000_000L / ACTUALIZACIONES_POR_SEGUNDO;

    private final Partida partida;

    private ScheduledExecutorService ejecutor;
    private long ultimaActualizacion;

    public BucleJuego(Partida partida) {
        this.partida = partida;
    }

    public synchronized void iniciar() {

        if (ejecutor != null && !ejecutor.isShutdown()) {
            return;
        }

        ejecutor = Executors.newSingleThreadScheduledExecutor();

        ultimaActualizacion = System.nanoTime();

        ejecutor.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        actualizar();
                    }
                },
                0,
                INTERVALO_NANOSEGUNDOS,
                TimeUnit.NANOSECONDS
        );

        System.out.println("Bucle iniciado a 60 TPS.");
    }

    private void actualizar() {

        long tiempoActual = System.nanoTime();

        float delta =
                (tiempoActual - ultimaActualizacion)
                        / 1_000_000_000f;

        ultimaActualizacion = tiempoActual;

        delta = Math.min(delta, 0.05f);

        try {
            partida.actualizar(delta);

        } catch (Exception e) {
            System.out.println(
                    "Error actualizando partida: "
                            + e.getMessage()
            );
        }
    }

    public synchronized void detener() {

        if (ejecutor != null) {

            ejecutor.shutdownNow();
            ejecutor = null;
        }

        System.out.println("Bucle detenido.");
    }
}
