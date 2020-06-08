package openllet.owlwg.cli;

import static java.lang.String.format;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import openllet.owlwg.testcase.Semantics;
import openllet.owlwg.testcase.SyntaxConstraint;
import openllet.owlwg.testcase.filter.ConjunctionFilter;
import openllet.owlwg.testcase.filter.DisjunctionFilter;
import openllet.owlwg.testcase.filter.FilterCondition;
import openllet.owlwg.testcase.filter.NegationFilter;
import openllet.owlwg.testcase.filter.SatisfiedSyntaxConstraintFilter;
import openllet.owlwg.testcase.filter.SemanticsFilter;
import openllet.owlwg.testcase.filter.StatusFilter;
import openllet.owlwg.testcase.filter.UnsatisfiedSyntaxConstraintFilter;
import openllet.shared.tools.Log;

/**
 * <p>
 * Title: Filter Condition Parser
 * </p>
 * <p>
 * Description: Create a filter _condition from a string
 * </p>
 * <p>
 * Copyright: Copyright &copy; 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <a href="http://clarkparsia.com/"/>http://clarkparsia.com/</a>
 * </p>
 *
 * @author Mike Smith &lt;msmith@clarkparsia.com&gt;
 */
public class FilterConditionParser
{
	private static final Logger _logger = Log.getLogger(FilterConditionParser.class);

	public static FilterCondition parse(final String filterString)
	{

		FilterCondition filter;
		final LinkedList<FilterCondition> filterStack = new LinkedList<>();
		final String[] splits = filterString.split("\\s");
		for (final String split : splits)
			if (split.equalsIgnoreCase("and"))
			{
				final ConjunctionFilter and = ConjunctionFilter.and(filterStack);
				filterStack.clear();
				filterStack.add(and);
			}
			else if (split.equalsIgnoreCase("approved"))
				filterStack.add(StatusFilter.APPROVED);
			else if (split.equalsIgnoreCase("direct"))
				filterStack.add(new SemanticsFilter(Semantics.DIRECT));
			else if (split.equalsIgnoreCase("dl"))
				filterStack.add(SatisfiedSyntaxConstraintFilter.DL);
			else if (split.equalsIgnoreCase("!dl"))
				filterStack.add(new UnsatisfiedSyntaxConstraintFilter(SyntaxConstraint.DL));
			else if (split.equalsIgnoreCase("el"))
				filterStack.add(SatisfiedSyntaxConstraintFilter.EL);
			else if (split.equalsIgnoreCase("!el"))
				filterStack.add(new UnsatisfiedSyntaxConstraintFilter(SyntaxConstraint.EL));
			else if (split.equalsIgnoreCase("extracredit"))
				filterStack.add(StatusFilter.EXTRACREDIT);
			else if (split.equalsIgnoreCase("not"))
			{
				final FilterCondition a = filterStack.removeLast();
				filterStack.add(NegationFilter.not(a));
			}
			else if (split.equalsIgnoreCase("or"))
			{
				final DisjunctionFilter or = DisjunctionFilter.or(filterStack);
				filterStack.clear();
				filterStack.add(or);
			}
			else if (split.equalsIgnoreCase("proposed"))
				filterStack.add(StatusFilter.PROPOSED);
			else if (split.equalsIgnoreCase("ql"))
				filterStack.add(SatisfiedSyntaxConstraintFilter.QL);
			else if (split.equalsIgnoreCase("!ql"))
				filterStack.add(new UnsatisfiedSyntaxConstraintFilter(SyntaxConstraint.QL));
			else if (split.equalsIgnoreCase("rdf"))
				filterStack.add(new SemanticsFilter(Semantics.RDF));
			else if (split.equalsIgnoreCase("rejected"))
				filterStack.add(StatusFilter.REJECTED);
			else if (split.equalsIgnoreCase("rl"))
				filterStack.add(SatisfiedSyntaxConstraintFilter.RL);
			else if (split.equalsIgnoreCase("!rl"))
				filterStack.add(new UnsatisfiedSyntaxConstraintFilter(SyntaxConstraint.RL));
			else
			{
				final String msg = format("Unexpected filter _condition argument: \"%s\"", split);
				_logger.severe(msg);
				throw new IllegalArgumentException(msg);
			}
		if (filterStack.isEmpty())
		{
			final String msg = format("Missing valid filter _condition. Filter option argument: \"%s\"", filterString);
			_logger.severe(msg);
			throw new IllegalArgumentException(msg);
		}
		if (filterStack.size() > 1)
		{
			final String msg = format("Filter conditions do not parse to a single _condition. Final parse stack: \"%s\"", filterStack);
			_logger.severe(msg);
			throw new IllegalArgumentException(msg);
		}

		filter = filterStack.iterator().next();
		if (_logger.isLoggable(Level.FINE)) _logger.fine(format("Filter _condition: \"%s\"", filter));
		return filter;
	}

}
