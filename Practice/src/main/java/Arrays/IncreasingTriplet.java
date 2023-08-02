package Arrays;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JuliWolf
 * @date 01.08.2023
 */
public class IncreasingTriplet {
  public static void main(String[] args) {
    System.out.println(increasingTriplet(new int[]{0, 4, 2, 1, 0, -1, -3}));
//    System.out.println(increasingTriplet(new int[]{20, 100, 10, 12, 5, 13}));
  }

  public static boolean increasingTriplet(int[] nums) {
    int max1 = Integer.MAX_VALUE;
    int max2 = Integer.MAX_VALUE;
    for(int n : nums) {
      if(n <= max1) max1 = n;
      else if(n <= max2) max2 = n;
      else return true;
    }
    return false;
  }
}
