package Objects.requireNonNullElse;

public class Main {
  public static void main(String[] args) {
    Car car  = new Car(null, null);
    System.out.println(car.getColor());
    System.out.println(car.getName());
  }
}
