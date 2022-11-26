# Базовые конструкции регулярных выражений

Регулярные выражения - это мощный инструкмент для работы со строками. 
С помощью них можно задать правила содержания строки

В примерах будет использоваться метод `match` - метод может принимать регулярное выражение. 
Метод возвращает ответ типа `boolean`

## \\\d

Выбор одной цифры

```
String d = "d";
System.out.println("d match \\d " + d.matches("\\d")); // false
```

## +

Один или более предыдущих символов, выражений

```
String numbers = "1231235";
System.out.println("1231235 matches \\d+ " + numbers.matches("\\d+")); // true
```

## *

0 или более предыдущих символов, выражений

```
String a = "35345345";
System.out.println("35345345 matches \\d* " + a.matches("\\d*")); // true

String empty = "";
System.out.println(" matches \\d* " + empty.matches("\\d*")); // true
```

## ?

Символ, который идет после него, может быть а может не быть

```
String isExists = "-234234";
System.out.println("-234234 matches -?\\d* " + isExists.matches("-?\\d*")); // true

String positive = "+234234";
System.out.println("+234234 matches -?\\d* " + positive.matches("-?\\d*")); // false
```

## ( | | )

или
(x|y|z)

```
String or = "+234234";
System.out.println("+234234 matches (-|\\+)?\\d* " + or.matches("(-|\\+)?\\d*")); // true
```

## []

перечисление всех возможных вариантов

[a-zA-Z] - все английские буквы
[abc] = (a|b|c)
[0-9] - \\d

```
String z = "gdfgdfgdf4353453";
System.out.println("gdfgdfgdf4353453 matches [a-zA-Z]+\\d+ " + z.matches("[a-zA-Z]+\\d+")); // true
```

## ^

не следующее выражение

[^abc] - все символы кроме [abc]

```
String eTrue = "hello";
String eFalse = "heallo";
System.out.println("hello matches [^abc]* " + eTrue.matches("[^abc]*")); // true
System.out.println("heallo matches [^abc]* " + eFalse.matches("[^abc]*")); // false
```

## .

любой символ

```
String url = "http://www.google.com";
System.out.println("http://www.google.com matches http://www\\..+\\.(com|ru) " + url.matches("http://www\\..+\\.(com|ru)")); // true

String url2 = "http://www.yandex.ru";
System.out.println("http://www.yandex.ru matches http://www\\..+\\.(com|ru) " + url2.matches("http://www\\..+\\.(com|ru)")); // true

String url3 = "Hello here";
System.out.println("Hello here matches http://www\\..+\\.(com|ru) " + url3.matches("http://www\\..+\\.(com|ru)")); // false
```

## {}

внутри скобок обозначается количество символов или выражений или диапазон символов или выражений

{2} - 2 символа до (\\d{2}) - ровно 2 цифры
{2, } - 2 или более символа (\\d{2, }) - от 2х до бесконечности цифр
{2, 4} - от 2х до 4х символов (\\d{2, 4}) - от 2х до 4х цифр

```
String f = "123";
System.out.println("123 matches \\d{2} " + f.matches("\\d{2}")); // false

String f1 = "123234234";
System.out.println("123234234 matches \\d{2,} " + f1.matches("\\d{2,}")); // true
```

## \\\w

одна английская буква

\\w = [a-zA-Z]

```
String str = "fdg";
System.out.println("fdg matches \\w+ " + str.matches("\\w+")); // true
```

[Больше символов для составления регулярных выражений](https://regexlib.com/(X(1)A(M2OUr0-OZo281FAvCnEjr8snTkIDk1wKLGZVfOoKH8mFaG1u7gkieE0GoNYxDlfC34OLWP2heIWCc2FgLQUI3WaIBX4k35tcaJx-k-Ci9qRdM-fR37_hqaqQUjUw18uHdI0vKNwfQS4dKpXll5cpw2u5DQfldJiUgQyf-9qKd3Y6kV0TsZOF9pNdfBsbs00_0))/CheatSheet.aspx)

[Попробовать регулярное выражений онлайн](https://regex101.com/)
