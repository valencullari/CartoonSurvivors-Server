
package org.example.entidades;

public class Jugador {

    private static final float VELOCIDAD = 200f;
    private static final long TIEMPO_SIN_MOVIMIENTO = 200;

    private final int id;
    private final String personaje;

    private float posicionX;
    private float posicionY;

    private float direccionX;
    private float direccionY;

    private long ultimoMovimiento;

    public Jugador(
            int id,
            String personaje,
            float posicionX,
            float posicionY) {

        this.id = id;
        this.personaje = personaje;
        this.posicionX = posicionX;
        this.posicionY = posicionY;

        this.direccionX = 0;
        this.direccionY = 0;
        this.ultimoMovimiento = System.currentTimeMillis();
    }

    public void establecerMovimiento(float x, float y) {

        // Evitar direcciones inválidas.
        if (!Float.isFinite(x) || !Float.isFinite(y)) {
            return;
        }

        // Limitar la dirección entre -1 y 1.
        x = Math.max(-1f, Math.min(1f, x));
        y = Math.max(-1f, Math.min(1f, y));

        // Normalizar la diagonal.
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

        // Si dejamos de recibir movimientos,
        // el personaje debe detenerse.
        if (System.currentTimeMillis() - ultimoMovimiento
                > TIEMPO_SIN_MOVIMIENTO) {

            direccionX = 0;
            direccionY = 0;
        }

        posicionX += direccionX * VELOCIDAD * delta;
        posicionY += direccionY * VELOCIDAD * delta;

        // Limitar el movimiento al mapa.
        posicionX = Math.max(0, Math.min(12760, posicionX));
        posicionY = Math.max(0, Math.min(12740, posicionY));
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
}
