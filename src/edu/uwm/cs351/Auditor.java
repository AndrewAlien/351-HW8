package edu.uwm.cs351;

/**
 * An interface for a bank auditor.
 */
public interface Auditor {
	/**
	 * Examine an account.  Return whether the audit process should be continued (or not).
	 * @param acct account to examine, must not be null
	 * @return whether the process should be continued.  If not, no more accounts should be audited
	 */
	public boolean examine(Account acct);
}
