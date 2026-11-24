package org.example.juego;

public class DificultadPartida {

    private static final int INICIO_FASE_FINAL = 30;
    private static final int DURACION_ETAPA = 60;

    private static final Etapa[] ETAPAS = {
            new Etapa(3.2f, 10, 300, 0, 0, 2),
            new Etapa(2.8f, 12, 400, 20, 50, 2),
            new Etapa(2.4f, 13, 500, 30, 100, 3),
            new Etapa(2.1f, 15, 500, 40, 150, 3),
            new Etapa(2f, 17, 500, 50, 200, 4),
            new Etapa(2f, 17, 600, 60, 200, 4),
            new Etapa(2f, 18, 600, 70, 250, 4),
            new Etapa(2f, 19, 700, 80, 300, 5),
            new Etapa(2f, 20, 700, 90, 350, 5),
            new Etapa(2f, 20, 700, 100, 400, 5),
    };

    private double tiempoTranscurrido;

    public void actualizar(float delta) {
        tiempoTranscurrido = Math.min(INICIO_FASE_FINAL, tiempoTranscurrido + delta);
    }

    public Etapa getEtapa() {
        int indice = (int) tiempoTranscurrido / DURACION_ETAPA;
        return ETAPAS[Math.min(indice, ETAPAS.length - 1)];
    }

    public int getTiempoTranscurrido() {
        return (int) tiempoTranscurrido;
    }

    public boolean estaEnFaseFinal() {
        return tiempoTranscurrido >= INICIO_FASE_FINAL;
    }

    public record Etapa(float intervalo, int cantidad, int maximo, int vidaExtraBasico, int vidaExtraJefe,
                        int maximoJefes) {
    }
}
