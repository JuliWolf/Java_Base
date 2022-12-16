package Arrays;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Partitioning {
  public static void main(String[] args) {
    List<Melon> melons = generateList();

    List<Melon> weightLessThan3000 = separateMelons(melons).get(false);
    System.out.println(weightLessThan3000);
    List<Melon> weightGreaterThan3000 = separateMelons(melons).get(true);
    System.out.println(weightGreaterThan3000);
  }

  public static Map<Boolean, List<Melon>> separateMelons (List<Melon> melons) {
    return melons.stream()
        .collect(Collectors.partitioningBy(
            (Melon t) -> t.getWeight() >= 3000
        ));
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
