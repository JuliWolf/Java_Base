package java_head.base;

public class Main {

    public static void main(String[] args) {
	      int beerNum = 99;
        String word = "бутылок (бутылки)";

        while (beerNum > 0) {
            if (beerNum == 1) {
                word = "бутылка";
            }

            System.out.println(beerNum + " " + word + " пива на стене");
            System.out.println(beerNum + " " + word + " пива.");
            System.out.println("Возьми одну");
            System.out.println("Пусти по кругу.");

            beerNum = beerNum - 1;

            if (beerNum > 0) {
                System.out.println(beerNum + " " + word + " пива на стене");
            }else {
                System.out.println("Нет бутылок пива на стене");
            }
        }
    }
}

class Shuffle1 {
  public static void main(String [] args) {
    int x = 3;

    while (x > 0) {
      if (x > 2) {
        System.out.print("a");

        x = x - 1;
        System.out.print('-');
      }

      if (x == 2) {
        System.out.print("b c");

        x = x - 1;
        System.out.print('-');
      }

      if (x == 1) {
        System.out.print("d");
        x = x - 1;
      }
    }
  }
}

class PoolPuzzleOne {
  public static void main(String [] args) {
    int x = 0;

    while (x < 4){
      System.out.print("a");

      if (x < 1) {
        System.out.print(" ");
      }
      System.out.print("n");

      if (x > 1) {
        System.out.print("oyster");
        x = x + 2;
      }

      if (x == 1) {
        System.out.print("noys");
      }

      if (x < 1) {
        System.out.print("oise");
      }

      System.out.println("");
      x = x + 1;
    }
  }
}

class Solution {

  public static void main(String[] args) {
    int[] array = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
    printArray(array);
    reverseArray(array);
    printArray(array);
  }

  public static void reverseArray(int[] array) {
    int[] temp = {};
    int length = array.length - 1;
    for (int i = 0; i < array.length / 2; i++) {
      temp[i] = array[length - i];
    }
    array = temp;
  }

  public static void printArray(int[] array) {
    for (int i : array) {
      System.out.print(i + ", ");
    }
    System.out.println();
  }
}

class Cube {
  public static void main(String[] args) {
    System.out.println(ninthDegree(3));
  }

  public static long cube(long a){
    return a*a*a;
  }

  //напишите тут ваш код
  public static long ninthDegree(long b) {
    return cube(cube(b));
  }
}
