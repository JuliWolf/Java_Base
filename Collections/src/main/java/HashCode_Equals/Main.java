package HashCode_Equals;

import java.util.*;

public class Main {
  public static void main(String[] args) {
//    Map<Integer, String> map = new HashMap<>();
//    Set<Integer> set = new HashSet<>();
//
//    map.put(1, "Один");
//    map.put(1, "Другое значение");
//
//    set.add(1);
//    set.add(1);
//
//    System.out.println(map);
//    System.out.println(set);

    Map<Person, String> map = new HashMap<>();
    Set<Person> set = new HashSet<>();

    Person person1 = new Person(1, "Mike");
    Person person2 = new Person(1, "Mike");

    map.put(person1, "123");
    map.put(person2, "123");

    set.add(person1);
    set.add(person2);

    System.out.println(map);
    System.out.println(set);
  }
}

class Person {
  private int id;
  private String name;

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
    return id == person.id && Objects.equals(name, person.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name);
  }
}
