
package org.example.red.modelo;

import java.net.InetAddress;

public class ClienteConectado {

    private final int id;
    private final InetAddress direccionIP;
    private final int puerto;

    private volatile long ultimoMensaje;
    private volatile boolean conectado;
    private volatile boolean listo;

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

    public boolean isConectado() {
        return conectado;
    }

    public boolean estaListo() {
        return listo;
    }

    public long getUltimoMensaje() {
        return ultimoMensaje;
    }

    public void actualizarActividad() {
        ultimoMensaje = System.currentTimeMillis();
    }

    public void setListo(boolean listo) {
        this.listo = listo;
    }

    public void desconectar() {
        conectado = false;
        listo = false;
    }
}
