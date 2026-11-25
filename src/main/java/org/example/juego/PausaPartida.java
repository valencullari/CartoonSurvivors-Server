package org.example.juego;

import java.util.concurrent.TimeUnit;

final class PausaPartida {
    private static final long DURACION = TimeUnit.SECONDS.toNanos(40);
    private static final int AMBOS = 0b11;

    private int revision;
    private int solicitante;
    private int confirmaciones;
    private long vencimiento;

    boolean abrir(int jugador, int revisionRecibida) {
        if (revisionRecibida != revision || estaActiva()) {
            return false;
        }
        revision++;
        solicitante = jugador;
        confirmaciones = 0;
        vencimiento = System.nanoTime() + DURACION;
        return true;
    }

    void confirmar(int jugador, int revisionRecibida) {
        if (revisionRecibida != revision || !estaActiva()) {
            return;
        }
        confirmaciones |= 1 << (jugador - 1);
        if (confirmaciones == AMBOS) {
            solicitante = 0;
        }
    }

    boolean estaActiva() {
        return milisegundosRestantes() > 0;
    }

    private long milisegundosRestantes() {
        if (solicitante == 0) {
            return 0;
        }
        long restante = Math.max(0, vencimiento - System.nanoTime());
        return (restante + 999_999) / 1_000_000;
    }

    String datosHud() {
        long restante = milisegundosRestantes();
        int solicitanteActivo = 0;
        int confirmacionesActivas = 0;
        if (restante > 0) {
            solicitanteActivo = solicitante;
            confirmacionesActivas = confirmaciones;
        }
        return "|PAUSA:" + revision
            + ":" + solicitanteActivo
            + ":" + restante
            + ":" + confirmacionesActivas;
    }
}
