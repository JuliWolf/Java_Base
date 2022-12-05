package Strings;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ReverseWords {
  private static final String WHITESPACE = " ";

  public static void main(String[] args) {
    String str = "Hello here, try to reverse it";

    System.out.println(reverseWords(str));
    System.out.println(reverseWords2(str));
  }

  public static String reverseWords (String str) {
    String[] words = str.split(WHITESPACE);
    StringBuilder reversedString = new StringBuilder();

    for (String word : words) {
      StringBuilder reversedWord = new StringBuilder();

      for (int i = word.length() - 1; i >= 0; i--) {
        reversedWord.append(word.charAt(i));
      }

      reversedString.append(reversedWord).append(WHITESPACE);
    }

    return reversedString.toString();
  }

  public static String reverseWords2 (String str) {
    Pattern PATTERN = Pattern.compile(" +");

    return PATTERN.splitAsStream(str)
        .map(w -> new StringBuilder(w).reverse())
        .collect(Collectors.joining(" "));
  }
}
