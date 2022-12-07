package Strings;

import java.util.HashSet;
import java.util.Set;

public class FindAllWordVariants {
  public static void main(String[] args) {
    System.out.println(permuteAndPrint("TEST"));
  }

  public static Set<String> permuteAndPrint (String str) {
    return permuteAndPrint("", str);
  }

  private static Set<String> permuteAndPrint (String prefix, String str) {
    Set<String> permutations = new HashSet<>();
    int n = str.length();

    if (n == 0) {
      permutations.add(prefix);
    } else {
      for (int i = 0; i < n; i++) {
        permutations.addAll(permuteAndPrint(
            prefix + str.charAt(i),
            str.substring(i + 1, n) + str.substring(0, i)
            )
        );
      }
    }

    return permutations;
  }
}
