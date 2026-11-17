package org.example.entidades;

public class Enemigo {

    private final int id;

    private float posicionX;
    private float posicionY;

    private int vida;

    private static final float VELOCIDAD = 80f;

    public Enemigo(int id, float posicionX, float posicionY) {

        this.id = id;
        this.posicionX = posicionX;
        this.posicionY = posicionY;

        this.vida = 25;
    }

    public void actualizar(float delta, Jugador jugador1, Jugador jugador2) {

        // Distancia a cada jugador.
        float distancia1 = calcularDistancia(jugador1);
        float distancia2 = calcularDistancia(jugador2);

        // Elegir el jugador más cercano.
        Jugador objetivo;

        if (distancia1 <= distancia2) {
            objetivo = jugador1;
        } else {
            objetivo = jugador2;
        }

        // Calcular hacia dónde tiene que moverse.
        float direccionX = objetivo.getPosicionX() - posicionX;

        float direccionY = objetivo.getPosicionY() - posicionY;

        float distancia = (float) Math.sqrt(direccionX * direccionX + direccionY * direccionY);

        // Evitar dividir por cero y detenerse
        // cerca del jugador.
        if (distancia <= 28f) {
            return;
        }

        direccionX /= distancia;
        direccionY /= distancia;

        float movimiento = VELOCIDAD * delta;

        // No atravesar al objetivo en un solo paso.
        movimiento = Math.min(movimiento, distancia - 28f);

        posicionX += direccionX * movimiento;
        posicionY += direccionY * movimiento;
    }

    private float calcularDistancia(Jugador jugador) {

        float diferenciaX = jugador.getPosicionX() - posicionX;

        float diferenciaY = jugador.getPosicionY() - posicionY;

        return (float) Math.sqrt(diferenciaX * diferenciaX + diferenciaY * diferenciaY);
    }

    public int getId() {
        return id;
    }

    public float getPosicionX() {
        return posicionX;
    }

    public float getPosicionY() {
        return posicionY;
    }

    public int getVida() {
        return vida;
    }
}
