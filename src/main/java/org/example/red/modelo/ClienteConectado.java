
package org.example.red.modelo;

import java.net.InetAddress;

public class ClienteConectado {

    private final int id;
    private final InetAddress direccionIP;
    private final int puerto;

    private long ultimoMensaje;

    private boolean conectado;
    private boolean listo;
    private String personajeSeleccionado;

    public ClienteConectado(
            int id,
            InetAddress direccionIP,
            int puerto) {

        this.id = id;
        this.direccionIP = direccionIP;
        this.puerto = puerto;

        this.ultimoMensaje = System.currentTimeMillis();

        this.conectado = true;
        this.listo = false;
    }

    public int getId() {
        return id;
    }

    public InetAddress getDireccionIP() {
        return direccionIP;
    }

    public int getPuerto() {
        return puerto;
    }

    public long getUltimoMensaje() {
        return ultimoMensaje;
    }

    public boolean isConectado() {
        return conectado;
    }

    public boolean isListo() {
        return listo;
    }

    public void actualizarActividad() {
        ultimoMensaje = System.currentTimeMillis();
    }

    public void setListo(boolean listo) {
        this.listo = listo;
    }
    public String getPersonajeSeleccionado() {
        return personajeSeleccionado;
    }

    public void setPersonajeSeleccionado(String personaje) {
        this.personajeSeleccionado = personaje;
    }

    public void desconectar() {
        conectado = false;
        listo = false;
    }
}
