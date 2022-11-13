# LinkedHashMap

LinkedHashMap - это структура данных где данные храняться в виде "ключ + значение".
В каком порядке были добавлены, в том порядке они и сохранятся.

## Создание

1. Ипортировать из пакета `java.util` класс `LinkedHashMap`
2. Объявить класс
```
import java.util.LinkedHashMap;

public class Main {
    public static void main(String[] args) {
        LinkedHashMap<Integer, String> map = new LinkedHashMap<>();
    }
}
```

## Добавление элементов

1. Для добавление элементов в `LinkedHashMap` используется метод `put`
```
import java.util.LinkedHashMap;

public class Main {
    public static void main(String[] args) {
        LinkedHashMap<Integer, String> map = new LinkedHashMap<>();
        
        map.put(2, "Два");
        map.put(3, "Три");
        map.put(1, "Один");
    }
}
```

## Удаление элементов

1. Для удаления элементов из `LinkedHashMap` используется метод `remove`
```
import java.util.LinkedHashMap;

public class Main {
    public static void main(String[] args) {
        LinkedHashMap<Integer, String> map = new LinkedHashMap<>();
        
        map.put(2, "Два");
        map.put(3, "Три");
        map.put(1, "Один");
 
        map.remove(1);
    }
}
```

## Получение размера `LinkedHashMap`

1. Для получения размера `LinkedHashMap` используется метод `size`
2. Метод возвращает тип `int`
```
import java.util.LinkedHashMap;

public class Main {
    public static void main(String[] args) {
        LinkedHashMap<Integer, String> map = new LinkedHashMap<>();
        
        map.put(2, "Два");
        map.put(3, "Три");
        map.put(1, "Один");
 
        System.out.println(map.size()); // 3
    }
}
```

## Перебор `LinkedHashMap`

1. Пройтись по листу можно с помощью конструкции `for`
Каждый элемент `LinkedHashMap` представляет собой отдельный тип `LinkedHashMap.Entry`, который хранит в себе ключ и значение
```
import java.util.LinkedHashMap;

public class Main {
    public static void main(String[] args) {
        LinkedHashMap<Integer, String> map = new LinkedHashMap<>();
        
        map.put(2, "Два");
        map.put(3, "Три");
        map.put(1, "Один");
        
        for (LinkedHashMap.Entry<Integer, String> entry: map.entrySet()) {
          System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }
}
```

## Рекомендации к объявлению

1. Существует конвенция, что при создании `LinkedHashMap` переменной, в которой будет храниться ссылка на массив назначить тип `LinkedHashMap`
Это необходимо для полноценной работы полиморфизма, т.е. чтобы не зависить от реализации конкретного списка и иметь возможность поменять реализацию без дополнительных манипуляций с приведением типов.

```
import java.util.LinkedHashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Map<Integer, String> map = new LinkedHashMap<>();
    }
}
```

## Когда использовать

1. Когда необходимо хранить данные в виде словаря (dictionary)
2. Когда важно сохранить тот порядок, в котором элементы были добавлены
3. LinkedHashMap представляет собой структуру "связанный список", поэтому наиболее эффективными операциями будут:
- Удаление элементов
- Чтение элементов с начала или с конца (двухсвязный список)


## Различия HashMap и TreeMap и LinkedHashMap

1. HashMap не сохраняет порядок элементов, порядок зависит от сгенерированного хеша
2. TreeMap сортирует элементы. Можно назначить свои правила сортировки. По дефолту используется "естественная сортировка"
3. LinkedHashMap сохраняет тот порядок, в котором элементы были добавлены


## Как работает

1. При добавлении элементов в `LinkedHashMap` сначала определяется `hash` ключа.
2. По полученному `hash` проверяется наличие данного ключа в `LinkedHashMap`. Если такого элемента не найдено, то происходит добавление данного "ключ+значение" в LinkedHashMap
3. Если же ключ ранее уже был добавлен, то значение переписывается.
4. По сути `LinkedHashMap` представляет собой массив собой массив с элементами "ключ+значение"
5. Когда происходит коллизиця (когда генерируется `hash` но ключ и значение зашифрованные в хеше разные) 
элемент "ключ + значение" превращается в связанный список и новфй элемент добавляется в конец данного списка.
