package Arrays;

import java.util.*;

import static java.util.stream.Collectors.toMap;

public class Sort {
  public static void main(String[] args) {
    Map<String, Melon> melons = new HashMap<>();
    melons.put("delicious", new Melon("Apollo", 3000));
    melons.put("refreshing", new Melon("Jade Dew", 3500));
    melons.put("famous", new Melon("Cantaloupe", 1500));

    /* Сортировка по ключу с помощью TreeMap */
    TreeMap<String, Melon> sortedMap = sortByKeyTreeMap(melons);
    System.out.println(sortedMap);

    /* Сортировка с помощью stream */
    Comparator<String> byInt = Comparator.naturalOrder();
    Map<String, Melon> sortedMapByStream = sortByKeyStream(melons, byInt);
    System.out.println(sortedMapByStream);

    Comparator<Melon> byWeight = Comparator.comparing(Melon::getWeight);
    Map<String, Melon> sortedMapByStreamValue = sortByValueStream(melons, byWeight);
    System.out.println(sortedMapByStreamValue);


    /* Сортировка с помощью List */
    List<String> sortedKeys = sortByKeyList(melons); // [delicious, famous, refreshing]
    System.out.println(sortedKeys);

    List<Melon> sortedValues = sortByValueList(melons);
    System.out.println(sortedValues);
  }

  public static <K, V> TreeMap<K, V> sortByKeyTreeMap (Map<K, V> map) {
    return new TreeMap<>(map);
  }

  public static <K, V> Map<K, V> sortByKeyStream (Map<K, V> map, Comparator<? super K> c) {
    return map.entrySet()
        .stream()
        .sorted(Map.Entry.comparingByKey(c))
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new));
  }

  public static <K, V> Map<K, V> sortByValueStream (Map<K, V> map, Comparator<? super V> c) {
    return map.entrySet()
        .stream()
        .sorted(Map.Entry.comparingByValue(c))
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new));
  }

  public static <K extends Comparable, V> List<K> sortByKeyList (Map<K, V> map) {
    List<K> list = new ArrayList<>(map.keySet());
    Collections.sort(list);

    return list;
  }

  public static <K, V extends Comparable> List<V> sortByValueList (Map<K, V> map) {
    List<V> list = new ArrayList<>(map.values());
    Collections.sort(list);

    return list;
  }
}
