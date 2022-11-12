package LinkedList;

import java.util.Arrays;

public class MyLinkedList {
  private Node head;

  private int size;

  public void add (int value) {
    // if first element
    if (head == null) {
      this.head = new Node(value);
    } else {
      Node temp = head;

      while (temp.getNext() != null) {
        temp = temp.getNext();
      }

      temp.setNext(new Node(value));
    }

    size++;
  }

  public int get (int index) {
    int currentIndex = 0;
    Node temp = head;

    while (temp != null) {
      if (currentIndex == index) {
        return temp.getValue();
      }

      temp = temp.getNext();
      currentIndex++;
    }

    throw new IllegalArgumentException();
  }

  public void remove (int index) {
    if (index == 0) {
      head = head.getNext();
      size--;
      return;
    }

    int currentIndex = 0;
    Node temp = head;

    while (temp != null) {
      if (currentIndex == index - 1) {
        temp.setNext(temp.getNext().getNext());

        size--;
        return;
      }

      temp = temp.getNext();
      currentIndex++;
    }

    throw new IllegalArgumentException();
  }

  public String toString () {
    int[] result = new int[size];

    int idx = 0;
    Node temp = head;

    while (temp != null) {
      result[idx++] = temp.getValue();
      temp = temp.getNext();
    }

    return Arrays.toString(result);
  }

  private static class Node {
    private Node next;
    private int value;

    public Node getNext() {
      return next;
    }

    public void setNext(Node next) {
      this.next = next;
    }

    public int getValue() {
      return value;
    }

    public void setValue(int value) {
      this.value = value;
    }

    public Node(int value) {
      this.value = value;
    }
  }
}
