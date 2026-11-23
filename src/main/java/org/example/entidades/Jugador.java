package org.example.entidades;

import org.example.entidades.armas.Arma;
import org.example.entidades.armas.Espada;
import org.example.entidades.armas.Pistola;
import java.util.ArrayList;
import java.util.List;
import org.example.juego.GestorEnemigos;

public class Jugador {

    private static final long TIEMPO_SIN_MOVIMIENTO = 200;
    private static final int SEGUNDOS_REAPARICION = 30;
    private static final float ANCHO_HITBOX = 40f;
    private static final float ALTO_HITBOX = 60f;
    private static final float REGENERACION_BASE = 0.5f;

    private final int id;
    private final String personaje;

    private float posicionX;
    private float posicionY;

    private float direccionX;
    private float direccionY;

    private float vida;
    private float vidaMaxima = 100f;
    private double tiempoReaparicion;
    private float regeneracion = REGENERACION_BASE;
    private float velocidad = 200f;
    private final List<Arma> armas = new ArrayList<>();

    private long ultimoMovimiento;

    public Jugador(int id, String personaje, float posicionX, float posicionY) {

        this.id = id;
        this.personaje = personaje;
        this.posicionX = posicionX;
        this.posicionY = posicionY;

        direccionX = 0;
        direccionY = 0;

        vida = vidaMaxima;

        agregarArma("MORDECAI".equals(personaje) ? new Pistola() : new Espada());

        ultimoMovimiento = System.currentTimeMillis();
    }

    public void establecerMovimiento(float x, float y) {

        if (!estaVivo()) {
            return;
        }

        if (!Float.isFinite(x) || !Float.isFinite(y)) {
            return;
        }

        x = Math.max(-1f, Math.min(1f, x));
        y = Math.max(-1f, Math.min(1f, y));

        float longitud = (float) Math.sqrt(x * x + y * y);

        if (longitud > 1f) {
            x /= longitud;
            y /= longitud;
        }

        direccionX = x;
        direccionY = y;

        ultimoMovimiento = System.currentTimeMillis();
    }

    public void actualizar(float delta) {

        if (!estaVivo()) {
            return;
        }

        if (System.currentTimeMillis() - ultimoMovimiento > TIEMPO_SIN_MOVIMIENTO) {

            direccionX = 0;
            direccionY = 0;
        }

        posicionX += direccionX * velocidad * delta;
        posicionY += direccionY * velocidad * delta;

        posicionX = Math.max(0, Math.min(12060, posicionX));

        posicionY = Math.max(0, Math.min(12140, posicionY));
        curar(regeneracion * delta);
    }

    public void recibirDanio(float cantidad) {

        if (!estaVivo() || !Float.isFinite(cantidad) || cantidad <= 0f) {
            return;
        }

        vida = Math.max(0f, vida - cantidad);
        if (!estaVivo()) {
            direccionX = 0f;
            direccionY = 0f;
            tiempoReaparicion = SEGUNDOS_REAPARICION;
        }
    }

    public void actualizarReaparicion(float delta) {
        if (!estaVivo()) {
            tiempoReaparicion = Math.max(0, tiempoReaparicion - delta);
            if (tiempoReaparicion == 0) {
                reaparecer();
            }
        }
    }

    public void reaparecer() {
        vida = vidaMaxima;
        tiempoReaparicion = 0;
    }

    public int getSegundosParaReaparecer() {
        return (int) Math.ceil(tiempoReaparicion);
    }

    public void agregarArma(Arma arma) {

        if (arma == null) {
            return;
        }
        armas.add(arma);
    }


    public void actualizarArmas(
            float delta,
            GestorEnemigos gestorEnemigos) {

        if (!estaVivo()) {
            return;
        }

        for (Arma arma : armas) {
            arma.actualizar(delta, this, gestorEnemigos);
        }
    }


    public void aumentarVelocidad() {
        velocidad += 20f;
    }

    public boolean puedeDesbloquearArma() {
        return armas.size() == 1;
    }

    public void desbloquearArma() {
        if (puedeDesbloquearArma()) {
            agregarArma(armas.getFirst() instanceof Espada ? new Pistola() : new Espada());
        }
    }

    public void aumentarVidaMaxima() {
        vidaMaxima += 20f;
        curar(30f);
    }

    public void aumentarRegeneracion() {
        regeneracion += 1f;
    }

    private void curar(float cantidad) {
        if (estaVivo()) {
            vida = Math.min(vidaMaxima, vida + cantidad);
        }
    }

    public boolean estaVivo() {
        return vida > 0f;
    }

    public float getVida() {
        return vida;
    }

    public float getVidaMaxima() {
        return vidaMaxima;
    }

    public int getId() {
        return id;
    }

    public String getPersonaje() {
        return personaje;
    }

    public float getPosicionX() {
        return posicionX;
    }

    public float getPosicionY() {
        return posicionY;
    }

    public float getDireccionX() {
        return direccionX;
    }

    public float getDireccionY() {
        return direccionY;
    }

    public List<Arma> getArmas() {
        return List.copyOf(armas);
    }

    public float getAnchoHitbox() {
        return ANCHO_HITBOX;
    }

    public float getAltoHitbox() {
        return ALTO_HITBOX;
    }
}
