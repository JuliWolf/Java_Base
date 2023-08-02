package Strings;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JuliWolf
 * @date 31.07.2023
 */
public class ReverseVowels {
  public static void main(String[] args) {
    System.out.println(reverseVowels("Yo! Bottoms up, U.S. Motto, boy!"));
  }

//  public static String reverseVowels(String s) {
//    StringBuilder stringBuilder = new StringBuilder(s);
//    List<Pair<Character, Integer>> vowelsList = new ArrayList<>();
//
//    for (int i = 0; i < stringBuilder.length(); i++) {
//      String charString = "" + stringBuilder.charAt(i);
//      if (charString.matches("^(?i:[aeiouy]).*")) {
//        vowelsList.add(new Pair<Character, Integer>(stringBuilder.charAt(i), i));
//      }
//    }
//
//    if (vowelsList.size() == 1) {
//      return s;
//    }
//
//    for (int i = 0; i < vowelsList.size(); i++) {
//      int lastEntryIndex = vowelsList.size() - 1 - i;
//
//      if (lastEntryIndex >= i) {
//        Pair<Character,Integer> firstEntry = vowelsList.get(i);
//        Pair<Character,Integer> lastEntry = vowelsList.get(lastEntryIndex);
//
//        String firstChar = "" + firstEntry.getKey();
//        String lastChar = "" + lastEntry.getKey();
//
//        stringBuilder.setCharAt(firstEntry.getValue(), lastEntry.getKey());
//        stringBuilder.setCharAt(lastEntry.getValue(), firstEntry.getKey());
//      }
//    }
//
//    return stringBuilder.toString();
//  }

  public static String reverseVowels(String s) {
    char[] word = s.toCharArray();
    int start = 0;
    int end = s.length() - 1;
    String vowels = "aeiouAEIOU";

    while (start < end) {
      // Move start pointer until it points to a vowel
      while (start < end && vowels.indexOf(word[start]) == -1) {
        start++;
      }

      // Move end pointer until it points to a vowel
      while (start < end && vowels.indexOf(word[end]) == -1) {
        end--;
      }

      // Swap the vowels
      char temp = word[start];
      word[start] = word[end];
      word[end] = temp;

      // Move the pointers towards each other
      start++;
      end--;
    }

    return new String(word);
  }
}
