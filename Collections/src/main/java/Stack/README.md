# Stack

Stack - Это структуру данных типа "первый зашел последний вышел" First in Last Out

## Создание

1. Ипортировать из пакета `java.util` класс `Stack`
3. Объявить класс
```
import java.util.Stack;

public class Main {
    public static void main(String[] args) {
        Stack<Integer> stack = new Stack<>();
    }
}
```

## Добавление элементов

1. Для добавление элементов в `Stack` используется метод `push`
```
import java.util.Stack;

public class Main {
    public static void main(String[] args) {
        Stack<Integer> stack = new Stack<>();
        
        stack.push(1);
    }
}
```

## Получение последнего элемента

1. Для получения последнего элемента из `Stack` используется метод `peek`
```
import java.util.Stack;

public class Main {
    public static void main(String[] args) {
        Stack<Integer> stack = new Stack<>();
        
        stack.push(5);
        stack.push(3);
        stack.push(1);
        
        System.out.println(stack.peek()); // 1
    }
}
```

## Извлечение последнего элемента

1. Для получения последнего элемента из `Stack` используется метод `pop`
2. Данный метод возвращает и удаляет из стека элемент
3. При попытке вытащить значение когда стек пустой, будет возвращено исключение
```
import java.util.Stack;

public class Main {
    public static void main(String[] args) {
        Stack<Integer> stack = new Stack<>();
        
        stack.push(5);
        stack.push(3);
        stack.push(1);
        
        System.out.println(stack.pop()); // 1
        System.out.println(stack.pop()); // 3
        System.out.println(stack.pop()); // 5
        System.out.println(stack.pop()); // Exception
    }
}
```
