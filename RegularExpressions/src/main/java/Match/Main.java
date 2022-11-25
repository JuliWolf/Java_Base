package Match;

public class Main {
  public static void main(String[] args) {
    String a = "1234";
    System.out.println("1234 match 1234 " + a.matches("1234")); // true
    System.out.println("1234 match 12345 " + a.matches("12345")); // false

    String hello = "hello";
    System.out.println("hello match hello " + hello.matches("hello"));

    /*
     \\d - одна цифра
    */
    String d = "d";
    System.out.println("d match \\d " + d.matches("\\d"));

    /*
     + - 1 или более
     * - 0 или более
     */
    String numbers = "1231235";
    System.out.println("1231235 matches \\d+ " + numbers.matches("\\d+"));

    String empty = "";
    System.out.println(" matches \\d* " + empty.matches("\\d*"));

    /*
     ? - символ, который идет до него, может быть а может не быть
     */
    String isExists = "-234234";
    System.out.println("-234234 matches -?\\d* " + isExists.matches("-?\\d*")); // true

    String positive = "+234234";
    System.out.println("+234234 matches -?\\d* " + positive.matches("-?\\d*")); // false

    /*
     (x|y|z) - или
     */
    String or = "+234234";
    System.out.println("+234234 matches (-|\\+)?\\d* " + or.matches("(-|\\+)?\\d*")); // true

    /*
     [a-zA-Z] - все английские буквы
     в квадратных скобках описываются все различные варианты
     [abc] = (a|b|c)
     [0-9] - \\d
     */
    String z = "gdfgdfgdf4353453";
    System.out.println("gdfgdfgdf4353453 matches [a-zA-Z]+\\d+ " + z.matches("[a-zA-Z]+\\d+")); // true

    /*
     ^ - не следующее выражение
     [^abc] - все символы кроме [abc]
     */
    String eTrue = "hello";
    String eFalse = "heallo";
    System.out.println("hello matches [^abc]* " + eTrue.matches("[^abc]*")); // true
    System.out.println("heallo matches [^abc]* " + eFalse.matches("[^abc]*")); // false

    /*
     . - любой символ
     */
    String url = "http://www.google.com";
    System.out.println("http://www.google.com matches http://www\\..+\\.(com|ru) " + url.matches("http://www\\..+\\.(com|ru)")); // true

    String url2 = "http://www.yandex.ru";
    System.out.println("http://www.yandex.ru matches http://www\\..+\\.(com|ru) " + url2.matches("http://www\\..+\\.(com|ru)")); // true

    String url3 = "Hello here";
    System.out.println("Hello here matches http://www\\..+\\.(com|ru) " + url3.matches("http://www\\..+\\.(com|ru)")); // false

    /*
     {2} - 2 символа до (\\d{2}) - ровно 2 цифры
     {2, } - 2 или более символа (\\d{2, }) - от 2х до бесконечности цифр
     {2, 4} - от 2х до 4х символов (\\d{2, 4}) - от 2х до 4х цифр
     */

    String f = "123";
    System.out.println("123 matches \\d{2} " + f.matches("\\d{2}")); // false

    String f1 = "123234234";
    System.out.println("123234234 matches \\d{2,} " + f1.matches("\\d{2,}")); // true

    /*
     \\w - одна английская буква
     \\w = [a-zA-Z]
     */
    String str = "fdg";
    System.out.println("fdg matches \\w+ " + str.matches("\\w+")); // true
  }
}
