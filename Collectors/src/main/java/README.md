# Collectors

Класс `Collectors` используется для манипуляций с коллекциями
С помощью класса можно поменять структуру коллекции, отфильтровать, разделить по определенному принципу и т.д.

## Данные для примеров

```
public class City {

  // Initializing the properties
  // of the city class
  private String name;
  private double temperature;

  // Parameterized constructor to
  // initialize the city class
  public City(String name, double temperature)
  {
    this.name = name;
    this.temperature = temperature;
  }

  // Getters to get the name and
  // temperature
  public String getName()
  {
    return name;
  }

  public Double getTemperature()
  {
    return temperature;
  }

  // Overriding the toString method
  // to return the name and temperature
  @Override
  public String toString()
  {
    return name + " --> " + temperature;
  }
}
```

```
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
```

## Преобразование листа

1. Отфильтровать все значения, где температура большше 10
2. Выбрать только названия городов
3. Вернуть список городов

```
System.out.println(prepareCities().stream()
    .filter(city -> city.getTemperature() > 10)
    .map(city -> city.getName())
    .collect(Collectors.toList())
);
```
Результат `[New Delhi, Mexico, New York, Dubai, London, Kolkata, Sydney, Mexico, Dubai]`

4. Взять только уникальные названия городов
```
System.out.println(prepareCities().stream()
    .filter(city -> city.getTemperature() > 10)
    .map(city -> city.getName())
    .collect(Collectors.toSet())
);
```
Результат `[New York, New Delhi, London, Mexico, Kolkata, Dubai, Sydney]`


1. Вывести названи городов из листа

```
List<String> list = prepareCities().stream()
    .map(city -> city.getName())
    .collect(Collectors.toList());
System.out.println(list);
```
Результат `[New Delhi, Mexico, New York, Dubai, London, Alaska, Kolkata, Sydney, Mexico, Dubai]`


## Изменить тип структуры

`List<City>` -> `List<String>`

```
System.out.println(
    prepareCities().stream()
        .filter(city -> city.getTemperature() > 10)
        .collect(Collectors.toMap(City::getName, City::getTemperature, (key, identicalKey) -> key))
);
```
Результат `{New York=13.0, New Delhi=33.5, London=15.0, Mexico=14.0, Kolkata=30.0, Dubai=43.0, Sydney=11.0}`


## Группировка по значению

1. Группируем по названию
2. Считаем сколько одиннаковых названий

** `collectingAndThen` используется для дополнительного изменения коллекции после действия
```
System.out.println(
    prepareCities().stream()
        .collect(Collectors.groupingBy(
            City::getName,
            Collectors.collectingAndThen(
                Collectors.counting(),
                Long::intValue
            )
        ))
);
```
Результат `{New York=1, New Delhi=1, London=1, Alaska=1, Mexico=2, Kolkata=1, Dubai=2, Sydney=1}`


3. Сгруппировать по названию 
Изменится структура на `Map<String, List>`

```
System.out.println(
    prepareCities().stream()
        .collect(Collectors.groupingBy(City::getName))
);
```
Результат `{New York=[New York --> 13.0], New Delhi=[New Delhi --> 33.5], London=[London --> 15.0], Alaska=[Alaska --> 1.0], Mexico=[Mexico --> 14.0, Mexico --> 14.0], Kolkata=[Kolkata --> 30.0], Dubai=[Dubai --> 43.0, Dubai --> 43.0], Sydney=[Sydney --> 11.0]}`


## Объединение, joining

1. Вывести строку со всеми названиями

```
System.out.println(
    prepareCities().stream()
        .filter(city -> city.getTemperature() > 10)
        .map(City::getName)
        .collect(Collectors.joining(", "))
);
```
Результат `New Delhi, Mexico, New York, Dubai, London, Kolkata, Sydney, Mexico, Dubai`


## Изменить структуру с `List<City>` -> `Map<String, List>`

```
System.out.println(
    prepareCities().stream()
        .collect(Collectors.groupingBy(
            City::getName,
            Collectors.mapping(
                City::getTemperature,
                Collectors.toList()
            )
        ))
);
```
Результат `{New York=[13.0], New Delhi=[33.5], London=[15.0], Alaska=[1.0], Mexico=[14.0, 14.0], Kolkata=[30.0], Dubai=[43.0, 43.0], Sydney=[11.0]}`


## Разделить на группы

1. Разделить на 2 группы по температуре
2. Возвращается структура `Map<Boolean, List>`
```
System.out.println(
    prepareCities().stream()
        .collect(Collectors.partitioningBy(
            city -> city.getTemperature() > 15
        ))
);
```
Результат `{false=[Mexico --> 14.0, New York --> 13.0, London --> 15.0, Alaska --> 1.0, Sydney --> 11.0, Mexico --> 14.0], true=[New Delhi --> 33.5, Dubai --> 43.0, Kolkata --> 30.0, Dubai --> 43.0]}`
