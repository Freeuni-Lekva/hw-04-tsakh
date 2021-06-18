import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class CrackerTests extends TestCase {
    public void test1() throws NoSuchAlgorithmException, InterruptedException {
        String[] args = {"molly"};
        Cracker.main(args);
        Cracker c = new Cracker(10);
        assertEquals(c.getHash("molly"), "4181eecbd7a755d19fdf73887c54837cbecf63fd");
    }
    public void test2() throws NoSuchAlgorithmException, InterruptedException {
        Cracker c = new Cracker(8);
        String[] args = {"a"};
        Cracker.main(args);
        assertEquals(c.getHash("a"), "86f7e437faa5a7fce15d1ddcb9eaeaea377667b8");
    }
    public void test3() throws NoSuchAlgorithmException, InterruptedException {
        Cracker c = new Cracker(6);
        String[] args = {"fm"};
        Cracker.main(args);
        assertEquals(c.getHash("fm"), "adeb6f2a18fe33af368d91b09587b68e3abcb9a7");
    }

    public void test4() throws NoSuchAlgorithmException, InterruptedException {
        Cracker c = new Cracker(20);
        c.processPasswords("4181eecbd7a755d19fdf73887c54837cbecf63fd", 5,  20);
        assertEquals(c.getResults().get(0), "molly");
        String[] args = {"4181eecbd7a755d19fdf73887c54837cbecf63fd", "5",  "20"};
        Cracker.main(args);
    }
    public void test5() throws NoSuchAlgorithmException, InterruptedException {
        Cracker c = new Cracker(5);
        c.processPasswords("86f7e437faa5a7fce15d1ddcb9eaeaea377667b8", 2, 5);
        assertEquals(c.getResults().get(0), "a");
        String[] args = {"86f7e437faa5a7fce15d1ddcb9eaeaea377667b8", "1", "10"};
        Cracker.main(args);
    }
    public void test6() throws NoSuchAlgorithmException, InterruptedException {
        Cracker c = new Cracker(5);
        c.processPasswords("adeb6f2a18fe33af368d91b09587b68e3abcb9a7", 2, 5);
        assertEquals(c.getResults().get(0), "fm");
        String[] args = {"adeb6f2a18fe33af368d91b09587b68e3abcb9a7", "2", "5"};
        Cracker.main(args);
    }

    public void testInvalidArguments() throws NoSuchAlgorithmException, InterruptedException {
        String[] args = {};
        Cracker.main(args);
    }
}
