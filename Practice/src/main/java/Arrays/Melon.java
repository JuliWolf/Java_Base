package Arrays;

import java.util.Objects;

public class Melon implements Comparable {
  private final String type;
  private final int weight;

  public String getType() {
    return type;
  }

  public int getWeight() {
    return weight;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Melon)) return false;
    Melon melon = (Melon) o;
    return weight == melon.weight && Objects.equals(type, melon.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, weight);
  }

  public Melon(String type, int weight) {
    this.type = type;
    this.weight = weight;
  }


  @Override
  public int compareTo(Object o) {
    Melon m = (Melon) o;

    return Integer.compare(this.getWeight(), m.getWeight());
  }
}
