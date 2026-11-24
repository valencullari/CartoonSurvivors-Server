package org.example.entidades.armas;

import org.example.entidades.Enemigo;
import org.example.entidades.Jugador;
import org.example.juego.GestorEnemigos;

public class Pistola extends Arma {
    private static final float ALCANCE = 260f;
    // Coseno de 60 grados respecto del lado al que mira el personaje.
    private static final float LIMITE_CONO_FRONTAL = 0.5f;

    public Pistola() {
        super(15, 1f);
    }

    @Override
    protected boolean atacar(Jugador jugador, GestorEnemigos gestorEnemigos) {
        float origenX = jugador.getPosicionX() + jugador.getAnchoHitbox() / 2f;
        float origenY = jugador.getPosicionY() + jugador.getAltoHitbox() / 2f;
        float alcance = ALCANCE * getAlcanceAtaque();
        float distanciaMinima = alcance * alcance;
        int lado = jugador.getLadoMirada();
        Enemigo objetivo = null;

        for (Enemigo enemigo : gestorEnemigos.getEnemigos()) {
            if (!enemigo.estaVivo()) {
                continue;
            }
            float dx = enemigo.getPosicionX() + enemigo.getAnchoHitbox() / 2f - origenX;
            float dy = enemigo.getPosicionY() + enemigo.getAltoHitbox() / 2f - origenY;
            float distancia = dx * dx + dy * dy;
            float frente = dx * lado;
            if (distancia <= distanciaMinima && frente > 0f
                    && frente * frente >= distancia * LIMITE_CONO_FRONTAL * LIMITE_CONO_FRONTAL) {
                distanciaMinima = distancia;
                objetivo = enemigo;
            }
        }

        if (objetivo == null) {
            return false;
        }
        gestorEnemigos.dispararBala(origenX, origenY, objetivo, jugador.calcularDanio(getDanio()));
        return true;
    }

    @Override
    public String getTipo() {
        return "PISTOLA";
    }
}
