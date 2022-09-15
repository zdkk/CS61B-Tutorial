package deque;

import java.util.Iterator;
import java.util.Objects;

/**
 * zdkk
 * 2022-09-14 21:42
 */
public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {

	private class Node {
		Node left, right;
		T val;

		Node(T val) {
			this.val = val;
		}
	}

	private final Node head, tail;
	private int size;

	public LinkedListDeque() {
		head = new Node(null);
		tail = new Node(null);
		head.right = tail;
		tail.left = head;
		size = 0;
	}

	@Override
	public void addFirst(T item) {
		add(head, item);
	}

	@Override
	public void addLast(T item) {
		add(tail.left, item);
	}


	@Override
	public void printDeque() {
		Node u = head.right;
		while (u != tail) {
			System.out.print(u.val + " ");
			u = u.right;
		}
		System.out.println();
	}

	@Override
	public T removeFirst() {
		if (isEmpty()) {
			return null;
		}
		T res = head.right.val;
		remove(head.right);
		return res;
	}

	@Override
	public T removeLast() {
		if (isEmpty()) {
			return null;
		}
		T res = tail.left.val;
		remove(tail.left);
		return res;
	}

	@Override
	public T get(int idx) {
		if (idx >= size || idx < 0) {
			return null;
		}
		Node u = head.right;
		while (idx-- > 0) {
			u = u.right;
		}
		return u.val;
	}

	public T getRecursive(int idx) {
		if (idx >= size || idx < 0) {
			return null;
		}
		return dfs(idx, head.right);
	}

	private T dfs(int idx, Node cur) {
		if (idx == 0) {
			return cur.val;
		}
		return dfs(idx - 1, cur.right);
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<>() {
			Node cur = head.right;
			@Override
			public boolean hasNext() {
				return cur != tail;
			}

			@Override
			public T next() {
				Node res = cur;
				cur = cur.right;
				return res.val;
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
		boolean equal = (o.getClass() == LinkedListDeque.class)
				? equalsLinkedListDeque((LinkedListDeque<?>) o)
				: equalsRange((Deque<?>) o);
		return equal;
	}

	private boolean equalsRange(Deque<?> other) {
		if (size() != other.size()) {
			return false;
		}
		boolean equal = true;
		for (int i = 0; i < size(); i++) {
			if (get(i) != other.get(i)) {
				equal = false;
				break;
			}
		}
		return equal;
	}

	private boolean equalsLinkedListDeque(LinkedListDeque<?> other) {
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
		return size;
	}

	private void remove(Node u) {
		u.right.left = u.left;
		u.left.right = u.right;
		size--;
	}
	private void add(Node u, T item) {
		Node t = new Node(item);
		t.right = u.right;
		t.left = u;
		u.right.left = t;
		u.right = t;
		size++;
	}
}
