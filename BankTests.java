import junit.framework.TestCase;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

public class BankTests extends TestCase {
    public void testSmall() throws InterruptedException, IOException {
        //Bank bank = new Bank(10);
        String[] args = {"small.txt", "10"};
        Bank.main(args);

        Bank b = new Bank(10);
        b.processFile("small.txt", 10);
        b.printAccountInformation(10);
        Vector<Account> accs;
        accs = b.getAccounts();
        for (int i = 0; i < accs.size(); i++){
            Account currAccount = accs.get(i);
            if (currAccount.getId() % 2 == 0){
                assertEquals(currAccount.getBalance(), 999);
            } else {
                assertEquals(currAccount.getBalance(), 1001);
            }
            assertEquals(currAccount.getTransactionsNum(), 1);
        }
    }

    public void test5K() throws InterruptedException, IOException {
        String[] args = {"5k.txt", "10"};
        Bank.main(args);

        Bank b = new Bank(10);
        b.processFile("5k.txt", 10);
        b.printAccountInformation(10);
        Vector<Account> accs;
        accs = b.getAccounts();
        for (int i = 0; i < accs.size(); i++){
            Account currAccount = accs.get(i);
            assertEquals(currAccount.getBalance(), 1000);
            assert(currAccount.getTransactionsNum() > 0);
        }
    }

    public void test100K() throws InterruptedException, IOException {
        String[] args = {"100k.txt", "10"};
        Bank.main(args);

        Bank b = new Bank(10);
        b.processFile("100k.txt", 10);
        b.printAccountInformation(10);
        Vector<Account> accs;
        accs = b.getAccounts();
        for (int i = 0; i < accs.size(); i++){
            Account currAccount = accs.get(i);
            assertEquals(currAccount.getBalance(), 1000);
            assert(currAccount.getTransactionsNum() > 0);
        }
    }


    //for coverage
    public void testInvalidArguments() {
        String[] args = {};
        Bank.main(args);
    }

    public void testInvalidFile() {
        String[] args = {"bla.txt", "1"};
        Bank.main(args);
    }

}
