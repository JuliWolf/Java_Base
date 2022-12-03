# Кастомные аннотации

## Создание

Аннотации создаются с помощью следующей конструкции

```
public @interface MyAnnotation {} 
```

```
@MyAnnotation
public class Test {}
```

## Поля аннотации

Поля объявляются как методы, но используются как поля

```
public @interface Author {
    String name();
    int dateOfBirth();
} 
```

```
@Author(name = "Stephen King", dateOfBirth = 1947)
public class Test {}
```

## Значения по умолчанию

Значения по умолчанию обозначаются ключевым словом `default`

```
public @interface Author {
    String name() default "Some name";
    int dateOfBirth() default 2000;
} 
```

```
@Author()
public class Test {}
```

## Аннотации для аннотаций

Базовые аннотации для аннотаций лежат в `java.lang.annotation`

1. `@Target` - указывает, к чему может быть применена аннотация. Значения берутся из перечисления (enum) ElementType 
- FIELDS - поле
- METHOD - метод
- TYPE - класс, интерфейс, перечисление

2. `@Retention` - политика удержания аннотации (до какого этапа компилирования или выполнения аннотация видна). Значения лежат в перечислении RetentionPolicy
- SOURCE - отбрасываются при комплиляции. Видны только в самом исходном коде
- CLASS - сохраняются в байт-коде, но нелоступны во время работы программы
- RUNTIME - сохраняются в байт-коде и доступны во время работы программы
