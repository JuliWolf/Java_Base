package Arrays;

/**
 * @author JuliWolf
 * @date 04.08.2023
 */
public class ContainerWithMostWater {
  public static void main(String[] args) {
    System.out.println(maxArea(new int[]{1,8,6,2,5,4,8,3,7}));
  }

  public static int maxArea(int[] height) {
    if (height.length == 1) {
      return 0;
    }

    int maxArea = 0;

    for (int i = 0; i < height.length; i++) {
      if (i+1 == height.length) continue;

      int currenStartHeight = height[i];

      for (int j = i + 1; j < height.length; j++) {
        int multiplier = j-i;
        int currentEndHeight = height[j];

        int currentMaxArea = currentEndHeight > currenStartHeight
            ? currenStartHeight * multiplier
            : currentEndHeight * multiplier;

        if (currentMaxArea > maxArea) {
          maxArea = currentMaxArea;
        }
      }

    }

    return maxArea;
  }

  public static int maxAreaOptimized (int[] height) {
    int left = 0;
    int right = height.length - 1;
    int maxArea = 0;

    while (left < right) {
      int currentArea = Math.min(height[left], height[right]) * (right - left);
      maxArea = Math.max(maxArea, currentArea);

      if (height[left] < height[right]) {
        left++;
      } else {
        right--;
      }
    }

    return maxArea;
  }
}
