package openllet.jena;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * Force the tests that depend of it to be executed one at a time. (with a reset of the KB each time). Also initialize the in-memory database and add some
 * helpers methods. Diagnosis is already massively parallel, to test this parallelism, we must make sure junit do not avoid the concurrency I want.
 *
 * @since 2.0.0
 */
public abstract class SequentialTestsContraintInitializer
{
	protected static final Lock _sequential = new ReentrantLock();
	public static long _timing = 0;

	@Rule
	public TestName _currentMethodName = new TestName();

	@Before
	public void setUp()
	{
		_sequential.lock();
		System.out.println("====================BEGIN========================= [" + this.getClass().getSimpleName() + "." + _currentMethodName.getMethodName() + "] ");
		_timing = System.currentTimeMillis();
	}

	@After
	public void tearDown()
	{
		// System.out.println("=====================SLEEP========================== [" + this.getClass().getSimpleName() + "." + _currentMethodName.getMethodName() + "] ");
		final long end = System.currentTimeMillis();
		try
		{
			TimeUnit.MILLISECONDS.sleep(100); // Make sure all message are display before starting another test.
		}
		catch (@SuppressWarnings("unused") final Exception e)
		{
			// ignore every sleep error.
		}
		System.out.println("=====================END========================== [" + this.getClass().getSimpleName() + "." + _currentMethodName.getMethodName() + "] in " + (end - _timing) + "ms");
		_sequential.unlock();
	}

	public static void sleep(final String msg, final long time)
	{
		System.err.println(msg);
		try
		{
			Thread.yield();
			Thread.sleep(time * 1000);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
}
