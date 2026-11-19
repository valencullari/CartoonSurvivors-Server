package org.example.entidades;

public class Enemigo {

    private static final float ANCHO_HITBOX = 24f;
    private static final float ALTO_HITBOX = 40f;

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

        if (!jugador1.estaVivo() && !jugador2.estaVivo()) {
            return;
        }

        Jugador objetivo;

        float distancia1 = calcularDistancia(jugador1);
        float distancia2 = calcularDistancia(jugador2);

        if (!jugador1.estaVivo()) {
            objetivo = jugador2;
        } else if (!jugador2.estaVivo()) {
            objetivo = jugador1;
        } else if (distancia1 <= distancia2) {
            objetivo = jugador1;
        } else {
            objetivo = jugador2;
        }

        // Centros de las hitboxes
        float centroEnemigoX = posicionX + ANCHO_HITBOX / 2f;
        float centroEnemigoY = posicionY + ALTO_HITBOX / 2f;

        float centroJugadorX = objetivo.getPosicionX() + objetivo.getAnchoHitbox() / 2f;

        float centroJugadorY = objetivo.getPosicionY() + objetivo.getAltoHitbox() / 2f;

        float direccionX = centroJugadorX - centroEnemigoX;
        float direccionY = centroJugadorY - centroEnemigoY;

        float distancia = (float) Math.sqrt(direccionX * direccionX + direccionY * direccionY);

        if (distancia <= 0.001f) {
            return;
        }

        // Normalizar dirección
        direccionX /= distancia;
        direccionY /= distancia;

        float movimiento = Math.min(tipo.getVelocidad() * delta, distancia);

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


    public void desplazarPorColision(float desplazamientoX, float desplazamientoY) {

        if (!estaVivo()) {
            return;
        }

        posicionX += desplazamientoX;
        posicionY += desplazamientoY;

        // Mantener la hitbox dentro del mapa.
        posicionX = Math.max(0f, Math.min(12800f - ANCHO_HITBOX, posicionX));

        posicionY = Math.max(0f, Math.min(12800f - ALTO_HITBOX, posicionY));
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

    public float getAnchoHitbox() {
        return ANCHO_HITBOX;
    }

    public float getAltoHitbox() {
        return ALTO_HITBOX;
    }
}
