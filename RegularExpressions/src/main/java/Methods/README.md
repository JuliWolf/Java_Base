# Методы регулярных выражений

## Split

Метод для разделения строки по определенному паттерну. Принимает регулярное выражение первым параметром

```
String a = "Hello there hey";
String[] words = a.split(" ");
System.out.println(Arrays.toString(words)); // [Hello, there, hey]

String b = "Hello.there.hey";
String[] words2 = b.split("\\.");
System.out.println(Arrays.toString(words2)); // [Hello, there, hey]

String c = "Hello345345there345345hey";
String[] words3 = c.split("\\d+");
System.out.println(Arrays.toString(words3)); // [Hello, there, hey]
```

## Replace

Метод для замены всей строки или части строки 

```
String d = "Hello there hey";
String replace = d.replace(" ", "."); // Hello.there.hey
System.out.println(replace);
```

## ReplaceAll

Метод для замены всей строки или части строки по паттерну. 
Первым параметром принимает регулярное выражение, вторым значение, на которое заменить

```
String e = "Hello43534there3423hey";
String replaceAll = e.replaceAll("\\d+", "-"); // Hello-there-hey
System.out.println(replaceAll);
```

## ReplaceFirst

Метод для замены первого совпадения в строке
Первым параметром принимает регулярное выражение, вторым значение, на которое заменить

```
String f = "Hello43534there3423hey";
String replaceFirst = f.replaceFirst("\\d+", "-"); // Hello-there3423hey
System.out.println(replaceFirst);
```
