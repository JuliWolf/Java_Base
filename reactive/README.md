# Reactive

## IO модели
- sync + blocking - Дефолтная работа запросов
  - запрос
  - ожидание
  - запрос
- async - Создается отдельный поток для запроса
  - В отдельном запросе запрос считается синхронным
- non-blocking - Не надо ждать ответа, когда ответ будет готов вызывающий код будет оповещен
  - Работет на low-level коде
- non-blocking + async 
  - создается отдельный поток, в котором будет вызвано не блокирующее действие
  - Когда запрос будет завершен поток получит оповещение о факте завершения процесса

## Задача reactive programming
- Упростить работу с разными io моделями
- Предоставляет абстрактные интерфейсы для работы с асинхронными запросами
- Построен на observable-pattern

## Publisher and Subscriber
- **Publisher**
  - Имеет метод `subscribe`, который дает возможность подписаться на действия `Publisher`
  - Вызывает метод `Subscriber.hasNext` чтобы передать данные
  - Когда все данные переданы будет вызван метод `Subscriber.onComplete`
  - Если в ходе выполнения возникнут ошибки, то будет вызван метод `Subscriber.onError`
```
public interface Publisher<T> {
 public void subscribe(Subscriber<? super T> s);
}
```

- **Subscriber**
  - Подписывается на действия `Publisher` используя метод `Publisher.subscribe`
  - Получает объект `Subscription` через метод `onSubscribe`
  - Может отменить подписку если отслеживать изменения больше не нужно `Subscription.cancel`
```
public interface Subscriber<T> {
 public void onSubscribe(Subscription s);
 public vois onNext(T t);
 public void onError(Throwable t);
 public void onComplete();
}
```

## Mono vs Flux Publishers
| Mono                                                                                  | Flux                                                                           |
|---------------------------------------------------------------------------------------|--------------------------------------------------------------------------------|
| Возвращает или 0 или 1 результат                                                      | Возвращает от 0 до бесконечности                                               |
| Используется когда нам нужно получить 1 конкретный результат                          | Используется когда мы хотим получить несколько значений                        |
| Используется когда необходимо вернуть ответ по единственному вычислительному процессу | Используется для возврата коллекций, стримов или других множественных значений |
| Возвращает Optional                                                                   | Возвращает Stream                                                              |

## Mono example
```
public class Lecture02MonoJust {
  public static void main(String[] args) {
    // publisher
    Mono<Integer> mono = Mono.just(1);

    System.out.println(mono); // MonoJust

    // subscribe
    mono.subscribe(i -> System.out.println("Received: " + i));
  }
}
```

- Мы так же можем сделать что-то по окончанию процесса
```
public class Lecture03MonoSubscribe {
  public static void main(String[] args) {
    // publisher
    Mono<String> mono = Mono.just("ball");

    // 2 provide consumer
    mono.subscribe(
        item -> System.out.println(item), // ball
        err -> System.out.println(err.getMessage()),
        () -> System.out.println("Completed") // Completed
    );
  }
}
```

- Если в процессе может быть ошибка, то мы можем его обработать
```
public class Lecture03MonoSubscribe {
  public static void main(String[] args) {
    // publisher
    Mono<Integer> mono = Mono.just("ball")
        .map(String::length)
        .map(l -> l / 0);

    // 2 provide consumer
    mono.subscribe(
        item -> System.out.println(item),
        err -> System.out.println(err.getMessage()), // / by zero
        () -> System.out.println("Completed")
    );
  }
}
```
- Если мы не обработаем ошибку, то она будет выброшена
```
public class Lecture03MonoSubscribe {
  public static void main(String[] args) {
    // publisher
    Mono<Integer> mono = Mono.just("ball")
        .map(String::length)
        .map(l -> l / 0);

    // 3 if we do not provide error handler
    mono.subscribe(
        item -> System.out.println(item) // throw error
    );
  }
}
```

- Возвращение пустого результата
```
public class Lecture04MonoEmptyOnError {
  public static void main(String[] args) {
    // return only Completed because result if empty
    userRepository(2)
        .subscribe(
            Util.onNext(),
            Util.onError(),
            Util.onComplete()
        );
  }

  private static Mono<String> userRepository (int userId) {
    // return data only if userId == 1
    if (userId == 1) {
      return Mono.just(Util.faker().name().firstName());
    } else if (userId == 2) {
      return Mono.empty();
    }

    return Mono.error(new RuntimeException("Not in the allowed range"));
  }
}
```

- Возвращение ошибки
```
public class Lecture04MonoEmptyOnError {
  public static void main(String[] args) {
    // Return error: "Error: Not in the allowed range"
    userRepository(20)
        .subscribe(
            Util.onNext(),
            Util.onError(),
            Util.onComplete()
        );
  }

  private static Mono<String> userRepository (int userId) {
    // return data only if userId == 1
    if (userId == 1) {
      return Mono.just(Util.faker().name().firstName());
    } else if (userId == 2) {
      return Mono.empty();
    }

    return Mono.error(new RuntimeException("Not in the allowed range"));
  }
}
```

## Just vs supplier
- Если данные уже есть то можно использовать just, так как он сразу выполняет метод, который был в него передан или записывает данные, которые получил
```
public class Lecture05FromSupplier {
  public static void main(String[] args) {
    // use just only when you have data already
    Mono<String> mono = Mono.just(getName()); // Generating name..
  }

  private static String getName () {
    System.out.println("Generating name..");
    return Util.faker().name().fullName();
  }
}
```
- Если для получения значений необходимо выполнить какие-то вычисления, то надо использовать fromSupplier
- fromSupplier делаем метод lazy
- Если не будет subscribers, то метод не вызовется
```
public class Lecture05FromSupplier {
  public static void main(String[] args) {
    Mono<String> mono = Mono.fromSupplier(() -> getName()); // nothing happen
    mono.subscribe(
        Util.onNext() // return name
    );
  }

  private static String getName () {
    System.out.println("Generating name..");
    return Util.faker().name().fullName();
  }
}
```

## Supplier vs Callable

| Supplier                                                                  | Callable                                                                  |
|---------------------------------------------------------------------------|---------------------------------------------------------------------------|
| Принимает функцию, которая не принимает аргументов и возвращает результат | Принимает функцию, которая не принимает аргументов и возвращает результат |
| Не выбрасывает исключения                                                 | Выбрасывает исключение в случае ошибки                                    |
| Используется для создания потокобезопасных функция генерации данных       | Используется для длительных асинхронных вычислений                        |                                                                           |                                                                           |

```
public class Lecture05FromSupplier {
  public static void main(String[] args) {
    Supplier<String> stringSupplier = () -> getName();
    Callable<String> stringCallable = () -> getName();
    Mono.fromCallable(stringCallable)
        .subscribe(
            Util.onNext() // Received: Mrs. Fanny Buckridge
        );
  }

  private static String getName () {
    System.out.println("Generating name..");
    return Util.faker().name().fullName();
  }
}
```

## Mono From Future
- Можно использовать `CompletableFuture` для получения данных из запроса

```
public class Lecture07MonoFromFuture {
  public static void main(String[] args) {
    Mono.fromFuture(getName())
        .subscribe(
            Util.onNext()
        );

    Util.sleepSeconds(1);
  }

  private static CompletableFuture<String> getName () {
    return CompletableFuture.supplyAsync(() -> Util.faker().name().fullName());
  }
}
```

## Mono from Runnable
- Когда нуэжно выполнить какой-то код после выполнения Runnable
```
public class Lecture08MonoFromRunnable {
  public static void main(String[] args) {
    Mono.fromRunnable(timeConsumingProcess())
        .subscribe(
            Util.onNext(),
            Util.onError(),
            () -> {
              System.out.println("process is done. Sending emails...");
            }
        );
  }

  private static Runnable timeConsumingProcess () {
    return () -> {
      Util.sleepSeconds(3);
      System.out.println("Operation completed");
    };
  }
}
```