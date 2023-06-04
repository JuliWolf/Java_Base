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
