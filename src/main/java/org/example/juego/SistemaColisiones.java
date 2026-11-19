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

            // Actualizar cooldown del enemigo.
            enemigo.actualizarCooldownAtaque(delta);

            if (!enemigo.puedeAtacar()) {
                continue;
            }

            // Comprobar contacto con jugador 1.
            if (jugador1.estaVivo() && colisionan(enemigo, jugador1)) {

                aplicarGolpe(enemigo, jugador1);
                continue;
            }

            // Comprobar contacto con jugador 2.
            if (jugador2.estaVivo() && colisionan(enemigo, jugador2)) {

                aplicarGolpe(enemigo, jugador2);
            }
        }
    }

    private void aplicarGolpe(Enemigo enemigo, Jugador jugador) {

        // 1. Aplicar una piña completa.
        jugador.recibirDanio(enemigo.getDanio());

        // 2. Iniciar cooldown.
        enemigo.registrarAtaque();

        // 3. Empujar al enemigo hacia atrás.
        enemigo.retrocederDesde(jugador);
    }

    private boolean colisionan(Enemigo enemigo, Jugador jugador) {

        float x1 = enemigo.getPosicionX();
        float y1 = enemigo.getPosicionY();

        float ancho1 = enemigo.getAnchoHitbox();
        float alto1 = enemigo.getAltoHitbox();

        float x2 = jugador.getPosicionX();
        float y2 = jugador.getPosicionY();

        float ancho2 = jugador.getAnchoHitbox();
        float alto2 = jugador.getAltoHitbox();

        return x1 < x2 + ancho2 && x1 + ancho1 > x2 && y1 < y2 + alto2 && y1 + alto1 > y2;
    }
}
