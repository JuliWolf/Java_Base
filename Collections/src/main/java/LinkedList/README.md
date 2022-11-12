# LinkedList

LinkedList - это связанный список

## Создание

1. Ипортировать из пакета `java.util` класс `LinkedList`
2. Объявить класс
```
import java.util.LinkedList;

public class Main {
    public static void main(String[] args) {
        LinkedList<Integer> linkedList = new LinkedList<>();
    }
}
```

## Добавление элементов

1. Для добавление элементов в `LinkedList` используется метод `add`
```
import java.util.LinkedList;

public class Main {
    public static void main(String[] args) {
        LinkedList<Integer> linkedList = new LinkedList<>();
        
        linkedList.add(1);
    }
}
```

## Удаление элементов

1. Для удаления элементов из `LinkedList` используется метод `remove`
```
import java.util.LinkedList;

public class Main {
    public static void main(String[] args) {
        LinkedList<Integer> linkedList = new LinkedList<>();
        
        for (int i = 0; i < 10; i++) {
          linkedList.add(i);
        }
 
        linkedList.remove(1);
    }
}
```

## Получение размера `LinkedList`

1. Для получения размера `LinkedList` используется метод `size`
2. Метод возвращает тип `int`
```
import java.util.LinkedList;

public class Main {
    public static void main(String[] args) {
        LinkedList<Integer> linkedList = new LinkedList<>();
        
        for (int i = 0; i < 10; i++) {
          linkedList.add(i);
        }
 
        System.out.println(linkedList.size());
    }
}
```

## Перебор `LinkedList`

1. Пройтись по листу можно с помощью конструкции `for`
```
import java.util.LinkedList;

public class Main {
    public static void main(String[] args) {
        LinkedList<Integer> linkedList = new LinkedList<>();
        
        for (int i = 0; i < 10; i++) {
          linkedList.add(i);
        }
        
        for (int i = 0; i < linkedList.size(); i++) {
          System.out.println(linkedList.get(i));
        }
    }
}
```

2. Использовать `foreach`
```
import java.util.LinkedList;

public class Main {
    public static void main(String[] args) {
        LinkedList<Integer> linkedList = new LinkedList<>();
        
        for (int i = 0; i < 10; i++) {
          linkedList.add(i);
        }
        
        for (Integer x: linkedList) {
          System.out.println(x);
        }
    }
}
```

## Рекомендации к объявлению

1. Существует конвенция, что при создании `LinkedList` переменной, в которой будет храниться ссылка на массив назначить тип `List`
Это необходимо для полноценной работы полиморфизма, т.е. чтобы не зависить от реализации конкретного списка и иметь возможность поменять реализацию без дополнительных манипуляций с приведением типов.

```
import java.util.LinkedList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Integer> list = new LinkedList<>();
    }
}
```

## Когда использовать

1. Когда невозможно определить длину массива наперед
2. Когда частой опрерацией будет добавление элементов в начало
3. Когда частой операцией будет удаление элементов


## Слабые стороны

1. Получение элементов по индексу
Такая сложность алгоритма связана с особенностей построения `LinkedList`.
`LinkedList` это цепочка элементов, где каждый элемент иметт ссылку на следующий. Поэтому, чтобы получить элемент с конкретной позиции необходимо пройтись по всем элементам с самого начала.


## Как работает

1. При объявлении `LinkedList` 