package Arrays;

/**
 * @author JuliWolf
 * @date 05.08.2023
 */
public class CountVowelsInSubstring {
  public static void main(String[] args) {
    System.out.println(maxVowels("weallloveyou", 7));
  }

  public static int maxVowels(String s, int k) {
    int result = 0;

    for (int i = k; i <= s.length(); i++) {
      String str = s.substring(i-k, i);

      int vowelsInSubSting = str.replaceAll("(?![aeiouAEIOU])[a-z]", "").length();

      if (vowelsInSubSting > result) {
        result = vowelsInSubSting;
      }

      if (result == k) {
        return result;
      }
    }

    return result;
  }
}
