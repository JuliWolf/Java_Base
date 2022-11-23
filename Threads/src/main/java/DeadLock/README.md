# DeadLock

DeadLock - состояние программы, когда 2 или более потоков не могут выполнить свою задачу из-за того что каждый блок занимает важный для дальнейшей работы другого потока монитор
DeadLock случается, когда несколько потоков последовательно занимают несколько мониторов, например
1. поток забирает сначала первый монитор, а потом второй монитор
2. поток забирает сначала второй монитор, а потом первый монитор

Такое блокирование мониторов приведет к взаимной блокировке потоков. 

## Пример с synchronized

```
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
  public static void main(String[] args) throws InterruptedException {
    Runner runner = new Runner();

    Thread thread1 = new Thread(new Runnable() {
      @Override
      public void run() {
        runner.firstThread();
      }
    });

    Thread thread2 = new Thread(new Runnable() {
      @Override
      public void run() {
        runner.secondThread();
      }
    });

    thread1.start();
    thread2.start();

    thread1.join();
    thread2.join();

    runner.finished();
  }
}

class Runner {
  private Account account1 = new Account();
  private Account account2 = new Account();

  public void firstThread () {
    Random random = new Random();

    for (int i = 0; i < 10000; i++) {
      synchronized (account1) {
        synchronized (account2) {
          Account.transfer(account1, account2, random.nextInt(100));
        }
      }
    }
  }

  public void secondThread () {
    Random random = new Random();

    for (int i = 0; i < 10000; i++) {
      synchronized (account2) {
        synchronized (account1) {
          Account.transfer(account2, account1, random.nextInt(100));
        }
      }
    }
  }

  public void finished () {
    System.out.println(account1.getBalance());
    System.out.println(account2.getBalance());
    System.out.println("Total balance " + (account1.getBalance() + account2.getBalance()));
  }
}

class Account {
  private int balance = 10000;

  public void deposit (int amount) {
    balance += amount;
  }

  public void withdraw (int amount) {
    balance -= amount;
  }

  public int getBalance () {
    return this.balance;
  }

  public static void transfer (Account acc1, Account acc2, int amount) {
    acc1.withdraw(amount);
    acc2.deposit(amount);
  }
}
```

## Пример с ReentrantLock

```
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
  public static void main(String[] args) throws InterruptedException {
    Runner runner = new Runner();

    Thread thread1 = new Thread(new Runnable() {
      @Override
      public void run() {
        runner.firstThread();
      }
    });

    Thread thread2 = new Thread(new Runnable() {
      @Override
      public void run() {
        runner.secondThread();
      }
    });

    thread1.start();
    thread2.start();

    thread1.join();
    thread2.join();

    runner.finished();
  }
}

class Runner {
  private Account account1 = new Account();
  private Account account2 = new Account();

  private Lock lock1 = new ReentrantLock();
  private Lock lock2 = new ReentrantLock();

  public void firstThread () {
    Random random = new Random();

    for (int i = 0; i < 10000; i++) {
      try {
        lock1.lock();
        // первый поток тут
        lock2.lock();

        Account.transfer(account1, account2, random.nextInt(100));
      } finally {
        lock1.unlock();
        lock2.unlock();
      }
    }
  }

  public void secondThread () {
    Random random = new Random();

    for (int i = 0; i < 10000; i++) {
      try {
        lock2.lock();
        // второй поток тут
        lock1.lock();

        Account.transfer(account2, account1, random.nextInt(100));
      } finally {
        lock1.unlock();
        lock2.unlock();
      }
    }
  }

  public void finished () {
    System.out.println(account1.getBalance());
    System.out.println(account2.getBalance());
    System.out.println("Total balance " + (account1.getBalance() + account2.getBalance()));
  }
}

class Account {
  private int balance = 10000;

  public void deposit (int amount) {
    balance += amount;
  }

  public void withdraw (int amount) {
    balance -= amount;
  }

  public int getBalance () {
    return this.balance;
  }

  public static void transfer (Account acc1, Account acc2, int amount) {
    acc1.withdraw(amount);
    acc2.deposit(amount);
  }
}
```

## Решение

1. Создать метод `takeLocks` для обработки локов
2. Внутри метода `takeLocks` с помощью метода класса ReentrantLock `tryLock` проверяем занят ли монитор или нет
3. Если оба монитора не заняты, то выходим из метода
4. Если один из монитор занят, то освобождаем тот монитор, который был монитор методом `tryLock`

```
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
  public static void main(String[] args) throws InterruptedException {
    Runner runner = new Runner();

    Thread thread1 = new Thread(new Runnable() {
      @Override
      public void run() {
        runner.firstThread();
      }
    });

    Thread thread2 = new Thread(new Runnable() {
      @Override
      public void run() {
        runner.secondThread();
      }
    });

    thread1.start();
    thread2.start();

    thread1.join();
    thread2.join();

    runner.finished();
  }
}

class Runner {
  private Account account1 = new Account();
  private Account account2 = new Account();

  private Lock lock1 = new ReentrantLock();
  private Lock lock2 = new ReentrantLock();

  private void takeLocks (Lock lock1, Lock lock2) {
    boolean firstLockTaken = false;
    boolean secondLockTaken = false;

    while (true) {
      try {
        firstLockTaken = lock1.tryLock();
        secondLockTaken = lock2.tryLock();
      } finally {
        if (firstLockTaken && secondLockTaken) {
          return;
        }

        if (firstLockTaken) {
          lock1.unlock();
        }

        if (secondLockTaken) {
          lock2.unlock();
        }
      }

      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void firstThread () {
    Random random = new Random();

    for (int i = 0; i < 10000; i++) {
      try {
        takeLocks(lock1, lock2);

        Account.transfer(account1, account2, random.nextInt(100));
      } finally {
        lock1.unlock();
        lock2.unlock();
      }
    }
  }

  public void secondThread () {
    Random random = new Random();

    for (int i = 0; i < 10000; i++) {
      try {
        takeLocks(lock2, lock1);

        Account.transfer(account2, account1, random.nextInt(100));
      } finally {
        lock1.unlock();
        lock2.unlock();
      }
    }
  }

  public void finished () {
    System.out.println(account1.getBalance());
    System.out.println(account2.getBalance());
    System.out.println("Total balance " + (account1.getBalance() + account2.getBalance()));
  }
}

class Account {
  private int balance = 10000;

  public void deposit (int amount) {
    balance += amount;
  }

  public void withdraw (int amount) {
    balance -= amount;
  }

  public int getBalance () {
    return this.balance;
  }

  public static void transfer (Account acc1, Account acc2, int amount) {
    acc1.withdraw(amount);
    acc2.deposit(amount);
  }
}
```