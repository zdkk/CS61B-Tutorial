package deque;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author: zdkk
 * @create 2022-09-15 10:25
 */
public class MaxArrayDequeTest {

	@Test
	public void maxNoParaTest() {
		MaxArrayDeque<Integer> a = new MaxArrayDeque<>((o1, o2) -> (o1 - o2));
		for (int i = 0; i < 10; i++)
			a.addFirst(i);
		assertEquals(a.max() - 0, 9);
	}

	@Test
	public void maxWithComparatorTest() {
		MaxArrayDeque<Integer> a = new MaxArrayDeque<>((o1, o2) -> (o1 - o2));
		for (int i = 0; i < 10; i++)
			a.addFirst(i);
		assertEquals(a.max((o1, o2) -> (o2 - o1)) - 0, 0);
	}
}
