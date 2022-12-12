package Arrays;

import java.util.Arrays;
import java.util.stream.IntStream;

public class Stream {
  public static void main(String[] args) {
    String[] arr = {"One", "Two", "Three", "Four", "Five"};

    // Create stream
    java.util.stream.Stream<String> stream = Arrays.stream(arr);
    // Выбрать элементы в диапазоне
    java.util.stream.Stream<String> stream12 = Arrays.stream(arr, 0, 2);

    /* asList */
    java.util.stream.Stream<String> streamList = Arrays.asList(arr).stream();
    // Выбрать элементы в диапазоне
    java.util.stream.Stream<String> streamSubList = Arrays.asList(arr).subList(0, 2).stream();


    /* of and toArray */
    java.util.stream.Stream<String> streamOf = java.util.stream.Stream.of(arr);
    java.util.stream.Stream<String> streamOfStrings = java.util.stream.Stream.of("One", "Two", "Three");

    String[] array = streamOfStrings.toArray(String[]::new);


    // Int
    int[] integers = {2, 3, 4, 1};
    IntStream intStream = Arrays.stream(integers);

    /* range rangeClosed */

    // range - (int inclusive, int exclusive)
    IntStream intStreamRange = IntStream.range(0 , integers.length);
    // rangeClosed - (int startInclusive, int endExclusive)
    IntStream intStreamRangeClosed = IntStream.rangeClosed(0 , integers.length);

    int[] intArray = intStream.toArray();
  }
}
