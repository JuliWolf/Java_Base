package ArrayList;

import java.util.ArrayList;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    int[] arr = new int[3];

//    for (int i = 0; i < 4; i++) {
//      arr[i] = 1; // Error out of bound exception
//    }

    createArrayList();
  }

  public static void createArrayList () {
    List<Integer> arrayList = new ArrayList<Integer>();
    arrayList.add(1);

    for (int i = 0; i < 10; i++) {
      arrayList.add(i);
    }

    System.out.println(arrayList);

    System.out.println(arrayList.get(0));
//    System.out.println(arrayList.get(99));

    System.out.println(arrayList.size());

    for (int i = 0; i < arrayList.size(); i++) {
      System.out.println(arrayList.get(i));
    }
//
    for (Integer x: arrayList) {
      System.out.println(x);
    }

    arrayList.remove(5);
    System.out.println(arrayList);
  }
}
