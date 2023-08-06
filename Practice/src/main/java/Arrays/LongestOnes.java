package Arrays;

/**
 * @author JuliWolf
 * @date 06.08.2023
 */
public class LongestOnes {
  public static void main(String[] args) {
    System.out.println(longestOnes(new int[]{1,1,1,0,0,0,1,1,1,1,0}, 2));
  }

  public static int longestOnes(int[] nums, int k) {
    int start = 0;
    int nOfZeros = 0;
    int result =0;

    for(int i=0; i < nums.length; i++){

      if (nums[i] == 0) {
        nOfZeros++;
      }

      while(nOfZeros > k){
        if (nums[start] == 0) {
          nOfZeros--;
        }

        start++;
      }

      result = Math.max(i-start+1,result);
    }

    return result;
  }
}
