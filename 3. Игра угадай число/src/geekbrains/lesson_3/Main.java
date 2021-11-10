package geekbrains.lesson_3;

import java.util.Scanner;

public class Main {

  private static Scanner scanner = new Scanner(System.in);

  public static void main(String[] args) {
    System.out.println("Ваша задача ушадать число.");

    int range = 10;

    // конструкция (int) отрезает дробную часть
    int number = (int) (Math.random() * range);

    // Вызов метода
    playLevel(range, number);

    scanner.close();
  }

  // Объявляем новый метод
  // private ограничивает видимость сущности в пределах одного класса
  // static - указывает на то, что сущность находится в классе, а не в объекте
  // void - означает, что метод ничего не возвращает
  // в круглых скобках указаны параметры, которые метод ожидает
  private static void playLevel (int range, int number) {
    // Бесконечный цикл
    while (true) {
      System.out.println("Угадайте число от 0 до " + range);

      int input_number = scanner.nextInt();

      if(input_number == number) {
        System.out.println("Вы угадали");
        break;
      } else if (input_number > number) {
        System.out.println("Загаданное число меньше");
      } else {
        System.out.println("Загаданное число больше");
      }
    }
  }
}
