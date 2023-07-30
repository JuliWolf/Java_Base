package Strings;

/**
 * @author JuliWolf
 * @date 30.07.2023
 */
public class GreatestCommonDivisor {
  public static void main(String[] args) {
//    Input: str1 = "ABCABC", str2 = "ABC"
//    Output: "ABC"
      gcdOfStrings("ABCABC", "ABC");

//    Input: str1 = "ABABAB", str2 = "ABAB"
//    Output: "AB"
      gcdOfStrings("ABABAB", "ABC");

//    Input: str1 = "LEET", str2 = "CODE"
//    Output: ""
      gcdOfStrings("LEET", "CODE");
  }

  public static String gcdOfStrings(String str1, String str2) {
    if (!(str1 + str2).equals(str2 + str1)) {
      return "";
    }

    int gcd = gcd(str1.length(), str2.length());
    return str1.substring(0, gcd);
  }

  public static int gcd (int a, int b) {
    System.out.println("a: " + a + " b: " + b);
    return b == 0 ? a : gcd(b, a % b);
  }
}
