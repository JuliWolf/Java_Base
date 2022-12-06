import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
  public static void main(String[] args) {
    // The following statement filters
    // cities having temp > 10
    // The map function transforms only
    // the names of the cities
    // The collect function collects the
    // output as a List
    System.out.println(prepareCities().stream()
        .filter(city -> city.getTemperature() > 10)
        .map(city -> city.getName())
        .collect(Collectors.toList())
    ); // [New Delhi, Mexico, New York, Dubai, London, Kolkata, Sydney, Mexico, Dubai]

    // Get unique cities names where temperature is more than 10
    System.out.println(prepareCities().stream()
        .filter(city -> city.getTemperature() > 10)
        .map(city -> city.getName())
        .collect(Collectors.toSet())
    ); // [New York, New Delhi, London, Mexico, Kolkata, Dubai, Sydney]


    // Get new list with only names
    List<String> list = prepareCities().stream()
        .map(city -> city.getName())
        .collect(Collectors.toList());
    System.out.println(list); // [New Delhi, Mexico, New York, Dubai, London, Alaska, Kolkata, Sydney, Mexico, Dubai]

    // Create map structure with name as key and temperature as value
    // Use third argument to avoid key collisions
    System.out.println(
        prepareCities().stream()
            .filter(city -> city.getTemperature() > 10)
            .collect(Collectors.toMap(City::getName, City::getTemperature, (key, identicalKey) -> key))
    ); // {New York=13.0, New Delhi=33.5, London=15.0, Mexico=14.0, Kolkata=30.0, Dubai=43.0, Sydney=11.0}


    // group values

    // 1. Set group by Name
    // 2. set after collect method Collectors.collectingAndThen
    // 3. count values
    System.out.println(
        prepareCities().stream()
            .collect(Collectors.groupingBy(
                City::getName,
                Collectors.collectingAndThen(
                    Collectors.counting(),
                    Long::intValue
                )
            ))
    ); // {New York=1, New Delhi=1, London=1, Alaska=1, Mexico=2, Kolkata=1, Dubai=2, Sydney=1}


    // Group values into Map
    System.out.println(
        prepareCities().stream()
            .collect(Collectors.groupingBy(City::getName))
    ); // {New York=[New York --> 13.0], New Delhi=[New Delhi --> 33.5], London=[London --> 15.0], Alaska=[Alaska --> 1.0], Mexico=[Mexico --> 14.0, Mexico --> 14.0], Kolkata=[Kolkata --> 30.0], Dubai=[Dubai --> 43.0, Dubai --> 43.0], Sydney=[Sydney --> 11.0]}


    // Joining

    // Join city names with ','
    System.out.println(
        prepareCities().stream()
            .filter(city -> city.getTemperature() > 10)
            .map(City::getName)
            .collect(Collectors.joining(", "))
    ); // New Delhi, Mexico, New York, Dubai, London, Kolkata, Sydney, Mexico, Dubai


    // Map List into another structure
    // List<City> -> Map<String, List>
    System.out.println(
        prepareCities().stream()
            .collect(Collectors.groupingBy(
                City::getName,
                Collectors.mapping(
                    City::getTemperature,
                    Collectors.toList()
                )
            ))
    ); // {New York=[13.0], New Delhi=[33.5], London=[15.0], Alaska=[1.0], Mexico=[14.0, 14.0], Kolkata=[30.0], Dubai=[43.0, 43.0], Sydney=[11.0]}


    // Divide list into groups by some rules
    // return Map<Boolean, List>
    System.out.println(
        prepareCities().stream()
            .collect(Collectors.partitioningBy(
                city -> city.getTemperature() > 15
            ))
    ); // {false=[Mexico --> 14.0, New York --> 13.0, London --> 15.0, Alaska --> 1.0, Sydney --> 11.0, Mexico --> 14.0], true=[New Delhi --> 33.5, Dubai --> 43.0, Kolkata --> 30.0, Dubai --> 43.0]}
  }

  public static List<City> prepareCities () {
    List<City> cities = new ArrayList<>();
    cities.add(new City("New Delhi", 33.5));
    cities.add(new City("Mexico", 14));
    cities.add(new City("New York", 13));
    cities.add(new City("Dubai", 43));
    cities.add(new City("London", 15));
    cities.add(new City("Alaska", 1));
    cities.add(new City("Kolkata", 30));
    cities.add(new City("Sydney", 11));
    cities.add(new City("Mexico", 14));
    cities.add(new City("Dubai", 43));
    return cities;
  }
}
