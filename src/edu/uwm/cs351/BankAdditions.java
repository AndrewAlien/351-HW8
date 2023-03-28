package edu.uwm.cs351;

import java.util.NoSuchElementException;

/**
 * A wrapper for a bank that provides extra operations using
 * the audit mechanism.
 * @see Bank#audit(Auditor, String)
 */
public class BankAdditions {
	private final Bank bank;
	
	/**
	 * WRap the bank so that we can provide extra operations
	 * @param b bank to wrap, must not be null
	 */
	public BankAdditions(Bank b) {
		if (b == null) throw new NullPointerException();
		bank = b;
	}
	
	/**
	 * Return the number of accounts in the bank.
	 * This will require someone to tally up the total.
	 * It is =not a constant-time operation.
	 * @return number of accounts at the bank.
	 */
	public int count() {
		// NB: Doesn't need to check invariant because it doesn't look
		// at the data structure: it calls "audit" which should check.
		class Counter implements Auditor {
			int total = 0;
	
			@Override
			public boolean examine(Account acct) {
				++total;
				return true;
			}
			
		}
		Counter counter = new Counter();
		bank.audit(counter, null);
		return counter.total;
	}
	
	/**
	 * Retrieve the account with the given identification
	 * @param id identification string, must not be null
	 * @return account with this id in this bank
	 * @throws NoSuchElementException if no such account exists
	 */
	public Account getAccount(String id) throws NoSuchElementException {
		// TODO: implement using "audit"
		throw new NoSuchElementException("No account for " + id);
	}

	/**
	 * The net assets of a bank are the assets reduced by the liabilities.
	 * For the purposes of this method, we only are interested in accounts.
	 * Liabilities accrue whenever a customer ha s a positive balance in their account,
	 * because they could close the account and withdraw all the money.
	 * On the other hand, loans (negative value accounts) are assets.
	 * This method ignores any other assets/liabilities a bank may have.
	 * (Because this simple class doesn't model those things.)
	 * @return the net assets of this bank from its accounts, never null
	 */
	public Money netAssets() {
		return null; // TODO -- use "audit"
	}
	
	/**
	 * Return the total balance of all accounts under the given prefix. 
	 * @param prefix account prefix, must not be null.
	 * This operation should be more efficient than examining all accounts in the system
	 * @return sum total of current balances of all accounts with this prefix
	 */
	public Money getBalanceSum(String prefix) {
		return null; // TODO
	}
	

}
