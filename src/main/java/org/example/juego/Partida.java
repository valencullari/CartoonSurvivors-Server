package org.example.juego;

import org.example.entidades.Jugador;
import org.example.entidades.Enemigo;
import org.example.entidades.TipoEnemigo;

public class Partida {

    private final Jugador jugador1;
    private final Jugador jugador2;

    private final GestorEnemigos gestorEnemigos;
    private final DificultadPartida dificultad;
    private final GeneradorEnemigos generadorEnemigos;
    private final SistemaColisiones sistemaColisiones;

    public Partida(String personaje1, String personaje2) {

        jugador1 = new Jugador(1, personaje1, 6340, 6400);
        jugador2 = new Jugador(2, personaje2, 6460, 6400);
        gestorEnemigos = new GestorEnemigos();
        dificultad = new DificultadPartida();
        generadorEnemigos = new GeneradorEnemigos(gestorEnemigos, dificultad);
        sistemaColisiones = new SistemaColisiones();
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
        dificultad.actualizar(delta);
        generadorEnemigos.actualizar(delta, jugador1, jugador2);
        gestorEnemigos.actualizar(delta, jugador1, jugador2);
        sistemaColisiones.actualizar(delta, jugador1, jugador2, gestorEnemigos);
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

        return jugador.getId() + ":" + jugador.getPosicionX() + ":" + jugador.getPosicionY() + ":" + jugador.getDireccionX() + ":" + jugador.getDireccionY() + ":" + jugador.getVida();
    }


    public synchronized String crearMensajeEnemigos(long secuencia) {

        StringBuilder mensaje = new StringBuilder();

        mensaje.append("ENEMIGOS");
        mensaje.append("|").append(secuencia);

        for (Enemigo enemigo : gestorEnemigos.getEnemigos()) {

            mensaje.append("|").append(enemigo.getId()).append(":").append(enemigo.getTipo().name()).append(":").append(enemigo.getPosicionX()).append(":").append(enemigo.getPosicionY()).append(":").append(enemigo.getVida());
        }

        return mensaje.toString();
    }

    public Jugador getJugador1() {
        return jugador1;
    }

    public Jugador getJugador2() {
        return jugador2;
    }
}
