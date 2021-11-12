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
