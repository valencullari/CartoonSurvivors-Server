package org.example.entidades;

import org.example.entidades.armas.Arma;
import org.example.entidades.armas.Espada;
import org.example.entidades.armas.Pistola;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.example.juego.GestorEnemigos;

public class Jugador {
    private final Random azar = new Random();

    public static final int AUMENTO_CRITICO = 5;
    private static final int ARMADURA_MAXIMA = 50;
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
    private int ladoMirada = -1;

    private float vida;
    private float vidaMaxima = 100f;
    private double tiempoReaparicion;
    private float regeneracion = REGENERACION_BASE;
    private float velocidad = 200f;
    private int nivelVidaMaxima;
    private int armadura;
    private int probabilidadCritico;
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

        if ("MORDECAI".equals(personaje)) {
            agregarArma(new Pistola());
        } else {
            agregarArma(new Espada());
        }

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
        if (x != 0f) {
            if (x > 0f) {
                ladoMirada = 1;
            } else {
                ladoMirada = -1;
            }
        }

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

        posicionX = Math.max(0, Math.min(12090, posicionX));

        posicionY = Math.max(0, Math.min(12080, posicionY));
        curar(regeneracion * delta);
    }

    public void recibirDanio(float cantidad) {

        if (!estaVivo() || !Float.isFinite(cantidad) || cantidad <= 0f) {
            return;
        }

        vida = Math.max(0f, vida - cantidad * ((100 - armadura) / 100f));
        if (!estaVivo()) {
            eliminar();
        }
    }

    public void eliminar() {
        vida = 0f;
        direccionX = 0f;
        direccionY = 0f;
        tiempoReaparicion = SEGUNDOS_REAPARICION;
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


    public void aumentarVelocidad(int porcentaje) {
        velocidad *= 1f + porcentaje / 100f;
    }

    public boolean puedeDesbloquearArma() {
        return armas.size() == 1;
    }

    public void desbloquearArma() {
        if (puedeDesbloquearArma()) {
            if (armas.get(0) instanceof Espada) {
                agregarArma(new Pistola());
            } else {
                agregarArma(new Espada());
            }
        }
    }

    public void aumentarVidaMaxima(int cantidad) {
        vidaMaxima += cantidad;
        nivelVidaMaxima++;
        curar(15f);
    }

    public int getNivelVidaMaxima() {
        return nivelVidaMaxima;
    }

    public void aumentarRegeneracion(int cantidad) {
        regeneracion += cantidad;
    }

    public int getMargenArmadura() {
        return ARMADURA_MAXIMA - armadura;
    }

    public void aumentarArmadura(int porcentaje) {
        armadura = Math.min(ARMADURA_MAXIMA, armadura + porcentaje);
    }

    public boolean puedeAumentarCritico() {
        return probabilidadCritico < 100;
    }

    public void aumentarCritico(int porcentaje) {
        probabilidadCritico = Math.min(100, probabilidadCritico + porcentaje);
    }

    public float calcularDanio(float danio) {
        if (azar.nextInt(100) < probabilidadCritico) {
            return danio * 2f;
        }
        return danio;
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

    public int getLadoMirada() {
        return ladoMirada;
    }

    public float getVelocidad() {
        return velocidad;
    }

    public float getRegeneracion() {
        return regeneracion;
    }

    public int getArmadura() {
        return armadura;
    }

    public int getProbabilidadCritico() {
        return probabilidadCritico;
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
