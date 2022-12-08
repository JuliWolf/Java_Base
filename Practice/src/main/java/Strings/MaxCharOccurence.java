package Strings;

import javafx.util.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MaxCharOccurence {
  public static void main(String[] args) {
    Pair<Character, Integer> result = maxOccurenceCharacter("AAAFHJKFS HHJHKSD HKAHK");
    System.out.println(result.getValue() + ": " + result.getKey());

    Pair<Character, Long> result2 = maxOccurenceCharacter2("AAAFHJKFS HHJHKSD HKAHK");
    System.out.println(result2.getValue() + ": " + result2.getKey());
  }

  public static Pair<Character, Integer> maxOccurenceCharacter (String str) {
    Map<Character, Integer> counter = new HashMap<>();
    char[] chStr = str.toCharArray();

    for (int i = 0; i < chStr.length; i++) {
      char currentChar = chStr[i];
      if (!Character.isWhitespace(currentChar)) {
        Integer count = counter.get(currentChar);
        if (count == null) {
          counter.put(currentChar, 1);
        } else {
          counter.put(currentChar, ++ count);
        }
      }
    }

    int maxOccurences = Collections.max(counter.values());
    char maxCharacter = Character.MIN_VALUE;

    for (Map.Entry<Character, Integer> entry : counter.entrySet()) {
      if (entry.getValue() == maxOccurences) {
        maxCharacter = entry.getKey();
      }
    }

    return new Pair<>(maxCharacter, maxOccurences);
  }

  public static Pair maxOccurenceCharacter2 (String str) {
    return str.chars()
        .filter(c -> Character.isWhitespace(c) == false)
        .mapToObj(c -> (char) c)
        .collect(Collectors.groupingBy(c -> c, Collectors.counting()))
        .entrySet()
        .stream()
        .max(Map.Entry.comparingByValue())
        .map(p -> new Pair(p.getKey(), p.getValue()))
        .orElse(new Pair(Character.MIN_VALUE, -1L));
  }
}
