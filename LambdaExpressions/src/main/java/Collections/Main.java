package Collections;

import java.util.*;
import java.util.stream.Collectors;

public class Main {
  public static void main(String[] args) {
    int[] arr = new int[10];
    List<Integer> list = new ArrayList<>();

    fillArr(arr);
    fillList(list);

    System.out.println(list);
    System.out.println(Arrays.toString(arr));

//    updateArr(arr);
//    updateList(list);
//
//    System.out.println(list);
//    System.out.println(Arrays.toString(arr));

    // map
    arr = mapArrWithLambda(arr);
    list = mapListWithLambda(list);

    System.out.println(list);
    System.out.println(Arrays.toString(arr));

    // filter
    int[] arr2 = new int[10];
    List<Integer> list2 = new ArrayList<>();

    fillArr(arr2);
    fillList(list2);

    arr2 = filterArr(arr2);
    list2 = filterList(list2);

    System.out.println(list2);
    System.out.println(Arrays.toString(arr2));

    // foreach
    foreachArr(arr2);
    foreachList(list2);

    // reduce
    int[] arr3 = new int[10];
    List<Integer> list3 = new ArrayList<>();

    fillArr(arr3);
    fillList(list3);

    int sumArr = reduceArr(arr3);
    int sumList = reduceList(list3);

    System.out.println(sumArr);
    System.out.println(sumList);

    // connect methods
    int[] arr4 = new int[10];
    fillArr(arr4);

    int[] newArr = connectStreamArr(arr4);
    System.out.println(Arrays.toString(newArr));

    // another collections
    Set<Integer> set = new HashSet<>();
    fillSet(set);

    System.out.println(set);
    set = mapSet(set);

    System.out.println(set);
  }

  private static Set<Integer> mapSet(Set<Integer> set) {
    return set.stream().map(a -> a * 3).collect(Collectors.toSet());
  }

  private static int[] connectStreamArr(int[] arr) {
    return Arrays.stream(arr)
        .filter(a -> a % 2 != 0)
        .map(a -> a * 2).toArray();
  }

  private static int reduceList(List<Integer> list) {
//    return list.stream().reduce((acc, item) -> acc + item).get();
//    return list.stream().reduce(Integer::sum).get();
    // начальное значение reduce
    return list.stream().reduce(0, (acc, item) -> acc + item).intValue();
  }

  private static int reduceArr(int[] arr) {
//    return Arrays.stream(arr).reduce((acc, item) -> acc + item).getAsInt();
    return Arrays.stream(arr).reduce(Integer::sum).getAsInt();
  }

  private static void foreachList(List<Integer> list) {
//    list.stream().forEach(a -> System.out.println(a));
    list.stream().forEach(System.out::println);
  }

  private static void foreachArr(int[] arr) {
//    Arrays.stream(arr).forEach(a -> System.out.println(a));
    Arrays.stream(arr).forEach(System.out::println);
  }

  private static List<Integer> filterList(List<Integer> list) {
    return list.stream().filter(a -> a % 2 == 0).collect(Collectors.toList());
  }

  private static int[] filterArr(int[] arr) {
    return Arrays.stream(arr).filter(a -> a % 2 == 0).toArray();
  }

  private static List<Integer> mapListWithLambda(List<Integer> list) {
      return list.stream().map(a -> a * 2).collect(Collectors.toList());
  }

  private static int[] mapArrWithLambda(int[] arr) {
    return Arrays.stream(arr).map(a -> a * 2).toArray();
  }

  private static void updateList(List<Integer> list) {
    for (int i = 0; i < 10; i++) {
      list.set(i, list.get(i) * 2);
    }
  }

  private static void updateArr(int[] arr) {
    for (int i = 0; i < 10; i++) {
      arr[i] = arr[i] * 2;
    }
  }

  private static void fillSet(Set<Integer> set) {
    set.add(1);
    set.add(2);
    set.add(5);
  }

  private static void fillList(List<Integer> list) {
    for (int i = 0; i < 10; i++) {
      list.add(i + 1);
    }
  }

  private static void fillArr(int[] arr) {
    for (int i = 0; i < 10; i++) {
      arr[i] = i + 1;
    }
  }
}
