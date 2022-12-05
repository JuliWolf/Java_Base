package Strings;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CountCharactersInString {
  public static void main(String[] args) {
    String str = "234fdg34wedfgdxs43eawtas";

    System.out.println(countDuplicateCharacters(str));
    System.out.println(countDuplicateCharacters2(str));
  }

  // Подсчет повторяющихся символов в строке
  public static Map<Character, Integer> countDuplicateCharacters (String str) {
    Map<Character, Integer> result = new HashMap<>();

    for (int i = 0; i < str.length(); i++) {
      char ch = str.charAt(i);

      result.compute(ch, (a, b) -> (b == null) ? 1 : ++b);
    }

    return result;
  }

  public static Map<Character, Long> countDuplicateCharacters2 (String str) {
    Map<Character, Long> result = str.chars()
        .mapToObj(a -> (char) a)
        .collect(Collectors.groupingBy(a -> a, Collectors.counting()));

    return result;
  }
}
