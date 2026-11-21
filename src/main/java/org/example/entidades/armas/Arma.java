package org.example.entidades.armas;

import org.example.entidades.Jugador;
import org.example.juego.GestorEnemigos;

public abstract class Arma {

    protected int danio;
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

        tiempoRestante = cooldown;
        numeroAtaque++;
        alcanceAtaque = alcance;
        direccionAtaque = direccion;

        atacar(jugador, gestorEnemigos);
    }

    protected abstract void atacar(Jugador jugador, GestorEnemigos gestorEnemigos);

    public abstract String getTipo();

    public int getDireccionAtaque() {
        return direccionAtaque;
    }

    public long getNumeroAtaque() {
        return numeroAtaque;
    }

    public void aumentarDanio() {
        danio += 5;
    }

    public void aumentarCadencia() {
        cooldown *= 0.9f;
    }

    public void aumentarAlcance() {
        alcance += 0.15f;
    }

    public float getAlcanceAtaque() {
        return alcanceAtaque;
    }

    public int getDanio() {
        return danio;
    }
}
