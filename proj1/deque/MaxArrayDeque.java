package deque;

import java.util.Comparator;
import java.util.Iterator;

/**
 * @author: zdkk
 * @create 2022-09-15 10:06
 */
public class MaxArrayDeque<T> extends ArrayDeque<T> {
	private Comparator<T> comparator;
	public MaxArrayDeque(Comparator<T> c) {
		super();
		this.comparator = c;
	}

	public T max() {
		if (isEmpty()) {
			return null;
		}
		Iterator<T> iterator = this.iterator();
		T res = null;
		while (iterator.hasNext()) {
			T cur = iterator.next();
			if (res == null) {
				res = cur;
			} else if (comparator.compare(res, cur) < 0) {
				res = cur;
			}
		}
		return res;
	}

	public T max(Comparator<T> c) {
		if (isEmpty()) {
			return null;
		}
		Iterator<T> iterator = this.iterator();
		T res = null;
		while (iterator.hasNext()) {
			T cur = iterator.next();
			if (res == null) {
				res = cur;
			} else if (c.compare(res, cur) < 0) {
				res = cur;
			}
		}
		return res;
	}
}
