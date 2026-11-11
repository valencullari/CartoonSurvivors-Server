
package org.example.red.gestor;

import org.example.red.modelo.EstadoPartida;

public class ControladorPartida {

    private EstadoPartida estado;

    public ControladorPartida() {
        estado = EstadoPartida.ESPERANDO_JUGADORES;
    }

    public synchronized EstadoPartida getEstado() {
        return estado;
    }

    public synchronized void establecerEstado(
            EstadoPartida nuevoEstado) {

        estado = nuevoEstado;

        System.out.println(
                "Estado de partida: " + estado
        );
    }

    public synchronized boolean estaEnCurso() {
        return estado == EstadoPartida.EN_CURSO;
    }

    public synchronized void reiniciar() {
        estado = EstadoPartida.ESPERANDO_JUGADORES;

        System.out.println("Sesión reiniciada.");
    }
}
