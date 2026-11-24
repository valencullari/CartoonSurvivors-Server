package org.example.entidades;

public class Enemigo {

    private static final float COOLDOWN_ATAQUE = 1.0f;

    private static final float FUERZA_RETROCESO = 18f;
    private static final float FUERZA_RETROCESO_VIOLETA = 36f;
    private static final float DURACION_RETROCESO = 0.15f;
    private static final float ACELERACION_MUERTE = 100f;


    private final int id;
    private final TipoEnemigo tipo;
    private final int vidaMaxima;
    private float posicionX;
    private float posicionY;
    private float vida;
    private float tiempoRestanteAtaque = 0f;
    private float tiempoRetroceso = 0f;
    private float velocidadRetrocesoX = 0f;
    private float velocidadRetrocesoY = 0f;
    private float velocidad;

    public Enemigo(int id, TipoEnemigo tipo, float posicionX, float posicionY, int vidaExtra) {

        this.id = id;
        this.tipo = tipo;

        this.posicionX = posicionX;
        this.posicionY = posicionY;

        this.vidaMaxima = tipo.getVidaMaxima() + vidaExtra;
        this.vida = vidaMaxima;
        this.velocidad = tipo.getVelocidad();
    }


    public void actualizar(float delta, Jugador jugador1, Jugador jugador2) {

        if (tipo == TipoEnemigo.VIOLETA && tiempoRetroceso > 0f) {
            return;
        }

        Jugador objetivo = elegirObjetivo(jugador1, jugador2);
        if (objetivo == null) {
            return;
        }
        if (tipo == TipoEnemigo.MUERTE) {
            velocidad += ACELERACION_MUERTE * delta;
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

        float movimiento = Math.min(velocidad * delta, distancia);

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

    public void recibirDanio(float cantidad) {

        if (tipo == TipoEnemigo.MUERTE || cantidad <= 0) {
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

        iniciarRetroceso(direccionX, direccionY, FUERZA_RETROCESO);
    }

    public void rebotarDesdeContacto(float direccionX, float direccionY) {
        iniciarRetroceso(direccionX, direccionY, FUERZA_RETROCESO_VIOLETA);
    }

    private void iniciarRetroceso(float direccionX, float direccionY, float distancia) {
        velocidadRetrocesoX = direccionX * distancia / DURACION_RETROCESO;
        velocidadRetrocesoY = direccionY * distancia / DURACION_RETROCESO;
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

    public float getVida() {
        return vida;
    }

    public int getVidaMaxima() {
        return vidaMaxima;
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
