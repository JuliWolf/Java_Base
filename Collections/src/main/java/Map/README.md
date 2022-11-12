# HashMap

HashMap - это структура данных где данные храняться в виде "ключ + значение". 

## Создание

1. Ипортировать из пакета `java.util` класс `HashMap`
2. Объявить класс
```
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        HashMap<Integer, String> map = new HashMap<>();
    }
}
```

## Добавление элементов

1. Для добавление элементов в `HashMap` используется метод `put`
```
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        HashMap<Integer, String> map = new HashMap<>();
        
        map.put(2, "Два");
        map.put(3, "Три");
        map.put(1, "Один");
    }
}
```

## Удаление элементов

1. Для удаления элементов из `HashMap` используется метод `remove`
```
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        HashMap<Integer, String> map = new HashMap<>();
        
        map.put(2, "Два");
        map.put(3, "Три");
        map.put(1, "Один");
 
        map.remove(1);
    }
}
```

## Получение размера `HashMap`

1. Для получения размера `HashMap` используется метод `size`
2. Метод возвращает тип `int`
```
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        HashMap<Integer, String> map = new HashMap<>();
        
        map.put(2, "Два");
        map.put(3, "Три");
        map.put(1, "Один");
 
        System.out.println(map.size()); // 3
    }
}
```

## Перебор `HashMap`

1. Пройтись по листу можно с помощью конструкции `for`
Каждый элемент `HashMap` представляет собой отдельный тип `Map.Entry`, который хранит в себе ключ и значение
```
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        HashMap<Integer, String> map = new HashMap<>();
        
        map.put(2, "Два");
        map.put(3, "Три");
        map.put(1, "Один");
        
        for (Map.Entry<Integer, String> entry: map.entrySet()) {
          System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }
}
```

## Рекомендации к объявлению

1. Существует конвенция, что при создании `HashMap` переменной, в которой будет храниться ссылка на массив назначить тип `Map`
Это необходимо для полноценной работы полиморфизма, т.е. чтобы не зависить от реализации конкретного списка и иметь возможность поменять реализацию без дополнительных манипуляций с приведением типов.

```
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Map<Integer, String> map = new HashMap<>();
    }
}
```

## Когда использовать

1. Когда необходимо хранить данные в виде словаря (dictionary)
2. Когда не важен порядок сохранения данных


## Слабые стороны

1. Данные при сохранении хешируются, что приводит к изменению порядка расположения элементов


## Как работает

1. При добавлении элементов в `HashMap` сначала определяется `hash` ключа.
2. По полученному `hash` проверяется наличие данного ключа в `HashMap`. Если такого элемента не найдено, то происходит добавление данного "ключ+значение" в Map
3. Если же ключ ранее уже был добавлен, то значение переписывается.
4. По сути `HashMap` представляет собой массив собой массив с элементами "ключ+значение"
5. Когда происходит коллизиця (когда генерируется `hash` но ключ и значение зашифрованные в хеше разные) 
элемент "ключ + значение" превращается в связанный список и новфй элемент добавляется в конец данного списка.
