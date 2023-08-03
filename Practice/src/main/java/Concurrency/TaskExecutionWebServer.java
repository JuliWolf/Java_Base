package Concurrency;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author JuliWolf
 * @date 03.08.2023
 */
public class TaskExecutionWebServer {
  private static final int NTHREADS = 100;
  private static final Executor exec = Executors.newFixedThreadPool(NTHREADS);

  public static void main (String[] args) throws IOException {
    ServerSocket socket = new ServerSocket(80);

    while (true) {
      final Socket connection = socket.accept();

      Runnable task = new Runnable() {
        @Override
        public void run() {
          handleRequest(connection);
        }
      };

      exec.execute(task);
    }
  }

  // Исполнитель, запускающий отельный поток для каждой задачи
  public class ThreadPerTaskExecutor implements Executor {

    @Override
    public void execute(Runnable r) {
      new Thread(r).start();
    }
  }

  // Для синхронно выполняющихся задач
  public class WithinThreadExecutor implements Executor {

    @Override
    public void execute(Runnable r) {
      r.run();
    }
  }
}
