
+ [1. Проблема несоответствия]()

## Проблема несоответствия

### Проблема детализации
Есть таблица `User`, в которую необходимо добавить информацию об адресе
Тут встает проблема:
- Добавить несколько столбцов (город, улица, номер дома и т.д.)
- Добавить один столбец

Хотелось бы иметь возможность создать новый тип в рамках таблице, как например:
```sql
create table USER (
    USERNAME varchar(15) not null primary key,
    ADDRESS address not null
)
```

Вариат решения:
```sql
create table USERS (
    USERNAME varchar(15) not null primary key,
    ADDRES_STREET varchar(255) not null,
    ADDRESS_ZIPCODE varchar(5) not null,
    ADDRESS_CITY varchar(255) not null
)
```

### Проблема подтипов
Мы хотим иметь возможность оплаты несколькими способами.
У нас есть суперкласс BilingDetails и его наследники CreditCard, BandAccount

Базы данных как правило не поддерживают табличного наследования


### Проблема идентичности
В Java определяется два понятия тождественности:
- Идентичность экземпляров (a == b)
- Равенство экземпляров, определяемое методом equals()

В базе данных идентичность определяется при помощи сравнения первичных ключей
Поэтому желательно всегда иметь скрытый идентификатор, при помощи которого будет просиходит сопоставление
```sql
CREATE table USERS (
    ID bigint ot null primaty key,
    USERNAME varchar(15) not null unique
   ...
)
```

### Проблемы, связанные с ассоциациями
В объектно-ориентированных языках ассоциации представлены объектными ссылками

Реализация многие ко многим
```java
public class User {
  Set billingDetails;
}

public class BillingDetails {
  Set users;
}
```

Для реализации многие ко многим в БД необходима создать новую таблицу ссылок
```sql
create table USER_BILLINGDETAILS (
    USER_ID bigint,
    BILLINGDETAILS_ID bigint,
    primary key (USER_ID, BILLINGDETAILS_ID),
    foreign key (USER_ID) references USERS,
    foreign key (BILLINGDETAILS_ID) references BILLINGDETAILS
)
```

### Проблема навигации по данным
Для навигации по данным в Java используется iterator

Для последовательного извлечения данных из бд необходимо делать множество запросов, что не оптимально
Именно из-за данной проблемы появляется проблема n+1