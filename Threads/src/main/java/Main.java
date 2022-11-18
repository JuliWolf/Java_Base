public class Main {
  public static void main(String[] args) throws InterruptedException {
//    MyThread myThread = new MyThread();
////    myThread.run();
//    myThread.start();
//
//    MyThread myThread2 = new MyThread();
//    myThread2.start();
//
//    System.out.println("I am going to sleep");
//    Thread.sleep(3000);
//    System.out.println("I amd awake!");

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

class MyThread extends Thread {
  public void run () {
    for(int i = 0; i < 1000; i++) {
      System.out.println("Hello from MyThread " + i);
    }
  }
}
