package org.example.juego;

import org.example.entidades.Jugador;
import org.example.entidades.Enemigo;

public class Partida {

    private final Jugador jugador1;
    private final Jugador jugador2;

    private final Enemigo enemigo;

    public Partida(String personaje1, String personaje2) {

        jugador1 = new Jugador(1, personaje1, 6340, 6400);

        jugador2 = new Jugador(2, personaje2, 6460, 6400);

        // Primer enemigo de prueba.
        enemigo = new Enemigo(1, 6400, 6650);
    }

    public synchronized void moverJugador(int id, float direccionX, float direccionY) {

        Jugador jugador = obtenerJugador(id);

        if (jugador != null) {

            jugador.establecerMovimiento(direccionX, direccionY);
        }
    }

    public synchronized void actualizar(float delta) {

        jugador1.actualizar(delta);
        jugador2.actualizar(delta);

        // El enemigo persigue al jugador más cercano.
        enemigo.actualizar(delta, jugador1, jugador2);
    }

    public synchronized Jugador obtenerJugador(int id) {

        if (id == 1) {
            return jugador1;
        }

        if (id == 2) {
            return jugador2;
        }

        return null;
    }

    public synchronized String crearMensajeEstado(long secuencia) {

        return "ESTADO|" + secuencia + "|" + datosJugador(jugador1) + "|" + datosJugador(jugador2);
    }

    private String datosJugador(Jugador jugador) {

        return jugador.getId() + ":" + jugador.getPosicionX() + ":" + jugador.getPosicionY() + ":" + jugador.getDireccionX() + ":" + jugador.getDireccionY();
    }

    public synchronized String crearMensajeEnemigos(long secuencia) {

        return "ENEMIGOS|" + secuencia + "|" + enemigo.getId() + ":" + enemigo.getPosicionX() + ":" + enemigo.getPosicionY() + ":" + enemigo.getVida();
    }

    public Jugador getJugador1() {
        return jugador1;
    }

    public Jugador getJugador2() {
        return jugador2;
    }
}
