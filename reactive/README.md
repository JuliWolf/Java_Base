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
+ [Overflow Strategy](#overflow-strategy)
+ [Combining publishers](#combining-publishers)
+ [Batching](#batching)
+ [Repeat & Retry](#repeat--retry)
+ [Sinks](#sinks)

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
- Вторым параметром может принимать OverflowStrategy для обработки данных, которые будут выкинуты в процессе при переполнении очереди
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

+ [Schedulers != Parallel-execution](#schedulers--parallel-execution)
+ [PublishOn](#publishOn)
+ [PublishOn vs SubscribeOn](#publishon-vs-subscribeon)
+ [Parallel-execution](#parallel-execution)
+ [Sequential](#sequential)
+ [Summary](#summary-4)

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


## Overflow Strategy

+ [Overflow Strategy table](#overflow-strategy-table)
+ [Drop](#drop)
+ [Latest](#latest)
+ [Error](#error)
+ [Buffer](#buffer)

- Проблема:
  - Provider быстро создает и отдает данные
  - Долгий процесс обработки данных прежде чем отдать их Consumer
- Итого:
  - Данные, которые были созданы Provider сохраняются в памяти до тех пор, пока не будут обработы и получены Consumer

### Overflow Strategy table
| Strategy | Behavior                                                    | Method                 |
|----------|-------------------------------------------------------------|------------------------|
| buffer   | Сохраняет в памяти                                          | `onBackpressureBuffer` |
| drop     | Когда очередь наполняется, все новые данные будут пропущены | `onBackpressureDrop`   |
| latest   | Когда очередь наполняется, самые первые начинают удаляться  | `onBackpressureLatest` |
| error    | Возвращает ошибку при переполеннии                          | `onBackpressureError`  |

* Изменить максимальное количество элементов для Queue
```
System.setProperty("reactor.bufferSize.small", "16");
```

### Drop
```
public class Lecture02Drop {
  public static void main(String[] args) {
    System.setProperty("reactor.bufferSize.small", "16");

    Flux.create(fluxSink -> {
          for (int i = 1; i < 501; i++) {
            fluxSink.next(i);
            System.out.println("Pushed: " + i);
            Util.sleepMillis(1);
          }

          fluxSink.complete();
        })
        .onBackpressureDrop()
        .publishOn(Schedulers.boundedElastic())
        .doOnNext(i -> {
          Util.sleepMillis(10);
        })
        .subscribe(Util.subscriber());

    Util.sleepSeconds(60);
  }
}
```

* Метод onBackpressureDrop может принимать callback метод для обработки значений, которые были скинуты
```
public class Lecture02Drop {
  public static void main(String[] args) {
    System.setProperty("reactor.bufferSize.small", "16");

    List<Object> list = new ArrayList<>();

    Flux.create(fluxSink -> {
          for (int i = 1; i < 201; i++) {
            fluxSink.next(i);
            System.out.println("Pushed: " + i);
            Util.sleepMillis(1);
          }

          fluxSink.complete();
        })
        .onBackpressureDrop(list::add)
        .publishOn(Schedulers.boundedElastic())
        .doOnNext(i -> {
          Util.sleepMillis(10);
        })
        .subscribe(Util.subscriber());

    Util.sleepSeconds(10);

    System.out.println(list);
  }
}
```

### Latest
```
public class Lecture03Latest {
  public static void main(String[] args) {
    System.setProperty("reactor.bufferSize.small", "16");

    Flux.create(fluxSink -> {
          for (int i = 1; i < 201; i++) {
            fluxSink.next(i);
            System.out.println("Pushed: " + i);
            Util.sleepMillis(1);
          }

          fluxSink.complete();
        })
        .onBackpressureLatest()
        .publishOn(Schedulers.boundedElastic())
        .doOnNext(i -> {
          Util.sleepMillis(10);
        })
        .subscribe(Util.subscriber());

    Util.sleepSeconds(10);
  }
}
```

### Error
```
public class Lecture04Error {
  public static void main(String[] args) {
    System.setProperty("reactor.bufferSize.small", "16");

    Flux.create(fluxSink -> {
          for (int i = 1; i < 201 && !fluxSink.isCancelled(); i++) {
            fluxSink.next(i);
            System.out.println("Pushed: " + i);
            Util.sleepMillis(1);
          }

          fluxSink.complete();
        })
        .onBackpressureError()
        .publishOn(Schedulers.boundedElastic())
        .doOnNext(i -> {
          Util.sleepMillis(10);
        })
        .subscribe(Util.subscriber());

    Util.sleepSeconds(10);
  }
}
```

### Buffer
- Можно ограничить количество сохраненных элементов в памяти передав первым параметром максимальное количество
- Кроме того можно передать коллбек метод для обработки значений, которые были выкинуты
```
public class Lecture05BufferWithSize {
  public static void main(String[] args) {

    Flux.create(fluxSink -> {
          for (int i = 1; i < 201 && !fluxSink.isCancelled(); i++) {
            fluxSink.next(i);
            System.out.println("Pushed: " + i);
            Util.sleepMillis(1);
          }

          fluxSink.complete();
        })
        .onBackpressureBuffer(20, o -> System.out.println("Dropped: " + o))
        .publishOn(Schedulers.boundedElastic())
        .doOnNext(i -> {
          Util.sleepMillis(10);
        })
        .subscribe(Util.subscriber());

    Util.sleepSeconds(10);
  }
}
```

## Combining publishers

+ [StartWith](#startwith)
+ [Concat / ConcatWith](#concat--concatwith)
+ [Merge](#merge)
+ [ZIP](#zip)
+ [CombineLatest](#combinelatest)

### StartWith

- Сначала забирает все данные из 1го Publisher
- Потом начинает забирать данные из 2го Publisher

* Данные будут сначала создаваться и кешироваться
* Далее сначала вернутся данные из кеша и только потом снова будут генерироваться
```
public class NameGenerator {
  private List<String> list = new ArrayList<>();

  public Flux<String> generateNames () {
    return Flux.generate(stringSynchronousSink -> {
      System.out.println("generator fresh");
      Util.sleepSeconds(1);
      String name = Util.faker().name().firstName();
      list.add(name);
      stringSynchronousSink.next(name);
    })
        .cast(String.class)
        .startWith(getFromCache());
  }

  private Flux<String> getFromCache () {
    return Flux.fromIterable(list);
  }
}
```

```
public class Lecture01StartWith {
  public static void main(String[] args) {
    NameGenerator generator = new NameGenerator();
    generator.generateNames()
        .take(2)
        .subscribe(Util.subscriber("sam"));

    generator.generateNames()
        .take(2)
        .subscribe(Util.subscriber("mike"));

    generator.generateNames()
        .take(3)
        .subscribe(Util.subscriber("Jake"));

    generator.generateNames()
        .filter(n -> n.startsWith("A"))
        .take(1)
        .subscribe(Util.subscriber("Jake"));
  }
}
```

### Concat / ConcatWith

- Процесс начинается с основного стрима
- Когда первый стрим закончил работу, данные начнут забираться из следующего стрима
```
public class Lecture02Concat {
  public static void main(String[] args) {
    Flux<String> flux1 = Flux.just("a", "b");
    Flux<String> flux2 = Flux.just("c", "d", "e");

    Flux<String> flux = flux1.concatWith(flux2);
    flux.subscribe(Util.subscriber());
  }
}
```
* Можно объеденить с несколькими стримами
```
public class Lecture02Concat {
  public static void main(String[] args) {
    Flux<String> flux1 = Flux.just("a", "b");
    Flux<String> flux2 = Flux.just("c", "d", "e");
    Flux<String> flux3 = Flux.just("f", "g", "h");

    Flux<String> fluxConcat = Flux.concat(flux1, flux2, flux3);
    fluxConcat.subscribe(Util.subscriber());
  }
}
```
* Если один из стримов возвращает ошибку, мы можем отложить ошибку до того момента, когда остальные стримы завершат свое выполнение
```
public class Lecture02Concat {
  public static void main(String[] args) {
    Flux<String> flux1 = Flux.just("a", "b");
    Flux<String> flux2 = Flux.just("c", "d", "e");
    Flux<String> flux3 = Flux.just("f", "g", "h");
    Flux<String> fluxError = Flux.error(new RuntimeException("oops"));

    Flux<String> fluxConcatWithError = Flux.concatDelayError(flux2, flux3, fluxError);
    fluxConcatWithError.subscribe(Util.subscriber());
  }
}
```

### Merge

- Обединяет все стримы и возвращает один
- Данные отдаются по мере готовности их в каждом отдельном стриме
- Процесс заканчивается тогда, когда все Provider заканчивают свою работу
```
public class EmirateFlights {
  public static Flux<String> getFlights () {
    return Flux.range(1, Util.faker().random().nextInt(1, 10))
        .delayElements(Duration.ofSeconds(1))
        .map(i -> "Emirates " + Util.faker().random().nextInt(100, 999))
        .filter(i -> Util.faker().random().nextBoolean());
  }
}
```
```
public class Lecture03Merge {
  public static void main(String[] args) {
    Flux<String> mergeFlux = Flux.merge(
        QatarFlights.getFlights(),
        EmirateFlights.getFlights(),
        AmericanAirlines.getFlights()
    );

    mergeFlux.subscribe(Util.subscriber());

    Util.sleepSeconds(10);

  }
}
```

### ZIP

- Забирает по одному элементу из каждого стрима для создания одного элемента для подписчика
- Если в одном из стримов нет элементов, то процесс завершается
- Возвращает тип `Tuple`
- Если передать больше 8 стримов, то нужно будет передать свою `ByFunction` для объединения данных
```
public class Lecture04ZIP {
  public static void main(String[] args) {
    // return Tuple
    // Если больше 8, тьо нужно будет передать свою ByFunction для объединения данных
    Flux.zip(
        getBody(),
        getEngine(),
        getTires()
    )
      .subscribe(Util.subscriber());
  }

  private static Flux<String> getBody () {
    return Flux.range(1, 5)
        .map(i -> "body");
  }

  private static Flux<String> getEngine () {
    return Flux.range(1, 2)
        .map(i -> "engine");
  }

  private static Flux<String> getTires () {
    return Flux.range(1, 8)
        .map(i -> "tires");
  }
}
```


### CombineLatest

- Объединяет последние элементы стримов
- Если стрим А последним элементом отдал 1 и стрим B последним элементом отдал 2, то результатом будет `12`
- После чего стрим А отдал элемент 4, результатом будет `42`
```
public class Lecture05CombineLast {
  public static void main(String[] args) {
    Flux.combineLatest(
        getString(),
        getNumber(),
        (stringStream, numberStream) -> stringStream + numberStream
    ).subscribe(Util.subscriber());

    Util.sleepSeconds(10);
  }

  private static Flux<String> getString () {
    return Flux.just("A", "B", "C", "D")
        .delayElements(Duration.ofSeconds(1));
  }

  private static Flux<String> getNumber () {
    return Flux.just("1", "2", "3")
        .delayElements(Duration.ofSeconds(3));
  }
}
```

## Batching

Позволяет объединять несколько элементов в пачки

+ [Buffer](#buffer-1)
+ [Window](#window)
+ [GroupBy](#groupby)

### Buffer
- Собирает указанное количество элементов в лист
- Отдает список когда он будет иметь указанное количество элементов
```
public class Lecture01Buffer {
  public static void main(String[] args) {
    eventStream()
        .buffer(5)
        .subscribe(Util.subscriber());

    Util.sleepSeconds(60);
  }

  private static Flux<String> eventStream () {
    return Flux.interval(Duration.ofMillis(300))
        .map(i -> "event" + i);
  }
}
```
* Если `complete` событие было вызвано, то процесс передачи завершится тем количеством элементов, которые уже были добавлены в список
* Если `complete` события не было вызвано, то buffer будет ожидать
```
public class Lecture01Buffer {
  public static void main(String[] args) {
    eventStream()
        .buffer(5)
        .subscribe(Util.subscriber());

    Util.sleepSeconds(60);
  }

  private static Flux<String> eventStream () {
    return Flux.interval(Duration.ofMillis(300))
        .take(3)
        .map(i -> "event" + i);
  }
}
```

- Можно указать период, по истечению которого все собранные данные будут отданы подписчику
```
public class Lecture01Buffer {
  public static void main(String[] args) {
    eventStream()
        .buffer(Duration.ofSeconds(2))
        .subscribe(Util.subscriber());

    Util.sleepSeconds(60);
  }

  private static Flux<String> eventStream () {
    return Flux.interval(Duration.ofMillis(300))
        .map(i -> "event" + i);
  }
}
```
- Так как в указанный период может быть передано очень много элементов, можно ограничить количество элементов для листа
- Если же необходимое количество за указанный промежуток времени не было получено, то вернется столько элементов, сколько есть в листе
```
public class Lecture01Buffer {
  public static void main(String[] args) {
    eventStream()
        .bufferTimeout(5, Duration.ofSeconds(2))
        .subscribe(Util.subscriber());

    Util.sleepSeconds(60);
  }

  private static Flux<String> eventStream () {
    return Flux.interval(Duration.ofMillis(10))
        .map(i -> "event" + i);
  }
}

```
- Можно всегда получать {n} последних элементов
  - Ограничиваем буффер 3мя элементами
  - Добавляем в буффер 1, 2, 3
  - Буффер возвращает подписчику [1,2,3]
  - Добавляем в буффер 4
  - При этом из листа убирается 1 
  - Подписчик получит -> [2,3,4]
```
public class Lecture02OverlapAndDrop {
  public static void main(String[] args) {
    eventStream()
        .buffer(3, 1)
        .subscribe(Util.subscriber());

    Util.sleepSeconds(60);
  }

  private static Flux<String> eventStream () {
    return Flux.interval(Duration.ofMillis(300))
        .map(i -> "event" + i);
  }
}
```

### Window

- Работает так же как buffer, но возвращает Flux

```
public class Lecture03Window {
  private static AtomicInteger atomicInteger = new AtomicInteger(1);

  public static void main(String[] args) {
    eventStream()
        .window(5)
        .flatMap(flux -> saveEvents(flux))
        .subscribe(Util.subscriber());

    Util.sleepSeconds(60);
  }

  private static Flux<String> eventStream () {
    return Flux.interval(Duration.ofMillis(500))
        .map(i -> "event" + i);
  }

  private static Mono<Integer> saveEvents (Flux<String> flux) {
    return flux
        .doOnNext(e -> System.out.println("saving " + e))
        .doOnComplete(() -> {
          System.out.println("saved this batch");
          System.out.println("----------------");
        })
        .then(Mono.just(atomicInteger.getAndIncrement()));
  }
}
```
* Метод then выполняется после того как выполнится все, что выше него

- Можно задать ограничение по времени
```
public class Lecture03Window {
  private static AtomicInteger atomicInteger = new AtomicInteger(1);

  public static void main(String[] args) {
    eventStream()
        .window(Duration.ofSeconds(2))
        .flatMap(flux -> saveEvents(flux))
        .subscribe(Util.subscriber());

    Util.sleepSeconds(60);
  }
}
```

### GroupBy

- Позволяет объеденить элементы по какому-то принципу (свойству)
- Возвращает `GroupedFlux`
- Метод subscribe вызывается по 1му разу для каждой группы
```
public class Lecture04GroupBy {
  public static void main(String[] args) {
    Flux.range(1, 30)
        .delayElements(Duration.ofSeconds(1))
        .groupBy(i -> i % 2) // key 0, 1
        .subscribe(groupedFlux -> process(groupedFlux, groupedFlux.key()));

    Util.sleepSeconds(60);
  }

  private static void process (Flux<Integer> flux, int key) {
    flux.subscribe( i -> System.out.println("Key: " + key + ", Item: " + i));
  }
}
```

## Repeat & Retry

+ [Repeat](#repeat)
+ [Repeat with condition](#repeat-with-condition)
+ [Retry](#retry)
+ [Retry with delay](#retry-with-delay)
+ [RetryWhen](#retrywhen)
+ [Summary](#summary-5)


### Repeat
- Позволяет переподписаться еще раз после получения Complete сигнала
```
public class Lecture01Repeat {
  public static void main(String[] args) {
    getIntegers()
        .repeat(2)
        .subscribe(Util.subscriber());
  }

  private static Flux<Integer> getIntegers () {
    return Flux.range(1, 3)
        .doOnSubscribe(s -> System.out.println("Subscribed"))
        .doOnComplete(() -> System.out.println("---Completed"));
  }
}
```

### Repeat with condition
- Можно задать условие, которое будет возвращать true/false
- Если условие возвращает true, то происходит переподписка
- Если условие возвращает false, то переподписки не будет
```
public class Lecture02RepeatWithCondition {
  public static void main(String[] args) {
    getIntegers()
        .repeat(() -> atomicInteger.get() < 14)
        .subscribe(Util.subscriber());
  }

  private static Flux<Integer> getIntegers () {
    return Flux.range(1, 3)
        .doOnSubscribe(s -> System.out.println("Subscribed"))
        .doOnComplete(() -> System.out.println("---Completed"))
        .map(i -> atomicInteger.getAndIncrement());
  }
}
```

### Retry
- Позволяет повторить подписку подписки при получении ошибки
```
public class Lecture03Retry {
  private static AtomicInteger atomicInteger = new AtomicInteger(1);

  public static void main(String[] args) {
    getIntegers()
        .retry(2)
        .subscribe(Util.subscriber());
  }

  private static Flux<Integer> getIntegers () {
    return Flux.range(1, 3)
        .doOnSubscribe(s -> System.out.println("Subscribed"))
        .doOnComplete(() -> System.out.println("---Completed"))
        .map(i -> i / (Util.faker().random().nextInt(1, 5) > 3 ? 0 : 1))
        .doOnError(err -> System.out.println("---error"));
  }
}
```

### Retry with delay
- Позволяет повторить попытку определенное количество раз, но через определенный промежуток времени
```
public class Lecture04RetryWithDelay {
  public static void main(String[] args) {
    getIntegers()
        .retryWhen(Retry.fixedDelay(2, Duration.ofSeconds(3)))
        .subscribe(Util.subscriber());

    Util.sleepSeconds(60);
  }

  private static Flux<Integer> getIntegers () {
    return Flux.range(1, 3)
        .doOnSubscribe(s -> System.out.println("Subscribed"))
        .doOnComplete(() -> System.out.println("---Completed"))
        .map(i -> i / (Util.faker().random().nextInt(1, 5) > 3 ? 0 : 1))
        .doOnError(err -> System.out.println("---error"));
  }
}
```

### RetryWhen
- Используется когда нужно по разному обрабатывать ошибки
- Например, при ошибке 500 мы пытаемся снова получить информацию
- При ошибке 404 процесс завершаем, так как это ошибка запроса
```
public class Lecture05RetryWhenAdvanced {
  public static void main(String[] args) {
    orderService(Util.faker().business().creditCardNumber())
        .doOnError(err -> System.out.println(err.getMessage()))
        .retryWhen(Retry.from(
            flux -> flux
              .doOnNext(retrySignal -> {
                System.out.println(retrySignal.totalRetries());
                System.out.println(retrySignal.failure());
              })
              .handle((retrySignal, synchronousSink) -> {
                if (retrySignal.failure().getMessage().equals("500")) {
                  synchronousSink.next(1);
                } else {
                  synchronousSink.error(retrySignal.failure());
                }
              })
              .delayElements(Duration.ofSeconds(1))
        ))
        .subscribe(Util.subscriber());

    Util.sleepSeconds(60);
  }

  private static Mono<String> orderService (String ccNumber) {
    return Mono.fromSupplier(() -> {
      processPayment(ccNumber);
      return Util.faker().idNumber().valid();
    });
  }

  // payment service
  private static void processPayment (String ccNumber) {
    int random = Util.faker().random().nextInt(1, 10);
    if (random < 8) {
      throw new RuntimeException("500");
    } else if (random < 10) {
      throw new RuntimeException("404");
    }
  }
}
```

### Summary
| Тип    | Поведение                                        |
|--------|--------------------------------------------------|
| repeat | Переподписаться после получения complete сигнала |
| retry  | Переподписаться после получения error сигнала    |


## Sinks

+ [Sinks types](#sinks-types)
+ [One](#one)
+ [Many-unicast](#many-unicast)

**Может использоваться для** 
- Передачи данных между различными компонентами
- Создания отдельного потока обработкаи данных
- Возможность асинхронного управления потоками данных
- Создания новых реактивных компонентов

**Предоставляет несколько типов конечных токек**
- UnicastProcessor
- MulticastProcessor
- DirectProcessor
- ReplayProcessor
- EmitterProcessor

### Sinks types

| Тип            | Поведение | Pub:Sub                                                                               |
|----------------|-----------|---------------------------------------------------------------------------------------|
| one            | Mono      | 1:N                                                                                   |
| many-unicast   | Flux      | 1:1                                                                                   |
| many-multicast | Flux      | 1:N                                                                                   |
| many-reply     | Flux      | 1:N(с возможностью повтора всех значений для подписчиков, которые подключились позже) |


### One
- Возвращает один элемент как Mono 
- Возвращает значение переданное в `tryEmitValue`
```
public class Lecture01SinkOne {
  public static void main(String[] args) {
    // Mono 1 value / empty / error
    Sinks.One<Object> sink = Sinks.one();

    Mono<Object> mono = sink.asMono();

    mono.subscribe(Util.subscriber("sam"));

    sink.tryEmitValue("hi");
  }
}
```
- Возврат ошибки `tryEmitError`
```
public class Lecture01SinkOne {
  public static void main(String[] args) {
    // Mono 1 value / empty / error
    Sinks.One<Object> sink = Sinks.one();

    Mono<Object> mono = sink.asMono();

    mono.subscribe(Util.subscriber("sam"));

    sink.tryEmitError(new RuntimeException("sam"));
  }
}
```

- Возврат пустого значения `tryEmitEmpty`
```
public class Lecture01SinkOne {
  public static void main(String[] args) {
    // Mono 1 value / empty / error
    Sinks.One<Object> sink = Sinks.one();

    Mono<Object> mono = sink.asMono();

    mono.subscribe(Util.subscriber("sam"));

    sink.tryEmitEmpty();
  }
}
```

- Попробовать передать значение и обработать ошибку если она произойдет `emitValue`
```
public class Lecture01SinkOne {
  public static void main(String[] args) {
    // Mono 1 value / empty / error
    Sinks.One<Object> sink = Sinks.one();

    Mono<Object> mono = sink.asMono();

    mono.subscribe(Util.subscriber("sam"));

    sink.emitValue("hi", (signalType, emitResult) -> {
      System.out.println(signalType.name());
      System.out.println(emitResult.name());
      // Вернуть true если нужно повторить попытку
      return false;
    });

    sink.emitValue("hello", (signalType, emitResult) -> {
      System.out.println(signalType.name());
      System.out.println(emitResult.name());
      // Вернуть true если нужно повторить попытку
      return false;
    });
  }
}
```

### Many-unicast
- Позволяет отдавать много элементов 1му подписчику
```
public class Lecture02SinkUnicast {
  public static void main(String[] args) {
    // Метод для отправки данных
    Sinks.Many<Object> sink = Sinks.many()
        .unicast()
        .onBackpressureBuffer();

    // Метод для получения данных подписчиками
    Flux<Object> flux = sink.asFlux();

    flux.subscribe(Util.subscriber("Sam"));


    sink.tryEmitNext("hi");
    sink.tryEmitNext("how are you");
    sink.tryEmitNext("?");
  }
}
```