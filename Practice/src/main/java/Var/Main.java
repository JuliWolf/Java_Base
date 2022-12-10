package Var;

import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
  public static void main(String[] args) {
    /* Примитивные типы */

    /* Для обозначения типов long, double, float необходимо указывать тип явно*/
//    var intNumber = 10;
//    var longNumber = 10L;
//    var floatNumber = 10F;
//    var doubleNumber = 10D;

    /* Числа с десятичными знаками */
//    var floatNumber = 10.5; // логически выводится как double
    var floatNumber = 10.5F; // логически выводится как float


    /* Понижающее приведение типов */
//    var byteNumber = 25; // логически выводится как int
//    var shortNumber = 1463; // логически выводится как int

    /* Для обозначения типов byte и short необходимо яделаю явное приведение типа */
    var byteNumber = (byte) 25; // логически выводится как byte
    var shortNumber = (short) 1463; // логически выводится как short


    /* Ромбовидный оператор */
    var playerList = new ArrayList<>(); // логически выводится как ArrayList<Object>
    var playerStack = new ArrayDeque<String>(); // логически выводится как ArrayDeque<String>

    Player p1 = new Player();
    Player p2 = new Player();
    var listOfPlayers = List.of(p1, p2); // логически выводится как List<Player>


    /* Тернарный оператор */
    boolean containsEven = true;
    boolean intOrString = true;

    // Error
//    List evensOrOdds = containsEven
//        ? List.of(10,2,12)
//        : Set.of(13,1,11);

    var evensOrOddsCollection = containsEven // Collection<Integer>
        ? Set.of(13,1,11)
        : List.of(10,2,12);


    var numberOrText = intOrString ? 2234 : "2234"; // Serializable


    /* var нельзя использовать в качестве возвращаемого типов метода */
    /* var нельзя использовать в типе аргумента метода */
  }

  public Object fetchTransferableData (String data) throws IOException, UnsupportedFlavorException {
    /*
     * так как var пишется без явного указания типа
     * необходимо давать понятные названия переменным
     */

    var stringSelection = new StringSelection(data);
    var dataFlavorsArray = stringSelection.getTransferDataFlavors();
    var obj = stringSelection.getTransferData(dataFlavorsArray[0]);

    return obj;
  }

  public void chainDivider () {
    /* var хорошо подходит для разбиения длинных цепочек */
    List<Integer> ints = List.of(1,1,2,3,4,4,6,2,1,5,4,5);

    /* before */
//    Collection<List<Integer>> evenAndOdd = ints.stream()
//        .collect(Collectors.partitioningBy(i -> i % 2 == 0))
//        .values();
//
//    List<Integer> evenOrOdd = evenAndOdd.stream()
//        .max(Comparator.comparing(List::size))
//        .orElse(Collections.emptyList());
//
//    int sumEvenOrOdd = evenOrOdd.stream()
//        .mapToInt(Integer::intValue)
//        .sum();

    /* after */
    var evenAndOdd = ints.stream()
        .collect(Collectors.partitioningBy(i -> i % 2 == 0))
        .values();

    var evenOrOdd = evenAndOdd.stream()
        .max(Comparator.comparing(List::size))
        .orElse(Collections.emptyList());

    var sumEvenOrOdd = evenOrOdd.stream()
        .mapToInt(Integer::intValue)
        .sum();
  }

//  public <T extends Number> T testTType (T t) {
//    List<T> numberList = new ArrayList<T>();
//    numberList.add(t);
//    numberList.add((T) Integer.valueOf(3));
//    numberList.add((T) Double.valueOf(3.9));
//
//    // Error: несравнимые типы
////    numberList.add("5");
//
//    return numberList.get(0);
//  }

  public <T extends Number> T testTType (T t) {
    var numberList = new ArrayList<T>(); // ArrayList<T>
    numberList.add(t);
    numberList.add((T) Integer.valueOf(3));
    numberList.add((T) Double.valueOf(3.9));

    // Error: несравнимые типы
//    numberList.add("5");

    return numberList.get(0);
  }
}
