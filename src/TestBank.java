import java.util.Arrays;
import java.util.List;

import edu.uwm.cs.junit.LockedTestCase;
import edu.uwm.cs351.Account;
import edu.uwm.cs351.Auditor;
import edu.uwm.cs351.Bank;
import edu.uwm.cs351.Money;

public class TestBank extends LockedTestCase {

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
	
	protected void assertDigitString(String string) {
		for (int i=0; i < string.length(); ++i) {
			int ch = string.charAt(i);
			assertTrue("Should be a string of digits: " + string, ch >= '0' && ch <= '9');
		}
	}

	protected void assertContains(List<?> poss, Object testValue) {
		assertTrue("List " + poss + " does not contain " + testValue, poss.contains(testValue));
	}
	
	protected List<String> l(String ss) {
		if (ss.startsWith("[") && ss.endsWith("]")) {
			return Arrays.asList(ss.substring(1,ss.length()-1).split(","));
		}
		throw new IllegalArgumentException("not formated as list");
	}
	
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

	protected String trail2string(Account[] tr) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i=0; i < tr.length; ++i) {
			if (tr[i] == null) break;
			if (i > 0) sb.append(",");
			sb.append(tr[i].getID());
		}
		sb.append(']');
		return sb.toString();
	}
	
	Bank self;
	Account[] trail;
	
	@Override
	protected void setUp() {
		try {
			assert 1/0 == 42 : "OK";
			System.err.println("Assertions must be enabled to use this test case.");
			System.err.println("In Eclipse: add -ea in the VM Arguments box under Run>Run Configurations>Arguments");
			assertFalse("Assertions must be -ea enabled in the Run Configuration>Arguments>VM Arguments",true);
		} catch (ArithmeticException ex) {
			// GOOD
		}
		self = new Bank();
	}
	
	
	
	/// Locked tests 
	
	public void test() {
		Account a = self.open("J.Q. Public", "2030", new Money(-1000.00), new Money(-25.00));
		assertEquals(Ts(1042987290), a.getID());
		a = self.open("J. Q. Public", "123", new Money(0.0), new Money(200.00));
		// What are the possibilities for the account number?
		// List in order in brackets separated by commas, e.g. [5678,5679]
		// Hint there are 10 possibilities
		assertContains(l(Ts(1183843932)), a.getID());
		a = self.open("J. Q. Public", "53211", new Money(1000.00), new Money(1234));
		assertEquals(Ts(421774707), a.getID());
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), "15");
		// What order will accounts be audited, if we start with "15" ?
		// Put in brackets with commas between account IDs
		assertEquals(Ts(6682764), trail2string(trail));
	}
	
	
	// normal tests
	
	public void test00() {
		Account a = self.open("Avery", "", new Money(3.14), new Money(-0.88));
		assertEquals("Avery", a.getOwner());
		assertEquals(new Money(3.14), a.getMinimum());
		assertEquals(new Money(-0.88), a.getCurrent());
		assertEquals(Account.MIN_ACCOUNT_ID, a.getID().length());
		assertDigitString(a.getID());
	}
	
	public void test01() {
		Account a = self.open("Bobi", "5", new Money(-5.67), new Money(8.88));
		assertEquals("Bobi", a.getOwner());
		assertEquals(new Money(-5.67), a.getMinimum());
		assertEquals(new Money(8.88), a.getCurrent());
		assertEquals(Account.MIN_ACCOUNT_ID, a.getID().length());
		assertDigitString(a.getID());
		assertTrue(a.getID() + " doesn't start with '5'", a.getID().startsWith("5"));
	}
	
	public void test02() {
		Account a = self.open("Cris", "23", new Money(1.23), new Money(-100));
		assertEquals(Account.MIN_ACCOUNT_ID, a.getID().length());
		assertDigitString(a.getID());
		assertTrue(a.getID() + " doesn't start with '23'", a.getID().startsWith("23"));
	}
	
	public void test03() {
		Account a = self.open("", "409", new Money(4.09), new Money(409));
		assertEquals(4, a.getID().length());
		assertDigitString(a.getID());
		assertTrue(a.getID() + " doens't start with '409'", a.getID().startsWith("409"));		
	}
	
	public void test04() {
		Account a = self.open("JTB", "9182", Money.ZERO, Money.ZERO);
		assertEquals("9182", a.getID());
	}
	
	public void test05() {
		Account a = self.open("UWM", "12345", Money.ZERO, Money.ZERO);
		assertEquals("12345", a.getID());
	}
	
	public void test06() {
		Account a1 = self.open("test", "3671", Money.ZERO, Money.ZERO);
		Account a2 = self.open("text", "3671", Money.ZERO, Money.ZERO);
		assertEquals("3671", a1.getID());
		assertEquals(5, a2.getID().length());
		assertTrue(a2.getID() + " doesn't start with '3671'", a2.getID().startsWith("3671"));
	}
	
	public void test07() {
		Account a1 = self.open("tent", "4096", Money.ZERO, Money.ZERO);
		Account a2 = self.open("test", "65536", Money.ZERO, Money.ZERO);
		Account a3 = self.open("text", "128", Money.ZERO, Money.ZERO);
		assertEquals("4096", a1.getID());
		assertEquals("65536", a2.getID());
		assertEquals(4, a3.getID().length());
		assertDigitString(a3.getID());
		assertTrue(a3.getID() + " doesn't start with '128'", a3.getID().startsWith("128"));
	}
	
	protected Account open(String prefix) {
		return self.open("Test", prefix, Money.ZERO, Money.ZERO);
	}
	
	public void test08() {
		open("1280");
		open("1281");
		open("1282");
		open("1283");
		open("1284");
		open("1285");
		open("1286");
		open("1287");
		open("1288");
		open("1289");
		Account a = open("128");
		assertEquals(5, a.getID().length());
		assertDigitString(a.getID());
	}
	
	public void test09() {
		Account[] acct = new Account[12];
		int found = 0;
		for (int i=0; i < 12; ++i) {
			acct[i] = open("3141");
			if (acct[i].getID().length() > 5) ++ found;
		}
		assertTrue("Should have created at least one string with 6 digits", found > 0);
		assertTrue("Should not have created so many with 6 digits", found < 8);
	}
	
	
	/// test1x: audit tests with passive auditor
	
	public void test10() {
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), null);
		assertEquals("[]", trail2string(trail));
	}
	
	public void test11() {
		trail = new Account[10];
		open("7709");
		self.audit(new TrailerAuditor(trail), null);
		assertEquals("[7709]", trail2string(trail));
	}
	
	public void test12() {
		trail = new Account[10];
		open("0112");
		open("8089");
		self.audit(new TrailerAuditor(trail), null);
		assertEquals("[0112,8089]", trail2string(trail));
	}
	
	public void test13() {
		open("53211");
		open("3200");
		open("9999");
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), null);
		assertEquals("[3200,53211,9999]", trail2string(trail));
	}
	
	public void test14() {
		open("1234");
		open("2345");
		open("3456");
		open("4567");
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), null);
		assertEquals("[1234,2345,3456,4567]", trail2string(trail));
	}
	
	public void test15() {
		open("9876");
		open("8765");
		open("7654");
		open("6543");
		open("5432");
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), null);
		assertEquals("[5432,6543,7654,8765,9876]", trail2string(trail));
	}
	
	public void test16() {
		open("6060");
		open("3333");
		open("54321");
		open("8428");
		open("70707");
		open("1122");
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), null);
		assertEquals("[1122,3333,54321,6060,70707,8428]", trail2string(trail));
	}
	
	public void test17() {
		open("3142020");
		open("741776");
		open("1848");
		open("1956");
		open("641989");
		open("9112001");
		open("2202022");
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), null);
		assertEquals("[1848,1956,2202022,3142020,641989,741776,9112001]", trail2string(trail));
	}
	
	public void test18() {
		open("1818");
		open("181818");
		open("18181818");
		open("1818181818");
		open("18181");
		open("1818181");
		open("181818181");
		open("18181818181");
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), null);
		assertEquals("[1818,18181,181818,1818181,18181818,181818181,1818181818,18181818181]", trail2string(trail));
	}
	
	public void test19() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), null);
		assertEquals("[1001,1002,1003,1004,1005,1006,1007,1008,1009]", trail2string(trail));
	}
	
	
	/// test2x: testing auditors that want to leave early
	
	public void test20() {
		open("0000");
		trail = new Account[1];
		self.audit(new TrailerAuditor(trail), null);
		assertEquals("[0000]", trail2string(trail));
	}
	
	public void test21() {
		open("2001");
		open("2100");
		trail = new Account[1];
		self.audit(new TrailerAuditor(trail), null);
		assertEquals("[2001]", trail2string(trail));
	}
	
	public void test22() {
		open("2022");
		open("2002");
		trail = new Account[1];
		self.audit(new TrailerAuditor(trail), null);
		assertEquals("[2002]", trail2string(trail));
	}
	
	public void test23() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[2];
		self.audit(new TrailerAuditor(trail), null);
		assertEquals("[1001,1002]", trail2string(trail));
	}
	
	public void test24() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[3];
		self.audit(new TrailerAuditor(trail), null);
		assertEquals("[1001,1002,1003]", trail2string(trail));
	}
	
	public void test25() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[4];
		self.audit(new TrailerAuditor(trail), null);
		assertEquals("[1001,1002,1003,1004]", trail2string(trail));
	}
	
	public void test26() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[5];
		self.audit(new TrailerAuditor(trail), null);
		assertEquals("[1001,1002,1003,1004,1005]", trail2string(trail));
	}
	
	public void test27() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[6];
		self.audit(new TrailerAuditor(trail), null);
		assertEquals("[1001,1002,1003,1004,1005,1006]", trail2string(trail));
	}
	
	public void test28() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[7];
		self.audit(new TrailerAuditor(trail), null);
		assertEquals("[1001,1002,1003,1004,1005,1006,1007]", trail2string(trail));
	}
	
	public void test29() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[8];
		self.audit(new TrailerAuditor(trail), null);
		assertEquals("[1001,1002,1003,1004,1005,1006,1007,1008]", trail2string(trail));
	}
	
	
	/// test3x: starting audit (small cases)
	
	public void test30() {
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), "1234");
		assertEquals("[]", trail2string(trail));		
	}
	
	public void test31() {
		open("3131");
		trail = new Account[7];
		self.audit(new TrailerAuditor(trail), "1234");
		assertEquals("[3131]", trail2string(trail));
	}
	
	public void test32() {
		open("3232");
		trail = new Account[7];
		self.audit(new TrailerAuditor(trail), "3232");
		assertEquals("[3232]", trail2string(trail));
	}
	
	public void test33() {
		open("3333");
		trail = new Account[7];
		self.audit(new TrailerAuditor(trail), "33333");
		assertEquals("[]", trail2string(trail));
	}
	
	public void test34() {
		open("3434");
		open("3000");
		trail = new Account[7];
		self.audit(new TrailerAuditor(trail), "3333");
		assertEquals("[3434]", trail2string(trail));
	}
	
	public void test35() {
		open("3535");
		open("7070");
		trail = new Account[7];
		self.audit(new TrailerAuditor(trail), "4444");
		assertEquals("[7070]", trail2string(trail));
	}
	
	public void test36() {
		open("3636");
		open("6363");
		trail = new Account[7];
		self.audit(new TrailerAuditor(trail), "3636");
		assertEquals("[3636,6363]", trail2string(trail));
	}
	
	public void test37() {
		open("3737");
		open("7373");
		trail = new Account[7];
		self.audit(new TrailerAuditor(trail), "8888");
		assertEquals("[]", trail2string(trail));
	}
	
	public void test38() {
		open("3838");
		open("3030");
		open("8383");
		trail = new Account[7];
		self.audit(new TrailerAuditor(trail), "3333");
		assertEquals("[3838,8383]", trail2string(trail));
	}
	
	public void test39() {
		open("3939");
		open("3030");
		open("9393");
		trail = new Account[7];
		self.audit(new TrailerAuditor(trail), "9090");
		assertEquals("[9393]", trail2string(trail));
	}
	

	public void test40() {
		assertException(NullPointerException.class, () -> self.audit(null,null));
	}
	
	/// test4x: larger audit tests with equal keys
	
	public void test41() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), "1001");
		assertEquals("[1001,1002,1003,1004,1005,1006,1007,1008,1009]", trail2string(trail));
	}

	public void test42() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), "1002");
		assertEquals("[1002,1003,1004,1005,1006,1007,1008,1009]", trail2string(trail));
	}
	
	public void test43() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), "1003");
		assertEquals("[1003,1004,1005,1006,1007,1008,1009]", trail2string(trail));
	}
	
	public void test44() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), "1004");
		assertEquals("[1004,1005,1006,1007,1008,1009]", trail2string(trail));
	}
	
	public void test45() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), "1005");
		assertEquals("[1005,1006,1007,1008,1009]", trail2string(trail));
	}
	
	public void test46() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), "1006");
		assertEquals("[1006,1007,1008,1009]", trail2string(trail));
	}
	
	public void test47() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), "1007");
		assertEquals("[1007,1008,1009]", trail2string(trail));
	}
	
	public void test48() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), "1008");
		assertEquals("[1008,1009]", trail2string(trail));
	}
	
	public void test49() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), "1009");
		assertEquals("[1009]", trail2string(trail));
	}

	
	/// test5x: audit starts with missing keys
	
	public void test50() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), "1000");
		assertEquals("[1001,1002,1003,1004,1005,1006,1007,1008,1009]", trail2string(trail));
	}
	
	public void test51() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), "10015");
		assertEquals("[1002,1003,1004,1005,1006,1007,1008,1009]", trail2string(trail));
	}

	public void test52() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), "10020");
		assertEquals("[1003,1004,1005,1006,1007,1008,1009]", trail2string(trail));
	}
	
	public void test53() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), "10033");
		assertEquals("[1004,1005,1006,1007,1008,1009]", trail2string(trail));
	}
	
	public void test54() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), "10049");
		assertEquals("[1005,1006,1007,1008,1009]", trail2string(trail));
	}
	
	public void test55() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), "10055");
		assertEquals("[1006,1007,1008,1009]", trail2string(trail));
	}
	
	public void test56() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), "10061");
		assertEquals("[1007,1008,1009]", trail2string(trail));
	}
	
	public void test57() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), "10078");
		assertEquals("[1008,1009]", trail2string(trail));
	}
	
	public void test58() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), "10080");
		assertEquals("[1009]", trail2string(trail));
	}
	
	public void test59() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[10];
		self.audit(new TrailerAuditor(trail), "1010");
		assertEquals("[]", trail2string(trail));
	}
	
	
	public void test60() {
		assertException(NullPointerException.class, () -> open(null));
	}
	
	
	/// test6x: hit tests with n = 1
	
	
	public void test61() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[1];
		self.audit(new TrailerAuditor(trail), "1001");
		assertEquals("[1001]", trail2string(trail));
	}

	public void test62() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[1];
		self.audit(new TrailerAuditor(trail), "1002");
		assertEquals("[1002]", trail2string(trail));
	}
	
	public void test63() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[1];
		self.audit(new TrailerAuditor(trail), "1003");
		assertEquals("[1003]", trail2string(trail));
	}
	
	public void test64() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[1];
		self.audit(new TrailerAuditor(trail), "1004");
		assertEquals("[1004]", trail2string(trail));
	}
	
	public void test65() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[1];
		self.audit(new TrailerAuditor(trail), "1005");
		assertEquals("[1005]", trail2string(trail));
	}
	
	public void test66() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[1];
		self.audit(new TrailerAuditor(trail), "1006");
		assertEquals("[1006]", trail2string(trail));
	}
	
	public void test67() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[1];
		self.audit(new TrailerAuditor(trail), "1007");
		assertEquals("[1007]", trail2string(trail));
	}
	
	public void test68() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[1];
		self.audit(new TrailerAuditor(trail), "1008");
		assertEquals("[1008]", trail2string(trail));
	}
	
	public void test69() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[1];
		self.audit(new TrailerAuditor(trail), "1009");
		assertEquals("[1009]", trail2string(trail));
	}

	
	/// test7x: tests of audit starting at non-key with n = 1
	
	public void test70() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[1];
		self.audit(new TrailerAuditor(trail), "1000");
		assertEquals("[1001]", trail2string(trail));
	}
	
	public void test71() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[1];
		self.audit(new TrailerAuditor(trail), "10015");
		assertEquals("[1002]", trail2string(trail));
	}

	public void test72() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[1];
		self.audit(new TrailerAuditor(trail), "10020");
		assertEquals("[1003]", trail2string(trail));
	}
	
	public void test73() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[1];
		self.audit(new TrailerAuditor(trail), "10033");
		assertEquals("[1004]", trail2string(trail));
	}
	
	public void test74() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[1];
		self.audit(new TrailerAuditor(trail), "10049");
		assertEquals("[1005]", trail2string(trail));
	}
	
	public void test75() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[1];
		self.audit(new TrailerAuditor(trail), "10055");
		assertEquals("[1006]", trail2string(trail));
	}
	
	public void test76() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[1];
		self.audit(new TrailerAuditor(trail), "10061");
		assertEquals("[1007]", trail2string(trail));
	}
	
	public void test77() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[1];
		self.audit(new TrailerAuditor(trail), "10078");
		assertEquals("[1008]", trail2string(trail));
	}
	
	public void test78() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[1];
		self.audit(new TrailerAuditor(trail), "10080");
		assertEquals("[1009]", trail2string(trail));
	}

	
	public void test79() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		assertException(NullPointerException.class, () -> self.open("Test", "100", null, Money.ZERO));
		assertException(NullPointerException.class, () -> self.open(null, "100", Money.ZERO, Money.ZERO));
	}
	
	/// test8x: starting on elements with n = 2
	
	public void test81() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[2];
		self.audit(new TrailerAuditor(trail), "1001");
		assertEquals("[1001,1002]", trail2string(trail));
	}

	public void test82() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[2];
		self.audit(new TrailerAuditor(trail), "1002");
		assertEquals("[1002,1003]", trail2string(trail));
	}
	
	public void test83() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[2];
		self.audit(new TrailerAuditor(trail), "1003");
		assertEquals("[1003,1004]", trail2string(trail));
	}
	
	public void test84() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[2];
		self.audit(new TrailerAuditor(trail), "1004");
		assertEquals("[1004,1005]", trail2string(trail));
	}
	
	public void test85() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[2];
		self.audit(new TrailerAuditor(trail), "1005");
		assertEquals("[1005,1006]", trail2string(trail));
	}
	
	public void test86() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[2];
		self.audit(new TrailerAuditor(trail), "1006");
		assertEquals("[1006,1007]", trail2string(trail));
	}
	
	public void test87() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[2];
		self.audit(new TrailerAuditor(trail), "1007");
		assertEquals("[1007,1008]", trail2string(trail));
	}
	
	public void test88() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[2];
		self.audit(new TrailerAuditor(trail), "1008");
		assertEquals("[1008,1009]", trail2string(trail));
	}
	
	public void test89() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[4];
		self.audit(new TrailerAuditor(trail), "1005");
		assertEquals("[1005,1006,1007,1008]", trail2string(trail));
	}

	
	/// test9x: larger tests with non-key starts
	
	
	public void test90() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[3];
		self.audit(new TrailerAuditor(trail), "1000");
		assertEquals("[1001,1002,1003]", trail2string(trail));
	}
	
	public void test91() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[3];
		self.audit(new TrailerAuditor(trail), "10015");
		assertEquals("[1002,1003,1004]", trail2string(trail));
	}

	public void test92() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[3];
		self.audit(new TrailerAuditor(trail), "10020");
		assertEquals("[1003,1004,1005]", trail2string(trail));
	}
	
	public void test93() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[3];
		self.audit(new TrailerAuditor(trail), "10033");
		assertEquals("[1004,1005,1006]", trail2string(trail));
	}
	
	public void test94() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[3];
		self.audit(new TrailerAuditor(trail), "10049");
		assertEquals("[1005,1006,1007]", trail2string(trail));
	}
	
	public void test95() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[3];
		self.audit(new TrailerAuditor(trail), "10055");
		assertEquals("[1006,1007,1008]", trail2string(trail));
	}
	
	public void test96() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[3];
		self.audit(new TrailerAuditor(trail), "10061");
		assertEquals("[1007,1008,1009]", trail2string(trail));
	}
	
	public void test97() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[4];
		self.audit(new TrailerAuditor(trail), "10044");
		assertEquals("[1005,1006,1007,1008]", trail2string(trail));
	}
	
	public void test98() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[5];
		self.audit(new TrailerAuditor(trail), "10033");
		assertEquals("[1004,1005,1006,1007,1008]", trail2string(trail));
	}
	
	public void test99() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		trail = new Account[6];
		self.audit(new TrailerAuditor(trail), "10020");
		assertEquals("[1003,1004,1005,1006,1007,1008]", trail2string(trail));
	}

}
