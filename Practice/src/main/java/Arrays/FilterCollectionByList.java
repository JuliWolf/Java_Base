package Arrays;

import java.util.*;
import java.util.stream.Collectors;

public class FilterCollectionByList {
  public static void main(String[] args) {
    List<Melon> melons = generateList();

    List<String> melonsByType = Arrays.asList("Apollo", "Gac", "Crenshaw", "Hami");

    /* List.contains() */
    List<Melon> results = melons.stream()
        .filter(t -> melonsByType.contains(t.getType()))
        .collect(Collectors.toList());
    System.out.println(results);

    /* Конвертировать List в HashSet множество для оптимизации */
    Set<String> melonsSetByType = new HashSet<>(melonsByType);

    List<Melon> results2 = melons.stream()
        .filter(t -> melonsSetByType.contains(t.getType())).toList();
  }

  public static List<Melon> generateList () {
    List<Melon> melons = new ArrayList<>();
    melons.add(new Melon("Apollo", 3000));
    melons.add(new Melon("Jade Dew", 3500));
    melons.add(new Melon("Cantaloupe", 1500));
    melons.add(new Melon("Gac", 1600));
    melons.add(new Melon("Hami", 1400));

    return melons;
  }
}
