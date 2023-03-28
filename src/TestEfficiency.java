import java.util.NoSuchElementException;

import edu.uwm.cs351.Account;
import edu.uwm.cs351.Auditor;
import edu.uwm.cs351.Bank;
import edu.uwm.cs351.BankAdditions;
import edu.uwm.cs351.Money;
import junit.framework.TestCase;

public class TestEfficiency extends TestCase {
	// exception tests must be chosen carefully: 
	// catching exceptions can be very slow
	protected void assertException(Class<? extends Throwable> c, Runnable r) {
		try {
			r.run();
			assertFalse("Exception should have been thrown",true);
		} catch (RuntimeException ex) {
			if (!c.isInstance(ex)) {
				ex.printStackTrace();
			}
			assertTrue("should throw exception of " + c + ", not of " + ex.getClass(), c.isInstance(ex));
		}
	}

	private Bank bank;
	private BankAdditions add;
	private Account[] trail;

	private static final int POWER = 20; // 1 million entries
	private static final int MAX = 1 << POWER;
	private static final int TESTS = 1 << 15;
	private static final int PREFIX = 10_000_000;

	private static class TrailerAuditor implements Auditor {
		Account[] output;
		int used = 0;
		
		public TrailerAuditor(Account[] a) {
			if (a.length < 1) throw new IllegalArgumentException("too short");
			output = a;
		}

		@Override
		public boolean examine(Account acct) {
			if (acct == null) throw new IllegalArgumentException("Auditor asked to examine null account");
			if (used >= output.length) throw new IllegalStateException("auditor called even after they requested audit to end");
			output[used++] = acct;
			return used < output.length;
		}
	}

	private String id(int i) {
		return ""+ (PREFIX+i);
	}
	
	@Override
	protected void setUp() {
		try {
			assert bank.equals((Object)TESTS) : "cannot run test with assertions enabled";
		} catch (NullPointerException ex) {
			throw new IllegalStateException("Cannot run test with assertions enabled");
		}
		bank = new Bank();
		add = new BankAdditions(bank);
	}

	@Override
	protected void tearDown() {
		bank = null;
	}

	public void testA() {
		for (int power = POWER; power > 0; --power) {
			int incr = 1 << power;
			for (int i=1 << (power-1); i < MAX; i += incr) {
				bank.open("Test",id(i), Money.ZERO, Money.ZERO);
			}
		}
		assertEquals(MAX-1, add.count());
	}
	
	public void testB() {
		for (int i=0; i < MAX; ++i) {
			bank.open("Test", "", Money.ZERO, Money.ZERO);
		}
		assertEquals(MAX, add.count());
	}

	public void testC() {
		for (int i=0; i < MAX; ++i) {
			bank.open("Test", "1234", Money.ZERO, Money.ZERO);
		}
		assertEquals(MAX, add.count());
	}
	
	public void testD() {
		for (int i=0; i < MAX; ++i) {
			bank.open("Test", "", Money.ZERO, Money.ZERO);
		}
		trail = new Account[1];
		for (int i=0; i < TESTS; ++i) {
			trail[0] = null;
			bank.audit(new TrailerAuditor(trail), "900000");
			assertNotNull(trail[0]);
		}
	}
	
	public void testE() {
		for (int power = POWER; power > 0; --power) {
			int incr = 1 << power;
			for (int i=1 << (power-1); i < MAX; i += incr) {
				bank.open("Test",id(i), Money.ZERO, Money.ZERO);
			}
		}
		for (int i=1; i < MAX; i += 1) {
			assertNotNull(add.getAccount(id(i)));
		}
	}
	
	public void testF() {
		for (int power = POWER; power > 0; --power) {
			int incr = 1 << power;
			for (int i=1 << (power-1); i < MAX; i += incr) {
				bank.open("Test",id(i), Money.ZERO, Money.ZERO);
			}
		}
		for (int i=0; i < MAX; i += POWER) {
			final String s = id(i)+"0";
			assertException(NoSuchElementException.class, () -> add.getAccount(s));
		}
	}
	
	public void testG() {
		for (int power = POWER; power > 0; --power) {
			int incr = 1 << power;
			for (int i=1 << (power-1); i < MAX; i += incr) {
				bank.open("Test",id(i), Money.ZERO, Money.DOLLAR);
			}
		}
		assertEquals(new Money(-MAX+1), add.netAssets());
	}
	
	public void testH() {
		for (int power = POWER; power > 0; --power) {
			int incr = 1 << power;
			for (int i=1 << (power-1); i < MAX; i += incr) {
				bank.open("Test",id(i), Money.ZERO, Money.DOLLAR);
			}
		}
		String prefix = id(543_210);
		bank.open("Test", prefix, Money.ZERO, new Money(9.00));
		for (int i=0; i < TESTS; ++i) {
			assertEquals(new Money(10.00), add.getBalanceSum(prefix));
		}
	}
}
