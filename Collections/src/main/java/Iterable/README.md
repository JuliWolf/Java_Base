# Iterator в ArrayList

## Iterator появился в Java 5
Текущая реализация итератора появилась в Java 5, до этого итерирование по массивам происходило по другому

1. Метод `hasNext` возвращает `true` если в списке еще есть элементы и `false` если нет
2. Метод `next` меняет указатель на следующий элемент списка

```
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    List<Integer> list = new ArrayList<>();

    list.add(1);
    list.add(2);
    list.add(3);

    Iterator<Integer> iterator = list.iterator();
 
    while (iterator.hasNext()) {
      iterator.next();
    }
  }
}
```

## При переборе списка через foreach нельзя удалять элементы

1. При попытке удалить элемент во время перебора списка через foreach будет возвращена ошибка ConcurrentException
2. При переборе элементов списка через итератор можно удалять текущий элемент c помощью метода `remove`
Итератор сохраняет некоторое локальное значение текущего значения. Поэтому есть возможность удалить элемент без влияния на дальнейшую работу итератора
```
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    List<Integer> list = new ArrayList<>();

    list.add(1);
    list.add(2);
    list.add(3);

    Iterator<Integer> iterator = list.iterator();
    
    int idx = 0;
    
    while (iterator.hasNext()) {
      if (idx == 1) {
        iterator.remove();
      }
      System.out.println(iterator.next());

      idx++;
    }
  }
}
```