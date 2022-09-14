package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
	@Test
	public void randomizedTest() {
		AListNoResizing<Integer> L = new AListNoResizing<>();
		BuggyAList<Integer> R = new BuggyAList<>();
		int N = 5000;
		for (int i = 0; i < N; i += 1) {
			int operationNumber = StdRandom.uniform(0, 4);
			if (operationNumber == 0) {
				// addLast
				int randVal = StdRandom.uniform(0, 100);
				L.addLast(randVal);
				R.addLast(randVal);
			} else if (operationNumber == 1) {
				// size
				int size = L.size();
				int size2 = R.size();
//				System.out.println("size: " + size);
				assertEquals(size, size2);
			} else if (operationNumber == 2) {
				if (L.size() > 0) {
					int x = L.getLast(), y = R.getLast();
					assertEquals(x, y);
				}
			} else if (operationNumber == 3) {
				if (L.size() > 0) {
					int x = L.removeLast(), y = R.removeLast();
					assertEquals(x, y);
				}
			}
		}
	}
	@Test
	public void testThreeAddThreeRemove() {
		AListNoResizing<Integer> a = new AListNoResizing<>();
		BuggyAList<Integer> b = new BuggyAList<>();
		a.addLast(4);
		a.addLast(5);
		a.addLast(6);
		b.addLast(4);
		b.addLast(5);
		b.addLast(6);

		assertEquals(getList(a), getList(b));
		a.removeLast();
		b.removeLast();
		assertEquals(getList(a), getList(b));
		a.removeLast();
		b.removeLast();
		assertEquals(getList(a), getList(b));
		a.removeLast();
		b.removeLast();
		assertEquals(getList(a), getList(b));
	}

	private String getList(AListNoResizing<Integer> a) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < a.size(); i++)
			sb.append(a.get(i) + " ");
		return sb.toString();
	}
	private String getList(BuggyAList<Integer> a) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < a.size(); i++)
			sb.append(a.get(i) + " ");
		return sb.toString();
	}
}
