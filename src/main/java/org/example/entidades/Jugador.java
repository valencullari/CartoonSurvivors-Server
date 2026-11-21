package org.example.entidades;

import org.example.entidades.armas.Arma;
import java.util.ArrayList;
import java.util.List;
import org.example.juego.GestorEnemigos;

public class Jugador {

    private static final long TIEMPO_SIN_MOVIMIENTO = 200;
    private static final float ANCHO_HITBOX = 40f;
    private static final float ALTO_HITBOX = 60f;
    private static final float VIDA_MAXIMA = 100f;

    private final int id;
    private final String personaje;

    private float posicionX;
    private float posicionY;

    private float direccionX;
    private float direccionY;

    private float vida;
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

        vida = VIDA_MAXIMA;

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
            direccionX = 0;
            direccionY = 0;
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
    }

    public void recibirDanio(float cantidad) {

        if (!estaVivo() || !Float.isFinite(cantidad) || cantidad <= 0f) {
            return;
        }

        vida = Math.max(0f, vida - cantidad);
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

    public boolean estaVivo() {
        return vida > 0f;
    }

    public float getVida() {
        return vida;
    }

    public float getVidaMaxima() {
        return VIDA_MAXIMA;
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
