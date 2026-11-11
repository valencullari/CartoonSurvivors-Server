
package org.example.red.gestor;

import org.example.red.modelo.ClienteConectado;

import java.util.ArrayList;
import java.util.List;

public class GestorDesconexiones {

    private static final long TIEMPO_MAXIMO_INACTIVIDAD = 5000;

    public List<ClienteConectado> detectarDesconexiones(
            GestorConexiones gestor) {

        List<ClienteConectado> desconectados = new ArrayList<>();

        long tiempoActual = System.currentTimeMillis();

        for (ClienteConectado cliente : gestor.getClientes()) {

            long inactividad = tiempoActual
                    - cliente.getUltimoMensaje();

            if (inactividad > TIEMPO_MAXIMO_INACTIVIDAD) {
                desconectados.add(cliente);
            }
        }

        return desconectados;
    }
}
