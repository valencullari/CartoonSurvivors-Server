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
    private int proyectiles = 1;

    public Pistola() {
        super(15, 0.85f);
    }

    @Override
    protected boolean atacar(Jugador jugador, GestorEnemigos gestorEnemigos) {
        float origenX = jugador.getPosicionX() + jugador.getAnchoHitbox() / 2f;
        float origenY = jugador.getPosicionY() + jugador.getAltoHitbox() / 2f;
        float alcance = ALCANCE * getAlcanceAtaque();
        float alcanceCuadrado = alcance * alcance;
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
        objetivos.sort(Comparator.comparingDouble(Objetivo::distanciaCuadrada));
        int blancos = Math.min(proyectiles, objetivos.size());
        for (int i = 0; i < proyectiles; i++) {
            float separacion = (i - (proyectiles - 1) / 2f) * 10f;
            Enemigo objetivo = objetivos.get(i % blancos).enemigo();
            gestorEnemigos.dispararBala(origenX, origenY + separacion, objetivo, jugador.calcularDanio(getDanio()));
        }
        return true;
    }

    public void agregarProyectil() {
        proyectiles++;
    }

    @Override
    public String getTipo() {
        return "PISTOLA";
    }

    private record Objetivo(Enemigo enemigo, float distanciaCuadrada) {
    }
}
