package Arrays;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class ReplaceElementInList {
  public static void main(String[] args) {
    List<Melon> melons = generateList();
    replaceElements(melons);

    List<Melon> melons2 = generateList();
    replaceElements2(melons2);
  }

  public static void replaceElements (List<Melon> melons) {
    for (int i = 0; i < melons.size(); i++) {
      if (melons.get(i).getWeight() < 3000) {
        melons.set(i, new Melon(melons.get(i).getType(), 3000));
      }
    }
  }

  public static void replaceElements2 (List<Melon> melons) {
    UnaryOperator<Melon> operator = t -> (t.getWeight() < 3000) ? new Melon(t.getType(), 3000) : t;

    melons.replaceAll(operator);
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
