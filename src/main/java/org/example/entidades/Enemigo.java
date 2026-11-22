package org.example.entidades;

public class Enemigo {

    private static final float COOLDOWN_ATAQUE = 1.0f;

    private static final float FUERZA_RETROCESO = 18f;
    private static final float DURACION_RETROCESO = 0.15f;


    private final int id;
    private final TipoEnemigo tipo;
    private float posicionX;
    private float posicionY;
    private int vida;
    private float tiempoRestanteAtaque = 0f;
    private float tiempoRetroceso = 0f;
    private float velocidadRetrocesoX = 0f;
    private float velocidadRetrocesoY = 0f;

    public Enemigo(int id, TipoEnemigo tipo, float posicionX, float posicionY, int vidaExtra) {

        this.id = id;
        this.tipo = tipo;

        this.posicionX = posicionX;
        this.posicionY = posicionY;

        this.vida = tipo.getVidaMaxima() + vidaExtra;
    }


    public void actualizar(float delta, Jugador jugador1, Jugador jugador2) {

        Jugador objetivo = elegirObjetivo(jugador1, jugador2);
        if (objetivo == null) {
            return;
        }

        // Centros de las hitboxes
        float centroEnemigoX = posicionX + getAnchoHitbox() / 2f;
        float centroEnemigoY = posicionY + getAltoHitbox() / 2f;

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


    public Jugador elegirObjetivo(Jugador jugador1, Jugador jugador2) {

        if (!jugador1.estaVivo()) {
            return jugador2.estaVivo() ? jugador2 : null;
        }
        if (!jugador2.estaVivo()) {
            return jugador1;
        }
        return calcularDistancia(jugador1) <= calcularDistancia(jugador2) ? jugador1 : jugador2;
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
        posicionX = Math.max(0f, Math.min(12800f - getAnchoHitbox(), posicionX));

        posicionY = Math.max(0f, Math.min(12800f - getAltoHitbox(), posicionY));
    }


    public void actualizarCooldownAtaque(float delta) {

        if (tiempoRestanteAtaque > 0f) {
            tiempoRestanteAtaque = Math.max(0f, tiempoRestanteAtaque - delta);
        }
    }


    public void retrocederDesde(Jugador jugador) {

        float centroEnemigoX = posicionX + getAnchoHitbox() / 2f;

        float centroEnemigoY = posicionY + getAltoHitbox() / 2f;

        float centroJugadorX = jugador.getPosicionX() + jugador.getAnchoHitbox() / 2f;

        float centroJugadorY = jugador.getPosicionY() + jugador.getAltoHitbox() / 2f;

        float direccionX = centroEnemigoX - centroJugadorX;
        float direccionY = centroEnemigoY - centroJugadorY;

        float distancia = (float) Math.sqrt(direccionX * direccionX + direccionY * direccionY);

        if (distancia < 0.001f) {
            direccionX = id % 2 == 0 ? 1f : -1f;
            direccionY = 0f;
        } else {
            direccionX /= distancia;
            direccionY /= distancia;
        }

        velocidadRetrocesoX = direccionX * FUERZA_RETROCESO / DURACION_RETROCESO;

        velocidadRetrocesoY = direccionY * FUERZA_RETROCESO / DURACION_RETROCESO;

        tiempoRetroceso = DURACION_RETROCESO;
    }

    public void actualizarRetroceso(float delta) {

        if (tiempoRetroceso <= 0f) {
            return;
        }

        float tiempoAplicado = Math.min(delta, tiempoRetroceso);

        desplazarPorColision(velocidadRetrocesoX * tiempoAplicado, velocidadRetrocesoY * tiempoAplicado);

        tiempoRetroceso -= tiempoAplicado;

        if (tiempoRetroceso <= 0f) {
            tiempoRetroceso = 0f;
            velocidadRetrocesoX = 0f;
            velocidadRetrocesoY = 0f;
        }
    }



    public boolean puedeAtacar() {
        return estaVivo() && tiempoRestanteAtaque <= 0f;
    }

    public void registrarAtaque() {
        tiempoRestanteAtaque = COOLDOWN_ATAQUE;
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
        return tipo.getAnchoHitbox();
    }

    public float getAltoHitbox() {
        return tipo.getAltoHitbox();
    }
}
