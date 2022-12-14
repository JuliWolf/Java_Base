package Arrays;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ComputeMap {
  public static void main(String[] args) throws UnknownHostException {
    Map<String, String> mapPresent = new HashMap<>();
    mapPresent.put("postgresql", "127.0.0.1");
    mapPresent.put("mysql", "192.168.0.50");

    // k -> ключ из отображения, v -> значение, ассоциированное с ключом
    BiFunction<String, String, String> jdbcUrl = (k, v) -> "jdbc:" + k + "://" + v + "/customers_db";

    /* computeIfPresent -> вычислить что-то, если значение присутствует */
    String mysqlJdbcUrl = mapPresent.computeIfPresent("mysql", jdbcUrl);
    System.out.println(mysqlJdbcUrl); // jdbc:mysql://192.168.0.50/customers_db

    String voltDbJdbcUrl = mapPresent.computeIfPresent("voltdb", jdbcUrl);
    System.out.println(voltDbJdbcUrl); // null

    /* computeIfAbsent -> вычислить что-то, если значения нет */

    Map<String, String> mapAbsent = new HashMap<>();
    mapAbsent.put("postgresql", "jdbc:postgresql://127.0.0.1/customers_db");
    mapAbsent.put("mysql", "jdbc:mysql://192.168.0.50/customers_db");

    String address = InetAddress.getLocalHost().getHostAddress();
    System.out.println("address: " + address);
    Function<String, String> jdbcUrlAbsent = k -> k + "://" + address + "/customers_db";

    String mongodbJdbcUrl = mapAbsent.computeIfAbsent("mongodb", jdbcUrlAbsent);
    System.out.println(mongodbJdbcUrl); // mongodb://91.230.208.137/customers_db

    /* compute -> метод для вычисления значения */
    // для избежания ошибок нужно обработать кейс с null
    BiFunction<String, String, String> jdbcUrlCompute = (k, v) -> "jdbc:" + k + "://" + ((v == null) ? address : v) + "/customers_db";

//    TODO: check what happen
    String mysqlJdbcUrl1 = mapPresent.compute("mysql", jdbcUrlCompute);
    System.out.println(mysqlJdbcUrl1); // jdbc:mysql://jdbc:mysql://192.168.0.50/customers_db/customers_db ?????

    String derbyJdbcUrl = mapPresent.compute("derby", jdbcUrlCompute);
    System.out.println(derbyJdbcUrl); // jdbc:derby://91.230.208.137/customers_db

    System.out.println(mapPresent); // {postgresql=127.0.0.1, derby=jdbc:derby://91.230.208.137/customers_db, mysql=jdbc:mysql://jdbc:mysql://192.168.0.50/customers_db/customers_db}

    /* Merge -> метод для склейки значений */
    Map<String, String> mapMerge = new HashMap<>();
    mapMerge.put("postgresql", "9.6.1");
    mapMerge.put("mysql", "5.1 5.2 5.6");

//    BiFunction<String, String, String> jdbcUrlMerge = String::concat;
    BiFunction<String, String, String> jdbcUrlMerge = (vold, vnew) -> vold.concat(vnew);

    String mySqlVersion = mapMerge.merge("mysql", "8.0 ", jdbcUrlMerge);
    System.out.println(mySqlVersion); // 5.1 5.2 5.68.0

    String derbyVersion = mapMerge.merge("derby", "10.11.1.1 ", jdbcUrlMerge);
    System.out.println(derbyVersion); // 10.11.1.1

    System.out.println(mapMerge); // {postgresql=9.6.1, derby=10.11.1.1 , mysql=5.1 5.2 5.68.0 }

    /* putIfAbsent -> положить элемент если нет в коллекции */
    // возвращает null если ключа нет
    // возвращает значение, которое уже находится по ключу
    Map<Integer, String> mapPutIfAbsent = new HashMap<>();
    mapPutIfAbsent.put(1, "postgresql");
    mapPutIfAbsent.put(2, "mysql");
    mapPutIfAbsent.put(3, null);

    String v1 = mapPutIfAbsent.putIfAbsent(1, "derby");
    System.out.println(v1); // postgresql
    String v2 = mapPutIfAbsent.putIfAbsent(3, "derby");
    System.out.println(v2); // null
    String v3 = mapPutIfAbsent.putIfAbsent(4, "cassandra");
    System.out.println(v3); // null

    System.out.println(mapPutIfAbsent); // {1=postgresql, 2=mysql, 3=derby, 4=cassandra}
  }
}
