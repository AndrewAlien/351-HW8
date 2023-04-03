//Andrew Lien
package edu.uwm.cs351;

import java.util.Random;
import java.util.function.Consumer;

/**
 * A class for managing accounts with money.
 * For now, this class is essentially just a glorified dictionary,
 * mapping accounts "numbers" (strings of digits) to accounts.
 */
public class Bank {
	private static Consumer<String> reporter = (s) -> System.out.println("Invariant error: "+ s);
	
	/**
	 * Used to report an error found when checking the invariant.
	 * By providing a string, this will help debugging the class if the invariant should fail.
	 * @param error string to print to report the exact error found
	 * @return false always
	 */
	private static boolean report(String error) {
		reporter.accept(error);
		return false;
	}

	// TODO: Declare a Node class with three fields:
	//   - key (the account)
	//   - left
	//   - right
	// and at least one constructor: one taking an account.
	private static class Node{
		//String key;
		Account key;
		Node left,right;
		public Node(Account a, Node l, Node r) {
			key = a;
			left = l;
			right = r;
		}
		public Node(Account a) {
			key = a;
			left = right = null;
		}
//		public Node(String k, Node l, Node r) {
//			key = k;
//			left = l;
//			right = r;
//		}
//		public Node() {
//			key = null;
//			left = null;
//			right = null;
//		}
	}
	
	// TODO: data structure.  (very simple)
	Node root;
	
	private final Random random = new Random(); // ignore in invariant -- it doesn't change)
	
	/**
	 * Check if the subtree is correctly ordered, with accounts with IDs all within the given bounds.
	 * If there is a problem, it will be reported (once only!) and false returned.
	 * If everything is fine, true is returned with no reports having been generated.
	 * @param tree subtree to check
	 * @param before exclusive lower bound, if null then no lower bound
	 * @param after exclusive upper bound, if null then no upper bound
	 * @return whether everything is ordered in bounds, or else one report is generated.
	 */
	private boolean inOrder(Node tree, String before, String after) {
		if (tree == null) return true;
		
		if (tree.key == null) return report("not working and pissing me off");

		
//		if (tree.left != null)
//			if (tree.key.getID().compareTo(tree.left.key.getID()) >= 0) return report("fuck me :)");
//		
//		if (tree.right != null)
//			if (tree.key.getID().compareTo(tree.right.key.getID()) >= 0) return report("fuck me :)");
//		
//		            
//
//		inOrder(tree.left, null, tree.key.getID());
//		
////		if (tree.key != null)
////			if (tree.key.getID().compareTo(after) >= 0) return report("i still hate my life");
//
//		System.out.println(tree.key.getID());
//
//		inOrder(tree.right, tree.key.getID(), null);
		
		
		//if (tree.left != null)
		inOrder(tree.left, before, after);
		
		if (tree.key == null) return report("just special case my way to victory fuck this assignment");
		
		
		if (before != null)
			if (tree.key.getID().compareTo(before) <= 0) return report("still wanna die :)");

		if (after != null)
			if (tree.key.getID().compareTo(after) >= 0) return report("still wanna die :)");

		inOrder(tree.right, before, after);

		
		//if (tree.key.getID().compareTo(tree.right.key.getID()) >= 0) return false;
		return true; // TODO
	}
	
	private boolean wellFormed() {
		return inOrder(root,null,null);
		//return true; // TODO: Very simple: use the helper method
	}
	
	private Bank(boolean ignored) {} // do not change this
	
	/**
	 * Create a bank with no accounts.
	 */
	public Bank() {
		// TODO (?)
		root = null;
		assert wellFormed() : "invariant broken in constructor";
	}
	
	// TODO: helper method for open (optional) ?? is this my while loop??
	private Node doOpen(Node r, String owner, String prefix, Money minBalance, Money initial) {
//		if (r == null) return new Node(a, null, null);
//		if (a.getID().compareTo(r.key.getID()) > 0)
//			r.left = doOpen(r.left, a);
//		else if (a.getID().compareTo(r.key.getID()) == 0)
//			a.getID().concat(random.nextInt(10));
//		else
//			r.right = doOpen(r.right, a);
//		return r;
		
		
		if (r == null) {
			Account a = new Account(owner, prefix, minBalance, initial);
			root = new Node(a,null,null);
			return root;
		}
		
		if (r.left != null)
		r.left = doOpen(r.left, owner, prefix, minBalance, initial);
			
		
		if (prefix.compareTo(r.key.getID()) == 0) {
			prefix += String.valueOf(random.nextInt(10));
		}
			
		else if (prefix.compareTo(r.key.getID()) < 0) {
			Account a = new Account(owner, prefix, minBalance, initial);
			return new Node(a, null, null);
		}
		
		if (r.right != null)
		r.right = doOpen(r.right, owner, prefix, minBalance, initial);
			
		
//		if (prefix.compareTo(r.key.getID()) > 0)
//			
//		else if (a.getID().compareTo(r.key.getID()) == 0)
//			a.getID().concat(random.nextInt(10));
//		else
//			r.right = doOpen(r.right, a);
		//return r;
		
		
		Account a = new Account(owner, prefix, minBalance, initial);
		return new Node(a, null, null);
	}
	
	/**
	 * Open a new account with the given minimum balance (negative credit limit) and 
	 * initial balance.
	 * @param owner name of owner for account
	 * @param prefix create an account starting with this prefix (must not be null)
	 * @param minBalance minimum balance, must not be null
	 * @param initial balance at opening, must not be null
	 * @return new account
	 */
	public Account open(String owner, String prefix, Money minBalance, Money initial) {
		while (prefix.length() < Account.MIN_ACCOUNT_ID)
			prefix += random.nextInt(10);
		
//		Account a = new Account(owner, prefix, minBalance, initial);
		Account a = doOpen(root, owner, prefix, minBalance, initial).key;
		//doOpen (root,a);
		
		return a; // TODO (And don't forget the invariant)
	}
		
	// TODO: Define a recursive helper method for "audit" that
	// returns a boolean whether to continue or not.
	
	
	/**
	 * Audit the accounts at this bank.
	 * @param a auditor that should be permitted to examine each account, in order.
	 * @param start starting auditing accounts with ids from this point on.  If null, the audit should begin
	 * with the first account overall.
	 */
	public void audit(Auditor a, String start) {
		// TODO: call the helper with the root,
		// ignoring the result since there's nothing more to do anyway.
		// And don't forget the invariant.
		
		//a.examine(root.key);
	}
	
	// Do not change anything in the Spy class
	/**
	 * A class to assist with internal testing.
	 */
	public static class Spy {
		/**
		 * Return the sink for invariant error messages
		 * @return current reporter
		 */
		public Consumer<String> getReporter() {
			return reporter;
		}

		/**
		 * Change the sink for invariant error messages.
		 * @param r where to send invariant error messages.
		 */
		public void setReporter(Consumer<String> r) {
			reporter = r;
		}

		public static class Node extends Bank.Node {
			public Node(Account a, Node l, Node r) {
				super(null);
				this.key = a;
				this.left = l;
				this.right = r;
			}
		}
		
		/**
		 * Create a test BST
		 * @param a account, may be null
		 * @param l left subtree, may be null
		 * @param r right subtree, may be null
		 * @return newly created node
		 */
		public Node makeNode(Account a, Node l, Node r) {
			return new Node(a,l,r);
		}
		
		/**
		 * Reinitialize the BST node.
		 * @param n node to re-initialize, must not be null
		 * @param a account, may be null
		 * @param l left subtree, may be null
		 * @param r right subtree, may be null
		 */
		public void reinit(Node n, Account a, Node l, Node r) {
			n.key = a;
			n.left = l;
			n.right = r;
		}
		
		/**
		 * Create a bank with the given (unchecked) data structure
		 * @param r root to use (may be null)
		 * @return new object with unchecked data structure
		 */
		public Bank makeBank(Node r) {
			Bank result = new Bank(false);
			result.root = r;
			return result;
		}
		
		/**
		 * Return whether the subtree has elements in order according to the
		 * helper method "inorder"
		 * @param n subtree to check
		 * @param lo before bound
		 * @param hi after bound
		 * @return whether the method returns true.
		 */
		public boolean inOrder(Node n, String lo, String hi) {
			Bank self = new Bank(false);
			return self.inOrder(n, lo, hi);
		}
		
		/**
		 * Check a Bank's data structure.
		 * @param b bank to check, must not be null
		 * @return whether the data structure is deemed OK
		 */
		public boolean wellFormed(Bank b) {
			return b.wellFormed();
		}
	}

}
