package Types;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

interface Executable {
//  int execute();
  int execute(int x, int y);
}

class Runner {
  public void run(Executable e) {
    int a = e.execute(10, 15);
    System.out.println(a);
  }
}

public class Main {
//  public static void main(String[] args) {
//    Runner runner = new Runner();
//    runner.run(new Executable() {
//      @Override
//      public int execute(int x, int y) {
//        System.out.println("Hello");
//        System.out.println("Goodbye");
//
//        return x + y;
//      }
//    });
//    runner.run((int x, int y) -> {
//      System.out.println("Hello");
//      System.out.println("Goodbye");
//
//
//      return x + y;
//    });
//
//    int a = 1;
//    a = 2; // error
//    runner.run((x, y) -> x + y + a);
//  }

//  public static void main(String[] args) {
//    Runner runner = new Runner();
//
//    int a = 1;
//    runner.run((int x, int y) -> {
//      System.out.println("Hello");
//      System.out.println("Goodbye");
//
////      int a = 2; // Variable 'a' is already defined in the scope
//      return x + y + a;
//    });
//
//    runner.run((x, y) -> {
////      int a = 5; // Variable 'a' is already defined in the scope
//      int b = 1;
//      return x + y;
//    });
//  }

  public static void main(String[] args) {
    List<String> list = new ArrayList<>();

    list.add("Hello");
    list.add("Goodbye");
    list.add("a");
    list.add("ab");

//    list.sort(new Comparator<String>() {
//      @Override
//      public int compare(String o1, String o2) {
//        if (o1.length() > o2.length()) {
//          return 1;
//        } else if (o1.length() < o2.length()) {
//          return -1;
//        } else {
//          return 0;
//        }
//      }
//    });

//    list.sort((s1, s2) -> {
//      if (s1.length() > s2.length()) return 1;
//      else if (s1.length() < s2.length()) return -1;
//      else return 0;
//    });

    Comparator<String> comparator = (s1, s2) -> {
      if (s1.length() > s2.length()) return 1;
      else if (s1.length() < s2.length()) return -1;
      else return 0;
    };

    list.sort(comparator);

    System.out.println(list);
  }
}
