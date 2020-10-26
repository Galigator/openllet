package openllet.core;

import static openllet.core.utils.TermFactory.not;
import static openllet.core.utils.TermFactory.term;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import openllet.aterm.ATermAppl;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.iterator.IteratorUtils;

/**
 * @since 2.6.3
 */
public class OpenlletComparisonsChecker
{
	@SafeVarargs
	public static <T> Set<T> set(final T... elements)
	{
		final Set<T> set = new HashSet<>();
		for (final T element : elements)
			set.add(element);
		return set;
	}

	public static <T> void assertIteratorContains(final Iterator<T> it, final T val)
	{
		boolean found = false;
		while (it.hasNext() && !found)
		{
			final Object obj = it.next();
			found = obj.equals(val);
		}

		assertTrue("Failed to find _expected iterator value: " + val, found);
	}

	@SuppressWarnings("unchecked")
	public static <T> void assertIteratorValues(final Iterator<? extends T> it, final Iterator<? extends T> expected)
	{
		assertIteratorValues(it, (T[]) IteratorUtils.toList(expected).toArray());
	}

	@SafeVarargs
	public static <T> void assertIteratorValues(final Iterator<? extends T> it, final T... expected)
	{
		final boolean[] found = new boolean[expected.length];

		for (int i = 0; i < expected.length; i++)
			found[i] = false;

		while (it.hasNext())
		{
			final Object n = it.next();
			boolean gotit = false;

			for (int i = 0; i < expected.length; i++)
				if (n.equals(expected[i]))
				{
					gotit = true;
					found[i] = true;
				}
			assertTrue("Found unexpected iterator value: " + n, gotit);
		}

		// check that all _expected values were found
		final List<T> unfound = new ArrayList<>();
		for (int i = 0; i < expected.length; i++)
			if (!found[i])
				unfound.add(expected[i]);

		assertTrue("Failed to find expected iterator values: " + unfound, unfound.isEmpty());
	}

	public static void printAll(final Iterator<?> i)
	{
		while (i.hasNext())
			System.out.println(i.next());
	}

	public static void printAll(final Iterator<?> i, final String head)
	{
		System.out.print(head + ": ");
		if (i.hasNext())
		{
			System.out.println();
			while (i.hasNext())
				System.out.println(i.next());
		}
		else
			System.out.println("<EMPTY>");
	}

	public static void assertSatisfiable(final KnowledgeBase kb, final ATermAppl c)
	{
		assertSatisfiable(kb, c, true);
	}

	public static void assertUnsatisfiable(final KnowledgeBase kb, final ATermAppl c)
	{
		assertSatisfiable(kb, c, false);
	}

	public static void assertSatisfiable(final KnowledgeBase kb, final ATermAppl c, final boolean isSatisfiable)
	{
		assertEquals("Satisfiability for " + c + " failed", isSatisfiable, kb.isSatisfiable(c));
	}

	public static void assertSubClass(final KnowledgeBase kb, final String c1, final String c2)
	{
		assertSubClass(kb, term(c1), term(c2));
	}

	public static void assertSubClass(final KnowledgeBase kb, final ATermAppl c1, final ATermAppl c2)
	{
		assertSubClass(kb, c1, c2, true);
	}

	public static void assertNotSubClass(final KnowledgeBase kb, final ATermAppl c1, final ATermAppl c2)
	{
		assertSubClass(kb, c1, c2, false);
	}

	public static void assertSubClass(final KnowledgeBase kb, final ATermAppl c1, final ATermAppl c2, final boolean expectedSubClass)
	{
		boolean computedSubClass = kb.isSubClassOf(c1, c2);

		assertEquals("Subclass check failed for (" + ATermUtils.toString(c1) + " [= " + ATermUtils.toString(c2) + ")", expectedSubClass, computedSubClass);

		kb.isSatisfiable(c1);
		kb.isSatisfiable(not(c1));
		kb.isSatisfiable(c2);
		kb.isSatisfiable(not(c2));

		final long satCount = kb.getABox().getStats()._satisfiabilityCount;
		computedSubClass = kb.isSubClassOf(c1, c2);
		final boolean cached = satCount == kb.getABox().getStats()._satisfiabilityCount;

		assertEquals("Subclass check (Cached: " + cached + ") failed for (" + ATermUtils.toString(c1) + " [= " + ATermUtils.toString(c2) + ")", expectedSubClass, computedSubClass);
	}
}
