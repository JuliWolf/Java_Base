# Рефлексия

- Все классы, которые создаются в Java можно рассматривать как экземпляры класса `Class`
- Конкретный человек - это эксземпляр класса `Person`
- А Класс `Person` в свою очередь это экземпляр класса `Class`
- То есть объекты класса `Class` - это конкретные классы со своим названием, методами и полями

Class - это служебный класс, экземпляры которого хранят конкретную информацию о конкретном классе

```
class Class {
    String name;
    String packageName;
    List<Attribute> attributes;
    List<Method> methods;
    ...
}
```

## Как получить доступ к объету классу Class

1. Классу `MyClass`
`Class c = MyClass.class;`

2. Объекту (obj - экземпляр класса `MyClass`)
`Class c = obj.getClass();`

3. Названию класса (полное имя класса: `ru.example.MyClass`)
`Class c = Class.forName("ru.example.MyClass");`


## Получить все методы класса

```
public class TestReflection {
    public static void main (String[] args) {
        Class personsClass = Person.class;
        Method[] methods = personsClass.getMethods();
        for (Method method : methods) {
            System.out.println(method.getName());
            System.out.println(method.getReturnType());
            System.out.println(Arrays.toString(method.getParameterTypes()));
        }
    }
}
```

## Получить все поля класса

* Рефлексия учитывает инкапсуляцию, поэтому будут возвразщены только `public` поля

```
public class TestReflection {
    public static void main (String[] args) {
        Class personsClass = Person.class;
        Field[] fields = personsClass.getFields();
        for (Field field : fields) {
            System.out.println(field.getName());
            System.out.println(field.getType());
        }
    }
}
```

Чтобы рефлексия не учитывала инкапсуляцию необходимо использовать методы со словом`Declared`

```
public class TestReflection {
    public static void main (String[] args) {
        Class personsClass = Person.class;
        Field[] fields = personsClass.getDeclaredFields();
        for (Field field : fields) {
            System.out.println(field.getName());
            System.out.println(field.getType());
        }
    }
}
```

## Получить все аннотации

Проверяем, что класс `Person` аннотирован `@Author`

```
public class TestReflection {
    public static void main (String[] args) {
        Class personsClass = Person.class;
        Annotation[] annotations = personsClass.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof Author) {
                System.out.println("Yes");
            }
        }
    }
}
```
