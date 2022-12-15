package Arrays;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Remove {
  public static void main(String[] args) {
    List<Melon> melons = generateList();

    removeIf(melons);
    System.out.println(melons);

    List<Melon> melons1 = generateList();
    System.out.println(removeWithStream(melons1));
  }

  public static List<Melon> removeWithStream (List<Melon> melons) {
    return melons.stream()
        .filter(t -> t.getWeight() >= 3000)
        .collect(Collectors.toList());
  }

  public static void removeIf (List<Melon> melons) {
    melons.removeIf(t -> t.getWeight() < 3000);
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
