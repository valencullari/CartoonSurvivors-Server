package org.example.juego;

import org.example.entidades.Enemigo;

final class Bala {
    private static final float VELOCIDAD = 1000f;

    private final Enemigo objetivo;
    private final float danio;
    private float x;
    private float y;
    private float angulo;

    Bala(float x, float y, Enemigo objetivo, float danio) {
        this.x = x;
        this.y = y;
        this.objetivo = objetivo;
        this.danio = danio;
        orientar(centroX() - x, centroY() - y);
    }

    boolean actualizar(float delta) {
        if (!objetivo.estaVivo()) {
            return false;
        }
        float dx = centroX() - x;
        float dy = centroY() - y;
        float distancia = (float) Math.hypot(dx, dy);
        float avance = VELOCIDAD * delta;
        if (distancia <= avance) {
            objetivo.recibirDanio(danio);
            return false;
        }
        x += dx * avance / distancia;
        y += dy * avance / distancia;
        orientar(dx, dy);
        return true;
    }

    private float centroX() {
        return objetivo.getPosicionX() + objetivo.getAnchoHitbox() / 2f;
    }

    private float centroY() {
        return objetivo.getPosicionY() + objetivo.getAltoHitbox() / 2f;
    }

    private void orientar(float dx, float dy) {
        angulo = (float) Math.toDegrees(Math.atan2(dy, dx));
    }

    String datos() {
        return "BALA:" + x + ":" + y + ":" + angulo;
    }
}
