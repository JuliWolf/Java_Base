package Iterable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    List<Integer> list = new ArrayList<>();

    list.add(1);
    list.add(2);
    list.add(3);

    int idx = 0;
    // Before Java 5
    Iterator<Integer> iterator = list.iterator();
    while (iterator.hasNext()) {
      System.out.println(iterator.next());

      if (idx == 1) {
        iterator.remove();
      }

      idx++;
    }

    System.out.println(list);

    // Java 5
    for (int x : list) {
      System.out.println(x);
    }
  }
}
