package LinkedList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    List<Integer> linkedList = new LinkedList<>();
    List<Integer> arrayList = new ArrayList<>();
//    linkedList.add(1);
//    linkedList.get(0);
//    linkedList.size();
//    linkedList.remove(0);

//    // Method add to end
//    measureAddToEndTime(linkedList); // ~102
//    measureAddToEndTime(arrayList); // ~78

//    // Method add to start
//    measureAddToStartTime(linkedList); // ~26
//    measureAddToStartTime(arrayList); // ~549

//    // Method get
//    measureShowTime(linkedList); // ~4836
//    measureShowTime(arrayList); // ~2

    MyLinkedList myLinkedList = new MyLinkedList();
    myLinkedList.add(1);
    myLinkedList.add(2);
    myLinkedList.add(10);

    System.out.println(myLinkedList);

    System.out.println(myLinkedList.get(2));
    System.out.println(myLinkedList.get(1));

    myLinkedList.remove(1);
    System.out.println(myLinkedList);
    myLinkedList.remove(1);
    System.out.println(myLinkedList);
  }

  private static void measureAddToEndTime(List<Integer> list) {
    long start = System.currentTimeMillis();

    for (int i = 0; i < 1000000; i++) {
      list.add(i);
    }

    long end = System.currentTimeMillis();

    System.out.println(end - start);
  }

  private static void measureShowTime(List<Integer> list) {
    for (int i = 0; i < 100000; i++) {
      list.add(i);
    }

    long start = System.currentTimeMillis();

    for (int i = 0; i < 100000; i++) {
      list.get(i);
    }

    long end = System.currentTimeMillis();

    System.out.println(end - start);
  }

  private static void measureAddToStartTime(List<Integer> list) {
    long start = System.currentTimeMillis();

    for (int i = 0; i < 100000; i++) {
      list.add(0, i);
    }

    long end = System.currentTimeMillis();

    System.out.println(end - start);
  }
}
