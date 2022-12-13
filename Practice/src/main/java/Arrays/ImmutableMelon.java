package Arrays;

import java.util.Objects;

public final class ImmutableMelon {
  private String type;
  private int weight;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public int getWeight() {
    return weight;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  public ImmutableMelon() {
  }

  public ImmutableMelon(String type, int weight) {
    this.type = type;
    this.weight = weight;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ImmutableMelon)) return false;
    ImmutableMelon that = (ImmutableMelon) o;
    return weight == that.weight && Objects.equals(type, that.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, weight);
  }
}
