package org.example.entidades.armas;

import org.example.entidades.Jugador;
import org.example.juego.GestorEnemigos;

public abstract class Arma {

    protected final int danio;
    protected final float cooldown;

    private float tiempoRestante;
    private long numeroAtaque;

    public Arma(int danio, float cooldown) {
        this.danio = danio;
        this.cooldown = cooldown;
        this.tiempoRestante = 0f;
        this.numeroAtaque = 0;
    }

    public void actualizar(float delta, Jugador jugador, GestorEnemigos gestorEnemigos) {

        if (!jugador.estaVivo()) {
            return;
        }

        tiempoRestante -= delta;

        if (tiempoRestante > 0f) {
            return;
        }

        tiempoRestante = cooldown;
        numeroAtaque++;

        atacar(jugador, gestorEnemigos);
    }

    protected abstract void atacar(Jugador jugador, GestorEnemigos gestorEnemigos);

    public abstract String getTipo();

    public int getDireccionAtaque() {
        return 0;
    }

    public long getNumeroAtaque() {
        return numeroAtaque;
    }

    public int getDanio() {
        return danio;
    }
}
