package org.example.juego;

public class DificultadPartida {

    private static final int INICIO_FASE_FINAL = 600;
    private static final int DURACION_ETAPA = 60;
    private static final float DANIO_EXTRA_POR_ETAPA = 0.14f;

    private static final Etapa[] ETAPAS = {
            new Etapa(3.2f, 10, 450, 0, 0, 3),
            new Etapa(2.8f, 15, 500, 20, 50, 3),
            new Etapa(2.4f, 18, 600, 30, 100, 4),
            new Etapa(2.1f, 18, 700, 40, 150, 4),
            new Etapa(2f, 20, 700, 50, 200, 5),
            new Etapa(2f, 22, 800, 65, 200, 5),
            new Etapa(2f, 22, 800, 80, 250, 6),
            new Etapa(2f, 25, 800, 90, 300, 6),
            new Etapa(2f, 25, 900, 100, 350, 7),
            new Etapa(2f, 30, 900, 120, 400, 7),
    };

    private double tiempoTranscurrido;

    public void actualizar(float delta) {
        tiempoTranscurrido += delta;
    }

    public Etapa getEtapa() {
        return ETAPAS[indiceEtapa()];
    }

    public float getMultiplicadorDanio() {
        return 1f + indiceEtapa() * DANIO_EXTRA_POR_ETAPA;
    }

    private int indiceEtapa() {
        return Math.min((int) tiempoTranscurrido / DURACION_ETAPA, ETAPAS.length - 1);
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
