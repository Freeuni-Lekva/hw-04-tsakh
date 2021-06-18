// Account.java

/*
 Simple, thread-safe Account class encapsulates
 a balance and a transaction count.
*/
public class Account {
	private int id;
	private int balance;
	private int transactions;

	// It may work out to be handy for the account to
	// have a pointer to its Bank.
	// (a suggestion, not a requirement)
	private Bank bank;

	public Account(Bank bank, int id, int balance) {
		this.bank = bank;
		this.id = id;
		this.balance = balance;
		transactions = 0;
	}

	public synchronized void withdrawal (int money){
		balance -= money;
		transactions++;
	}

	public synchronized void introduction (int money){
		balance += money;
		transactions++;
	}

	@Override
	public String toString(){
		String res = new String();
		res += "acct:" + id + " bal:" + balance +
				" trans:" + transactions;
		return res;
	}

	public int getId(){return id;}
	public int getBalance(){ return balance;}
	public int getTransactionsNum(){return transactions;}

}