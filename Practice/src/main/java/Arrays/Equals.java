package Arrays;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

public class Equals {
  public static void main(String[] args) {
    Map<Integer, Melon> melonsMap1 = new HashMap<>();
    Map<Integer, Melon> melonsMap2 = new HashMap<>();

    melonsMap1.put(1, new Melon("Apollo", 3000));
    melonsMap1.put(2, new Melon("Jade Dew", 3500));
    melonsMap1.put(3, new Melon("Cantaloupe", 1500));

    melonsMap2.put(1, new Melon("Apollo", 3000));
    melonsMap2.put(2, new Melon("Jade Dew", 3500));
    melonsMap2.put(3, new Melon("Cantaloupe", 1500));
  }

  public static <A, B> boolean equalsWithArrays (Map<A, B[]> first, Map<A, B[]> second) {
    if (first.size() != second.size()) {
      return false;
    }

    return first.entrySet().stream()
        .allMatch(e -> Arrays.equals(e.getValue(), second.get(e.getKey())));
  }
}
