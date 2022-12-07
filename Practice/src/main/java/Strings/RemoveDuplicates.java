package Strings;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class RemoveDuplicates {
  public static void main(String[] args) {
    System.out.println(removeDuplicates("Hello here dude"));
    System.out.println(removeDuplicates2("Hello here dude"));
    System.out.println(removeDuplicates3("Hello here dude"));
  }

  public static String removeDuplicates (String str) {
    char[] chArray = str.toCharArray();
    StringBuilder sb = new StringBuilder();

    for (char ch : chArray) {
      if (sb.indexOf(String.valueOf(ch)) == -1) {
        sb.append(ch);
      }
    }

    return sb.toString();
  }

  public static String removeDuplicates2 (String str) {
    char[] chArray = str.toCharArray();
    StringBuilder sb = new StringBuilder();
    Set<Character> chHashSet = new HashSet<>();

    for (char ch : chArray) {
      if (chHashSet.add(ch)) {
        sb.append(ch);
      }
    }

    return sb.toString();
  }

  public static String removeDuplicates3 (String str) {
    return Arrays.stream(str.split(""))
        .distinct()
        .collect(Collectors.joining());
  }
}
