package org.example.juego;

public class DificultadPartida {

    private float tiempoTranscurrido;

    public DificultadPartida() {
        tiempoTranscurrido = 0;
    }

    public void actualizar(float delta) {
        tiempoTranscurrido += delta;
    }

    public float getIntervaloAparicion() {

        if (tiempoTranscurrido < 30f) {
            return 3f;
        }

        if (tiempoTranscurrido < 60f) {
            return 2f;
        }

        if (tiempoTranscurrido < 120f) {
            return 1f;
        }

        return 0.5f;
    }

    public int getMaximoEnemigos() {
        return 200;
    }

    public int getCantidadPorAparicion() {

        if (tiempoTranscurrido < 60f) {
            return 10;
        }

        if (tiempoTranscurrido < 120f) {
            return 10;
        }

        return 10;
    }

    public float getTiempoTranscurrido() {
        return tiempoTranscurrido;
    }
}
