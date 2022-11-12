# ArrayList

ArrayList - это динамический массив, в базе которого используется обычный массив java.
У него нет фиксированного размера. Размер увеличивается автоматически по мере добавление элементов.

## Создание

1. Ипортировать из пакета `java.util` класс `ArrayList`
2. Объявить класс
```
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        ArrayList<Integer> list = new ArrayList<>();
    }
}
```

## Добавление элементов

1. Для добавление элементов в `ArrayList` используется метод `add`
```
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        ArrayList<Integer> list = new ArrayList<>();
        
        arrayList.add(1);
    }
}
```

## Удаление элементов

1. Для удаления элементов из `ArrayList` используется метод `remove`
```
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        ArrayList<Integer> list = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
          arrayList.add(i);
        }
 
        arrayList.remove(1);
    }
}
```

## Получение размера `ArrayList`

1. Для получения размера `ArrayList` используется метод `size`
2. Метод возвращает тип `int`
```
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        ArrayList<Integer> list = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
          arrayList.add(i);
        }
 
        System.out.println(arrayList.size());
    }
}
```

## Перебор `ArrayList`

1. Пройтись по листу можно с помощью конструкции `for`
```
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        ArrayList<Integer> list = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
          arrayList.add(i);
        }
        
        for (int i = 0; i < arrayList.size(); i++) {
          System.out.println(arrayList.get(i));
        }
    }
}
```

2. Использовать `foreach`
```
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        ArrayList<Integer> list = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
          arrayList.add(i);
        }
        
        for (Integer x: arrayList) {
          System.out.println(x);
        }
    }
}
```

## Рекомендации к объявлению

1. Существует конвенция, что при создании `ArrayList` переменной, в которой будет храниться ссылка на массив назначить тип `List`
Это необходимо для полноценной работы полиморфизма, т.е. чтобы не зависить от реализации конкретного списка и иметь возможность поменять реализацию без дополнительных манипуляций с приведением типов.

```
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
    }
}
```

## Когда использовать

1. Когда невозможно определить длину массива наперед
2. Когда основные опрерацией будет добавление элементов в конец и удаление с конца
3. Когда необходим максимально быстрый доступ к элементам по индексу


## Слабые стороны

1. Удаление элементов - сложность O(n)
Такая сложность алгоритма связана с тем, что при удалении элемента, все элементы что остаются с левой стороны (после удаляемого элемента) должны сдвинуться на одну позицию в сторону начала массива. 
Когда элементов много, данная операци может занять значительное количество времени.


## Как работает

1. При объявлении `ArrayList` создается массив. Массив создается или дефолтного размера 10 или того размера, который был задан при инициализации.
2. При добавлении элементов происходит проверка на то, не закончилось ли место в массиве. Если места достаточно, то добавляется новый элемент. Если место в массиве не хватает, то вычисляется новая длина массива, создается новый массив и в него копируются все элементы из старого массива. 
3. При удалении элементов элемент удаляется, и все элементы, что остались после удаленного элемента сдвигаются на одну позицию вправо(к началу)