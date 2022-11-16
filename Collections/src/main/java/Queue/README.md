# Queue

Queue - Это структуру данных типа "первый зашел первый вышел" First in First Out

## Создание

1. Ипортировать из пакета `java.util` класс `Queue`
2. `LinkedList` имплементирует интерфейс `Queue` поэтому для примера используем его
3. Объявить класс
```
import java.util.Queue;
import java.util.LinkedList;

public class Main {
    public static void main(String[] args) {
        Queue<Person> people = new LinkedList<>();
    }
}
```

## Добавление элементов

1. Для добавление элементов в `Queue` используется метод `add` или `offer`
2. `add` в случае ошибки выбрасывает исключени, а `offer` возвращае специальное значение
```
import java.util.Queue;
import java.util.LinkedList;

public class Main {
    public static void main(String[] args) {
        Queue<Person> people = new LinkedList<>();
        
        people.add(new Person(3));
    }
}
```

```
import java.util.Queue;
import java.util.LinkedList;

public class Main {
    public static void main(String[] args) {
        Queue<Person> people = new LinkedList<>();
        
        people.offer(new Person(3));
    }
}
```

## Удаление элементов

1. Для удаления элементов из `Queue` используется метод `remove` или `poll`
2. `remove` в случае ошибки выбрасывает исключени, а `poll` возвращае специальное значение
```
import java.util.Queue;
import java.util.LinkedList;

public class Main {
    public static void main(String[] args) {
        Queue<Person> people = new LinkedList<>();
        
        people.add(new Person(3));
        people.remove(1); // Exception
    }
}
```

```
import java.util.Queue;
import java.util.LinkedList;

public class Main {
    public static void main(String[] args) {
        Queue<Person> people = new LinkedList<>();
        
        people.add(new Person(3));
        people.poll(1); // false
    }
}
```