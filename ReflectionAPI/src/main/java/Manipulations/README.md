# Что можно делать с помощью рефлекции

1. Для объектов класса `Class`
- Создавать новые объекты класса `newInstance()`
- Получать методы по сигнатуре `getMethod(...)`
- Получать конструктор по сигнатуре `getConstructor(...)`

2. Для объекта класса `Method`
- Вызвать методы `invoke()`
- и т.д.

```
public class TestReflection {
    public static void main (String[] args) throws NoSuchMethodException {
        Class stringClass = String.class;
        Method m = stringClass.getMethod("indexOf", String.class, int.class);
        System.out.println(m);
    }
}
```
