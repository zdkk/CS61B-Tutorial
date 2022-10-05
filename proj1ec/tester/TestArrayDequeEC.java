package tester;

import static org.junit.Assert.*;
import org.junit.Test;
import student.StudentArrayDeque;
import edu.princeton.cs.introcs.StdRandom;

/**
 * @author zdkk
 * @create 2022-10-04 20:40
 */
public class TestArrayDequeEC {
    @Test
    public void test1() {
        StudentArrayDeque<Integer> b = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> a = new ArrayDequeSolution<>();
        for (int i = 0; i < 1000; i++) {
            int x = StdRandom.uniform(0, 3);
            if (x == 0) {
                a.addFirst(i);
                b.addFirst(i);
                System.out.println("addFirst(" + i + ")");
            } else if (x == 1){
                a.addLast(i);
                b.addLast(i);
                System.out.println("addLast(" + i + ")");
            } else if (x == 2 && a.size() > 0) {
                Integer p = a.removeFirst();
                Integer q = b.removeFirst();
                System.out.println("removeFirst()");
                assertEquals("removeFirst()", p, q);
            } else if (x == 3 && a.size() > 0) {
                Integer p = a.removeLast();
                Integer q = b.removeLast();
                System.out.println("removeLast()");
                assertEquals("removeLast()", p, q);
            }
        }
    }
}
