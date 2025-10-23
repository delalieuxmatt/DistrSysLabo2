import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class Main {
    private void doTest(){
        try {
            // fire to localhost port 1099
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
            // search for CounterService
            Counter impl = (Counter) myRegistry.lookup("CounterService");
            // call server's method
            System.out.println(impl.berekenSom(10, 5));
            System.out.println(impl.berekenVerschil(10,5));


        } catch (Exception e) { e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        Main main = new Main(); main.doTest();
    }
}