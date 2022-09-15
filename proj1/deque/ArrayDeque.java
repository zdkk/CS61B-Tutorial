package deque;

import java.util.Iterator;
import java.util.Objects;

/**
 * zdkk
 * 2022-09-14 22:17
 */
public class ArrayDeque<T> implements Deque<T>, Iterable<T> {

	private T[] items;
	private int hh, tt;

	public ArrayDeque() {
		items = (T[]) (new Object[8]);
		hh = tt = 0;
	}

	@Override
	public void addFirst(T item) {
		if (isFull()) {
			resize(items.length * 2);
		}
		hh = (hh + items.length - 1) % items.length;
		items[hh] = item;
	}

	@Override
	public void addLast(T item) {
		if (isFull()) {
			resize(items.length * 2);
		}
		items[tt++] = item;
		if (tt == items.length) {
			tt = 0;
		}
	}


	@Override
	public void printDeque() {
		int i = hh;
		while (i != tt) {
			System.out.print(items[i] + " ");
			i = (i + 1) % items.length;
		}
		System.out.println();
	}

	@Override
	public T removeFirst() {
		if (isEmpty()) {
			return null;
		}
		T res = items[hh];
		hh = (hh + 1) % items.length;
		if (items.length > 16 && items.length / 4 > size()) {
			resize(items.length / 4);
		}
		return res;
	}

	@Override
	public T removeLast() {
		if (isEmpty()) {
			return null;
		}
		tt = (tt + items.length - 1) % items.length;
		T res = items[tt];
		if (items.length > 16 && items.length / 4 > size()) {
			resize(items.length / 4);
		}
		return res;
	}

	@Override
	public T get(int idx) {
		if (idx < 0 || idx >= size()) {
			return null;
		}
		return items[(hh + idx) % items.length];
	}

	public Iterator<T> iterator() {
		return new Iterator<>() {
			int cur = hh;
			@Override
			public boolean hasNext() {
				return cur != tt;
			}

			@Override
			public T next() {
				T res = items[cur];
				cur = (cur + 1) % items.length;
				return res;
			}
		};
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Deque)) {
			return false;
		}
		boolean equal = (o.getClass() == ArrayDeque.class)
				? equalsArrayDeque((ArrayDeque<?>) o)
				: equalsRange((Deque<?>) o);

		return equal;
	}

	private boolean equalsRange(Deque<?> other) {
		if (size() != other.size()) {
			return false;
		}
		boolean equal = true;
		Iterator<T> a = iterator();
		Iterator<?> b = other.iterator();
		while (a.hasNext() && b.hasNext()) {
			if (!Objects.equals(a.next(), b.next())) {
				equal = false;
				break;
			}
		}
		return equal && !a.hasNext() && !b.hasNext();
	}

	private boolean equalsArrayDeque(ArrayDeque<?> other) {
		final int s = other.size();
		if (this.size() != s) {
			return false;
		}
		boolean equal = true;
		Iterator<T> a = iterator();
		Iterator<?> b = other.iterator();
		while (a.hasNext() && b.hasNext()) {
			if (!Objects.equals(a.next(), b.next())) {
				equal = false;
				break;
			}
		}
		return equal && !a.hasNext() && !b.hasNext();
	}

	@Override
	public int size() {
		if (hh <= tt) {
			return tt - hh;
		} else {
			return tt + items.length - hh;
		}
	}


	private boolean isFull() {
		return (tt + 1) % items.length == hh;
	}

	private void resize(int length) {
		T[] a = (T[]) (new Object[length]);
		for (int i = 0, j = hh; i < size(); i++, j = (j + 1) % items.length) {
			a[i] = items[j];
		}
		int t = size();
		hh = 0;
		tt = t;
		items = a;
	}
}
