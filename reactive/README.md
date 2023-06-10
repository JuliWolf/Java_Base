# Reactive

+ [IO модели](#io-модели)
+ [Задача reactive programming](#задача-reactive-programming)
+ [Publisher and Subscriber](#publisher-and-subscriber)
+ [Mono vs Flux Publishers](#mono-vs-flux-publishers)
+ [Mono example](#mono-example)
+ [Flux examples](#flux-examples)
+ [Emitting](#emitting)
+ [Flux Take operator](#flux-take-operator)
+ [Stop emitting if process is cancelled](#stop-emitting-if-process-is-cancelled)
+ [Flux generate](#flux-generate)
+ [Operators](#operators)
+ [Cold and Hot Publishers](#cold-and-hot-publishers)
+ [Threads](#threads)

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

+ [Just vs supplier](#just-vs-supplier)
+ [Supplier vs Callable](#supplier-vs-callable)
+ [Mono From Future](#mono-from-future)
+ [Mono from Runnable](#mono-from-runnable)
+ [Summary](#summary)

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

### Just vs supplier
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

### Supplier vs Callable

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
`            Util.onNext() // Received: Mrs. Fanny Buckridge
        );
  }

  private static String getName () {
    System.out.println("Generating name..");
    return Util.faker().name().fullName();
  }
}
```

### Mono From Future
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

### Mono from Runnable
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

### Summary
| Type                     | Condition                                                            | What to use                                                                    |
|--------------------------|----------------------------------------------------------------------|--------------------------------------------------------------------------------|
| Создать Mono             | Данные уже есть                                                      | `Mono.just(data)`                                                              |
| Создать Mono             | Необходимо вычислить данные                                          | `Mono.fromSupplier(() -> getData())`<br/> `Mono.fromCallable(() -> getData())` |
| Создать Mono             | Необходимо получить данные из асинхронной completableFuture операции | `Mono.fromFuture(future)`                                                      |
| Создать Mono             | Выполнить какое-то действие после завершения асинхронного процесса   | `Mono.fromRunnable(runnable)`                                                  |
| Вернуть пустой результат | Ожидаем результат, но получаем ничего                                | `Mono.empty()`                                                                 |
| Вернуть моно             | Вернуть моно                                                         | `Mono.error()`<br/>`Mono.empty()`                                              |

## Flux examples

+ [Multiple subscribers (will work with Mono)](#multiple-subscribers-will-work-with-mono)
+ [Create Flux from Collection and array](#create-flux-from-collection-and-array)
+ [Create Flux from Stream](#create-flux-from-stream)
+ [From range](#from-range)
+ [Logging](#logging)
+ [Custom Subscribe](#custom-subscribe)
+ [List vs Flux](#list-vs-flux)
+ [Flux with Interval](#flux-with-interval)
+ [Convert mono to flux](#convert-mono-to-flux)
+ [Convert flux to mono](#convert-flux-to-mono)
+ [summary](#summary-1)

1. Create flux
```
public class Lecture01FluxIntro {
  public static void main(String[] args) {
    // can have more than 1 item
    Flux<Integer> flux = Flux.just(1, 2, 3,4);

    flux.subscribe(Util.onNext());
  }
}
```

2. Return empty value
```
public class Lecture01FluxIntro {
  public static void main(String[] args) {
    // empty flux
    Flux<Object> flux = Flux.empty();

    // complete will be return once
    flux.subscribe(
        Util.onNext(),
        Util.onError(),
        Util.onComplete()
    );
  }
}
```

### Multiple subscribers (will work with Mono)
```
public class Lecture02MultipleSubscribers {
  public static void main(String[] args) {

    Flux<Integer> integerFlux = Flux.just(1, 2, 3, 4);
    Flux<Integer> evenFlux = integerFlux.filter(i -> i % 2 == 0);

    integerFlux.subscribe( i -> System.out.println("Sub 1: " + i));
    evenFlux.subscribe( i -> System.out.println("Sub 2: " + i));
  }
}
```

### Create Flux from Collection and array
```
public class Lecture03FluxFromArrayOrList {
  public static void main(String[] args) {
    List<String> strings = Arrays.asList("a", "b", "c");

    // works like just
    // Use when data is exists
    Flux.fromIterable(strings)
        .subscribe(Util.onNext());

    Integer[] arr = {2,5,7,8};
    Flux.fromArray(arr)
        .subscribe(Util.onNext());
  }
}
```

### Create Flux from Stream
- Так как после выполнения терминальной операции стрим больше нельзя использовать, при повторной подписке возникнет ошибка
```
public class Lecture04FluxFromStream {
  public static void main(String[] args) {

    List<Integer> list = List.of(1, 2, 3, 4, 5);
    Stream<Integer> stream = list.stream();

    Flux<Integer> integerFlux = Flux.fromStream(stream);
    integerFlux.subscribe(
        Util.onNext(),
        Util.onError(),
        Util.onComplete()
    );

    // Will get error
    // Error: stream has already been operated upon or closed
    integerFlux.subscribe(
        Util.onNext(),
        Util.onError(),
        Util.onComplete()
    );
  }
}
```
- Для множественной подписки на Flux от стрима необходимо вернуть лист стримов
```
public class Lecture04FluxFromStream {
  public static void main(String[] args) {

    List<Integer> list = List.of(1, 2, 3, 4, 5);

    // In order to connect multiple subscribers we need to use Stream suppliers
    Flux<Integer> supplierStream = Flux.fromStream(() -> list.stream());

    supplierStream.subscribe(
        Util.onNext(),
        Util.onError(),
        Util.onComplete()
    );

    supplierStream.subscribe(
        Util.onNext(),
        Util.onError(),
        Util.onComplete()
    );
  }
}
```

### From range
```
public class Lecture05FluxRange {
  public static void main(String[] args) {
    // Will create from 1 2 3 4 5 6 7 8 9 10 elements
    Flux.range(1, 10)
        .subscribe(
            Util.onNext()
        );

    // 3 4 5 6 7 8 9 10 11 12
    Flux.range(3, 10)
        .subscribe(
            Util.onNext()
        );

    // will receive 10 names
    Flux.range(3, 10)
        .map(i -> Util.faker().name().fullName())
        .subscribe(
            Util.onNext()
        );
  }
}
```

### Logging
- Для логирования можно использовать `log` метод
```
public class Lecture05FluxRange {
  public static void main(String[] args) {
    Flux.range(3, 10)
        .log()
        .map(i -> Util.faker().name().fullName())
        .log()
        .subscribe(
            Util.onNext()
        );
  }
}
```

### Custom Subscribe
- Так как мы используем кастомную реализацию Subscriber весь процесс получения элементов тоже лежит на нас
```
public class Lecture06Subscription {
  public static void main(String[] args) {
    AtomicReference<Subscription> atomicReference = new AtomicReference<>();

    Flux.range(1, 20)
        .log()
        .subscribeWith(new Subscriber<Integer>() {
          @Override
          public void onSubscribe(Subscription subscription) {
            System.out.println("Received Sub: " + subscription);
            atomicReference.set(subscription);
          }

          @Override
          public void onNext(Integer integer) {
            System.out.println("onNext: " + integer);
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("onError: " + throwable.getMessage());
          }

          @Override
          public void onComplete() {
            System.out.println("onComplete");
          }
        });
  }
}
```
- Для того чтобы запустить процесс передачи данных неоюходимо вызвать метод `request`
* мы получим 3 элемента (1, 2, 3) и через 5 секунд еще 3 элемента (4, 5, 6)
```
Util.sleepSeconds(3);
atomicReference.get().request(3);
Util.sleepSeconds(5);
atomicReference.get().request(3);
Util.sleepSeconds(5);
```
- После чего можем отписаться
```
System.out.println("Going to cancel");
atomicReference.get().cancel();
Util.sleepSeconds(3);
```
- Полсе отписки мы снова можем попытаться получить оставшиеся 4 элемента, но так как мы уже отписались ничего не произойдет
```
atomicReference.get().request(4);
Util.sleepSeconds(3); 
```

### List vs Flux
- Задача:
  - Сгенерировать список с именами
  - Вывести все имена в консоль
  - Генерация каждого имени занимает минимум 1 секунду
  

**List**
```
public class NameGenerator {
  public static List<String> getNames (int count) {
    List<String> list = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      list.add(getName());
    }

    return list;
  }

  private static String getName () {
    Util.sleepSeconds(1);
    return Util.faker().name().fullName();
  }
}
```
- Ожидаем 5 секунд прежде чем вывести 5 имен
```
public class Lecture07FluxVsList {
  public static void main(String[] args) {
    List<String> names = NameGenerator.getNames(5);
    // wait 5 seconds
    System.out.println(names);
  }
}
```

**Flux**
```
public class NameGenerator {
  public static Flux<String> getNames (int count) {
    return Flux.range(0, count)
        .map(i -> getName());
  }

  private static String getName () {
    Util.sleepSeconds(1);
    return Util.faker().name().fullName();
  }
}
```
- Выводим имя как только оно будет доступно
```
public class Lecture07FluxVsList {
  public static void main(String[] args) {
    // when item is ready publisher will give it
    NameGenerator.getNames(5)
        .subscribe(
            Util.onNext()
        );
  }
}
```

### Flux with Interval
- Похоже на создание flux with range
- Как только элемент будет готов он будет передан в метод onNext
- Происходит в отдельном не блокирующем потоке
```
public class Lecture08FluxInterval {
  public static void main(String[] args) {
    // like range
    // will publish items periodically
    // in non-blocking async way
    Flux.interval(Duration.ofSeconds(1))
        .subscribe(Util.onNext());

    Util.sleepSeconds(5);

  }
}
```

### Convert mono to flux
```
public class Lecture09FluxFromMono {
  public static void main(String[] args) {
    Mono<String> mono = Mono.just("a");
    Flux<String> flux = Flux.from(mono);
    flux.subscribe(
        Util.onNext()
    );
  }

  private static void doSomething (Flux<String> flux) {

  }
}
```

### Convert flux to mono
- Возвращаем первый элемент из flux
```
public class Lecture10MonoFromFlux {
  public static void main(String[] args) {
    Flux.range(1, 10)
        .filter(i -> i > 3)
        .next() // 4
        .subscribe(
            Util.onNext()
        );
  }
}
```

### summary
| Type                     | Condition       | What to use                                                                                                  |
|--------------------------|-----------------|--------------------------------------------------------------------------------------------------------------|
| Создать Flux             | Данные уже есть | `Flux.just(...)`<br/>`Flux.fromIterable(iterable)`<br/>`Flux.fromArray(array)`<br/>`Flux.fromStream(stream)` |
| Создать Flux             | Range/Count     | `Flux.range(start, count)`                                                                                   |
| Создать Flux             | Периодически    | `Flux.interval(duration)`                                                                                    |
| Создать Flux             | Mono -> Flux    | `Flux.from(mono)`                                                                                            |


## Emitting

+ [Summary](#summary-2)

- С помощью метода `Flux.create` мы можем реализовать своего собственного Consumer для эмита данных
```
public class Lecture01FluxCreate {
  public static void main(String[] args) {
    // Полностью контролируем те данные, которые будут возвращаться
    // Контролируем процесс, когда завершить процесс или выкинуть ошибку
    Flux.create(fluxSink -> {
          fluxSink.next(1);
          fluxSink.next(2);
          fluxSink.complete();
      }).subscribe(Util.onNext());
  }
}
```

- Можно задать условия и определить до какого момента данные будут эмитится и когда процесс завершится
```
public class Lecture01FluxCreate {
  public static void main(String[] args) {
    Flux.create(fluxSink -> {
      String country;
      do {
        country = Util.faker().country().name();
        fluxSink.next(country);
      } while (!country.toLowerCase().equals("canada"));
      fluxSink.complete();

    }).subscribe(Util.onNext());
  }
}

```

- Consumer это интерфейс, который можно использовать при создании своей реализации
```
public class NameProducer implements Consumer<FluxSink<String>> {
  private FluxSink<String> fluxSink;

  @Override
  public void accept(FluxSink<String> stringFluxSink) {
    this.fluxSink = stringFluxSink;
  }


  public void produce () {
    String name = Util.faker().name().fullName();
    this.fluxSink.next(name);
  }
}
```
- Использование
  - Создаем инстанс класса, реализающего интерфейс Consumer
  - Создаем Flux
  - Подписываемся
  - При вызове метода `NameProducer.produce` будет вызываться метод `fluxSink.next()`
```
public class Lecture02FluxCreateRefactoring {

  public static void main(String[] args) {
    NameProducer nameProducer = new NameProducer();

    Flux.create(nameProducer)
        .subscribe(Util.subscriber());

    nameProducer.produce();
  }
}

```

### Summary
- Flux.create предоставляет только 1 инстанс Flux
- Позволяет задавать свою логику генерации значений, логику прекращений процесса и выброса ошибок
- Дает возможность проверять был ли завершен процесс

## Flux Take operator
- Создаем коллекцию из 10 чисел, которые должен получить подписчик
- Оператор take получает значения и передает их дальше до тех пор, пока счетчик не будет достигнут указанного значения
- Как только оператор take получает указанное значение + 1 он отписывается (вызывает cancel) и эмитит complete событие дальше, что приводит к завершению процесса
```
public class Lecture03FluxTake {
  public static void main(String[] args) {
    // map
    // filter
    Flux.range(1, 10)
        .log()
        .take(3)
        .log()
        .subscribe(Util.subscriber());
  }
}
```

## Stop emitting if process is cancelled
- Проблема
  - У нас есть логика, по которой процесс генерации значений может завершится
  - Пользователь хочет получить лишь первые несколько значений, ему не важно какие
  - После отписки, которую эмитит оператор take мы продолжаем эмитить значения
- Решение
  - Добавить проверки на `fluxSink.isCancelled`
```
public class Lecture04FluxCreateIssueFix {
  public static void main(String[] args) {
    Flux.create(fluxSink -> {
      String country;
      do {
        country = Util.faker().country().name();
        System.out.println("emitting : " + country);
        fluxSink.next(country);
      } while (!country.equalsIgnoreCase("canada") && !fluxSink.isCancelled());
      fluxSink.complete();

    })
        .take(3)
        .subscribe(Util.onNext());
  }
}
```

## Flux generate

+ [Flux Generate Flux](#flux-generate-flux)
+ [Flux push vs Flux create](#flux-push-vs-flux-create)
+ [Summary](#summary-3)

- Можно передавать только 1 значение
- Запускает бесконечный цикл
- Для каждой итерации будет передаваться отдельный инстанс `synchronousSink`
- При использовании оператора `take`, generate сам проверит, что процесс уже был завершен
- Процесс можно завершить вызвав метод `complete` или `error`
```
public class Lecture05FluxGenerate {
  public static void main(String[] args) {
    // Можно передавать только 1 значение
    // Запускает бесконечный цикл
    // Для каждой итерации будет передаваться отдельный инстанс synchronousSink
    Flux.generate(synchronousSink -> {
      System.out.println("emitting");
      synchronousSink.next(Util.faker().country().name());
      synchronousSink.complete();
    })
        .take(2)
        .subscribe(Util.subscriber());
  }
}

```

### Flux Generate Flux
- метод generate первым параметром может принимать метод, который будет возвращать state
- В таком случае во второй параметр будет ожидаться метод, первым атрибутом которого будет state, а вторым будет sink
```
public class Lecture07FluxGenerateCounter {
  public static void main(String[] args) {
    // canada
    // max = 10
    Flux.generate(
        () -> 1,
        (counter, sink) -> {
          String country = Util.faker().country().name();
          sink.next(country);

          if (country.equalsIgnoreCase("canada") || counter >= 10) {
            sink.complete();
          }
          return counter + 1;
        }
    )
        .subscribe(Util.subscriber());
  }
}
```

### Flux push vs Flux create
- `Flux.create` 
  - позволяет генерировать события асинхронно, используя поток события
  - Предоставляет больше гибкости в настройке потока
  - Для генерации события использует `Consumer<FluxSink<T>>`
- `Flux.push`
  - Ориентирован на динамическую генерацию события во время выполнения
  - Возможность генерации событий предоставляет отдельным методам push и complete
```
public class Lecture08FluxPush {
  public static void main(String[] args) {
    NameProducer nameProducer = new NameProducer();

    Flux.push(nameProducer)
        .subscribe(Util.subscriber());

    Runnable runnable = nameProducer::produce;

    for (int i = 0; i < 10; i++) {
      new Thread(runnable).start();
    }
  }
}
```

### Summary
| Create                                                                                                                          | Generate                                                                   |
|---------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------|
| Принимает `Consumer<FluxSink<T>>`                                                                                               | Принимает `Consumer<SynchronousSink<T>>`                                   |
| Consumer вызывается единожды                                                                                                    | Consumer вызывается множество раз                                          |
| Consumer может эмитить от 0 до N элементов                                                                                      | Consumer может эмитить только 1 элемент                                    |
| Publisher может узнать о дальнейших процесса. Поэтому необходимо определить логику дальнейшего стрима дополнительным параметром | Publisher создает элементы в зависимости от потребности дальнейшего стрима |
| Thread-safe                                                                                                                     | NA                                                                         |
| `fluxSink.requestedFromDownstream()`<br/>`fluxSink.isCancelled()`                                                               |                                                                            |


## Operators

+ [Handle](#handle)
+ [Do-events and lifecycle](#do-events-and-lifecycle)
+ [Limit Rate](#limit-rate)
+ [Delay](#delay)
+ [OnError](#onerror)
+ [Timeout](#timeout)
+ [Default if empty](#default-if-empty)
+ [Switch if empty](#switch-if-empty)
+ [Transform](#transform)
+ [SwitchOnFirst](#switchonfirst)
+ [FlatMap](#flatmap)
+ [ConcatMap](#concatmap)

### Handle
- Работает как смесь методов `filter и map`
```
public class Lecture01Handle {
  public static void main(String[] args) {
    // handle = filter +map
    Flux.range(1, 20)
        .handle(((integer, synchronousSink) -> {
          if (integer == 7) {
            synchronousSink.complete();
          }

          if (integer % 2 == 0) {
            synchronousSink.next(integer); // filter
          } else {
            synchronousSink.next(integer + "a"); // map
          }
        }))
        .subscribe(Util.subscriber());
  }
}
```

### Do-events and lifecycle
- Success way
  - все события исполняются снизу вверх, начиная от subscribe и далше вверх
  - doFirst - выполняются сверху вниз (subscriber -> publisher)
  - doOnSubscribe - Выполняются сверху вниз (от publisher -> subscriber)
  - doOnRequest - subscriber запрашивает данные (subscriber -> publisher)
  - Запускается внутренняя часть fluxSink
  - doOnNext
  - doOnComplete - При завершении работы
  - doOnTerminate
  - выполняется код, который реализован в подписчике в методе onComplete
  - doFinally
  - Выполняется код после fluxSink.complete();
```
public class Lecture03DoCallbacks {
  public static void main(String[] args) {
    Flux.create(fluxSink -> {
      System.out.println("inside create");
      for (int i = 0; i < 5; i++) {
        fluxSink.next(i);
      }

      fluxSink.complete();
      System.out.println("--completed");
    })
        .doFirst(() -> System.out.println("doFirst 1")) // order 1
        .doOnSubscribe(s -> System.out.println("doOnSubscribe 1: " + s)) // order 2
        .doOnComplete(() -> System.out.println("doOnComplete")) // order 5
        .doOnNext(o -> System.out.println("doOnNext: " + o)) // order 4
        .doOnRequest(l -> System.out.println("doOnRequest: " + l)) // order 3
        .doOnError(err -> System.out.println("doOnError: " + err.getMessage()))
        .doOnTerminate(() -> System.out.println("doOnTerminate")) // order 6
        .doOnCancel(() -> System.out.println("doOnCancel"))
        .doFinally(signal -> System.out.println("doFinally: " + signal)) // order 7
        .doOnDiscard(Object.class, o -> System.out.println("doOnDiscard: " + o))
        .subscribe(Util.subscriber());
  }
}
```

- Error way
  - Error way
  - все события исполняются снизу вверх, начиная от subscribe и далше вверх
  - doFirst - выполняются сверху вниз (subscriber -> publisher)
  - doOnSubscribe - Выполняются сверху вниз (от publisher -> subscriber)
  - doOnRequest - subscriber запрашивает данные (subscriber -> publisher)
  - Запускается внутренняя часть fluxSink
  - doOnNext
  - doOnError - При получении ошибки
  - doOnTerminate
  - выполняется код, который реализован в подписчике в методе onError
  - doFinally
  - Выполняется код после fluxSink.error();
```
public class Lecture03DoCallbacks {
  public static void main(String[] args) {
    Flux.create(fluxSink -> {
      System.out.println("inside create");
      for (int i = 0; i < 5; i++) {
        fluxSink.next(i);
      }

      fluxSink.error(new RuntimeException("oops"));
      System.out.println("--completed");
    })
        .doFirst(() -> System.out.println("doFirst 1")) // order 1
        .doOnSubscribe(s -> System.out.println("doOnSubscribe 1: " + s)) // order 2
        .doOnComplete(() -> System.out.println("doOnComplete")) // order 5
        .doOnNext(o -> System.out.println("doOnNext: " + o)) // order 4
        .doOnRequest(l -> System.out.println("doOnRequest: " + l)) // order 3
        .doOnError(err -> System.out.println("doOnError: " + err.getMessage()))
        .doOnTerminate(() -> System.out.println("doOnTerminate")) // order 6
        .doOnCancel(() -> System.out.println("doOnCancel"))
        .doFinally(signal -> System.out.println("doFinally: " + signal)) // order 7
        .doOnDiscard(Object.class, o -> System.out.println("doOnDiscard: " + o))
        .subscribe(Util.subscriber());
  }
}
```
- Cancel way
  - Cancel way
  - все события исполняются снизу вверх, начиная от subscribe и далше вверх
  - doFirst - выполняются сверху вниз (subscriber -> publisher)
  - doOnSubscribe - Выполняются сверху вниз (от publisher -> subscriber)
  - doOnRequest - subscriber запрашивает данные (subscriber -> publisher)
  - Запускается внутренняя часть fluxSink
  - doOnNext
  - doOnCancel - Когда желаемое количество элементов получено
  - doFinally
  - выполняется код, который реализован в подписчике в методе onComplete
  - doOnDiscard - выполняется для всех элементов, который не были получены (если нет обработки isCancelled)
  - Выполняется код после fluxSink.complete();
```
public class Lecture03DoCallbacks {
  public static void main(String[] args) {
    Flux.create(fluxSink -> {
      System.out.println("inside create");
      for (int i = 0; i < 5; i++) {
        fluxSink.next(i);
      }

      fluxSink.complete();
      System.out.println("--completed");
    })
        .doFirst(() -> System.out.println("doFirst 1")) // order 1
        .doOnSubscribe(s -> System.out.println("doOnSubscribe 1: " + s)) // order 2
        .doOnComplete(() -> System.out.println("doOnComplete")) // order 5
        .doOnNext(o -> System.out.println("doOnNext: " + o)) // order 4
        .doOnRequest(l -> System.out.println("doOnRequest: " + l)) // order 3
        .doOnError(err -> System.out.println("doOnError: " + err.getMessage()))
        .doOnTerminate(() -> System.out.println("doOnTerminate")) // order 6
        .doOnCancel(() -> System.out.println("doOnCancel"))
        .doFinally(signal -> System.out.println("doFinally: " + signal)) // order 7
        .doOnDiscard(Object.class, o -> System.out.println("doOnDiscard: " + o))
        .take(2)
        .subscribe(Util.subscriber());
  }
}
```

* Если необходимо что-то сделать по окончанию всего процессе, то необходимо поместить `doFinally` прямо перед `.subscribe(...)`

### Limit Rate
- В случае когда мы хотим получать данные постепенно, догружать данные по мере получения
- limitRate начально загружает указанное количество элементов
- Как только 75% элементов получено, будет догружено еще 100
- Вторым параметром можно указать процент от 0-99, по достижению которого будет загружаться следующая пачка элементов
```
public class Lecture04LimitRate {
  public static void main(String[] args) {
    Flux.range(1, 1000)
        .log()
        .limitRate(100)
        .subscribe(Util.subscriber());
    // 100
    // 175
  }
}
```

### Delay
- Отдает элементы с определенной задержкой
- Изначально загружает 1
```
public class Lecture05Delay {
  public static void main(String[] args) {

    Flux.range(1, 100) // request 1
        .log()
        .delayElements(Duration.ofSeconds(1))
        .subscribe(Util.subscriber());

    Util.sleepSeconds(50);
  }
}
```

### OnError
- В случае если происходит ошибка в Provider мы можем обработать ошибку разными способами

1. Вернуть другое значени (программа завершится)
```
public class Lecture06OnError {
  public static void main(String[] args) {
    Flux.range(1, 10)
        .log()
        .map(i -> 10 / (5 - i))
        .onErrorReturn(-1)
        .subscribe(Util.subscriber());
  }
}
```
2. Выполнить какое-то действие (программа завершится)
```
public class Lecture06OnError {
  public static void main(String[] args) {
    Flux.range(1, 10)
        .log()
        .map(i -> 10 / (5 - i))
        .onErrorResume(e -> fallback())
        .subscribe(Util.subscriber());
  }

  private static Mono<Integer> fallback () {
    return Mono.fromSupplier(() -> Util.faker().random().nextInt(100, 200));
  }
}
```
3. Выполнить какое-то действие и продолжить выполнение
```
public class Lecture06OnError {
  public static void main(String[] args) {
    Flux.range(1, 10)
        .log()
        .map(i -> 10 / (5 - i))
        .onErrorContinue((err, obj) -> {
          
        })
        .subscribe(Util.subscriber());
  }
}
```

### Timeout
1. Используется для ограничения времени ожидания
2. Если время ожидание прошло и ответ не был получен будет выброшена ошибка
3. Если время ожидания прошло и вторым параметром была передана callback функция, то будет выполнена она
```
public class Lecture07Timeout {
  public static void main(String[] args) {
    getOrderNumbers()
        .timeout(Duration.ofSeconds(2), fallback())
        .subscribe(Util.subscriber());

    Util.sleepSeconds(60);
  }

  private static Flux<Integer> getOrderNumbers () {
    return Flux.range(1, 10)
        .delayElements(Duration.ofSeconds(1));
  }

  private static Flux<Integer> fallback () {
    return Flux.range(100, 10)
        .delayElements(Duration.ofMillis(200));
  }
}
```

### Default if empty
- Если Producer возвращает 0 результатов можно вернуть что-то дефолтное
```
public class Lecture08DefaultIfEmpty {
  public static void main(String[] args) {
    getOrderNumbers()
        .filter(i -> i > 10)
        .defaultIfEmpty(-100)
        .subscribe(Util.subscriber());
  }

  private static Flux<Integer> getOrderNumbers () {
    return Flux.range(1, 10);
  }
}
```

### Switch if empty
- Если Producer возвращает 0 результатов, то можно переключиться на другого Producer
```
public class Lecture09SwitchIfEmpty {
  public static void main(String[] args) {
    getOrderNumbers()
        .filter(i -> i > 10)
        .switchIfEmpty(fallback())
        .subscribe(Util.subscriber());
  }

  private static Flux<Integer> getOrderNumbers () {
    return Flux.range(1, 10);
  }

  private static Flux<Integer> fallback () {
    return Flux.range(20, 5);
  }
}
```

### Transform
- Если необходимо объеденить несколько операций над данными
- Позволяет передавать "свои" фильтры
```
public class Lecture10Transform {
  public static void main(String[] args) {
    getPerson()
        .transform(applyFilterMap())
        .subscribe(Util.subscriber());
  }

  public static Flux<Person> getPerson () {
    return Flux.range(1, 10)
        .map(i -> new Person());
  }

  public static Function<Flux<Person>, Flux<Person>> applyFilterMap () {
    return flux -> flux
        .filter(p -> p.getAge() > 10)
        .doOnNext(p -> p.setName(p.getName().toUpperCase()))
        .doOnDiscard(Person.class, p -> System.out.println("Not allowing: " + p));
  }
}
```

### SwitchOnFirst
- Если необходимо поменять Provider в случае не подходящих данных
- signal хранит в себе данные текущего действия, а так же текущего элемента например в случае действия onNext
- Вторым аргументом получает Flux
```
public class Lecture11SwitchOnFirst {
  public static void main(String[] args) {
    getPerson()
        .switchOnFirst(((signal, personFlux) -> {
          if (signal.isOnNext() && signal.get().getAge() > 10) {
            return personFlux;
          }

          return applyFilterMap().apply(personFlux);
        }))
        .subscribe(Util.subscriber());
  }

  public static Flux<Person> getPerson () {
    return Flux.range(1, 10)
        .map(i -> new Person());
  }

  public static Function<Flux<Person>, Flux<Person>> applyFilterMap () {
    return flux -> flux
        .filter(p -> p.getAge() > 10)
        .doOnNext(p -> p.setName(p.getName().toUpperCase()))
        .doOnDiscard(Person.class, p -> System.out.println("Not allowing: " + p));
  }
}
```

### Flatmap
- Используется для преобразования элементов одного потока в другой
- Пример
  - Есть пользователь
  - Есть покупки, которые привязаны к пользователю
  - По userId мы хотим получить список всех покупок
  - UserService - возвращает Flux<User>
  - OrderService - возвращает Flux<PurchaseOrder>
```
public class Lecture12FlatMap {
  public static void main(String[] args) {
    UserService.getUsers()
        .flatMap(user -> OrderService.getOrders(user.getUserId())) // Flux
        .subscribe(Util.subscriber());
  }
}
```

### ConcatMap
- Работает точно так же как FlatMap только переключение между Publisher происходит только после вызова метода onComplete


## Cold and Hot Publishers

+ [Cold publisher](#cold-publisher)
+ [Hot publisher](#hot-publisher)


### Cold publisher
- Холодная подписка означает, что Provider будет начинать свою работу только в момент подписки на него
```
public class Lecture01ColdPublisher {
  public static void main(String[] args) {
    Flux<String> movieStream = Flux.fromStream(() -> getMovie())
        .delayElements(Duration.ofSeconds(2));

    movieStream
        .subscribe(Util.subscriber("sam"));

    Util.sleepSeconds(5);

    movieStream
        .subscribe(Util.subscriber("mike"));

    Util.sleepSeconds(60);
  }

  // netflix
  private static Stream<String> getMovie () {
    System.out.println("got the movie streaming req");
    return Stream.of(
        "Scene 1",
        "Scene 2",
        "Scene 3",
        "Scene 4",
        "Scene 5",
        "Scene 6",
        "Scene 7"
    );
  }
}
```

### Hot publisher

+ [Share](#share)
+ [RefCount](#refcount)
+ [AutoConnect](#autoconnect)

- Горячая подписка - Publisher начинает раздавать данные вне зависимости от того есть у него подписчики или нет
- Если в процессе появится еще один подписчик он получит данные не с начала, а с того момента когда он присоеденился
```
public class Lecture02HotPublisher {
  public static void main(String[] args) {
    Flux<String> movieStream = Flux.fromStream(() -> getMovie())
        .delayElements(Duration.ofSeconds(2))
        // Активирует горячую подписку
        .share();

    movieStream
        .subscribe(Util.subscriber("sam"));

    Util.sleepSeconds(5);

    movieStream
        .subscribe(Util.subscriber("mike"));

    Util.sleepSeconds(60);
  }

  // netflix
  private static Stream<String> getMovie () {
    System.out.println("got the movie streaming req");
    return Stream.of(
        "Scene 1",
        "Scene 2",
        "Scene 3",
        "Scene 4",
        "Scene 5",
        "Scene 6",
        "Scene 7"
    );
  }
}
```

#### Share
- Метод Share объединяет 2 оператора 
  - `.publish()`
  - `.refCount(1)` - минимальное колчество подписчиков для начала работы

#### RefCount
- Если подписчик подписывается после того как для первого подписчика Flux отдал все данные и вызвал метод onComplete </br>
  данные будут отдаваться с самого начала (начнется новая подписка)

#### AutoConnect
- Позволяет определить минимально количество подписчиков
- Если подписчик подписывается после того как onComplete был вызван он не получает данных
- Если указать 0, то данные начнут раздаваться сразу, вне зависимости от того, есть подписчики или нет
```
public class Lecture04HotPublishAutoConnect {
  public static void main(String[] args) {
    // share = publish().refCount(1)
    Flux<String> movieStream = Flux.fromStream(() -> getMovie())
        .delayElements(Duration.ofSeconds(1))
        // Активирует горячую подписку
        .publish()
        .autoConnect(0);

    Util.sleepSeconds(3);

    movieStream
        .subscribe(Util.subscriber("sam"));

    Util.sleepSeconds(10);

    System.out.println("Mike if about to join");

    movieStream
        .subscribe(Util.subscriber("mike"));

    Util.sleepSeconds(60);
  }

  // movie theatre
  private static Stream<String> getMovie () {
    System.out.println("got the movie streaming req");
    return Stream.of(
        "Scene 1",
        "Scene 2",
        "Scene 3",
        "Scene 4",
        "Scene 5",
        "Scene 6",
        "Scene 7"
    );
  }
}
```

#### Cache
- Обединяет 2 метода
  - `publish()`
  - `replay()` - можно указать максимальное количество элементов, которые будут сохранены (по дефолту int.max)
```
public class Lecture05HotPublishCache {
  public static void main(String[] args) {
    // share = publish().refCount(1)
    Flux<String> movieStream = Flux.fromStream(() -> getMovie())
        .delayElements(Duration.ofSeconds(1))
        .cache(2);

    Util.sleepSeconds(2);

    movieStream
        .subscribe(Util.subscriber("sam"));

    Util.sleepSeconds(10);

    System.out.println("Mike if about to join");

    movieStream
        .subscribe(Util.subscriber("mike"));

    Util.sleepSeconds(60);
  }

  // movie theatre
  private static Stream<String> getMovie () {
    System.out.println("got the movie streaming req");
    return Stream.of(
        "Scene 1",
        "Scene 2",
        "Scene 3",
        "Scene 4",
        "Scene 5",
        "Scene 6",
        "Scene 7"
    );
  }
}
```

## Threads

+[Schedulers != Parallel-execution](#schedulers--parallel-execution)
+[PublishOn](#publishOn)
+[PublishOn vs SubscribeOn](#publishon-vs-subscribeon)
+[Parallel-execution](#parallel-execution)
+[Sequential](#sequential)
+[Summary](#summary-3)

- Можно запускать Flux в отдельных потоках самостоятельно, но лучше использовать те методы, которые нам предоставляет WebFlux
- Для выполнения flux  разных потоках необходимо использовать метод `subscribeOn`
  - parallel - создает по 1 потоку на 1 CPU
  - boundedElastic - создает по 10 потоков на 1 CPU (лучше использовать)
```
public class Lecture02SubscribeOnDemo {
  public static void main(String[] args) {
    Flux<Object> flux = Flux.create(fluxSink -> {
          printThreadName("create");
          fluxSink.next(1);
        })
        .doOnNext(i -> printThreadName("next " + i));

    flux
        .doFirst(() -> printThreadName("first2"))
        .subscribeOn(Schedulers.boundedElastic())
        .doFirst(() -> printThreadName("first1"))
        .subscribe(v -> printThreadName("sub "+ v));

    Util.sleepSeconds(5);

  }

  private static void printThreadName (String msg) {
    System.out.println(msg + "\t\t: Thread:" + Thread.currentThread().getName());
  }
}
```

- Если мы указываем несколько методов subscribeOn, то выполняться будет в том, который ближе к Provider
```
public class Lecture02SubscribeOnDemo {
  public static void main(String[] args) {
    Flux<Object> flux = Flux.create(fluxSink -> {
          printThreadName("create");
          fluxSink.next(1);
        })
        // Выполняться все будет в этом потоке
        .subscribeOn(Schedulers.newParallel("vins"))
        .doOnNext(i -> printThreadName("next " + i));

    Runnable runnable = () -> flux
        .doFirst(() -> printThreadName("first2"))
        .subscribeOn(Schedulers.boundedElastic())
        .doFirst(() -> printThreadName("first1"))
        .subscribe(v -> printThreadName("sub "+ v));

    for (int i = 0; i < 2; i++) {
      new Thread(runnable).start();
    }

    Util.sleepSeconds(5);

  }

  private static void printThreadName (String msg) {
    System.out.println(msg + "\t\t: Thread:" + Thread.currentThread().getName());
  }
}
```

### Schedulers != Parallel-execution
* Все операции всегда выполняются последовательно
* Данные передается 1 за другим в ThreadPool
* `Schedulers.parallel()` - означает создаение потоков для CPU. Не ведет к параллельному выполнению

### PublishOn
- В теории данную настройку должен устанавливать тот, кто разрбатывает Provider
- Определяет, что раздача данных будет происходить параллельно
- Можно менять Scheduler в процессе
- Влияет только на downstream операции, то есть только те, что идут после вызова метода `publishOn`
```
public class Lecture04PublishOn {
  public static void main(String[] args) {
    Flux<Object> flux = Flux.create(fluxSink -> {
          printThreadName("create");
          for (int i = 0; i < 4; i++) {
            fluxSink.next(i);
          }

          fluxSink.complete();
        })
        .doOnNext(i -> printThreadName("next " + i));

    flux
        .publishOn(Schedulers.boundedElastic())
        .doOnNext(i -> printThreadName("next " + i))
        .publishOn(Schedulers.parallel())
        .subscribe(v -> printThreadName("sub "+ v));

    Util.sleepSeconds(5);

  }

  private static void printThreadName (String msg) {
    System.out.println(msg + "\t\t: Thread:" + Thread.currentThread().getName());
  }
}
```

### PublishOn vs SubscribeOn
| PublishOn                                                        | SubscribeOn                                                |
|------------------------------------------------------------------|------------------------------------------------------------|
| Вляет на операции, которые выполняются после вызова downstream   | Влияют на все операции, который написаны выше upstream     |

### Parallel-execution
- Используется чтобы Publisher раздавал данные в нескольких потоках
- Необходимо использовать Thread-save структуры, для предотвращения ошибок
- Можно указать максимальное количество потоков, в рамках которых будет выполнятся раздава данных
```
public class Lecture06Parallel {
  public static void main(String[] args) {
    List<Integer> list = new ArrayList<>();

    Flux.range(1, 999)
        .parallel()
        .runOn(Schedulers.parallel())
        .subscribe(v -> list.add(v));

    Util.sleepSeconds(5);
    System.out.println(list.size());

  }

  private static void printThreadName (String msg) {
    System.out.println(msg + "\t\t: Thread:" + Thread.currentThread().getName());
  }
}
```

* Flux.interval в своей реализации уже использует parallel

### Sequential
- Превращает параллельный Flux в не паралльный
```
public class Lecture07Sequential {
  public static void main(String[] args) {
    Flux.range(1, 10)
        .parallel(2)
        .runOn(Schedulers.boundedElastic())
        .doOnNext(i -> printThreadName("next " + i))
        .sequential()
        .subscribe(v -> printThreadName("sub " + v));

    Util.sleepSeconds(5);

  }

  private static void printThreadName (String msg) {
    System.out.println(msg + "\t\t: Thread:" + Thread.currentThread().getName());
  }
}
```

### Summary
| Schedulers методы | Использование                                    |
|-------------------|--------------------------------------------------|
| boundedElastic    | Сетевый операции и операции затратные по времени |
| parallel          | CPU задачи                                       |
| single            | Одиночные задачи для одного потока               |
| immediate         | В текущем потоке                                 |
