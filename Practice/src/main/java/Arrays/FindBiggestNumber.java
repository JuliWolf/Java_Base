package Arrays;

public class FindBiggestNumber {
  public static void main(String[] args) {
    int[] integers = {1, 2, 3, 4, 12, 2, 1, 4};

    findBiggestNumber(integers);
  }

  public static void findBiggestNumber (int[] arr) {
    int nge;
    int length = arr.length;

    for (int i = 0; i < length; i++) {
      nge = -1;

      for (int j = i + 1; j < length; j++) {
        if (arr[i] < arr[j]) {
          nge = arr[j];
          break;
        }
      }

      System.out.println(arr[i] + " : " + nge);
    }
  }
}
