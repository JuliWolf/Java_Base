package Arrays;

import java.util.Arrays;

/**
 * @author JuliWolf
 * @date 02.08.2023
 */
public class MoveZeros {
  public static void main(String[] args) {
    int[] ints = {0, 1, 0, 3, 12};

    moveZeroes(ints);

    System.out.println(Arrays.toString(Arrays.stream(ints).toArray()));
  }

  public static void moveZeroes(int[] nums) {
    for (int i = 0; i < nums.length; i++) {
      if (nums[i] != 0) continue;

      if (nums.length - 1 == i) continue;

      int currentInt = nums[i];
      int nextNotZeroIndex = findClosestNotZeroValue(nums, i+1);

      if (nextNotZeroIndex == -1) return;

      nums[i] = nums[nextNotZeroIndex];
      nums[nextNotZeroIndex] = currentInt;
    }
  }

  public static int findClosestNotZeroValue (int[] nums, int startIndex) {
    for (int i = startIndex; i < nums.length; i++) {
      if (nums[i] != 0) {
        return i;
      }
    }

    return -1;
  }

  public static void moveZeroesRecommended(int[] nums) {
    int i = 0;
    for (int num:nums){
      if(num != 0){
        nums[i] = num;
        i++;
      }
    }
    while(i<nums.length){
      nums[i] = 0;
      i++;
    }
  }
}
