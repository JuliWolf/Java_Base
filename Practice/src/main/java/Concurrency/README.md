# Потокобезопасные коллекции, четки, очереди

## Списки

| Потоконебезопасный | Потокобезопсный                                         |
|--------------------|---------------------------------------------------------|
| ArrayList          | CopyOrWhiteArrayList (частые чтения, редкие обновления) |
| LinkedList         | Vector (deprecated)                                     |


## Множество

| Потоконебезопасный | Потокобезопсный                                         |
|--------------------|---------------------------------------------------------|
| HashSet            | CopyOrWhiteArraySet (частые чтения, редкие обновления)  |
| TreeSet            | CopyOrWhiteArraySet (частые чтения, редкие обновления)  |
| LinkedHashSet      | CopyOrWhiteArraySet (частые чтения, редкие обновления)  |
| BitSet             | CopyOrWhiteArraySet (частые чтения, редкие обновления)  |
| EnumSet            | CopyOrWhiteArraySet (частые чтения, редкие обновления)  |


## Map

| Потоконебезопасный | Потокобезопсный                                   |
|--------------------|---------------------------------------------------|
| HashMap            | ConcurrentHashMap                                 |
| TreeMap            | ConcurrentSkipListMap (сортированное отображение) |
| LinkedHashMap      | HashTable                                         |
| IdentityHashMap    |                                                   |
| WeakHashMap        |                                                   |
| EnumMap            |                                                   |


## Очередь

| Потоконебезопасный                       | Потокобезопсный                                |
|------------------------------------------|------------------------------------------------|
| ArrayDeque                               | ArrayBlockingQueue (ограниченная)              |
| PriorityQueue (Сортированные извлечения) | ConcurrentLinkedQueue (неограниченная)         |
|                                          | ConcurrentLinkedDeque (неограниченная)         |
|                                          | LinkedBlockingQueue (опционально ограниченная) |
|                                          | LinkedBlockingDeque (опционально ограниченная) |
|                                          | LinkedTransferQueue                            |
|                                          | PriorityBlockingQueue                          |
|                                          | SynchronousQueue                               |
|                                          | DelayQueue                                     |
|                                          | Stack                                          |

## Синхронизированные коллекции

1. `synchronizedCollection(Collection<T> c)` - возвращает синхронизированную коллекцию, поддерживаемую заданной коллекцией
2. `synchronizedList (List<T> list)` - возвращает синхронизированные список, пожжерэиваемый заданным кодом
3. `synchronizedMap (Map<K, V> m)` - возвращает синхронизированное отображение, поддерживаемое заданным отображением
4. `synchronizedSet(Set<T> s)` - возвращает синхронизированное множествоа, поддерживаемое заданным множеством
5. 