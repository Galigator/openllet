package openllet.query.sparqldl.model;

import java.util.Iterator;
import openllet.aterm.ATermAppl;
import openllet.core.utils.ATermUtils;
import openllet.jena.JenaUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;

/**
 * Provide a way to jena to build its specific parameters.
 * 
 * @since 2.6.0
 */
public interface QueryParametersBuilder
{
	static QueryParameters getQueryParameters(QuerySolution initialBindingParam)
	{
		final QueryParameters parameters = new QueryParameters();

		QuerySolution initialBinding = initialBindingParam;

		if (initialBinding == null)
			initialBinding = new QuerySolutionMap();

		for (final Iterator<String> iter = initialBinding.varNames(); iter.hasNext();)
		{
			final String varName = iter.next();
			final ATermAppl key = ATermUtils.makeVar(varName);
			final ATermAppl value = JenaUtils.makeATerm(initialBinding.get(varName));
			parameters.add(key, value);
		}
		return parameters;
	}
}
