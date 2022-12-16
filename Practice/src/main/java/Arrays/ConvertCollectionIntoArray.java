package Arrays;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ConvertCollectionIntoArray {
  public static void main(String[] args) {
    /* Collection.toArray() */
    List<String> names = Arrays.asList("ana", "mario", "vio");
    Object[] namesArrayAsObjects = names.toArray();

    // with type
    String[] namesArraysAsStrings = names.toArray(new String[names.size()]);
    String[] namesArraysAsStrings1 = names.toArray(new String[0]);

    /* Создать немодифицируемую коллекцию List/Set */
    String[] namesArray = { "ana", "mario", "vio" };

    List<String> namesArraysAsList = List.of(namesArray);
    Set<String> namesArraysAsSet = Set.of(namesArray);
  }
}
