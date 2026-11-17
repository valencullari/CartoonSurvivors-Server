package org.example.entidades;

public class Enemigo {

    private final int id;
    private final TipoEnemigo tipo;

    private float posicionX;
    private float posicionY;

    private int vida;

    public Enemigo(int id, TipoEnemigo tipo, float posicionX, float posicionY) {

        this.id = id;
        this.tipo = tipo;

        this.posicionX = posicionX;
        this.posicionY = posicionY;

        this.vida = tipo.getVidaMaxima();
    }

    public void actualizar(float delta, Jugador jugador1, Jugador jugador2) {

        float distancia1 = calcularDistancia(jugador1);
        float distancia2 = calcularDistancia(jugador2);

        Jugador objetivo;

        if (distancia1 <= distancia2) {
            objetivo = jugador1;
        } else {
            objetivo = jugador2;
        }

        float direccionX = objetivo.getPosicionX() - posicionX;

        float direccionY = objetivo.getPosicionY() - posicionY;

        float distancia = (float) Math.sqrt(direccionX * direccionX + direccionY * direccionY);

        if (distancia <= 28f) {
            return;
        }

        direccionX /= distancia;
        direccionY /= distancia;

        float movimiento = tipo.getVelocidad() * delta;

        movimiento = Math.min(movimiento, distancia - 28f);

        posicionX += direccionX * movimiento;
        posicionY += direccionY * movimiento;
    }

    private float calcularDistancia(Jugador jugador) {

        float diferenciaX = jugador.getPosicionX() - posicionX;

        float diferenciaY = jugador.getPosicionY() - posicionY;

        return (float) Math.sqrt(diferenciaX * diferenciaX + diferenciaY * diferenciaY);
    }

    public void recibirDanio(int cantidad) {

        if (cantidad <= 0) {
            return;
        }

        vida -= cantidad;

        if (vida < 0) {
            vida = 0;
        }
    }

    public boolean estaVivo() {
        return vida > 0;
    }

    public int getId() {
        return id;
    }

    public TipoEnemigo getTipo() {
        return tipo;
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

    public int getDanio() {
        return tipo.getDanio();
    }
}
