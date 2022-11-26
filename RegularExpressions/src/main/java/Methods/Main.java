package Methods;

import java.util.Arrays;

public class Main {
  public static void main(String[] args) {
    String a = "Hello there hey";
    String[] words = a.split(" ");
    System.out.println(Arrays.toString(words)); // [Hello, there, hey]

    String b = "Hello.there.hey";
    String[] words2 = b.split("\\.");
    System.out.println(Arrays.toString(words2)); // [Hello, there, hey]

    String c = "Hello345345there345345hey";
    String[] words3 = c.split("\\d+");
    System.out.println(Arrays.toString(words3)); // [Hello, there, hey]

    String d = "Hello there hey";
    String replace = d.replace(" ", "."); // Hello.there.hey
    System.out.println(replace);

    String e = "Hello43534there3423hey";
    String replaceAll = e.replaceAll("\\d+", "-"); // Hello-there-hey
    System.out.println(replaceAll);

    String f = "Hello43534there3423hey";
    String replaceFirst = f.replaceFirst("\\d+", "-"); // Hello-there3423hey
    System.out.println(replaceFirst);

  }
}
