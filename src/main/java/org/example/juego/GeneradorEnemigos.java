package org.example.juego;

import org.example.entidades.Enemigo;
import org.example.entidades.Jugador;
import org.example.entidades.TipoEnemigo;

import java.util.Random;

public class GeneradorEnemigos {

    private static final int PRIMER_JEFE = 20;
    private static final int INTERVALO_JEFE = 45;
    private static final float PROBABILIDAD_VIOLETA = 0.1f;
    private static final float DISTANCIA_MINIMA = 450f;
    private static final float DISTANCIA_MAXIMA = 550f;

    private static final float ANCHO_MAPA = 12800f;
    private static final float ALTO_MAPA = 12800f;

    private final GestorEnemigos gestorEnemigos;
    private final DificultadPartida dificultad;
    private final Random random;

    private float tiempoAparicion;
    private int siguienteJefe = PRIMER_JEFE;
    private Enemigo muerte;

    public GeneradorEnemigos(GestorEnemigos gestorEnemigos, DificultadPartida dificultad) {

        this.gestorEnemigos = gestorEnemigos;
        this.dificultad = dificultad;

        random = new Random();
        tiempoAparicion = 0;
    }

    public void actualizar(float delta, Jugador jugador1, Jugador jugador2) {

        if (!jugador1.estaVivo() && !jugador2.estaVivo()) {
            return;
        }

        DificultadPartida.Etapa etapa = dificultad.getEtapa();
        int tiempo = dificultad.getTiempoTranscurrido();
        if (tiempo >= siguienteJefe && gestorEnemigos.cantidadJefesVivos() < etapa.maximoJefes()) {
            generarEnemigo(TipoEnemigo.JEFE, etapa.vidaExtraJefe(), jugador1, jugador2);
            siguienteJefe = tiempo + INTERVALO_JEFE;
        }

        tiempoAparicion += delta;

        float intervalo = etapa.intervalo();

        if (tiempoAparicion < intervalo) {
            return;
        }

        tiempoAparicion -= intervalo;

        int maximo = etapa.maximo();

        int cantidad = etapa.cantidad();

        for (int i = 0; i < cantidad; i++) {

            if (gestorEnemigos.cantidadEnemigos() >= maximo) {
                break;
            }

            TipoEnemigo tipo = random.nextFloat() < PROBABILIDAD_VIOLETA
                    ? TipoEnemigo.VIOLETA : TipoEnemigo.BASICO;
            generarEnemigo(tipo, etapa.vidaExtraBasico(), jugador1, jugador2);
        }
    }

    public void actualizarFaseFinal(Jugador jugador1, Jugador jugador2) {
        if (muerte == null) {
            muerte = generarEnemigo(TipoEnemigo.MUERTE, 0, jugador1, jugador2);
        }
    }

    private Enemigo generarEnemigo(TipoEnemigo tipo, int vidaExtra, Jugador jugador1, Jugador jugador2) {

        // Elegir aleatoriamente un jugador
        // alrededor del cual aparecerá el enemigo.

        Jugador referencia;

        if (random.nextBoolean()) {
            referencia = jugador1;
        } else {
            referencia = jugador2;
        }

        if (!referencia.estaVivo()) {
            referencia = referencia == jugador1 ? jugador2 : jugador1;
        }

        // Elegir una distancia aleatoria.
        float distancia = DISTANCIA_MINIMA + random.nextFloat() * (DISTANCIA_MAXIMA - DISTANCIA_MINIMA);

        // Elegir una dirección aleatoria.
        double angulo = random.nextDouble() * Math.PI * 2;

        float posicionX = referencia.getPosicionX() + (float) Math.cos(angulo) * distancia;

        float posicionY = referencia.getPosicionY() + (float) Math.sin(angulo) * distancia;

        // Mantener la aparición dentro del mapa.
        posicionX = Math.max(0, Math.min(ANCHO_MAPA - Math.max(40f, tipo.getAnchoHitbox()), posicionX));

        posicionY = Math.max(0, Math.min(ALTO_MAPA - Math.max(60f, tipo.getAltoHitbox()), posicionY));

        // Crear el enemigo mediante el gestor.
        return gestorEnemigos.crearEnemigo(tipo, posicionX, posicionY, vidaExtra);
    }
}
