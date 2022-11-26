# Pattern and Matcher

класс Pattern используется для создания регулярного выражения
класс Matcher используется для поиска совпадений по тексту
Например, когда необходимо найти совпадения по большому тексту

## Пример

1. Создаем объекта класса `Pattern` с помощью метода `Pattern.compile`, в который первым параметром передается регулярное выражение
2. Создает объект класса `Matcher` с помощью метода `Pattern.compile().matcher`, который первым параметром принимает строку, по которой будет просиходить поиск совпадений
3. Метод `Matcher.find` возвращает true|false в зависимости от того, было ли найдено еще одно совпадение
4. Метод `matcher.group()` возвращает совпадение. Может принимать первым параметром номер группы, которую нужно вывести из совпадения
NOTE: Группой в резуряных выражениях считается та область, которая заключена в круглые скобки `()`

```
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
  public static void main(String[] args) {
    String text = "Hello, Guys! I send you my email joe@gmail.com so we can\n" +
        "keep in touch. Thank, Joe! That's cool. I am sending you\n" +
        "my address: tim@yandex.ru. Let's stay in touch...";

    Pattern email = Pattern.compile("(\\w+)@(gmail|yandex)\\.(com|ru)");
    Matcher matcher = email.matcher(text);

    while (matcher.find()) {
      System.out.println(matcher.group()); // joe@gmail.com , tim@yandex.ru
      System.out.println(matcher.group(1)); // joe, tim
      System.out.println(matcher.group(2)); // gmail, yandex
    }
  }
}
```