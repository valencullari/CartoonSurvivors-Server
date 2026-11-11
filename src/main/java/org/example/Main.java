import org.example.red.ServidorUDP;

public class Main {

    public static void main(String[] args) {

        ServidorUDP servidor = new ServidorUDP(7777);

        servidor.iniciar();
    }
}