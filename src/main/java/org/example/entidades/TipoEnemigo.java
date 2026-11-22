
package org.example.entidades;

public enum TipoEnemigo {

    BASICO(25, 80f, 15, 24f, 40f),
    JEFE(380, 78f, 38, 84f, 94f);

    private final int vidaMaxima;
    private final float velocidad;
    private final int danio;

    private final float anchoHitbox;
    private final float altoHitbox;

    TipoEnemigo(int vidaMaxima, float velocidad, int danio, float anchoHitbox, float altoHitbox) {
        this.anchoHitbox = anchoHitbox;
        this.altoHitbox = altoHitbox;

        this.vidaMaxima = vidaMaxima;
        this.velocidad = velocidad;
        this.danio = danio;
    }

    public int getVidaMaxima() {
        return vidaMaxima;
    }

    public float getVelocidad() {
        return velocidad;
    }

    public float getAnchoHitbox() {
        return anchoHitbox;
    }

    public float getAltoHitbox() {
        return altoHitbox;
    }

    public int getDanio() {
        return danio;
    }
}
