package Objects.requireNonNull;

import java.awt.*;
import java.util.Objects;

public class Car {
  private final String name;
  private final Color color;


  public Car(String name, Color color) {
    // Objects.requireNonNull заменяет следующую конструкцию
//    if (name == null) {
//      throw new NullPointerException("Имя автомобиля не может быть null");
//    }
    this.name = Objects.requireNonNull(name, "Имя автомобиля не может быть null");
    this.color = Objects.requireNonNull(color, "Цвет автомобиля не может быть null");
  }

  public void assignDriver (String licence, Point location) {
    Objects.requireNonNull(licence, "Лицензия не может быть null");
    Objects.requireNonNull(location, "Местоположение не может быть null");
  }
}
