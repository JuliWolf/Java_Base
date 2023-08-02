package Arrays;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * @author JuliWolf
 * @date 02.08.2023
 */
public class StringCompression {
  public static void main(String[] args) {
    char[] chars = {'a', 'a', 'b', 'b', 'c', 'c', 'c'};
    System.out.println(compress(chars));
    System.out.println(chars);
//    System.out.println(compress(new char[]{'a'}));
  }

  public static int compress(char[] chars) {
    StringBuilder result = new StringBuilder();
    long count = 0l;
    char currentChar = 0;

    for (int i = 0; i < chars.length; i++) {
      if (currentChar != chars[i]) {
        currentChar = chars[i];
        if (count > 1) {
          result.append(count);
        }

        count = 1;
        result.append(currentChar);
      } else {
        count++;
      }

      if (chars.length - 1 == i) {
        if (count > 1) {
          result.append(count);
        }
      }
    }

    for (int i = 0; i < result.length(); i++) {
      chars[i] = result.charAt(i);
    }

    return result.length();
  }

  public static int recommendedCompress(char[] chars) {
    int i = 0, res = 0;
    while (i < chars.length) {
      int groupLength = 1;
      while (i + groupLength < chars.length && chars[i + groupLength] == chars[i]) {
        groupLength++;
      }
      chars[res++] = chars[i];
      if (groupLength > 1) {
        for (char c : Integer.toString(groupLength).toCharArray()) {
          chars[res++] = c;
        }
      }
      i += groupLength;
    }
    return res;
  }
}
