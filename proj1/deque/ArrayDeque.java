package deque;

import java.util.Iterator;

/**
 * @author: zdkk
 * @create 2022-09-14 22:17
 */
public class ArrayDeque<T> {
	private T[] items;
	private int hh, tt;

	public ArrayDeque() {
		items = (T[])(new Object[8]);
		hh = tt = 0;
	}

	public void addFirst(T item) {
		if (isFull())
			resize(items.length * 2);
		hh = (hh + items.length - 1) % items.length;
		items[hh] = item;
	}

	public void addLast(T item) {
		if (isFull())
			resize(items.length * 2);
		items[tt++] = item;
		if (tt == items.length) tt = 0;
	}

	public boolean isEmpty() {
		return hh == tt;
	}

	public void printDeque() {
		int i = hh;
		while (i != tt) {
			System.out.print(items[i] + " ");
			i = (i + 1) % items.length;
		}
		System.out.println();
	}

	public T removeFirst() {
		if (isEmpty()) return null;
		T res = items[hh];
		hh = (hh + 1) % items.length;
		if (items.length > 16 && items.length / 4 > size())
			resize(items.length / 4);
		return res;
	}

	public T removeLast() {
		if (isEmpty()) return null;
		tt = (tt + items.length - 1) % items.length;
		T res = items[tt];
		if (items.length > 16 && items.length / 4 > size())
			resize(items.length / 4);
		return res;
	}

	public T get(int idx) {
		if (idx < 0 || idx >= size())
			return null;
		return items[(hh + idx) % items.length];
	}

	public Iterator<T> iterator() {
		return new Iterator<T>() {
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

	public boolean equals(Object o) {
		if (o == null) return false;
		if (!(o instanceof ArrayDeque))
			return false;
		return toString().equals(o.toString());
	}

	@Override
	public String toString() {
		int idx = hh;
		StringBuilder sb = new StringBuilder();
		while (idx != tt) {
			sb.append(items[idx].toString() + " ");
			idx = (idx + 1) % items.length;
		}
		return sb.toString();
	}

	private int size() {
		if (hh <= tt)
			return tt - hh;
		else return tt + items.length - hh;
	}


	private boolean isFull() {
		return (tt + 1) % items.length == hh;
	}

	private void resize(int length) {
		T[] a = (T[])(new Object[length]);
		for (int i = 0, j = hh; i < items.length - 1; i++, j = (j + 1) % items.length) {
			a[i] = items[j];
		}
		hh = 0;
		tt = items.length - 1;
		items = a;
	}
}