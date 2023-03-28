import java.util.function.Consumer;
import java.util.function.Supplier;

import edu.uwm.cs351.Account;
import edu.uwm.cs351.Bank;
import edu.uwm.cs351.Bank.Spy;
import edu.uwm.cs351.Money;
import junit.framework.TestCase;

public class TestInternals extends TestCase {
	protected Bank.Spy spy;

	protected int reports;

	protected void assertReporting(boolean expected, Supplier<Boolean> test) {
		reports = 0;
		Consumer<String> savedReporter = spy.getReporter();
		try {
			spy.setReporter((String message) -> {
				++reports;
				if (message == null || message.trim().isEmpty()) {
					assertFalse("Uninformative report is not acceptable", true);
				}
				if (expected) {
					assertFalse("Reported error incorrectly: " + message, true);
				}
			});
			assertEquals(expected, test.get().booleanValue());
			if (!expected) {
				assertEquals("Expected exactly one invariant error to be reported;", 1, reports);
			}
			spy.setReporter(null);
		} finally {
			spy.setReporter(savedReporter);
		}
	}

	protected Account[] a;
	protected Bank self;
	private Spy.Node[] n;
	
	@Override
	protected void setUp() {
		spy = new Bank.Spy();
	
		a = new Account[15];
		n = new Spy.Node[15];
		n[0] = spy.makeNode(null, null, null);
		for (int i=1; i < 15; ++i) {
			a[i] = new Account("Test",""+(1000+i),Money.ZERO,new Money(i));
			n[i] = spy.makeNode(a[i], null, null);
		}
		// We build the tree
		//             6
		//       2           10
		//    1     5      9      12
		//         3      7      11  13
		//          4      8
		spy.reinit(n[6], a[6], n[2], n[10]);
		spy.reinit(n[2], a[2], n[1], n[5]);
		spy.reinit(n[5], a[5], n[3], null);
		spy.reinit(n[3], a[3], null, n[4]);
		spy.reinit(n[10], a[10], n[9], n[12]);
		spy.reinit(n[9], a[9], n[7], null);
		spy.reinit(n[7], a[7], null, n[8]);
		spy.reinit(n[12], a[12], n[11], n[13]);
	}

	protected void assertInOrder(boolean expected, Spy.Node n, String lo, String hi) {
		assertReporting(expected, () -> spy.inOrder(n, lo, hi));
	}
	
	protected void assertWellFormed(boolean expected, Bank b) {
		assertReporting(expected, () -> spy.wellFormed(b));
	}
	
	
	public void testA0() {
		assertInOrder(true, null, null, null);
	}
	
	public void testA1() {
		assertInOrder(true, null, "0001", null);
	}

	public void testA2() {
		assertInOrder(true, null, null, "0001");
	}
	
	public void testA3() {
		assertInOrder(true, null, "0001", "0002");
	}
	
	public void testA4() {
		assertInOrder(true, null, "0002", "0001");
	}
	
	public void testA5() {
		assertInOrder(true, null, "1002", "0001");
	}

	
	public void testB0() {
		assertInOrder(false, n[0], null, null);
	}
	
	public void testB1() {
		assertInOrder(false, n[0], "0001", null);
	}

	public void testB2() {
		assertInOrder(false, n[0], null, "0001");
	}
	
	public void testB3() {
		assertInOrder(false, n[0], "0001", "0002");
	}
	
	public void testB4() {
		assertInOrder(false, n[0], "0002", "0001");
	}
	
	public void testB5() {
		assertInOrder(false, n[0], "1002", "0001");
	}

	
	public void testC0() {
		assertInOrder(true, n[1], null, null);
	}
	
	public void testC1() {
		assertInOrder(true, n[1], "0001", null);
	}

	public void testC2() {
		assertInOrder(true, n[1], "1000", null);
	}

	public void testC3() {
		assertInOrder(false, n[1], "1001", null);
	}

	public void testC4() {
		assertInOrder(true, n[1], "10001", null);
	}

	public void testC5() {
		assertInOrder(false, n[1], null, "1000");
	}

	public void testC6() {
		assertInOrder(false, n[1], null, "10002");
	}

	public void testC7() {
		assertInOrder(false, n[1], null, "1001");
	}

	public void testC8() {
		assertInOrder(true, n[1], null, "1002");
	}

	public void testC9() {
		assertInOrder(true, n[1], null, "10010");
	}
	
	public void testD0() {
		assertInOrder(true, n[1], "1000", "1002");
	}
	
	public void testD1() {
		assertInOrder(true, n[1], "10000", "10010");
	}
	
	public void testD2() {
		assertInOrder(false, n[1], "10010", "1002");
	}
	
	public void testD3() {
		assertInOrder(false, n[1], "110", "1002");
	}
	
	public void testD4() {
		assertInOrder(false, n[1], "1000", "1001");
	}
	
	public void testD5() {
		assertInOrder(false, n[1], "10000", "10002");
	}
	
	public void testD6() {
		assertInOrder(true, n[1], "100", "102");
	}
	
	public void testD7() {
		assertInOrder(false, n[1], "100", "10002");
	}

	public void testD8() {
		assertInOrder(true, n[8], "1007", "1009");
	}
	
	public void testD9() {
		assertInOrder(false, n[13], "1000", "1002");
	}
	
	
	public void testE0() {
		spy.reinit(n[1], a[1], n[0], null);
		assertInOrder(false, n[1], null, null);
	}
	
	public void testE1() {
		spy.reinit(n[1], a[1], n[0], null);
		assertInOrder(false, n[1], null, "102");
	}
	
	public void testE2() {
		spy.reinit(n[1], a[1], n[0], null);
		assertInOrder(false, n[1], "10000", null);
	}

	public void testE3() {
		spy.reinit(n[1], a[1], n[0], null);
		assertInOrder(false, n[1], "1000", "1002");
	}
	
	public void testE4() {
		spy.reinit(n[1], a[1], null, n[0]);
		assertInOrder(false, n[1], null, null);
	}
	
	public void testE5() {
		spy.reinit(n[1], a[1], null, n[0]);
		assertInOrder(false, n[1], "1000", null);
	}

	public void testE6() {
		spy.reinit(n[1], a[1], null, n[0]);
		assertInOrder(false, n[1], null, "1002");
	}

	public void testE7() {
		spy.reinit(n[1], a[1], null, n[0]);
		assertInOrder(false, n[1], "1000", "1002");
	}
	
	
	public void testF0() {
		spy.reinit(n[1], a[1], n[14], null);
		assertInOrder(false, n[1], null, null);
	}
	
	public void testF1() {
		spy.reinit(n[1], a[1], n[14], null);
		assertInOrder(false, n[1], "1000", null);
	}
	
	public void testF2() {
		spy.reinit(n[1], a[1], n[14], null);
		assertInOrder(false, n[1], null, "102");
	}
	
	public void testF3() {
		spy.reinit(n[1], a[1], n[14], null);
		assertInOrder(false, n[1], "1000", "1020");
	}
	
	public void testF4() {
		spy.reinit(n[8], a[8], null, n[1]);
		assertInOrder(false, n[8], null, null);
	}
	
	public void testF5() {
		spy.reinit(n[8], a[8], null, n[1]);
		assertInOrder(false, n[8], "1000", null);
	}
	
	public void testF6() {
		spy.reinit(n[8], a[8], null, n[1]);
		assertInOrder(false, n[8], null, "101");
	}
	
	public void testF7() {
		spy.reinit(n[8], a[8], null, n[1]);
		assertInOrder(false, n[8], "1000", "1009");
	}
	
	public void testF8() {
		spy.reinit(n[1], a[1], n[1], null);
		assertInOrder(false, n[1], null, null);
	}
	
	public void testF9() {
		spy.reinit(n[1], a[1], null, n[1]);
		assertInOrder(false, n[1], null, null);
	}


	public void testG0() {
		spy.reinit(n[1], a[1], null, n[4]);
		assertInOrder(true, n[1], null, null);
	}

	public void testG1() {
		spy.reinit(n[1], a[1], null, n[4]);
		assertInOrder(true, n[1], "1000", null);
	}

	public void testG2() {
		spy.reinit(n[1], a[1], null, n[4]);
		assertInOrder(false, n[1], "1002", null);
	}

	public void testG3() {
		spy.reinit(n[1], a[1], null, n[4]);
		assertInOrder(false, n[1], null, "1002");
	}

	public void testG4() {
		spy.reinit(n[1], a[1], null, n[4]);
		assertInOrder(false, n[1], null, "1004");
	}

	public void testG5() {
		spy.reinit(n[1], a[1], null, n[4]);
		assertInOrder(true, n[1], null, "1005");
	}

	public void testG6() {
		spy.reinit(n[1], a[1], null, n[4]);
		assertInOrder(true, n[1], "1000", "1005");
	}

	public void testG7() {
		spy.reinit(n[1], a[1], null, n[4]);
		assertInOrder(false, n[1], "1000", "1002");
	}

	public void testG8() {
		spy.reinit(n[1], a[1], null, n[4]);
		assertInOrder(false, n[1], "1002", "1005");
	}
	
	
	public void testH0() {
		spy.reinit(n[8], a[8], n[4], null);
		assertInOrder(true, n[8], null, null);
	}

	public void testH1() {
		spy.reinit(n[8], a[8], n[4], null);
		assertInOrder(true, n[8], "1000", null);
	}

	public void testH2() {
		spy.reinit(n[8], a[8], n[4], null);
		assertInOrder(false, n[8], "1004", null);
	}

	public void testH3() {
		spy.reinit(n[8], a[8], n[4], null);
		assertInOrder(false, n[8], "1007", null);
	}

	public void testH4() {
		spy.reinit(n[8], a[8], n[4], null);
		assertInOrder(true, n[8], null, "1009");
	}

	public void testH5() {
		spy.reinit(n[8], a[8], n[4], null);
		assertInOrder(false, n[8], null, "1005");
	}

	public void testH6() {
		spy.reinit(n[8], a[8], n[4], null);
		assertInOrder(true, n[8], "1001", "1009");
	}

	public void testH7() {
		spy.reinit(n[8], a[8], n[4], null);
		assertInOrder(false, n[8], "1000", "10009");
	}

	public void testH8() {
		spy.reinit(n[8], a[8], n[4], null);
		assertInOrder(false, n[8], "11", "1020");
	}

	
	public void testI0() {
		spy.reinit(n[1], a[1], n[4], n[8]);
		assertInOrder(false, n[1], null, null);
	}
	
	public void testI1() {
		spy.reinit(n[1], a[1], n[4], n[8]);
		assertInOrder(false, n[1], "1000", "1010");
	}
	
	public void testI2() {
		spy.reinit(n[8], a[8], n[1], n[4]);
		assertInOrder(false, n[8], null, null);
	}
	
	public void testI3() {
		spy.reinit(n[8], a[8], n[1], n[4]);
		assertInOrder(false, n[8], "1000", "1010");
	}
	
	public void testI4() {
		spy.reinit(n[8], a[8], n[4], n[14]);
		assertInOrder(true, n[8], null, null);
	}
	
	public void testI5() {
		spy.reinit(n[8], a[8], n[4], n[14]);
		assertInOrder(true, n[8], "1000", "1020");
	}
	
	public void testI6() {
		spy.reinit(n[8], a[8], n[4], n[14]);
		assertInOrder(false, n[8], "1004", "1020");
	}
	
	public void testI7() {
		spy.reinit(n[8], a[8], n[4], n[14]);
		assertInOrder(false, n[8], "1003", "1010");
	}
	
	public void testI8() {
		spy.reinit(n[8], a[8], n[14], n[4]);
		assertInOrder(false, n[8], "1000", "1015");
	}

	
	public void testJ0() {
		spy.reinit(n[8], a[8], n[4], n[14]);
		spy.reinit(n[4], a[4], n[0], null);
		assertInOrder(false, n[8], null, null);
	}
	
	public void testJ1() {
		spy.reinit(n[8], a[8], n[4], n[14]);
		spy.reinit(n[4], a[4], n[0], null);
		assertInOrder(false, n[8], "1000", "1020");
	}
	
	public void testJ2() {
		spy.reinit(n[8], a[8], n[4], n[14]);
		spy.reinit(n[4], a[4], null, n[0]);
		assertInOrder(false, n[8], null, null);
	}
	
	public void testJ3() {
		spy.reinit(n[8], a[8], n[4], n[14]);
		spy.reinit(n[4], a[4], null, n[0]);
		assertInOrder(false, n[8], "1000", "1020");
	}
	
	public void testJ4() {
		spy.reinit(n[8], a[8], n[4], n[14]);
		spy.reinit(n[14], a[14], n[0], null);
		assertInOrder(false, n[8], null, null);
	}
	
	public void testJ5() {
		spy.reinit(n[8], a[8], n[4], n[14]);
		spy.reinit(n[14], a[14], n[0], null);
		assertInOrder(false, n[8], "1000", "1020");
	}
	
	public void testJ6() {
		spy.reinit(n[8], a[8], n[4], n[14]);
		spy.reinit(n[14], a[14], null, n[0]);
		assertInOrder(false, n[8], null, null);
	}
	
	public void testJ7() {
		spy.reinit(n[8], a[8], n[4], n[14]);
		spy.reinit(n[14], a[14], null, n[0]);
		assertInOrder(false, n[8], "1000", "1020");
	}
	
	public void testJ8() {
		spy.reinit(n[8], a[8], n[4], n[14]);
		spy.reinit(n[4], a[4], null, n[8]);
		assertInOrder(false, n[8], "1000", "1020");
	}
	
	public void testJ9() {
		spy.reinit(n[8], a[8], n[4], n[14]);
		spy.reinit(n[14], a[14], n[8], null);
		assertInOrder(false, n[8], "1000", "1020");
	}
	
	
	public void testK0() {
		spy.reinit(n[8],  a[8],  n[4],  null);
		spy.reinit(n[4], a[4], n[1], null);
		assertInOrder(true, n[8], null, null);
	}
	
	public void testK1() {
		spy.reinit(n[8], a[8], n[4], null);
		spy.reinit(n[4], a[4], n[1], null);
		assertInOrder(true, n[8], "1000", "1009");
	}
	
	public void testK2() {
		spy.reinit(n[8], a[8], n[4], null);
		spy.reinit(n[4], a[4], n[1], null);
		assertInOrder(false, n[8], "1001", "1009");
	}
	
	public void testK3() {
		spy.reinit(n[8], a[8], n[4], null);
		spy.reinit(n[4], a[4], n[1], null);
		assertInOrder(false, n[8], "1000", "1005");
	}
	
	public void testK4() {
		spy.reinit(n[8], a[8], n[1], null);
		spy.reinit(n[1], a[1], n[4], null);
		assertInOrder(false, n[8], "1000", "1009");
	}
	
	public void testK5() {
		spy.reinit(n[8], a[8], n[1], null);
		spy.reinit(n[1], a[1], null, n[4]);
		assertInOrder(true, n[8], null, null);
	}
	
	public void testK6() {
		spy.reinit(n[8], a[8], n[1], null);
		spy.reinit(n[1], a[1], null, n[4]);
		assertInOrder(true, n[8], "1000", "1009");
	}
	
	public void testK7() {
		spy.reinit(n[8], a[8], n[1], null);
		spy.reinit(n[1], a[1], null, n[14]);
		assertInOrder(false, n[8], null, null);
	}
	
	public void testK8() {
		spy.reinit(n[8], a[8], n[1], null);
		spy.reinit(n[1], a[1], null, n[14]);
		assertInOrder(false, n[8], "1000", "1009");
	}
	
	public void testK9() {
		spy.reinit(n[8], a[8], n[1], null);
		spy.reinit(n[1], a[1], null, n[4]);
		assertInOrder(false, n[8], "1001", "1009");
	}
	
	
	public void testL0() {
		spy.reinit(n[4], a[4], null, n[8]);
		spy.reinit(n[8], a[8], null, n[14]);
		assertInOrder(true, n[4], null, null);
	}
	
	public void testL1() {
		spy.reinit(n[4], a[4], null, n[8]);
		spy.reinit(n[8], a[8], null, n[14]);
		assertInOrder(true, n[4], "1003", "1020");
	}
	
	public void testL2() {
		spy.reinit(n[4], a[4], null, n[8]);
		spy.reinit(n[8], a[8], null, n[14]);
		assertInOrder(false, n[4], "1004", "1020");
	}
	
	public void testL3() {
		spy.reinit(n[4], a[4], null, n[8]);
		spy.reinit(n[8], a[8], null, n[14]);
		assertInOrder(false, n[4], "1002", "1013");
	}
	
	public void testL4() {
		spy.reinit(n[4], a[4], null, n[14]);
		spy.reinit(n[14], a[14], n[8], null);
		assertInOrder(true, n[4], null, null);
	}
	
	public void testL5() {
		spy.reinit(n[4], a[4], null, n[14]);
		spy.reinit(n[14], a[14], n[8], null);
		assertInOrder(true, n[4], "1003", "1020");
	}
	
	public void testL6() {
		spy.reinit(n[4], a[4], null, n[14]);
		spy.reinit(n[14], a[14], n[8], null);
		assertInOrder(false, n[4], "1003", "1009");
	}
	
	public void testL7() {
		spy.reinit(n[4], a[4], null, n[14]);
		spy.reinit(n[14], a[14], n[8], null);
		assertInOrder(false, n[4], "1005", "1020");
	}
	
	public void testL8() {
		spy.reinit(n[4], a[4], null, n[14]);
		spy.reinit(n[14], a[14], n[1], null);
		assertInOrder(false, n[4], null, null);
	}
	
	public void testL9() {
		spy.reinit(n[4], a[4], null, n[14]);
		spy.reinit(n[14], a[14], n[1], null);
		assertInOrder(false, n[4], "1000", "1020");
	}
	
	
	/// the test tests use the big tree created in setup.
	
	public void testM0() {
		assertInOrder(true, n[6], null, null);
	}
	
	public void testM1() {
		assertInOrder(true, n[6], "1000", "10130");
	}
	
	public void testM2() {
		assertInOrder(false, n[6], "10010", "10130");
	}
	
	public void testM3() {
		assertInOrder(false, n[6], "1000", "10129");
	}
	
	public void testM4() {
		assertInOrder(false, n[6], "1000", "1013");
	}
	
	public void testM5() {
		assertInOrder(false, n[6], "1001", "1013");
	}
	
	
	public void testN0() {
		spy.reinit(n[1], a[1], n[0], null);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testN1() {
		spy.reinit(n[1], a[1], n[1], null);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testN2() {
		spy.reinit(n[0], new Account("Test","10000", Money.ZERO, Money.ZERO), null, null);
		spy.reinit(n[1], a[1], n[0], null);
		assertInOrder(true, n[6], "1000", "1014");
	}
	
	public void testN3() {
		spy.reinit(n[0], new Account("Test","10010", Money.ZERO, Money.ZERO), null, null);
		spy.reinit(n[1], a[1], n[0], null);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testN5() {
		spy.reinit(n[1], a[1], null, n[0]);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testN6() {
		spy.reinit(n[1], a[1], null, n[1]);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testN7() {
		spy.reinit(n[0], new Account("Test","10010", Money.ZERO, Money.ZERO), null, null);
		spy.reinit(n[1], a[1], null, n[0]);
		assertInOrder(true, n[6], "1000", "1014");
	}
	
	public void testN8() {
		spy.reinit(n[0], new Account("Test","10001", Money.ZERO, Money.ZERO), null, null);
		spy.reinit(n[1], a[1], null, n[0]);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testN9() {
		spy.reinit(n[0], new Account("Test","10020", Money.ZERO, Money.ZERO), null, null);
		spy.reinit(n[1], a[1], null, n[0]);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	
	public void testO0() {
		spy.reinit(n[3], a[3], n[0], n[4]);
		assertInOrder(false, n[6], "1000", "1014");		
	}
	
	public void testO1() {
		spy.reinit(n[3], a[3], n[2], n[4]);
		assertInOrder(false, n[6], "1000", "1014");	
	}
	
	public void testO2() {
		spy.reinit(n[0], new Account("Test","10025", Money.ZERO, Money.ZERO), null, null);
		spy.reinit(n[3], a[3], n[0], n[4]);
		assertInOrder(true, n[6], "1000", "1014");		
	}
	
	public void testO3() {
		spy.reinit(n[0], new Account("Test","1002", Money.ZERO, Money.ZERO), null, null);
		spy.reinit(n[3], a[3], n[0], n[4]);
		assertInOrder(false, n[6], "1000", "1014");		
	}
	
	public void testO4() {
		spy.reinit(n[0], new Account("Test","10030", Money.ZERO, Money.ZERO), null, null);
		spy.reinit(n[3], a[3], n[0], n[4]);
		assertInOrder(false, n[6], "1000", "1014");		
	}
	
	
	public void testP0() {
		spy.reinit(n[4], a[4], n[0], null);
		assertInOrder(false, n[6], "1000", "1014");			
	}
	
	public void testP1() {
		spy.reinit(n[4], a[4], n[3], null);
		assertInOrder(false, n[6], "1000", "1014");			
	}
	
	public void testP2() {
		spy.reinit(n[0], new Account("Test","10030", Money.ZERO, Money.ZERO), null, null);
		spy.reinit(n[4], a[4], n[0], null);
		assertInOrder(true, n[6], "1000", "1014");			
	}
	
	public void testP3() {
		spy.reinit(n[0], new Account("Test","10029", Money.ZERO, Money.ZERO), null, null);
		spy.reinit(n[4], a[4], n[0], null);
		assertInOrder(false, n[6], "1000", "1014");			
	}
	
	public void testP4() {
		spy.reinit(n[0], new Account("Test","1004", Money.ZERO, Money.ZERO), null, null);
		spy.reinit(n[4], a[4], n[0], null);
		assertInOrder(false, n[6], "1000", "1014");			
	}
	
	public void testP5() {
		spy.reinit(n[4], a[4], null, n[0]);
		assertInOrder(false, n[6], "1000", "1014");			
	}
	
	public void testP6() {
		spy.reinit(n[4], a[4], null, n[5]);
		assertInOrder(false, n[6], "1000", "1014");			
	}
	
	public void testP7() {
		spy.reinit(n[0], new Account("Test", "10040", Money.ZERO, Money.ZERO), null, null);
		spy.reinit(n[4], a[4], null, n[0]);
		assertInOrder(true, n[6], "1000", "1014");			
	}
	
	public void testP8() {
		spy.reinit(n[0], new Account("Test", "1004", Money.ZERO, Money.ZERO), null, null);
		spy.reinit(n[4], a[4], null, n[0]);
		assertInOrder(false, n[6], "1000", "1014");			
	}
	
	public void testP9() {
		spy.reinit(n[0], new Account("Test", "10050", Money.ZERO, Money.ZERO), null, null);
		spy.reinit(n[4], a[4], null, n[0]);
		assertInOrder(false, n[6], "1000", "1014");			
	}

	
	public void testQ0() {
		spy.reinit(n[5], a[5], n[3], n[0]);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testQ1() {
		spy.reinit(n[5], a[5], n[3], n[6]);
		assertInOrder(false, n[6], "1000", "1014");
	}

	public void testQ2() {
		spy.reinit(n[0], new Account("Test", "10050", Money.ZERO, Money.ZERO), null, null);
		spy.reinit(n[5], a[5], n[3], n[0]);
		assertInOrder(true, n[6], "1000", "1014");
	}

	public void testQ3() {
		spy.reinit(n[0], new Account("Test", "10045", Money.ZERO, Money.ZERO), null, null);
		spy.reinit(n[5], a[5], n[3], n[0]);
		assertInOrder(false, n[6], "1000", "1014");
	}

	public void testQ4() {
		spy.reinit(n[0], new Account("Test", "1007", Money.ZERO, Money.ZERO), null, null);
		spy.reinit(n[5], a[5], n[3], n[0]);
		assertInOrder(false, n[6], "1000", "1014");
	}

	
	public void testR0() {
		spy.reinit(n[7], a[7], n[0], n[8]);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testR1() {
		spy.reinit(n[7], a[7], n[6], n[8]);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testR2() {
		spy.reinit(n[0], new Account("Test", "10065", Money.ZERO, Money.ZERO), null, null);
		spy.reinit(n[7], a[7], n[0], n[8]);
		assertInOrder(true, n[6], "1000", "1014");
	}
	
	public void testR3() {
		spy.reinit(n[0], new Account("Test", "1006", Money.ZERO, Money.ZERO), null, null);
		spy.reinit(n[7], a[7], n[0], n[8]);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testR4() {
		spy.reinit(n[0], new Account("Test", "10070", Money.ZERO, Money.ZERO), null, null);
		spy.reinit(n[7], a[7], n[0], n[8]);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	
	public void testS0() {
		spy.reinit(n[8], a[8], n[0], null);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testS1() {
		spy.reinit(n[8], a[8], n[7], null);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testS2() {
		spy.reinit(n[8], a[8], n[0], null);
		spy.reinit(n[0], new Account("Test", "10070", Money.ZERO, Money.ZERO), null, null);
		assertInOrder(true, n[6], "1000", "1014");
	}
	
	public void testS3() {
		spy.reinit(n[8], a[8], n[0], null);
		spy.reinit(n[0], new Account("Test", "10069", Money.ZERO, Money.ZERO), null, null);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testS4() {
		spy.reinit(n[8], a[8], n[0], null);
		spy.reinit(n[0], new Account("Test", "1010", Money.ZERO, Money.ZERO), null, null);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testS5() {
		spy.reinit(n[8], a[8], null, n[0]);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testS6() {
		spy.reinit(n[8], a[8], null, n[9]);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testS7() {
		spy.reinit(n[8], a[8], null, n[0]);
		spy.reinit(n[0], new Account("Test", "10085", Money.ZERO, Money.ZERO), null, null);
		assertInOrder(true, n[6], "1000", "1014");
	}
	
	public void testS8() {
		spy.reinit(n[8], a[8], null, n[0]);
		spy.reinit(n[0], new Account("Test", "10008", Money.ZERO, Money.ZERO), null, null);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testS9() {
		spy.reinit(n[8], a[8], null, n[0]);
		spy.reinit(n[0], new Account("Test", "10095", Money.ZERO, Money.ZERO), null, null);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	
	public void testT0() {
		spy.reinit(n[9], a[9], n[7], n[0]);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testT1() {
		spy.reinit(n[9], a[9], n[7], n[10]);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testT2() {
		spy.reinit(n[9], a[9], n[7], n[0]);
		spy.reinit(n[0], new Account("Test", "10095", Money.ZERO, Money.ZERO), null, null);
		assertInOrder(true, n[6], "1000", "1014");
	}
	
	public void testT3() {
		spy.reinit(n[9], a[9], n[7], n[0]);
		spy.reinit(n[0], new Account("Test", "10095", Money.ZERO, Money.ZERO), null, null);
		assertInOrder(true, n[6], "1000", "1014");
	}
	
	public void testT4() {
		spy.reinit(n[9], a[9], n[7], n[0]);
		spy.reinit(n[0], new Account("Test", "10095", Money.ZERO, Money.ZERO), null, null);
		assertInOrder(true, n[6], "1000", "1014");
	}
	
	
	
	public void testU0() {
		spy.reinit(n[11], a[11], n[0], null);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testU1() {
		spy.reinit(n[11], a[11], n[10], null);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testU2() {
		spy.reinit(n[11], a[11], n[0], null);
		spy.reinit(n[0], new Account("Test", "10101", Money.ZERO, Money.ZERO), null, null);
		assertInOrder(true, n[6], "1000", "1014");
	}
	
	public void testU3() {
		spy.reinit(n[11], a[11], n[0], null);
		spy.reinit(n[0], new Account("Test", "1010", Money.ZERO, Money.ZERO), null, null);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testU4() {
		spy.reinit(n[11], a[11], n[0], null);
		spy.reinit(n[0], new Account("Test", "10110", Money.ZERO, Money.ZERO), null, null);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testU5() {
		spy.reinit(n[11], a[11], null, n[0]);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testU6() {
		spy.reinit(n[11], a[11], null, n[12]);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testU7() {
		spy.reinit(n[11], a[11], null, n[0]);
		spy.reinit(n[0], new Account("Test", "10115", Money.ZERO, Money.ZERO), null, null);
		assertInOrder(true, n[6], "1000", "1014");
	}
	
	public void testU8() {
		spy.reinit(n[11], a[11], null, n[0]);
		spy.reinit(n[0], new Account("Test", "10105", Money.ZERO, Money.ZERO), null, null);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testU9() {
		spy.reinit(n[11], a[11], null, n[0]);
		spy.reinit(n[0], new Account("Test", "10125", Money.ZERO, Money.ZERO), null, null);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	
	public void testV0() {
		spy.reinit(n[13], a[13], n[0], null);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testV1() {
		spy.reinit(n[13], a[13], n[12], null);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testV2() {
		spy.reinit(n[13], a[13], n[0], null);
		spy.reinit(n[0], new Account("Test", "10121", Money.ZERO, Money.ZERO), null, null);
		assertInOrder(true, n[6], "1000", "1014");
	}
	
	public void testV3() {
		spy.reinit(n[13], a[13], n[0], null);
		spy.reinit(n[0], new Account("Test", "1011", Money.ZERO, Money.ZERO), null, null);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testV4() {
		spy.reinit(n[13], a[13], n[0], null);
		spy.reinit(n[0], new Account("Test", "10130", Money.ZERO, Money.ZERO), null, null);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testV5() {
		spy.reinit(n[13], a[13], null, n[0]);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testV6() {
		spy.reinit(n[13], a[13], null, n[13]);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testV7() {
		spy.reinit(n[13], a[13], null, n[0]);
		spy.reinit(n[0], new Account("Test", "10135", Money.ZERO, Money.ZERO), null, null);
		assertInOrder(true, n[6], "1000", "1014");
	}
	
	public void testV8() {
		spy.reinit(n[13], a[13], null, n[0]);
		spy.reinit(n[0], new Account("Test", "10125", Money.ZERO, Money.ZERO), null, null);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	public void testV9() {
		spy.reinit(n[13], a[13], null, n[0]);
		spy.reinit(n[0], new Account("Test", "10145", Money.ZERO, Money.ZERO), null, null);
		assertInOrder(false, n[6], "1000", "1014");
	}
	
	
	public void testW0() {
		spy.reinit(n[1], a[1], n[0], n[2]);
		spy.reinit(n[11], a[11], n[10], n[0]);
		assertInOrder(false, n[6], "1000", "1020");
	}
	
	
	public void testX0() {
		self = spy.makeBank(null);
		assertWellFormed(true, self);
	}
	
	public void testX1() {
		self = spy.makeBank(n[1]);
		assertWellFormed(true, self);
	}
	
	public void testX2() {
		self = spy.makeBank(n[0]);
		assertWellFormed(false, self);
	}
	
	public void testX3() {
		self = spy.makeBank(n[6]);
		assertWellFormed(true, self);
	}
	
	public void testX4() {
		self = spy.makeBank(n[6]);
		spy.reinit(n[7], a[7], n[6], n[8]);
		spy.reinit(n[4], a[4], n[0], null);
		assertWellFormed(false, self);
	}
}