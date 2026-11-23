package org.example.juego;

import org.example.entidades.Enemigo;
import org.example.entidades.Jugador;
import org.example.entidades.TipoEnemigo;

public class SistemaColisiones {

    private static final float MARGEN_CONTACTO = 0.1f;

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

            resolverContacto(enemigo, jugador1);
            resolverContacto(enemigo, jugador2);
        }
    }

    private void resolverContacto(Enemigo enemigo, Jugador jugador) {
        boolean esVioleta = enemigo.getTipo() == TipoEnemigo.VIOLETA;
        if ((!esVioleta && !enemigo.puedeAtacar()) || !jugador.estaVivo() || !colisionan(enemigo, jugador)) {
            return;
        }

        if (enemigo.puedeAtacar()) {
            jugador.recibirDanio(enemigo.getDanio());
            enemigo.registrarAtaque();
        }

        if (esVioleta) {
            separarYRebotar(enemigo, jugador);
        } else {
            enemigo.retrocederDesde(jugador);
        }
    }

    private void separarYRebotar(Enemigo enemigo, Jugador jugador) {
        float diferenciaX = enemigo.getPosicionX() - jugador.getPosicionX()
                + (enemigo.getAnchoHitbox() - jugador.getAnchoHitbox()) / 2f;
        float diferenciaY = enemigo.getPosicionY() - jugador.getPosicionY()
                + (enemigo.getAltoHitbox() - jugador.getAltoHitbox()) / 2f;
        float superposicionX = (enemigo.getAnchoHitbox() + jugador.getAnchoHitbox()) / 2f - Math.abs(diferenciaX);
        float superposicionY = (enemigo.getAltoHitbox() + jugador.getAltoHitbox()) / 2f - Math.abs(diferenciaY);

        if (superposicionX < superposicionY) {
            float direccion = diferenciaX >= 0f ? 1f : -1f;
            enemigo.desplazarPorColision(direccion * (superposicionX + MARGEN_CONTACTO), 0f);
            if (!colisionan(enemigo, jugador)) {
                enemigo.rebotarDesdeContacto(direccion, 0f);
                return;
            }
        }

        // El borde del mapa puede bloquear la separación por la izquierda o por abajo.
        float direccion = diferenciaY >= 0f ? 1f : -1f;
        enemigo.desplazarPorColision(0f, direccion * (superposicionY + MARGEN_CONTACTO));
        if (colisionan(enemigo, jugador)) {
            direccion = 1f;
            enemigo.desplazarPorColision(0f, jugador.getPosicionY() + jugador.getAltoHitbox()
                    - enemigo.getPosicionY() + MARGEN_CONTACTO);
        }
        enemigo.rebotarDesdeContacto(0f, direccion);
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
