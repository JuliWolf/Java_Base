# Аннотации

Аннотации - это специальный тип комментариев, которыми можно
- Передавать какие либо инструкции для Java компилятора
- Передавать инструкции для анализаторов исходного кода (создание документации)
- Передавать метаданные, которые могут быть использованы приложением или фреймворками (например Spring)

Метаданные - это данные о данных. Не влияют непосредственно на работу программу, но могут быть выявлены другими программами на этапе компилирования
Например, Аннотации, которые дают дополнительную информацию о коде

Аннотировать можно
- Класс
- Метод
- Параметр
- Поле и т.д.

```
public @interface MyAnnotation {
}
```

```
@MyAnnotation
public class Main {
  @MyAnnotation
  private String name;

  @MyAnnotation
  public Main() {

  }

  @MyAnnotation
  public void test (@MyAnnotation int value) {
    @MyAnnotation String localVar = "Hello";
  }

  @MyAnnotation
  public static void main (@MyAnnotation String[] args) {
    
  }
}
```