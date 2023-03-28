import java.util.NoSuchElementException;
import java.util.function.Supplier;

import edu.uwm.cs.junit.LockedTestCase;
import edu.uwm.cs351.Account;
import edu.uwm.cs351.Bank;
import edu.uwm.cs351.BankAdditions;
import edu.uwm.cs351.Money;

public class TestBankAdditions extends LockedTestCase {
	
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

	Bank bank;
	BankAdditions self;
	
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
		bank = new Bank();
		self = new BankAdditions(bank);
	}

	protected String string(Supplier<?> supp) {
		try {
			Object x = supp.get();
			return ""+x;
		} catch (RuntimeException ex) {
			return ex.getClass().getSimpleName();
		}
	}
	
	/// test: Locked tests
	
	public void test() {
		bank.open("J. Q. Public", "1234", new Money(-10000), new Money(-1000));
		bank.open("J. Q. Public", "1239", new Money(20.00), new Money(410.00));
		bank.open("J. Q. Public", "53211", Money.ZERO, new Money(40));
		assertEquals("3", string(() -> self.count()));
		// "string(...)" converts to string, *or* shows name of exception thrown. 
		// Remember that an account prints as <owner> <last 4 digits>
		assertEquals(Ts(759664936), string(() -> self.getAccount("53211")));
		assertEquals(Ts(843720236), string(() -> self.getAccount("123")));
		// Remember how Money prints!
		assertEquals(Ts(629464292), string( () -> self.netAssets()));
		assertEquals(Ts(1514335264), string( () -> self.getBalanceSum("123")));
	}
	
	
	/// testCx: testing count

	protected Account open(String prefix) {
		return bank.open("Test", prefix, Money.ZERO, Money.ZERO);
	}
	
	
	public void testCA() {
		assertEquals(0, self.count());
	}
	
	public void testCB() {
		open("3612");
		assertEquals(1, self.count());
	}
	
	public void testCC() {
		open("5555");
		open("5555");
		assertEquals(2, self.count());
	}
	
	public void testCD() {
		open("1");
		open("2");
		open("3");
		assertEquals(3, self.count());
	}
	
	public void testCE() {
		open("3333");
		open("2222");
		open("5555");
		open("1111");
		open("4444");
		assertEquals(5, self.count());
	}
	
	
	/// testGx: testing getAccount
	
	public void testGA() {
		assertException(NoSuchElementException.class, () -> self.getAccount("1234"));
	}
	
	public void testGB() {
		open("128");
		assertException(NoSuchElementException.class, () -> self.getAccount("128"));		
	}
	
	public void testGC() {
		Account a = open("256");
		assertSame(a, self.getAccount(a.getID()));
	}
	
	public void testGD() {
		open("4444");
		Account a = open("2030");
		assertSame(a, self.getAccount("2030"));
	}
	
	public void testGE() {
		Account a = open("53211");
		open("2023");
		assertSame(a, self.getAccount("53211"));
	}
	
	public void testGF() {
		open("1006");
		open("1003");
		Account a = open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		assertSame(a, self.getAccount("1001"));
	}
	
	public void testGG() {
		open("1006");
		open("1003");
		open("1001");
		Account a = open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		assertSame(a, self.getAccount("1002"));
	}
	
	public void testGH() {
		open("1006");
		Account a = open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		assertSame(a, self.getAccount("1003"));
	}
	
	public void testGI() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		Account a = open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		assertSame(a, self.getAccount("1004"));
	}
	
	public void testGJ() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		Account a = open("1005");
		open("1009");
		open("1007");
		open("1008");
		assertSame(a, self.getAccount("1005"));
	}
	
	public void testGK() {
		Account a = open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		assertSame(a, self.getAccount("1006"));
	}
	
	public void testGL() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		Account a = open("1007");
		open("1008");
		assertSame(a, self.getAccount("1007"));
	}
	
	public void testGM() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		Account a = open("1008");
		assertSame(a, self.getAccount("1008"));
	}
	
	public void testGN() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		Account a = open("1009");
		open("1007");
		open("1008");
		assertSame(a, self.getAccount("1009"));
	}
	
	public void testGO() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		assertException(NoSuchElementException.class, () -> self.getAccount("1000"));
	}
	
	public void testGP() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		assertException(NoSuchElementException.class, () -> self.getAccount("10011"));
	}
	
	public void testGQ() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		assertException(NoSuchElementException.class, () -> self.getAccount("100200"));
	}
	
	public void testGR() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		assertException(NoSuchElementException.class, () -> self.getAccount("10033"));
	}
	
	public void testGS() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		assertException(NoSuchElementException.class, () -> self.getAccount("10049"));
	}
	
	public void testGT() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		assertException(NoSuchElementException.class, () -> self.getAccount("10055"));
	}
	
	public void testGU() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		assertException(NoSuchElementException.class, () -> self.getAccount("10061"));
	}
	
	public void testGV() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		assertException(NoSuchElementException.class, () -> self.getAccount("10079"));
	}
	
	public void testGW() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		assertException(NoSuchElementException.class, () -> self.getAccount("100088"));
	}
	
	public void testGX() {
		open("1006");
		open("1003");
		open("1001");
		open("1002");
		open("1004");
		open("1005");
		open("1009");
		open("1007");
		open("1008");
		assertException(NoSuchElementException.class, () -> self.getAccount("1010"));
	}
	
	public void testGY() {
		assertException(NullPointerException.class, () -> self.getAccount(null));
	}
	
	public void testGZ() {
		open("9999");
		assertException(NullPointerException.class, () -> self.getAccount(null));		
	}
	
	
	/// testNx: netAssets
	
	public void testNA() {
		assertEquals(Money.ZERO, self.netAssets());
	}
	
	public void testNB() {
		bank.open("Test", "", new Money(-1000), new Money(-134.89));
		assertEquals(new Money(134.89), self.netAssets());
	}
	
	public void testNC() {
		bank.open("Test", "", new Money(-1000), new Money(-134.89));
		bank.open("Test", "", new Money(20.00), new Money(400.00));
		assertEquals(new Money(-265.11), self.netAssets());
	}
	
	public void testND() {
		bank.open("Test", "", new Money(-10000), new Money(-1600));
		bank.open("Test", "", new Money(-1000), new Money(-134.89));
		bank.open("Test", "", new Money(20.00), new Money(400.00));
		assertEquals(new Money(1334.89), self.netAssets());
	}
	
	public void testNE() {
		bank.open("Test", "", new Money(-10000), new Money(-1600));
		bank.open("Test", "", new Money(-1000), new Money(-134.89));
		bank.open("Test", "", new Money(20.00), new Money(400.00));
		bank.open("Test", "", new Money(1000.00), new Money(8500.50));
		assertEquals(new Money(-7165.61), self.netAssets());
	}
	
	
	/// testSx: getBalanceSum tests
	
	public void testSA() {
		assertEquals(Money.ZERO, self.getBalanceSum("0"));
	}
	
	public void testSB() {
		bank.open("Test", "1234", new Money(-1000), new Money(-134.89));
		assertEquals(new Money(-134.89), self.getBalanceSum("123"));
	}
	
	public void testSC() {
		bank.open("Test", "1234", new Money(-1000), new Money(-134.89));
		assertEquals(Money.ZERO, self.getBalanceSum("1233"));		
	}
	
	public void testSD() {
		bank.open("Test", "53211", new Money(20.00), new Money(400.00));
		bank.open("Test", "53202", new Money(1000.00), new Money(8500.50));
		assertEquals(new Money(8900.50), self.getBalanceSum("532"));
	}
	
	public void testSE() {
		bank.open("Test", "53211", new Money(20.00), new Money(400.00));
		bank.open("Test", "53202", new Money(1000.00), new Money(8500.50));
		assertEquals(new Money(400), self.getBalanceSum("5321"));
	}
	
	public void testSF() {
		bank.open("Test", "1234", Money.ZERO, new Money(20));
		bank.open("Test", "1010", Money.ZERO, new Money(33));
		bank.open("Test", "120", Money.ZERO, new Money(10));
		bank.open("Test", "2030", Money.ZERO, new Money(66));
		bank.open("Test", "1296", Money.ZERO, new Money(40));
		assertEquals(new Money(70.00), self.getBalanceSum("12"));
	}
	
	public void testSG() {
		assertException(NullPointerException.class, () -> self.getBalanceSum(null));
	}
	
	public void testSH() {
		bank.open("Test", "1234", Money.ZERO, new Money(20));
		assertException(NullPointerException.class, () -> self.getBalanceSum(null));		
	}
}
