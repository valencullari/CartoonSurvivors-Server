package org.example.juego;

import org.example.entidades.Jugador;
import org.example.entidades.Enemigo;
import org.example.entidades.armas.Arma;
import org.example.entidades.armas.Espada;
import org.example.red.protocolo.Protocolo;

import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;

public class Partida {
    private static final int MAX_BYTES_ENEMIGOS_POR_BLOQUE = 750;

    private final Jugador jugador1;
    private final Jugador jugador2;

    private final GestorEnemigos gestorEnemigos;
    private final DificultadPartida dificultad;
    private final GeneradorEnemigos generadorEnemigos;
    private final SistemaColisiones sistemaColisiones;

    public Partida(String personaje1, String personaje2) {

        jugador1 = new Jugador(1, personaje1, 6300, 6400);
        jugador2 = new Jugador(2, personaje2, 6460, 6400);
        jugador1.agregarArma(new Espada());
        jugador2.agregarArma(new Espada());
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
        jugador1.actualizarArmas(delta, gestorEnemigos);
        jugador2.actualizarArmas(delta, gestorEnemigos);
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

    public synchronized String crearMensajeHud(long secuencia) {
        return Protocolo.HUD + "|" + secuencia + "|" + (int) dificultad.getTiempoTranscurrido()
            + "|" + gestorEnemigos.getEnemigosEliminados();
    }

    private String datosJugador(Jugador jugador) {

        return jugador.getId() + ":" + jugador.getPosicionX() + ":" + jugador.getPosicionY() + ":" + jugador.getDireccionX() + ":" + jugador.getDireccionY() + ":" + jugador.getVida() + ":" + datosArmas(jugador);
    }

    private String datosArmas(Jugador jugador) {

        StringBuilder datos = new StringBuilder();
        List<Arma> armas = jugador.getArmas();
        for (int i = 0; i < armas.size(); i++) {
            Arma arma = armas.get(i);
            if (i > 0) {
                datos.append(";");
            }
            datos.append(i).append(",").append(arma.getTipo()).append(",").append(arma.getNumeroAtaque()).append(",").append(arma.getDireccionAtaque());
        }

        return datos.toString();
    }


    public synchronized List<String> crearMensajesEnemigos(long secuencia) {

        List<String> bloquesDatos = new ArrayList<>();

        StringBuilder bloqueActual = new StringBuilder();

        for (Enemigo enemigo : gestorEnemigos.getEnemigos()) {

            String datosEnemigo = enemigo.getId() + ":" + enemigo.getTipo().name() + ":" + enemigo.getPosicionX() + ":" + enemigo.getPosicionY() + ":" + enemigo.getVida();

            String entrada = "|" + datosEnemigo;

            int bytesActuales = bloqueActual.toString().getBytes(StandardCharsets.UTF_8).length;

            int bytesEntrada = entrada.getBytes(StandardCharsets.UTF_8).length;

            if (bytesActuales + bytesEntrada > MAX_BYTES_ENEMIGOS_POR_BLOQUE && bloqueActual.length() > 0) {

                bloquesDatos.add(bloqueActual.toString());

                bloqueActual = new StringBuilder();
            }

            bloqueActual.append(entrada);
        }

        // Agregar el último bloque.
        // También sirve cuando no existen enemigos.
        bloquesDatos.add(bloqueActual.toString());

        List<String> mensajes = new ArrayList<>();

        int totalBloques = bloquesDatos.size();

        for (int i = 0; i < totalBloques; i++) {

            String mensaje = "ENEMIGOS" + "|" + secuencia + "|" + i + "|" + totalBloques + bloquesDatos.get(i);

            mensajes.add(mensaje);
        }

        return mensajes;
    }


    public Jugador getJugador1() {
        return jugador1;
    }

    public Jugador getJugador2() {
        return jugador2;
    }
}
