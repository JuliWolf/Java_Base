package Arrays;

/**
 * @author JuliWolf
 * @date 31.07.2023
 */
public class CanPlaceFlowers {
  public static void main(String[] args) {
    int[] flowerbed = new int[]{ 1, 0,1,0,1,0, 1 };
    System.out.println(canPlaceFlowers(flowerbed, 1));
  }

  public static boolean canPlaceFlowers(int[] flowerbed, int n) {
    int seedsLeft = n;

    if (flowerbed.length == 1 && flowerbed[0] == 0) {
      return true;
    }

    for (int i = 0; i < flowerbed.length; i++) {
      int nextInt = flowerbed.length - 1 > i
          ? flowerbed[i + 1]
          : flowerbed[i];

      int prevInt = i == 0
          ? flowerbed[0]
          : flowerbed[i - 1];

      if (
          nextInt == 0 && prevInt == 0 && flowerbed[i] == 0
      ) {
        flowerbed[i] = 1;
        seedsLeft--;
      }
    }

    return seedsLeft <= 0;
  }
}
