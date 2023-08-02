package Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JuliWolf
 * @date 01.08.2023
 */
public class ReverseWordsInAString {
  public static void main(String[] args) {
    System.out.println(reverseWords("the sky is blue"));
  }

  public static String reverseWords(String s) {
    ArrayList<String> collection = Arrays.stream(s.trim().split(" "))
        .filter(str -> !str.isEmpty())
        .collect(Collectors.toCollection(ArrayList::new));

    Collections.reverse(collection);
    return collection.stream().collect(Collectors.joining(" "));
  }
}
