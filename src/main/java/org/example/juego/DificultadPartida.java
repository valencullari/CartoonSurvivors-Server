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

    public static final class Etapa {
        private final float intervalo;
        private final int cantidad;
        private final int maximo;
        private final int vidaExtraBasico;
        private final int vidaExtraJefe;
        private final int maximoJefes;

        public Etapa(float intervalo, int cantidad, int maximo, int vidaExtraBasico, int vidaExtraJefe, int maximoJefes) {
            this.intervalo = intervalo;
            this.cantidad = cantidad;
            this.maximo = maximo;
            this.vidaExtraBasico = vidaExtraBasico;
            this.vidaExtraJefe = vidaExtraJefe;
            this.maximoJefes = maximoJefes;
        }

        public float getIntervalo() {
            return intervalo;
        }

        public int getCantidad() {
            return cantidad;
        }

        public int getMaximo() {
            return maximo;
        }

        public int getVidaExtraBasico() {
            return vidaExtraBasico;
        }

        public int getVidaExtraJefe() {
            return vidaExtraJefe;
        }

        public int getMaximoJefes() {
            return maximoJefes;
        }

        @Override
        public boolean equals(Object objeto) {
            if (this == objeto) {
                return true;
            }
            if (!(objeto instanceof Etapa)) {
                return false;
            }
            Etapa otra = (Etapa) objeto;
            return Float.compare(intervalo, otra.intervalo) == 0
                && cantidad == otra.cantidad
                && maximo == otra.maximo
                && vidaExtraBasico == otra.vidaExtraBasico
                && vidaExtraJefe == otra.vidaExtraJefe
                && maximoJefes == otra.maximoJefes;
        }

        @Override
        public int hashCode() {
            int resultado = Float.hashCode(intervalo);
            resultado = 31 * resultado + Integer.hashCode(cantidad);
            resultado = 31 * resultado + Integer.hashCode(maximo);
            resultado = 31 * resultado + Integer.hashCode(vidaExtraBasico);
            resultado = 31 * resultado + Integer.hashCode(vidaExtraJefe);
            resultado = 31 * resultado + Integer.hashCode(maximoJefes);
            return resultado;
        }

        @Override
        public String toString() {
            return "Etapa[intervalo=" + intervalo
                + ", cantidad=" + cantidad
                + ", maximo=" + maximo
                + ", vidaExtraBasico=" + vidaExtraBasico
                + ", vidaExtraJefe=" + vidaExtraJefe
                + ", maximoJefes=" + maximoJefes
                + "]";
        }
    }
}
