package Arrays;

import java.util.Arrays;
import java.util.Comparator;

public class LexicalComparing {
  public static void main(String[] args) {
    /* Compare */

    // 0, если заданные массивы равны и содержат одиннаковые элементы в одиннаковом порядке
    // значение меньше 0, если первый массив лексикографически меньше второго массива
    // значение больше 0, если первый массив лексикографически больше второго массива

    int[] integers1 = {3,4,5,6,1,5};
    int[] integers2 = {3,4,5,6,1,5};
    int[] integers3 = {3,4,5,6,1,3};

    int i12 = Arrays.compare(integers1, integers2); // 0
    // 5 больше 3
    int i13 = Arrays.compare(integers1, integers3); // 1

    int is13 = Arrays.compare(integers1, 3, 6, integers3, 3, 6); // 1

    // метод compare не использует `equals`
    // для того, чтобы не создавать каждый раз Comparator необходимо имлементировать интерфейс Comparable
    Melon[] melons1 = {
        new Melon("Horned", 1500), new Melon("Gac", 1000)
    };

    Melon[] melons2 = {
        new Melon("Horned", 1500), new Melon("Gac", 1000)
    };

    Melon[] melons3 = {
        new Melon("Mami", 1500), new Melon("Gac", 1000)
    };

    int m12 = Arrays.compare(melons1, melons2); // 0

    // в индексе 0 дыня Horned имеет вес 1500, что менбьше веса дыни Hami, равного 1600
    int m13 = Arrays.compare(melons1, melons3); // -1

    // вес Gac составляет 1000 в melons1 и 800 в melons3
    int ms13 = Arrays.compare(melons1, 1, 2, melons3, 1, 2); // 1

    Comparator<Melon> byType = Comparator.comparing(Melon::getType);
    int mt13 = Arrays.compare(melons1, melons3, byType); // 14
  }
}
