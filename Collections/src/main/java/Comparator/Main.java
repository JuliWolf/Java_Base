package Comparator;

import java.util.*;

public class Main {

  public static void main(String[] args) {
    List<Person> peopleList = new ArrayList<>();
    Set<Person> peopleSet = new TreeSet<>();

    addElements(peopleSet);
    addElements(peopleList);

    System.out.println(peopleSet);
    System.out.println(peopleList);
  }

  private static void addElements (Collection collection) {
    collection.add(new Person(2, "To"));
    collection.add(new Person(1, "Bob"));
    collection.add(new Person(3, "Katy"));
    collection.add(new Person(4, "George"));
  }
}

class Person implements Comparable<Person> {
  private int id;
  private String name;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Person(int id, String name) {
    this.id = id;
    this.name = name;
  }

  @Override
  public String toString() {
    return "Person{" +
        "id=" + id +
        ", name='" + name + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Person)) return false;
    Person person = (Person) o;
    return Objects.equals(name, person.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public int compareTo(Person person) {
    if (this.name.length() > person.getName().length()) {
      return 1;
    } else if(this.name.length() < person.getName().length()) {
      return -1;
    } else {
      return 0;
    }
  }
}
