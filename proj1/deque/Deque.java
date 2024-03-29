package deque;

public interface Deque<T> extends Iterable<T> {
    void addFirst(T item);
    void addLast(T item);
//    boolean isEmpty();
    int size();
    void printDeque();
    T removeFirst();
    T removeLast();
    T get(int index);

    default boolean isEmpty() {
        return size() == 0;
    }
}
