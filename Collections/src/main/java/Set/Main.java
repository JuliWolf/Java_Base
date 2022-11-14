package Set;

import java.util.*;

public class Main {
  public static void main(String[] args) {
    Set<String> hashSet = new HashSet<>();
    Set<String> linkedHashSet = new LinkedHashSet<>();
    Set<String> treeSet = new TreeSet<>();

//    System.out.println("");
//    testSet(hashSet);
//    System.out.println("");
//    testSet(linkedHashSet);
//    System.out.println("");
//    testSet(treeSet);

//    System.out.println(hashSet.contains("Tom"));
//    System.out.println(hashSet.contains("Tim"));
//    System.out.println(hashSet.isEmpty());
    

    Set<Integer> intHashSet1 = new HashSet<>();
    intHashSet1.add(0);
    intHashSet1.add(1);
    intHashSet1.add(2);
    intHashSet1.add(3);
    intHashSet1.add(4);
    intHashSet1.add(5);

    Set<Integer> intHashSet2 = new HashSet<>();
    intHashSet2.add(2);
    intHashSet2.add(3);
    intHashSet2.add(4);
    intHashSet2.add(5);
    intHashSet2.add(6);
    intHashSet2.add(7);

    // union - объединение множеств
    Set<Integer> unionSet = new HashSet<>(intHashSet1);
    unionSet.addAll(intHashSet2);
    System.out.println(unionSet);

    // intersection - пересечение множеств
    Set<Integer> intersectionSet = new HashSet<>(intHashSet1);
    intersectionSet.retainAll(intHashSet2);
    System.out.println(intersectionSet);

    // difference - разность множеств
    Set<Integer> differenceSet = new HashSet<>(intHashSet1);
    differenceSet.removeAll(intHashSet2);
    System.out.println(differenceSet);
  }

  public static void testSet (Set<String> set) {
    set.add("Mike");
    set.add("Katy");
    set.add("Tom");
    set.add("George");
    set.add("Donald");
    set.add("Tom");
    set.add("Tom");

    for (String name: set) {
      System.out.println(name);
    }
  }
}
