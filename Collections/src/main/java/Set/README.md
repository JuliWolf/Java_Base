# Set

Set - это структура данных где данные храняться только уникальные данные.

## Создание

1. Ипортировать из пакета `java.util` класс `HashSet`
2. Объявить класс
```
import java.util.HashSet;

public class Main {
    public static void main(String[] args) {
        HashSet<Integer, String> map = new HashSet<>();
    }
}
```

## Добавление элементов

1. Для добавление элементов в `HashSet` используется метод `add`
```
import java.util.HashSet;

public class Main {
    public static void main(String[] args) {
        HashSet<Integer, String> set = new HashSet<>();
        
        set.add("Два");
        set.add("Три");
        set.add("Один");
    }
}
```

## Удаление элементов

1. Для удаления элементов из `HashSet` используется метод `remove`
```
import java.util.HashSet;

public class Main {
    public static void main(String[] args) {
        HashSet<Integer, String> set = new HashSet<>();
        
        set.add("Два");
        set.add("Три");
        set.add("Один");
 
        set.remove(1);
    }
}
```

## Получение размера `HashSet`

1. Для получения размера `HashSet` используется метод `size`
2. Метод возвращает тип `int`
```
import java.util.HashSet;

public class Main {
    public static void main(String[] args) {
        HashSet<Integer, String> set = new HashSet<>();
        
        set.add("Два");
        set.add("Три");
        set.add("Один");
 
        System.out.println(set.size()); // 3
    }
}
```

## Перебор `HashSet`

1. Пройтись по листу можно с помощью конструкции `for`

```
import java.util.HashSet;

public class Main {
    public static void main(String[] args) {
        HashSet<Integer, String> set = new HashSet<>();
        
        set.add("Два");
        set.add("Три");
        set.add("Один");
        
        for (String name: set) {
          System.out.println(name);
        }
    }
}
```

## Проверить наличие значения

1. Для определения наличия значения используется метод `contains`
```
import java.util.HashSet;

public class Main {
    public static void main(String[] args) {
        HashSet<Integer, String> set = new HashSet<>();
        
        set.add("Два");
        set.add("Три");
        set.add("Один");
        
        System.out.println(hashSet.contains("Два")); // true
        System.out.println(hashSet.contains("Десять")); // false
    }
}
```

## Рекомендации к объявлению

1. Существует конвенция, что при создании `HashSet` переменной, в которой будет храниться ссылка на массив назначить тип `HashSet`
Это необходимо для полноценной работы полиморфизма, т.е. чтобы не зависить от реализации конкретного списка и иметь возможность поменять реализацию без дополнительных манипуляций с приведением типов.

```
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Set<Integer, String> set = new HashSet<>();
    }
}
```

## Особенности

### HashSet
1. Порядок сохранения элементов рандомный

### TreeSet
1. Естественный порядок элементов

### LinkedHashSet
1. Сохраняет тот порядок, в котором элементы были добавлены

## Как работает

Внутри `Set` иcпользуется `HashMap`