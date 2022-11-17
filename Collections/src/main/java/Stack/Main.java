package Stack;

import java.util.Stack;

public class Main {
  public static void main(String[] args) {
    Stack<Integer> stack = new Stack<>();
    // 5 <- 3 <- 1

    stack.push(5);
    stack.push(3);
    stack.push(1);

    System.out.println(stack.peek()); // 1

    // Извлекает последний добавленный элемент
//    System.out.println(stack.pop());
//    System.out.println(stack.pop());
//    System.out.println(stack.pop());

    stack.empty();

    while(!stack.isEmpty()) {
      System.out.println(stack.pop());
    }
  }
}
