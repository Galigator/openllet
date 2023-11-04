// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * A simple class to experiment with your JVM's garbage collector and memory sizes for various _data types.
 *
 * @author <a href="mailto:vlad@trilogy.com">Vladimir Roubtsov</a>
 */
public class MemUtils
{
	private static final Runtime runtime = Runtime.getRuntime();

	public static final double BYTES_PER_MB = 1048576.0;

	private static final DecimalFormat MB_FORMAT = (DecimalFormat) NumberFormat.getNumberInstance();

	static
	{
		MB_FORMAT.setMaximumFractionDigits(2);
	}

	public static void runGC()
	{
		try
		{
			// It helps to call Runtime.gc()
			// using several method calls:
			for (int r = 0; r < 4; ++r)
				_runGC();
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void _runGC()
	{
		long usedMem1 = usedMemory(), usedMem2 = Long.MAX_VALUE;
		for (int i = 0; usedMem1 < usedMem2 && i < 500; ++i)
		{
			runtime.gc();
			Thread.yield();

			usedMem2 = usedMem1;
			usedMem1 = usedMemory();
		}
	}

	public static long usedMemory()
	{
		return runtime.totalMemory() - runtime.freeMemory();
	}

	public static long freeMemory()
	{
		return runtime.freeMemory();
	}

	public static long totalMemory()
	{
		return runtime.totalMemory();
	}

	public static double mb(final long bytes)
	{
		return bytes / BYTES_PER_MB;
	}

	public static void printUsedMemory(final String msg)
	{
		System.out.println(msg + " " + MB_FORMAT.format(mb(usedMemory())) + "mb");
	}

	public static void printMemory(final String msg, final long mem)
	{
		System.out.println(msg + " " + MB_FORMAT.format(mb(mem)) + "mb");
	}
}
