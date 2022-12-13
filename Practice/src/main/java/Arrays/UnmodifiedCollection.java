package Arrays;

import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import java.util.Map;

import static java.util.Map.entry;

public class UnmodifiedCollection {
  public static void main(String[] args) {
    List<Integer> list = Collections.unmodifiableList(Arrays.asList(1, 2, 3, 4, 5));
    List<Integer> list1 = List.of(1, 2, 3, 4, 5);

    final MutableMelon melon1 = new MutableMelon("Crenshaw", 2000);
    final MutableMelon melon2 = new MutableMelon("Gac", 1200);

    // немодифицируемый список -> можно менять значения объектов
    final List<MutableMelon> melonList = Collections.unmodifiableList(Arrays.asList(melon1, melon2));
    final List<MutableMelon> melonList1 = List.of(melon1, melon2);


    final ImmutableMelon melon_1 = new ImmutableMelon("Crenshaw", 2000);
    final ImmutableMelon melon_2 = new ImmutableMelon("Gac", 1200);

    // немутируемый список -> нельзя менять значения объектов
    final List<ImmutableMelon> melonList2 = Collections.unmodifiableList(Arrays.asList(melon_1, melon_2));
    final List<ImmutableMelon> melonList3 = List.of(melon_1, melon_2);


    /* Map */
    // Можно создать с помощью unmodifiableMap() ИЛИ Map.of()

    Map<Integer, MutableMelon> emptyMap = Collections.emptyMap();
    Map<Integer, MutableMelon> mapOfSingleMelon = Collections.singletonMap(1, new MutableMelon("Gac", 1200));

    Map<Integer, MutableMelon> mapOfMelon = Map.ofEntries(
        entry(1, new MutableMelon("Apollo", 3000)),
        entry(2, new MutableMelon("Jade Dew", 3500)),
        entry(3, new MutableMelon("Cantaloupe", 1500))
    );

    Map<Integer, ImmutableMelon> mapOfImmutableMelon = Map.ofEntries(
        entry(1, new ImmutableMelon("Apollo", 3000)),
        entry(2, new ImmutableMelon("Jade Dew", 3500)),
        entry(3, new ImmutableMelon("Cantaloupe", 1500))
    );
  }
}
