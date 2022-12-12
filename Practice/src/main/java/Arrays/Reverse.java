package Arrays;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.IntStream;

public class Reverse {
  public static void main(String[] args) {
    int[] int1 = {1,2,3,4,5};

    reverse(int1);
    System.out.println(Arrays.toString(int1)); // [5, 4, 3, 2, 1]
    System.out.println(Arrays.toString(reverse2(int1))); // [1, 2, 3, 4, 5]

    Melon[] melons = {
        new Melon("Gremshaw", 2000),
        new Melon("Gac", 1200),
        new Melon("Britter", 2200)
    };

    reverse3(melons);
    Collections.reverse(Arrays.asList(melons));

    // Без мутации основного массива
    Object[] reversed = IntStream.rangeClosed(1, melons.length)
        .mapToObj(i -> melons[melons.length - i])
        .toArray();
    System.out.println(Arrays.toString(reversed));
  }

  public static void reverse (int[] arr) {
    for (int leftHead = 0, rightHead = arr.length - 1; leftHead < rightHead; leftHead++, rightHead--) {
      int elem = arr[leftHead];
      arr[leftHead] = arr[rightHead];
      arr[rightHead] = elem;
    }
  }

  public static int[] reverse2 (int[] arr) {
    return IntStream.rangeClosed(1, arr.length)
        .map(i -> arr[arr.length - i]).toArray();
  }

  public static <T> void reverse3 (T[] arr) {
    for (int leftHead = 0, rightHead = arr.length - 1; leftHead < rightHead; leftHead++, rightHead--) {
      T elem = arr[leftHead];
      arr[leftHead] = arr[rightHead];
      arr[rightHead] = elem;
    }
  }
}
