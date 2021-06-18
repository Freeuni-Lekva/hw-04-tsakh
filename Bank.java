// Bank.java

/*
 Creates a bunch of accounts and uses threads
 to post transactions to the accounts concurrently.
*/

import java.io.*;
import java.sql.Array;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class Bank {
	private class Worker extends Thread{
		@Override
		public void run(){
			while(true){
				Transaction curr = null;
				try {
					curr = q.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//q.remove();
				if (curr == nullTrans) break;
				Account fromAcc = null;
				Account toAcc = null;
				int fromId = curr.from;
				int toId = curr.to;
				for (int i = 0; i < accounts.size(); i++){
					if (fromId == accounts.get(i).getId()) fromAcc = accounts.get(i);
					if (toId == accounts.get(i).getId()) toAcc = accounts.get(i);
				}
				performTransaction(fromAcc, toAcc, curr.amount);
			}
			countDown.countDown();
		}

		private void performTransaction(Account from, Account to, int money){
			if (from != null && to != null){
				from.withdrawal(money);
				to.introduction(money);
			}
		}
	}
	public static final int ACCOUNTS = 20;	 // number of accounts
	private static final int CAPACITY = 10; // capacity for BlockingQueue
	private final Transaction nullTrans = new Transaction(-1,0,0);
	private BlockingQueue<Transaction> q;
	private Vector<Account> accounts;
	private CountDownLatch countDown;
	private int numWorkingThreads;

	public Bank(int numWorkers){
		accounts = new Vector<>();
		for (int i = 0; i < ACCOUNTS; i++){
			accounts.add(new Account(this, i, 1000));
		}
		q = new ArrayBlockingQueue<>(CAPACITY);
		numWorkingThreads = numWorkers;
		countDown = new CountDownLatch(numWorkingThreads);
	}

	/*
	 Reads transaction data (from/to/amt) from a file for processing.
	 (provided code)
	 */
	public void readFile(String file) throws IOException, InterruptedException {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));

			// Use stream tokenizer to get successive words from file
			StreamTokenizer tokenizer = new StreamTokenizer(reader);

			while (true) {
				int read = tokenizer.nextToken();
				if (read == StreamTokenizer.TT_EOF) break;  // detect EOF
				int from = (int)tokenizer.nval;

				tokenizer.nextToken();
				int to = (int)tokenizer.nval;

				tokenizer.nextToken();
				int amount = (int)tokenizer.nval;

				// Use the from/to/amount
				q.put(new Transaction(from, to, amount));
			}
		}
		catch (Exception e) {
			//e.printStackTrace();
			throw e;
		}
	}

	//starts thread for each worker
	private void startWorkers(int numWorkers){
		for (int i = 0; i < numWorkers; i++){
			Worker curr = new Worker();
			curr.start();
		}
	}
	//adds nullTransactions to the blockQueue
	private void addNullTransactions(int numWorkers) throws InterruptedException {
		for (int i = 0; i < numWorkers; i++){
			q.put(nullTrans);
		}
	}

	/*
	 Processes one file of transaction data
	 -fork off workers
	 -read file into the buffer
	 -wait for the workers to finish
	*/
	public void processFile(String file, int numWorkers) throws InterruptedException, IOException {
		startWorkers(numWorkers);
		readFile(file);
		addNullTransactions(numWorkers);
		countDown.await();
	}

	public void printAccountInformation(int numWorkers){
		for (int i = 0; i < accounts.size(); i++){
			System.out.println(accounts.get(i).toString());
		}
	}

	public final Vector<Account> getAccounts(){
		return accounts;
	}

	/*
	 Looks at commandline args and calls Bank processing.
	*/
	public static void main(String[] args) {
		// deal with command-lines args
		if (args.length == 0) {
			System.out.println("Args: transaction-file [num-workers [limit]]");
			return;
			//System.exit(1);
		}

		String file = args[0];

		int numWorkers = 1;
		if (args.length >= 2) {
			numWorkers = Integer.parseInt(args[1]);
		}
		Bank b = new Bank(numWorkers);
		try {
			b.processFile(file, numWorkers);
		} catch (InterruptedException | IOException e) {
			//e.printStackTrace();
			return;
		}
		b.printAccountInformation(numWorkers);
	}
}
