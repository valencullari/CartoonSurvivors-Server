package org.example.juego;

import org.example.entidades.Enemigo;
import org.example.entidades.Jugador;

public class SistemaColisiones {

    public void actualizar(float delta, Jugador jugador1, Jugador jugador2, GestorEnemigos gestorEnemigos) {

        if (!Float.isFinite(delta) || delta <= 0f) {
            return;
        }

        for (Enemigo enemigo : gestorEnemigos.getEnemigos()) {

            if (!enemigo.estaVivo()) {
                continue;
            }

            comprobarContacto(enemigo, jugador1, delta);
            comprobarContacto(enemigo, jugador2, delta);
        }
    }

    private void comprobarContacto(Enemigo enemigo, Jugador jugador, float delta) {

        if (!jugador.estaVivo()) {
            return;
        }

        if (colisionan(enemigo.getPosicionX(), enemigo.getPosicionY(), enemigo.getAnchoHitbox(), enemigo.getAltoHitbox(), jugador.getPosicionX(), jugador.getPosicionY(), jugador.getAnchoHitbox(), jugador.getAltoHitbox())) {

            float danio = enemigo.getDanio() * delta;

            jugador.recibirDanio(danio);
        }
    }

    private boolean colisionan(float x1, float y1, float ancho1, float alto1, float x2, float y2, float ancho2, float alto2) {

        return x1 < x2 + ancho2 && x1 + ancho1 > x2 && y1 < y2 + alto2 && y1 + alto1 > y2;
    }
}
