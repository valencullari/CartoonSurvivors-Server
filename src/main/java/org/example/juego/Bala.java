package org.example.juego;

import org.example.entidades.Enemigo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class Bala {
    private static final float VELOCIDAD = 1000f;
    private static final float ALCANCE_REBOTE = 200f;

    private final Set<Enemigo> alcanzados = new HashSet<>();
    private final float retencionDanio;
    private Enemigo objetivo;
    private float danio;
    private int rebotesRestantes;
    private float x;
    private float y;
    private float angulo;

    Bala(float x, float y, Enemigo objetivo, float danio, int rebotes, float retencionDanio) {
        this.x = x;
        this.y = y;
        this.objetivo = objetivo;
        this.danio = danio;
        this.rebotesRestantes = rebotes;
        this.retencionDanio = retencionDanio;
        orientar(centroX(objetivo) - x, centroY(objetivo) - y);
    }

    boolean actualizar(float delta, List<Enemigo> enemigos) {
        if (!objetivo.estaVivo()) {
            return false;
        }
        float dx = centroX(objetivo) - x;
        float dy = centroY(objetivo) - y;
        float distancia = (float) Math.hypot(dx, dy);
        float avance = VELOCIDAD * delta;
        if (distancia <= avance) {
            x += dx;
            y += dy;
            objetivo.recibirDanio(danio);
            alcanzados.add(objetivo);
            return rebotar(enemigos);
        }
        x += dx * avance / distancia;
        y += dy * avance / distancia;
        orientar(dx, dy);
        return true;
    }

    private boolean rebotar(List<Enemigo> enemigos) {
        if (rebotesRestantes == 0) {
            return false;
        }
        Enemigo siguiente = null;
        float distanciaMinima = ALCANCE_REBOTE * ALCANCE_REBOTE;
        for (Enemigo enemigo : enemigos) {
            if (!enemigo.estaVivo() || alcanzados.contains(enemigo)) {
                continue;
            }
            float dx = centroX(enemigo) - x;
            float dy = centroY(enemigo) - y;
            float distancia = dx * dx + dy * dy;
            if (distancia <= distanciaMinima) {
                siguiente = enemigo;
                distanciaMinima = distancia;
            }
        }
        if (siguiente == null) {
            return false;
        }
        objetivo = siguiente;
        rebotesRestantes--;
        danio *= retencionDanio;
        orientar(centroX(objetivo) - x, centroY(objetivo) - y);
        return true;
    }

    private static float centroX(Enemigo enemigo) {
        return enemigo.getPosicionX() + enemigo.getAnchoHitbox() / 2f;
    }

    private static float centroY(Enemigo enemigo) {
        return enemigo.getPosicionY() + enemigo.getAltoHitbox() / 2f;
    }

    private void orientar(float dx, float dy) {
        angulo = (float) Math.toDegrees(Math.atan2(dy, dx));
    }

    String datos() {
        return "BALA:" + x + ":" + y + ":" + angulo;
    }
}
