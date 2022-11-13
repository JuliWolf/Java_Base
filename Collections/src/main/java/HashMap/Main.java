package HashMap;

import java.util.HashMap;
import java.util.Map;

public class Main {
  public static void main(String[] args) {
    Map<Integer, String> map = new HashMap<>();

    map.put(2, "Два");
    map.put(3, "Три");
    map.put(1, "Один");

    System.out.println(map.size());

//    System.out.println(map);
//
//    map.put(3, "Другое значение для ключа три");
//
//    System.out.println(map);
//
//    System.out.println(map.get(1));
//    System.out.println(map.get(10));

    for (Map.Entry<Integer, String> entry: map.entrySet()) {
      System.out.println(entry.getKey() + " : " + entry.getValue());
    }
  }
}
