package org.example.juego;

import org.example.entidades.Enemigo;
import org.example.entidades.Jugador;
import org.example.entidades.TipoEnemigo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GestorEnemigos {

    private final List<Enemigo> enemigos;

    private int siguienteId;

    public GestorEnemigos() {

        enemigos = new ArrayList<>();

        siguienteId = 1;
    }

    public void crearEnemigo(TipoEnemigo tipo, float posicionX, float posicionY) {

        Enemigo enemigo = new Enemigo(siguienteId, tipo, posicionX, posicionY);

        enemigos.add(enemigo);

        siguienteId++;
    }

    public void actualizar(float delta, Jugador jugador1, Jugador jugador2) {

        for (int i = enemigos.size() - 1; i >= 0; i--) {

            Enemigo enemigo = enemigos.get(i);

            if (!enemigo.estaVivo()) {
                enemigos.remove(i);
                continue;
            }

            enemigo.actualizar(delta, jugador1, jugador2);
        }
    }

    public Enemigo obtenerEnemigo(int id) {

        for (Enemigo enemigo : enemigos) {

            if (enemigo.getId() == id) {
                return enemigo;
            }
        }

        return null;
    }

    public List<Enemigo> getEnemigos() {

        return Collections.unmodifiableList(enemigos);
    }

    public int cantidadEnemigos() {

        return enemigos.size();
    }
}
