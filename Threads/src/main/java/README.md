# Thread

Thread - поток, дают возможность выполнять код параллельно.
Потоки выполняются не синхронно. 

Изначально вся программа выполняется в потоке `main`. Все потоки так же вызываются из потока `main`.
Если код в `main` заканчивается, но остались еще работащие потоки, то приложение будет работать до тех пор пока все потоки не завершат свою работу.

## Создание и запуск

1. Для создания потока необходимо унаследовать класс `Thread` и реализовать метод run
2. Все что должен выполнять поток должно находиться в методе `run` этот метод будет автоматически вызван java когда поток запустится
3. Вызвать метод `start` для запуска потока
```
public class Main {
  public static void main(String[] args) throws InterruptedException {
    MyThread myThread = new MyThread();

    myThread.start();

  }
}

class MyThread extends Thread {
  public void run () {
    for(int i = 0; i < 1000; i++) {
      System.out.println("Hello from MyThread " + i);
    }
  }
}
```

1. Имлементация интерфейса `Runnable`
2. Все что должен выполнять поток должно находиться в методе `run` этот метод будет автоматически вызван java когда поток запустится
3. Передать класс, который имплементит интерфейс `Runnable` при создании нового потока
4. Вызвать метод `start` для запуска потока
```
public class Main {
  public static void main(String[] args) {
    Thread thread = new Thread(new Runner());
    thread.start();
  }
}

class Runner implements Runnable {
  @Override
  public void run() {
    for(int i = 0; i < 1000; i++) {
      System.out.println("Hello from MyThread " + i);
    }
  }
}
```

## Метод sleep

1. Метод `sleep` заставляет поток заснуть (переводит в режим ожидаения), то есть поток перестает выполнять дальшейший код на время переданное в метод
2. При вызове метода `sleep` поток не особождается.
3. Время передается в милисекундах

```
public class Main {
  public static void main(String[] args) throws InterruptedException {
    System.out.println("I am going to sleep");
    Thread.sleep(3000);
    System.out.println("I amd awake!");
  }
}
```