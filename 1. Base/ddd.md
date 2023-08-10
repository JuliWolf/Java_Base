+ [Общие понятия]()


## Общие понятия

+ [1. Что такое DDD?]()
+ [2. Что такое Sub-domain?]()
+ [3. Типы поддоменов]()
+ [4. Что помогает определить DDD?]()
+ [5. Элементы DDD]()
+ [5.1. Value objects]()


### 1. Что такое Domain?
- Тесла работает в домене электрических двигателей
- Нетфликс предоставляет онлайн фильмы и шоу
- Макдональдс предоставляет еду

### 2. Что такое Sub-domain?
Делит домен на мелкие части для упрощения управления этими частями
- Retail domain
  - products
    - catalog
    - search
    - reviews
  - inventory
  - rewards
  - shopping cart
  - online orders

### 3. Типы поддоменов
- Основные (core) - основной фокус бизнеса
  - для retail
    - shopping carts
    - orders
- Вспомогательные (supporting) - Являются не менее важными для реализации основных, но стоят на втором плане
  - для retail
    - online ordering
    - catalog management
- Общие (generic) - Инструменты, для решения проблемы. Стоят в стороне от основных и вспомогательных

### 4. Что помогает определить DDD?
- Общий язык для домена
- Общие модели домена
- Разбить проблему на части
- Придерживаться установленных правил в течение всего периода разработки

### 5. Элементы DDD
- Ui
- Services
- Repositories - 5.5
- Entries - 5.2
- Value Objects - 5.1
- Aggregates - 5.3
- Domain events - 5.4
- Factories

```
Transaction

id: TransactionId

amount: MonetaryAmount
type: TransactionType
date: Date
status: TransactionStatus

void approved()
void rejected()
```

### 5.1. Value objects
Иммутабельные объекты, которые содержат данные и поведения одного или более атрибутов
- Объекты не содержат свойств идентификации `MonetaryAmount`

### 5.2. Entries
- Объект с уникальным идентификатором. Инкапсулирует данные и поведение атрибутов
- Объекты мутабельны `Transaction`

### 5.3. Aggregates
- Объединяет свойства объекта и связанные с ним сущности
- Инкапусулирует доступ до дочерних сущностей предоставляя команды для управления ими
- Завязано на бизнес-логику

```
CheckingAccount

primaryHolder: AccountHolder (entry)
secondaryHolders: Collection<AccountHolder> (entry)
currentBalance: MonetaryAmount (value)
openingDate: Date
status: Boolean
transactions: Collection<Transaction> (entry)

void tryWithdraw()
void tryDeposit()
void addSecondaryHolder()
```
1. ТОлько клиенты high-net-worth individuals(HNI) могут иметь отрицательный баланс
2. Каждое изменение баланса должно происходить через `Transaction` для аудита
3. `Transaction` предоставляет методы `approve` и `reject` 
```
class CheckingAccount {
    private AccountHolder primaryHolder;
    private Collection<Transaction> transtactions;
    private MonetaryAmount currentBalance;
    
    ...
    
    void tryWithdraw (MonetaryAmount amount) {
        MonetaryAmount newBalance = currentBalance.substract(amount);
        Transaction transaction = add(Transaction.withdrawal(id, amount));
        
        if (primaryHolder.isNotHNI() && newBalance.isOverdrawn()) {
            transaction.rejected();
        } else {
            transaction.approved();
            currentBalance = newBalance;
        }
    }
}
```

### 5.4. Domain events
- События, которые оповещают систему о некоторых важных изменениях в состоянии
  - для retail
    - создан депозит
    - подозрение на мошенническая операцию

### 5.5. Repositories
- Задача выгрузить объект со связанными с ним данными

### 5.6. Factories
Из-за того что данные при аггрегации могут разрастаться, логично делегировать логику создания таких объектов фабрике
- фабричный метод
- билдер
- инъекция зависимостей

### 5.7. Services
Когда необходимо аркестрировать взаимодействие нескольких агрегаторов
- Domain service - взаимодействие между более чем одним агрегатором
- Infrastructure services - взаимодействие внутри утилитами, которые не являются основными для бизнеса, например, логирование или отправка писем
- Application service - Координация между `domain service`, `Infrastructure services` и другими сервисами, например, отправка письма после успешной транзакции


## END ----------------- Общие понятия -----------------