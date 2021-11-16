package java_head.base;

//public class Main {
//    public static void main(String[] args) {
//	      int beerNum = 99;
//        String word = "бутылок (бутылки)";
//
//        while (beerNum > 0) {
//            if (beerNum == 1) {
//                word = "бутылка";
//            }
//
//            System.out.println(beerNum + " " + word + " пива на стене");
//            System.out.println(beerNum + " " + word + " пива.");
//            System.out.println("Возьми одну");
//            System.out.println("Пусти по кругу.");
//
//            beerNum = beerNum - 1;
//
//            if (beerNum > 0) {
//                System.out.println(beerNum + " " + word + " пива на стене");
//            }else {
//                System.out.println("Нет бутылок пива на стене");
//            }
//        }
//    }
//}
//
//class Shuffle1 {
//  public static void main(String [] args) {
//    int x = 3;
//
//    while (x > 0) {
//      if (x > 2) {
//        System.out.print("a");
//
//        x = x - 1;
//        System.out.print('-');
//      }
//
//      if (x == 2) {
//        System.out.print("b c");
//
//        x = x - 1;
//        System.out.print('-');
//      }
//
//      if (x == 1) {
//        System.out.print("d");
//        x = x - 1;
//      }
//    }
//  }
//}
//
//class PoolPuzzleOne {
//  public static void main(String [] args) {
//    int x = 0;
//
//    while (x < 4){
//      System.out.print("a");
//
//      if (x < 1) {
//        System.out.print(" ");
//      }
//      System.out.print("n");
//
//      if (x > 1) {
//        System.out.print("oyster");
//        x = x + 2;
//      }
//
//      if (x == 1) {
//        System.out.print("noys");
//      }
//
//      if (x < 1) {
//        System.out.print("oise");
//      }
//
//      System.out.println("");
//      x = x + 1;
//    }
//  }
//}
//
//class Solution {
//
//  public static void main(String[] args) {
//    int[] array = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
//    printArray(array);
//    reverseArray(array);
//    printArray(array);
//  }
//
//  public static void reverseArray(int[] array) {
//    int[] temp = {};
//    int length = array.length - 1;
//    for (int i = 0; i < array.length / 2; i++) {
//      temp[i] = array[length - i];
//    }
//    array = temp;
//  }
//
//  public static void printArray(int[] array) {
//    for (int i : array) {
//      System.out.print(i + ", ");
//    }
//    System.out.println();
//  }
//}
//
//class Cube {
//  public static void main(String[] args) {
//    System.out.println(ninthDegree(3));
//  }
//
//  public static long cube(long a){
//    return a*a*a;
//  }
//
//  //напишите тут ваш код
//  public static long ninthDegree(long b) {
//    return cube(cube(b));
//  }
//}


/* Guess number game */

//class GameLauncher {
//  public static void main (String[] args) {
//    GuessGame game = new GuessGame();
//    game.startGame();
//  }
//}
//
//class GuessGame {
////  содержит три переменных экземпляра для трех объектов Player
//  Player p1;
//  Player p2;
//  Player p3;
//
//  public void startGame () {
////  Создаем три объекта Player и присваиваем их трем переменным экземпляра
//    p1 = new Player();
//    p2 = new Player();
//    p3 = new Player();
//
////  Объявляем три переменные для хранения вариантов от каждого игрока
//    int guessp1 = 0;
//    int guessp2 = 0;
//    int guessp3 = 0;
//
////  Объявляем три переменные для хранения прапвильности или неправильности ответов игрока
//    boolean p1isRight = false;
//    boolean p2isRight = false;
//    boolean p3isRight = false;
//
////  Создаем число, которое игроки должны угадать
//    int targetNumber = (int) (Math.random() * 10);
//    System.out.println("Я загадываю число от 0 до 9...");
//
//    while (true) {
//      System.out.println("Число, которое нужно угадать, - " + targetNumber);
//
////    Вызываем метод guess() из каждого объекта Player
//      p1.guess();
//      p2.guess();
//      p3.guess();
//
////    Извлекаем варианты каждого игрока получая доступ к их переменным number
//      guessp1 = p1.number;
//      System.out.println("Первый игрок думает, что это " + guessp1);
//
//      guessp2 = p2.number;
//      System.out.println("Второй игрок думает, что это " + guessp2);
//
//      guessp3 = p3.number;
//      System.out.println("Третий игрок думает, что это " + guessp3);
//
////    Проверяем варианты каждого из игроков на соответсвие загаданному числу
////    Если игрок угадал, то присваеваем соответствующей переменной значение true
//      if (guessp1 == targetNumber) {
//        p1isRight = true;
//      }
//
//      if (guessp2 == targetNumber) {
//        p2isRight = true;
//      }
//
//      if (guessp3 == targetNumber) {
//        p3isRight = true;
//      }
//
//      if (p1isRight || p2isRight || p3isRight) {
//        System.out.println("У нас есть победитель!");
//        System.out.println("Первый игрок угадал?" + p1isRight);
//        System.out.println("Второй игрок угадал?" + p2isRight);
//        System.out.println("Третий игрок угадал?" + p3isRight);
//        System.out.println("Конец игры");
////      Игра окончена, так что прекращаем цикл
//        break;
//      } else {
////      Мы должны продолжать, так как никто не угадал!
//        System.out.println("Игроки должны попробовать еще раз.");
//      }
//    }
//  }
//}
//
//class Player {
//  // Здесь хранится вариант числа
//  int number = 0;
//
//  public void guess () {
//    number = (int) (Math.random() * 10);
//
//    System.out.println("Думаю, это число " + number);
//  }
//}


// ------------------- Test page 73
//class DrumKit {
//
//  void playSnare () {
//    System.out.println("бах бах ба-бах");
//  }
//
//  void playTopHat () {
//    System.out.println("динь динь ди-динь");
//  }
//}
//
//class DrumKitTestDrive {
//  public static void main (String[] args) {
//    DrumKit d = new DrumKit();
//    d.playSnare();
//    d.playTopHat();
//  }
//}

// ------------------- Test page 74
//class EchoTestDrive {
//  public static void main (String[] args) {
//    Echo e1 = new Echo();
//    Echo e2 = new Echo();
//    int x = 0;
//
//    while (x < 4) {
//      e1.hello();
//      e1.count = e1.count + 1;
//
//      if (x == 3) {
//        e2.count = e2.count + 1;
//      }
//
//      if (x > 0) {
//        e2.count = e2.count + e1.count;
//      }
//
//      x = x + 1;
//    }
//
//    System.out.println(e2.count);
//  }
//}
//
//class Echo {
//  int count = 0;
//
//  void hello () {
//    System.out.println("привеееееет...");
//  }
//}


// ------------------- Инициализация переменных
//class Foo {
//  public void go () {
//    int x;
////  Нельзя использовать локальные переменные, которым не было присвоено значение
////  Локальным переменным не присваиваются значения по умолчанию
//    int z = x + 3;
//  }
//}


// ------------------- Test page 121
//class Puzzle4 {
//  public static void main(String[] ars) {
//    Puzzleb[] obs = new Puzzleb[6];
//    int y = 1;
//    int x = 0;
//    int result = 0;
//    while (x < 6) {
//      obs[x] = new Puzzleb();
//      obs[x].ivar = y;
//      y = y * 10;
//      x = x + 1;
//    }
//
//    x = 6;
//    while (x > 0) {
//      x = x -1;
//      result = result + obs[x].doStuff(x);
//    }
//
//    System.out.println("Результат " + result);
//  }
//}
//
//class Puzzleb {
//  int ivar;
//  public int doStuff (int factor) {
//    if (ivar > 100) {
//      return ivar * factor;
//    }else {
//      return ivar * (5 - factor);
//    }
//  }
//}

// ------------------- Test page 149
//class MultiFor {
//  public static void main (String[] args) {
//    for(int x = 0; x < 4; x++) {
//      for(int y = 4; x < 4; x++) {
//        System.out.println(x + " " + y);
//      }
//
//      if (x == 1) {
//        x++;
//      }
//    }
//  }
//}


// ------------------- Игра морско бой -------------------
import javax.crypto.AEADBadTagException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


// ------------------- Версия 1
//class SimpleDotComTestDrive {
//  public static void main (String[] args) throws IOException {
////  Создаем переменную чтобы следить за количеством ходов пользователя
//    int numOfGuesses = 0;
////  Это специальны класс, который содержит метод для приема пользовательского ввода
//    GameHelper helper = new GameHelper();
//
////  Создаем экземляр класса SimpleDotCom
//    SimpleDotCom testDotCom = new SimpleDotCom();
//
////  Генерируем случаные число для первой ячейки и спользуем его для формирования ячеек
//    int randomNum = (int) (Math.random() * 5);
//
//
////  Создаем массив для местоположения "сайта"
//    int[] locations = {randomNum, randomNum+1, randomNum+2};
//
////  Вызываем сеттер "сайта"
//    testDotCom.setLocationCells(locations);
//
////  Создаем булеву переменную, чтобы проверять в цикле, не закончилась ли игра
//    boolean isAlive = true;
//
//    while (isAlive) {
//      String guess = helper.getUserInput("Введите число");
//
//      String result = testDotCom.checkYourself(guess);
//
//      numOfGuesses++;
//
//      if(result.equals("Потопил")) {
//        isAlive = false;
//
//        System.out.println("Вам потребовалось " + numOfGuesses + " попыток(и)");
//      }
//    }
//  }
//}
//
//class GameHelper {
//  public String getUserInput(String prompt) throws IOException {
//    String inputLine = null;
//
//    System.out.print(prompt + " ");
//
//    try {
//      BufferedReader is = new BufferedReader(new InputStreamReader(System.in));
//
//      inputLine = is.readLine();
//
//      if (inputLine.length() == 0) return null;
//    } catch (IOException e) {
//      System.out.println("IOException: " + e);
//    }
//
//    return inputLine;
//  }
//}
//
//class SimpleDotCom {
//  int[] locationCells;
//  int numberOfHits = 0;
//
//  public void setLocationCells(int[] locs) {
//    locationCells = locs;
//  }
//
//  public String checkYourself(String stringGuess) {
//    int guess = Integer.parseInt(stringGuess);
//
//    String result = "Мимо";
//
//    for(int cell: locationCells) {
//      if (guess == cell) {
//        result = "Попал";
//        numberOfHits++;
//        break;
//      }
//    }
//
//    if (numberOfHits == locationCells.length) {
//      result = "Потопил";
//    }
//
//    System.out.println(result);
//    return result;
//  }
//}


// ------------------- Версия 2
//class DotComBust {
//  private GameHelper helper = new GameHelper();
//  private ArrayList<DotCom> dotComsList = new ArrayList<DotCom>();
//  private int numOfGuesses = 0;
//
//  private void setUpGame () throws IOException {
////  Создадим несколько "сайтов" и присвоим им адреса
//    DotCom one = new DotCom();
//    one.setName("Pets.com");
//    DotCom two = new DotCom();
//    two.setName("eToys.com");
//    DotCom three = new DotCom();
//    three.setName("Go2.com");
//
//    dotComsList.add(one);
//    dotComsList.add(two);
//    dotComsList.add(three);
//
//    System.out.println("Ваша цель - потопить три 'сайта'");
//    System.out.println("Pets.com, eToys.com, Go2.com");
//    System.out.println("Попытайтесь потопить их за минимальное количество ходов");
//
//    for (DotCom dotComToSet: dotComsList) {
//      ArrayList<String> newLocation = helper.placeDotCom(3);
//      dotComToSet.setLocationCells(newLocation);
//    }
//  }
//
//  private void startPlaying () throws IOException {
//    while(!dotComsList.isEmpty()) {
//      String userGuess = helper.getUserInput("Сделайте ход");
//      checkUserGuess(userGuess);
//    }
//
//    finishGame();
//  }
//
//  private  void checkUserGuess (String userGuess) {
//    numOfGuesses++;
//
//    String result = "Мимо";
//
//    for(DotCom dotComToTest: dotComsList) {
//      result = dotComToTest.checkYourself(userGuess);
//
//      if(result.equals("Попал")) {
//        break;
//      }
//
//      if (result.equals("Потопил")) {
//        dotComsList.remove(dotComToTest);
//        break;
//      }
//    }
//
//    System.out.println(result);
//  }
//
//  private void finishGame() {
//    System.out.println("Все 'сайты' ушли ко дну! Ваши акции теперь ничего не стоят.");
//
//    if (numOfGuesses <= 18) {
//      System.out.println("Это заняло у вас всего " + numOfGuesses + " попыток.");
//      System.out.println("вы успели выбраться до того, как ваши вложения утонули.");
//    }else {
//      System.out.println("Это заняло у вас довольно много времени. " + numOfGuesses + " попыток.");
//      System.out.println("Рыбы водят хороводы вокруг ваших вложений.");
//    }
//  }
//
//  public static void main (String[] args) throws IOException {
//    DotComBust game = new DotComBust();
//    game.setUpGame();
//    game.startPlaying();
//  }
//}
//
//class GameHelper {
//  private static final String alphabet = "abcdefg";
//  private int gridLength = 7;
//  private int gridSize = 49;
//  private int [] grid = new int[gridSize];
//  private int comCount = 0;
//
//  public String getUserInput(String prompt) throws IOException {
//    String inputLine = null;
//
//    System.out.print(prompt + " ");
//
//    try {
//      BufferedReader is = new BufferedReader(new InputStreamReader(System.in));
//
//      inputLine = is.readLine();
//
//      if (inputLine.length() == 0) return null;
//    } catch (IOException e) {
//      System.out.println("IOException: " + e);
//    }
//
//    return inputLine;
//  }
//
//  public ArrayList<String> placeDotCom(int comSize) {
//    ArrayList<String> alphaCells = new ArrayList<String>();
//    String [] alphacoords = new String [comSize]; // Хранит координаты типы f6
//    String temp = null; //Временная строка для конкатенации
//    int [] coords = new int[comSize]; // Координаты текущего сайта
//    int attempts = 0; // Счетчик текущих попыток
//    boolean success = false; // Нашли подходящее местолопожение?
//    int location = 0; // Текущее начальное местоположение
//
//    comCount++; // энный сайта для размещения
//    int incr = 1; // Устанавливаем горизонтальны инкремент
//
//    if ((comCount % 2) == 1) { // Если нечетный - размещаем вертикально
//      incr = gridLength; // Устанавливаем вертикальны инкремент
//    }
//
//    while (!success & attempts++ < 200) { // Главный поисковый цикл
//      location = (int) (Math.random() * gridSize); // Получаем случайную стратовую строку
//
//      System.out.print("пробуем" + location);
//
//      int x = 0; // Энная позиция в сайте, который нужно разместить
//      success = true; // Предполагаем успешны исход
//      while (success && x < comSize) { // Ищем соседнюю неиспользованную ячейку
//        if (grid[location] == 0) { // Если еще не используется
//          coords[x++] = location; // Сохраняем местоположение
//          location += incr; // Попробуем следующую соседнюю ячейку
//
//          if (location >= gridSize) { // Вышли за рамки - низ
//            success = false; // Неудача
//          }
//
//          if (x > 0 && (location % gridLength == 0)) { // вышли за рамки - правый край
//            success = false; // Неудача
//          }
//        } else { // Нашли уже использующееся положение
//          System.out.print("используется" + location);
//
//          success = false; // Неудача
//        }
//      }
//    }
//
//    int x = 0;
//    int row = 0; // Переводим местоположение в символьные координаты
//    int column = 0;
//
//    System.out.println("\n");
//    while (x < comSize) {
//      grid[coords[x]] = 1; // Помечаем ячейки на главной сетке как использованные
//      row = (int) (coords[x] / gridLength); // Получаем значение строки
//      column = coords[x] % gridLength; // Получаем числовое значение столбца
//      temp = String.valueOf(alphabet.charAt(column)); // Преобразуем его в строковой символ
//
//      alphaCells.add(temp.concat(Integer.toString(row)));
//      x++;
//
//      System.out.print("  coord" + x + " = " + alphaCells.get(x - 1));
//    }
//
//    System.out.println("\n");
//
//    return alphaCells;
//  }
//}
//
//class DotCom {
//  private String name;
//  private ArrayList<String> locationCells;
//
//  public void setLocationCells(ArrayList<String> loc) {
//    locationCells = loc;
//  }
//
//  public void setName(String n) {
//    name = n;
//  }
//
//  public String checkYourself(String userInput) {
//    String result = "Мимо";
//
////  Проверяем содержится ли загаданная пользователем ячейка внутри ArrayList
////  Если ее нет в списке, то indexOf вернет -1
//    int index = locationCells.indexOf(userInput);
//
//    if(index >= 0) {
//      locationCells.remove(index);
//
//      if (locationCells.isEmpty()) {
//        result = "Потопил";
//      } else {
//        result = "Попал";
//      }
//    }
//    return result;
//  }
//}


// ------------------- Test page 191
//class ArrayListMagnet {
//  public static void main (String[] args) {
//    ArrayList<String> a = new ArrayList<String>();
//
//    a.add(0, "ноль");
//    a.add(1, "один");
//    a.add(2, "два");
//    a.add(3, "три");
//    printAL(a);
//
//    if (a.contains("три")) {
//      a.add("четыре");
//    }
//
//    a.remove(2);
//
//    printAL(a);
//
//    if (a.indexOf("четыре") != 4) {
//      a.add(4, "4.2");
//    }
//    printAL(a);
//    printAL(a);
//  }
//
//  public static void printAL(ArrayList<String> al) {
//    for (String element: al) {
//      System.out.print(element + " ");
//    }
//
//    System.out.println(" ");
//  }
//}


// ------------------- Test page 224
 class TestBoats {
  public static void main (String[] args) {
    Boat b1 = new Boat();
    Sailboat b2 = new Sailboat();
    Rowboat b3 = new Rowboat();
    b2.setLength(32);
    b1.move();
    b3.move();
    b2.move();
  }
}

class Boat {
  private int length;

  private int getLength(){
    return length;
  }

  public void setLength (int len) {
    length = len;
  }

  public void move(){
    System.out.print("Дрейф");
  }
}

class Sailboat extends Boat {
   public void rowTheBoat () {
     System.out.print("Дава, Наташа!");
   }
}

class Rowboat extends Boat {
   public void move() {
     System.out.print("Поднять паруса!");
   }
}

