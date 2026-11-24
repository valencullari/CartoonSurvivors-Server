package org.example.entidades.armas;

import org.example.entidades.Jugador;
import org.example.juego.GestorEnemigos;

public abstract class Arma {

    private float danio;
    private float cooldown;

    private float tiempoRestante;
    private float alcance = 1f;
    private float alcanceAtaque = 1f;
    private long numeroAtaque;
    private int direccion;
    private int direccionAtaque;

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

        float dx = jugador.getDireccionX();
        float dy = jugador.getDireccionY();
        if (dx * dx + dy * dy > 0.0001f) {
            int sector = (int) Math.round(Math.atan2(dy, dx) / (Math.PI / 4.0));
            direccion = Math.floorMod(sector, 8);
        }

        tiempoRestante -= delta;

        if (tiempoRestante > 0f) {
            return;
        }

        alcanceAtaque = alcance;
        direccionAtaque = direccion;

        if (atacar(jugador, gestorEnemigos)) {
            tiempoRestante = cooldown;
            numeroAtaque++;
        } else {
            tiempoRestante = 0f;
        }
    }

    protected abstract boolean atacar(Jugador jugador, GestorEnemigos gestorEnemigos);

    public abstract String getTipo();

    public int getDireccionAtaque() {
        return direccionAtaque;
    }

    public long getNumeroAtaque() {
        return numeroAtaque;
    }

    public void mejorar(int porcentajeDanio, int porcentajeAlcance, int porcentajeIntervalo) {
        danio *= 1f + porcentajeDanio / 100f;
        alcance *= 1f + porcentajeAlcance / 100f;
        float factorIntervalo = 1f - porcentajeIntervalo / 100f;
        cooldown *= factorIntervalo;
        tiempoRestante *= factorIntervalo;
    }

    public float getAlcanceAtaque() {
        return alcanceAtaque;
    }

    public float getDanio() {
        return danio;
    }
}
