import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class ProdutorConsumidorSemaforo {
    private static final int CAPACIDADE = 3;
    private static final Queue<Integer> buffer = new LinkedList<>();

    // Semáforos de Sincronização e Sinalização
    private static final Semaphore vagasDisponiveis = new Semaphore(CAPACIDADE); // Inicializa com o tamanho do buffer (3)
    private static final Semaphore itensDisponiveis = new Semaphore(0);           // Inicializa vazio (0)
    private static final Semaphore mutex = new Semaphore(1);                      // Trinco binário para exclusão mútua

    public static void main(String[] args) {
        new Thread(new ProdutorS(), "Produtor-Semaforo").start();
        new Thread(new ConsumidorS(), "Consumidor-Semaforo").start();
    }

    static class ProdutorS implements Runnable {
        @Override
        public void run() {
            int item = 0;
            try {
                while (true) {
                    vagasDisponiveis.acquire(); // Decrementa vagas livres. Se 0, transita para WAITING
                    mutex.acquire();            // Entra na Região Crítica

                    buffer.add(item);
                    System.out.println(Thread.currentThread().getName() + " produziu item: " + item 
                                       + " | Ocupação: " + buffer.size() + "/" + CAPACIDADE);
                    item++;

                    mutex.release();            // Sai da Região Crítica
                    itensDisponiveis.release(); // Sinaliza ao consumidor e incrementa contador

                    Thread.sleep(500); // Simula tempo gasto na CPU
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }

    static class ConsumidorS implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    itensDisponiveis.acquire(); // Aguarda a existência de itens. Se 0, transita para WAITING
                    mutex.acquire();            // Entra na Região Crítica

                    int item = buffer.poll();
                    System.out.println(Thread.currentThread().getName() + " consumiu item: " + item 
                                       + " | Ocupação: " + buffer.size() + "/" + CAPACIDADE);

                    mutex.release();            // Sai da Região Crítica
                    vagasDisponiveis.release(); // Sinaliza liberação de espaço para o produtor

                    Thread.sleep(800);
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }
}