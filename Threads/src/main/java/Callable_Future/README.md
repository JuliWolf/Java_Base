# Future and Callable

`Callable` - вид потоков, из которых можно возвращать значения  
`Future` - Интерфейс возвращаемых из потоков значений

## Callable

1. Создается по аналогии с `Runnable`
2. Из потоков типа `Callable` можно возвращать значения
3. Возвращаемое значение из потока типа `Callable` будет тип `Future`
4. Метод `get` у `Future` будет ожидать завершения потока
5. Метод `get` может вызвать `InterruptedException` а также исключение, которое было возвращено в методе `call`

```
import java.util.Random;
import java.util.concurrent.*;

public class Main {
  public static void main(String[] args) {
    ExecutorService executorService = Executors.newFixedThreadPool(1);
    Future<Integer> future = executorService.submit(() -> {
      try {
        System.out.println("Starting");

        Thread.sleep(500);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

      System.out.println("Finished");

      Random random = new Random();

      int randomValue = random.nextInt(10);

      if (randomValue < 5) {
        throw new Exception("Something bad happened!");
      }

      return randomValue;
    });

    executorService.shutdown();

    try {
      int result = future.get(); // get дожидается окончания выполнения потока
      System.out.println(result);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      Throwable ex = e.getCause();

      System.out.println(ex);
    }

  }
}
```