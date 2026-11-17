
package org.example.entidades;

public enum TipoEnemigo {

    BASICO(25, 80f, 15),
    JEFE(300, 45f, 30);

    private final int vidaMaxima;
    private final float velocidad;
    private final int danio;

    TipoEnemigo(int vidaMaxima, float velocidad, int danio) {

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

    public int getDanio() {
        return danio;
    }
}
