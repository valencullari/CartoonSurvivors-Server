
package org.example.red.gestor;

import org.example.red.modelo.ClienteConectado;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class GestorConexiones {

    private static final int MAX_JUGADORES = 2;

    private final List<ClienteConectado> clientes;

    public GestorConexiones() {
        clientes = new ArrayList<ClienteConectado>();
    }

    public synchronized ClienteConectado registrarCliente(
            InetAddress ip,
            int puerto) {

        ClienteConectado existente = buscarCliente(ip, puerto);

        if (existente != null) {
            existente.actualizarActividad();
            return existente;
        }

        if (clientes.size() >= MAX_JUGADORES) {
            return null;
        }

        int id = clientes.size() + 1;

        ClienteConectado nuevo = new ClienteConectado(
                id,
                ip,
                puerto
        );

        clientes.add(nuevo);

        System.out.println("Jugador " + id + " conectado: " + ip);

        return nuevo;
    }

    public synchronized ClienteConectado buscarCliente(
            InetAddress ip,
            int puerto) {

        for (ClienteConectado cliente : clientes) {

            if (cliente.getDireccionIP().equals(ip)
                    && cliente.getPuerto() == puerto
                    && cliente.isConectado()) {

                return cliente;
            }
        }

        return null;
    }

    public synchronized ClienteConectado buscarCliente(int id) {

        for (ClienteConectado cliente : clientes) {

            if (cliente.getId() == id) {
                return cliente;
            }
        }

        return null;
    }

    public synchronized List<ClienteConectado> getClientes() {
        return new ArrayList<ClienteConectado>(clientes);
    }

    public synchronized int cantidadClientes() {
        return clientes.size();
    }

    public synchronized boolean partidaCompleta() {
        return clientes.size() == MAX_JUGADORES;
    }

    public synchronized boolean todosListos() {

        if (!partidaCompleta()) {
            return false;
        }

        for (ClienteConectado cliente : clientes) {

            if (!cliente.isListo()) {
                return false;
            }
        }

        return true;
    }

    public synchronized void eliminarTodos() {

        for (ClienteConectado cliente : clientes) {
            cliente.desconectar();
        }

        clientes.clear();

        System.out.println("Todos los jugadores fueron eliminados.");
    }

    public synchronized void eliminarCliente(int id) {

        ClienteConectado cliente = buscarCliente(id);

        if (cliente != null) {
            cliente.desconectar();
            clientes.remove(cliente);
        }
    }
}
