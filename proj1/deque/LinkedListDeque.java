package deque;

import java.util.Deque;
import java.util.Iterator;

/**
 * @author: zdkk
 * @create 2022-09-14 21:42
 */
public class LinkedListDeque<T> {
	class Node<T> {
		Node<T> left, right;
		T val;

		Node(T val) {
			this.val = val;
		}
	}

	private Node<T> head, tail;
	private int size;

	public LinkedListDeque() {
		head = new Node<>(null);
		tail = new Node<>(null);
		head.right = tail;
		tail.left = head;
		size = 0;
	}

	public void addFirst(T item) {
		add(head, item);
	}

	public void addLast(T item) {
		add(tail.left, item);
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public void printDeque() {
		Node u = head.right;
		while (u != tail) {
			System.out.print(u.val + " ");
			u = u.right;
		}
		System.out.println();
	}

	public T removeFirst() {
		if (isEmpty()) return null;
		T res = head.right.val;
		remove(head.right);
		return res;
	}

	public T removeLast() {
		if (isEmpty()) return null;
		T res = tail.left.val;
		remove(tail.left);
		return res;
	}

	public T get(int idx) {
		if (idx >= size || idx < 0) return null;
		Node<T> u = head.right;
		while (idx-- > 0) {
			u = u.right;
		}
		return u.val;
	}

	public Iterator<T> iterator() {
		return new Iterator<T>() {
			Node<T> cur = head.right;
			@Override
			public boolean hasNext() {
				return cur != tail;
			}

			@Override
			public T next() {
				Node<T> res = cur;
				cur = cur.right;
				return res.val;
			}
		};
	}

	public boolean equals(Object o) {
		if (o == null) return false;
		if (!(o instanceof LinkedListDeque))
			return false;
		return toString().equals(o.toString());
	}

	public int size() {
		return size;
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Node<T> u = head.right;
		while (u != tail) {
			sb.append(u.val.toString() + " ");
			u = u.right;
		}
		return sb.toString();
	}


	private void remove(Node<T> u) {
		u.right.left = u.left;
		u.left.right = u.right;
		size--;
	}
	private void add(Node<T> u, T item) {
		Node<T> t = new Node<>(item);
		t.right = u.right;
		t.left = u;
		u.right.left = t;
		u.right = t;
		size++;
	}
}
