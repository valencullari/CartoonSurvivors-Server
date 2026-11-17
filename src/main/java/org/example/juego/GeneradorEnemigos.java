package org.example.juego;

import org.example.entidades.Jugador;
import org.example.entidades.TipoEnemigo;

import java.util.Random;

public class GeneradorEnemigos {

    private static final float DISTANCIA_MINIMA = 350f;
    private static final float DISTANCIA_MAXIMA = 550f;

    private static final float ANCHO_MAPA = 12800f;
    private static final float ALTO_MAPA = 12800f;

    private final GestorEnemigos gestorEnemigos;
    private final DificultadPartida dificultad;
    private final Random random;

    private float tiempoAparicion;

    public GeneradorEnemigos(GestorEnemigos gestorEnemigos, DificultadPartida dificultad) {

        this.gestorEnemigos = gestorEnemigos;
        this.dificultad = dificultad;

        random = new Random();
        tiempoAparicion = 0;
    }

    public void actualizar(float delta, Jugador jugador1, Jugador jugador2) {

        tiempoAparicion += delta;

        float intervalo = dificultad.getIntervaloAparicion();

        if (tiempoAparicion < intervalo) {
            return;
        }

        tiempoAparicion -= intervalo;

        int maximo = dificultad.getMaximoEnemigos();

        int cantidad = dificultad.getCantidadPorAparicion();

        for (int i = 0; i < cantidad; i++) {

            if (gestorEnemigos.cantidadEnemigos() >= maximo) {
                break;
            }

            generarEnemigo(jugador1, jugador2);
        }
    }

    private void generarEnemigo(Jugador jugador1, Jugador jugador2) {

        // Elegir aleatoriamente un jugador
        // alrededor del cual aparecerá el enemigo.

        Jugador referencia;

        if (random.nextBoolean()) {
            referencia = jugador1;
        } else {
            referencia = jugador2;
        }

        // Elegir una distancia aleatoria.
        float distancia = DISTANCIA_MINIMA + random.nextFloat() * (DISTANCIA_MAXIMA - DISTANCIA_MINIMA);

        // Elegir una dirección aleatoria.
        double angulo = random.nextDouble() * Math.PI * 2;

        float posicionX = referencia.getPosicionX() + (float) Math.cos(angulo) * distancia;

        float posicionY = referencia.getPosicionY() + (float) Math.sin(angulo) * distancia;

        // Mantener la aparición dentro del mapa.
        posicionX = Math.max(0, Math.min(ANCHO_MAPA - 40, posicionX));

        posicionY = Math.max(0, Math.min(ALTO_MAPA - 60, posicionY));

        // Crear el enemigo mediante el gestor.
        gestorEnemigos.crearEnemigo(TipoEnemigo.BASICO, posicionX, posicionY);
    }
}
