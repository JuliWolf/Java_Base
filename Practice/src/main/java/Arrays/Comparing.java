package Arrays;

import java.util.Arrays;
import java.util.Comparator;

public class Comparing {

  /* Equals */
  int[] integers1 = {3,4,5,6,1,5};
  int[] integers2 = {3,4,5,6,1,5};
  int[] integers3 = {3,4,5,6,1,3};

  boolean i12 = Arrays.equals(integers1, integers2); // true
  boolean i13 = Arrays.equals(integers1, integers3); // false

  /* Comparing array of objects */

  Melon[] melons1 = {
      new Melon("Horned", 1500), new Melon("Gac", 1000)
  };

  Melon[] melons2 = {
      new Melon("Horned", 1500), new Melon("Gac", 1000)
  };

  Melon[] melons3 = {
      new Melon("Mami", 1500), new Melon("Gac", 1000)
  };

  // сравнение объектов считается эквивалентным на основе контракта метода `equals()`
  boolean m12 = Arrays.equals(melons1, melons2); // true
  boolean m13 = Arrays.equals(melons1, melons3); // true


  /* Comparator */
  Comparator<Melon> byType = Comparator.comparing(Melon::getType);
  Comparator<Melon> byWeight = Comparator.comparing(Melon::getWeight);

  boolean mw12 = Arrays.equals(melons1, melons2, byWeight); // true
  boolean mt13 = Arrays.equals(melons1, melons3, byType); // true


  /* Несовпадение */

  // метод `mismatch` возвращает индекс первого несовпадения и -1 если несовпадений нет
  int mi12 = Arrays.mismatch(integers1, integers2); // -1
  int mi13 = Arrays.mismatch(integers1, integers3); // 5

  int mm12 = Arrays.mismatch(melons1, melons2); // -1
  int mm13 = Arrays.mismatch(melons1, melons3); // 0
}
