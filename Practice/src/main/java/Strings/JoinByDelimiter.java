package Strings;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class JoinByDelimiter {
  public static void main(String[] args) {
    System.out.println(joinByDelimiter('_', "hello", "There", "How", "Are", "You"));
    System.out.println(joinByDelimiter2('_', "hello", "There", "How", "Are", "You"));
    System.out.println(joinByDelimiter3('_', "hello", "There", "How", "Are", "You"));
  }

  public static String joinByDelimiter (char delimiter, String ...args) {
    StringBuilder result = new StringBuilder();

    int i = 0;
    for (; i < args.length - 1; i++) {
      result.append(args[i]).append(delimiter);
    }

    result.append(args[i]);

    return result.toString();
  }

  public static String joinByDelimiter2 (char delimiter, String ...args) {
    StringJoiner joiner = new StringJoiner(String.valueOf(delimiter));

    for (String arg : args) {
      joiner.add(arg);
    }

    return joiner.toString();
  }

  public static String joinByDelimiter3 (char delimiter, String ...args) {
    return Arrays.stream(args, 0, args.length)
        .collect(Collectors.joining(String.valueOf(delimiter)));
  }
}
