// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import openllet.core.OpenlletComparisonsChecker;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

public final class JenaStatementsChecker extends OpenlletComparisonsChecker
{
	protected static boolean isAnonValue(final Object n)
	{
		return n instanceof Resource && ((Resource) n).isAnon() || n instanceof Statement && ((Statement) n).getSubject().isAnon() || n instanceof Statement && isAnonValue(((Statement) n).getObject());
	}

	@SuppressWarnings("unused")
	@Deprecated
	private static Set<Statement> createStatements(final Resource subject, final Property predicate, final RDFNode... objects)
	{
		final Set<Statement> set = new HashSet<>();
		for (final RDFNode object : objects)
			set.add(ResourceFactory.createStatement(subject, predicate, object));

		return set;
	}

	public static void addStatements(final Model model, final Resource subject, final Property predicate, final RDFNode... objects)
	{
		for (final RDFNode object : objects)
			model.add(subject, predicate, object);
	}

	public static void assertPropertyValues(final Model model, final Resource subject, final Property predicate, final RDFNode... objects)
	{
		final Model values = ModelFactory.createDefaultModel();
		addStatements(values, subject, predicate, objects);
		assertPropertyValues(model, predicate, values);
	}

	public static void assertPropertyValues(final Model model, final Property pred, final Model inferences)
	{
		final Predicate<Statement> predFilter = stmt -> stmt.getPredicate().equals(pred);

		for (final StmtIterator i = inferences.listStatements(); i.hasNext();)
		{
			final Statement statement = i.nextStatement();

			assertEquals(pred, statement.getPredicate());

			assertTrue(statement + " not inferred", model.contains(statement));
		}

		assertIteratorValues(model.listStatements(null, pred, (RDFNode) null), inferences.listStatements());

		final Set<Resource> testedSubj = new HashSet<>();
		final Set<RDFNode> testedObj = new HashSet<>();
		for (final StmtIterator i = inferences.listStatements(); i.hasNext();)
		{
			final Statement statement = i.nextStatement();
			final Resource subj = statement.getSubject();
			final RDFNode obj = statement.getObject();

			if (testedSubj.add(subj))
			{
				assertIteratorValues(model.listStatements(subj, pred, (RDFNode) null), inferences.listStatements(subj, pred, (RDFNode) null));

				assertIteratorValues(model.listStatements(subj, null, (RDFNode) null).filterKeep(predFilter), inferences.listStatements(subj, null, (RDFNode) null).filterKeep(predFilter));
			}

			if (testedObj.add(obj))
			{
				assertIteratorValues(model.listStatements(null, pred, obj), inferences.listStatements(null, pred, obj));

				assertIteratorValues(model.listStatements(null, null, obj).filterKeep(predFilter), inferences.listStatements(null, null, obj).filterKeep(predFilter));
			}
		}
	}

	public static void testResultSet(final ResultSet results, final List<Map<String, RDFNode>> ans)
	{
		final List<Map<String, RDFNode>> answers = new ArrayList<>(ans);
		while (results.hasNext())
		{
			final QuerySolution sol = results.nextSolution();
			assertNotNull("QuerySolution", sol);

			final Map<String, RDFNode> answer = new HashMap<>();
			for (final String var : results.getResultVars())
			{
				final RDFNode val = sol.get(var);
				assertNotNull("Variable: " + var, val);

				answer.put(var, val);
			}

			assertTrue("Unexpected binding found: " + answer, answers.remove(answer));
		}

		assertTrue("Binding not found: " + answers, answers.isEmpty());
	}

	public static Map<String, RDFNode> createBinding(final String[] keys, final RDFNode[] values)
	{
		assertTrue(keys.length == values.length);

		final Map<String, RDFNode> answer = new HashMap<>();
		for (int i = 0; i < keys.length; i++)
			answer.put(keys[i], values[i]);

		return answer;
	}

	public static List<Map<String, RDFNode>> createBindings(final String[] keys, final RDFNode[][] values)
	{
		final List<Map<String, RDFNode>> answers = new ArrayList<>();
		for (final RDFNode[] value : values)
		{
			final Map<String, RDFNode> answer = new HashMap<>();
			for (int j = 0; j < keys.length; j++)
				answer.put(keys[j], value[j]);
			answers.add(answer);
		}

		return answers;
	}

}
