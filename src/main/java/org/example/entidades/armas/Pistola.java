package org.example.entidades.armas;

import org.example.entidades.Enemigo;
import org.example.entidades.Jugador;
import org.example.juego.GestorEnemigos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Pistola extends Arma {
    private static final float ALCANCE = 260f;
    // Coseno de 60 grados respecto del lado al que mira el personaje.
    private static final float LIMITE_CONO_FRONTAL = 0.5f;
    private static final int PERDIDA_MINIMA_REBOTE = 5;
    private static final int MEJORA_RETENCION_REBOTE = 5;
    private int proyectiles = 1;
    private int rebotes = 1;
    private int perdidaPorRebote = 30;

    public Pistola() {
        super(15, 0.85f);
    }

    @Override
    protected boolean atacar(Jugador jugador, GestorEnemigos gestorEnemigos) {
        float origenX = jugador.getPosicionX() + jugador.getAnchoHitbox() / 2f;
        float origenY = jugador.getPosicionY() + jugador.getAltoHitbox() / 2f;
        float alcanceCuadrado = ALCANCE * ALCANCE;
        int lado = jugador.getLadoMirada();
        List<Objetivo> objetivos = new ArrayList<>();

        for (Enemigo enemigo : gestorEnemigos.getEnemigos()) {
            if (!enemigo.estaVivo()) {
                continue;
            }
            float dx = enemigo.getPosicionX() + enemigo.getAnchoHitbox() / 2f - origenX;
            float dy = enemigo.getPosicionY() + enemigo.getAltoHitbox() / 2f - origenY;
            float distancia = dx * dx + dy * dy;
            float frente = dx * lado;
            if (distancia <= alcanceCuadrado && frente > 0f
                    && frente * frente >= distancia * LIMITE_CONO_FRONTAL * LIMITE_CONO_FRONTAL) {
                objetivos.add(new Objetivo(enemigo, distancia));
            }
        }

        if (objetivos.isEmpty()) {
            return false;
        }
        objetivos.sort(new Comparator<Objetivo>() {
            @Override
            public int compare(Objetivo primero, Objetivo segundo) {
                return Float.compare(primero.distanciaCuadrada(), segundo.distanciaCuadrada());
            }
        });
        int blancos = Math.min(proyectiles, objetivos.size());
        for (int i = 0; i < proyectiles; i++) {
            float separacion = (i - (proyectiles - 1) / 2f) * 10f;
            Enemigo objetivo = objetivos.get(i % blancos).enemigo();
            gestorEnemigos.dispararBala(origenX, origenY + separacion, objetivo, jugador.calcularDanio(getDanio()),
                rebotes, (100 - perdidaPorRebote) / 100f);
        }
        return true;
    }

    public void agregarProyectil() {
        proyectiles++;
    }

    public int mejoraRetencionDisponible() {
        return Math.min(MEJORA_RETENCION_REBOTE, perdidaPorRebote - PERDIDA_MINIMA_REBOTE);
    }

    public void mejorarRebote(int mejoraRetencion) {
        rebotes++;
        perdidaPorRebote -= Math.min(mejoraRetencion, mejoraRetencionDisponible());
    }

    @Override
    public String getTipo() {
        return "PISTOLA";
    }

    private record Objetivo(Enemigo enemigo, float distanciaCuadrada) {
    }
}
