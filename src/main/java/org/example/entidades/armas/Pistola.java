package org.example.entidades.armas;

import org.example.entidades.Enemigo;
import org.example.entidades.Jugador;
import org.example.juego.GestorEnemigos;

public class Pistola extends Arma {

    private static final float ALCANCE = 260f;
    private static final float SEMIANCHO = 36f;

    private float origenX;
    private float origenY;

    public Pistola() {
        super(15, 1f);
    }

    @Override
    protected void atacar(Jugador jugador, GestorEnemigos gestorEnemigos) {
        origenX = jugador.getPosicionX() + jugador.getAnchoHitbox() / 2f;
        origenY = jugador.getPosicionY() + jugador.getAltoHitbox() / 2f;

        double angulo = getDireccionAtaque() * Math.PI / 4.0;
        float dx = (float) Math.cos(angulo);
        float dy = (float) Math.sin(angulo);
        float absDx = Math.abs(dx);
        float absDy = Math.abs(dy);
        float semilargo = ALCANCE * getAlcanceAtaque() / 2f;
        float centroX = origenX + dx * semilargo;
        float centroY = origenY + dy * semilargo;

        for (Enemigo enemigo : gestorEnemigos.getEnemigos()) {
            if (!enemigo.estaVivo()) {
                continue;
            }

            float mitadX = enemigo.getAnchoHitbox() / 2f;
            float mitadY = enemigo.getAltoHitbox() / 2f;
            float x = enemigo.getPosicionX() + mitadX - centroX;
            float y = enemigo.getPosicionY() + mitadY - centroY;
            // Comprueba la separación en los ejes del enemigo y del disparo.
            if (Math.abs(x) <= mitadX + semilargo * absDx + SEMIANCHO * absDy
                && Math.abs(y) <= mitadY + semilargo * absDy + SEMIANCHO * absDx
                && Math.abs(x * dx + y * dy) <= semilargo + mitadX * absDx + mitadY * absDy
                && Math.abs(-x * dy + y * dx) <= SEMIANCHO + mitadX * absDy + mitadY * absDx) {
                enemigo.recibirDanio(danio);
            }
        }
    }

    @Override
    public String getTipo() {
        return "PISTOLA";
    }

    public float getOrigenX() {
        return origenX;
    }

    public float getOrigenY() {
        return origenY;
    }
}
