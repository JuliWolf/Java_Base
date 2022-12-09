package Objects.requireNonNullElse;

import java.awt.*;
import java.util.Objects;

public class Car {
  private final String name;
  private final Color color;

  public String getName() {
    return name;
  }

  public Color getColor() {
    return color;
  }

  public Car(String name, Color color) {
    // Objects.requireNonNullElse заменяет следующую конструкцию
//    if (name == null) {
//      this.name = "No name";
//    }
    this.name = Objects.requireNonNullElse(name, "No name");
    this.color = Objects.requireNonNullElse(color, new Color(0,0,0));
  }
}
