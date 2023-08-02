package Arrays;

import java.util.Arrays;

/**
 * @author JuliWolf
 * @date 01.08.2023
 */
public class ProductExpectItself {
  public static void main(String[] args) {
    productExceptSelf(new int[]{1,2,3,4});
  }

  public static int[] productExceptSelf(int[] nums) {
    int n = nums.length;
    int ans[] = new int[n];
    Arrays.fill(ans, 1);
    int curr = 1;
    for(int i = 0; i < n; i++) {
      // 3 * 1 = 3
      // 3 * 1 = 1
      ans[i] *= curr;
      curr *= nums[i];
    }
    curr = 1;
    for(int i = n - 1; i >= 0; i--) {
      ans[i] *= curr;
      curr *= nums[i];
    }
    return ans;
  }
}
