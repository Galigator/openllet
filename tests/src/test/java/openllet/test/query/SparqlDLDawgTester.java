// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.test.query;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.engine.binding.Binding;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBaseImpl;
import openllet.core.utils.PermutationGenerator;
import openllet.core.utils.Timer;
import openllet.jena.PelletInfGraph;
import openllet.jena.PelletReasonerFactory;
import openllet.query.sparqldl.engine.QueryEngine;
import openllet.query.sparqldl.jena.JenaIOUtils;
import openllet.query.sparqldl.jena.SparqlDLResultSet;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.QueryResult;
import openllet.query.sparqldl.model.ResultBinding;
import openllet.query.sparqldl.parser.QueryEngineBuilder;
import openllet.shared.tools.Log;

/**
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Petr Kremen
 */
public class SparqlDLDawgTester implements SparqlDawgTester
{
	private static final Logger _logger = Log.getLogger(SparqlDLDawgTester.class);
	private String _queryURI = "";
	private Set<String> _graphURIs = new HashSet<>();
	private Set<String> _namedGraphURIs = new HashSet<>();
	private OntModel _model = null;
	private Query _query = null;
	private String _resultURI = null;
	private final boolean _allOrderings;
	private final boolean _writeResults = true;
	private boolean _noCheck;

	public SparqlDLDawgTester(final boolean allOrderings, final boolean noCheck)
	{
		_allOrderings = allOrderings;
		_noCheck = noCheck;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDatasetURIs(final Set<String> graphURIs, final Set<String> namedGraphURIs)
	{
		if (_graphURIs.equals(graphURIs) && _namedGraphURIs.equals(namedGraphURIs))
			return;

		_graphURIs = graphURIs;
		_namedGraphURIs = namedGraphURIs;

		_model = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);

		for (final String dataURI : graphURIs)
			_model.read(dataURI, null, JenaIOUtils.fileType(dataURI).jenaName());

		_model.prepare();

		//		((PelletInfGraph) _model.getGraph()).classify();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setQueryURI(final String queryURI)
	{
		if (_queryURI.equals(queryURI))
			return;

		_queryURI = queryURI;
		final org.apache.jena.query.Query query = QueryFactory.read(queryURI);

		_query = QueryEngineBuilder.getParser().parse(query.toString(Syntax.syntaxSPARQL), ((PelletInfGraph) _model.getGraph()).getKB());

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setResult(final String resultURI)
	{
		_resultURI = resultURI;
		if (resultURI == null)
			_noCheck = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isParsable()
	{
		try (var in = new FileInputStream(_queryURI.substring(5)))
		{
			QueryEngineBuilder.getParser().parse(in, new KnowledgeBaseImpl());
			return true;
		}
		catch (final Exception e)
		{
			_logger.log(Level.INFO, e.getMessage(), e);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCorrectlyEvaluated()
	{
		try
		{
			boolean ok = true;

			if (_query.getDistVars().isEmpty())
			{
				Boolean expected = null;
				if (!_noCheck)
				{
					expected = JenaIOUtils.parseAskResult(_resultURI);

					if (_logger.isLoggable(Level.INFO))
						_logger.info("Expected=" + expected);
				}

				if (_allOrderings)
				{
					final PermutationGenerator g = new PermutationGenerator(_query.getAtoms().size());

					while (g.hasMore())
						ok &= runSingleAskTest(_query.reorder(g.getNext()), expected);
				}
				else
					ok = runSingleAskTest(_query, expected);

				return ok;
			}
			else
			{
				ResultSetRewindable expected = null;
				if (!_noCheck)
				{
					expected = ResultSetFactory.makeRewindable(JenaIOUtils.parseResultSet(_resultURI));

					final List<?> expectedList = ResultSetFormatter.toList(expected);
					if (expected.size() > 10)
					{
						if (_logger.isLoggable(Level.INFO))
							_logger.log(Level.INFO, "Expected=" + expectedList.subList(0, 9) + " ... " + expectedList.size());
					}
					else
						if (_logger.isLoggable(Level.INFO))
							_logger.info("Expected=" + expectedList);
				}

				if (_allOrderings)
				{
					final PermutationGenerator g = new PermutationGenerator(_query.getAtoms().size());

					while (g.hasMore())
						ok &= runSingleSelectTest(_query.reorder(g.getNext()), expected);
				}
				else
					ok = runSingleSelectTest(_query, expected);

				return ok;
			}
		}
		catch (final IOException e)
		{
			_logger.log(Level.SEVERE, e.getMessage(), e);
			return false;
		}
	}

	private static QueryResult runSingleTest(final Query query)
	{
		final Timer t = new Timer("Single _query execution");

		t.start();
		final QueryResult bindings = QueryEngine.exec(query);
		_logger.info("Execution time=" + t.getElapsed());
		t.stop();
		_logger.info("Result size = " + bindings.size());

		return bindings;
	}

	private final boolean runSingleAskTest(final Query query, final Boolean expected)
	{
		final QueryResult bindings = runSingleTest(query);

		boolean ok = true;

		if (!_noCheck)
		{
			final Boolean real = !bindings.isEmpty();

			_logger.log(Level.INFO, "real=" + real + ", exp=" + expected);
			ok = real.equals(expected);
		}

		return ok;
	}

	private final boolean runSingleSelectTest(final Query query, final ResultSetRewindable expected)
	{
		final QueryResult bindings = runSingleTest(query);

		boolean ok = true;

		if (!_noCheck)
		{
			final ResultSetRewindable real = realResultsHandler(bindings);

			real.reset();
			expected.reset();
			ok &= ResultSetUtils.assertEquals(real, expected);

			if (_writeResults)
			{
				real.reset();
				expected.reset();
				// final ResultSetRewindable rMinusE = ResultSetFactory
				// .makeRewindable(ResultSetFactory.copyResults(real));
				// final ResultSetRewindable eMinusR = ResultSetFactory
				// .makeRewindable(ResultSetFactory.copyResults(_expected));

				// real.reset();
				// final Model realModel = ResultSetFormatter.toModel(real);
				// _expected.reset();
				// final Model expectedModel = ResultSetFormatter
				// .toModel(_expected);

				real.reset();
				try (var out = new FileOutputStream("real"))
				{
					ResultSetFormatter.out(out, real);
				}
				catch (final IOException e)
				{
					_logger.log(Level.SEVERE, e.getMessage(), e);
					throw new RuntimeException(e);
				}
				try (var out = new FileOutputStream("real-expected"))
				{
					ResultSetFormatter.out(out, new DifferenceResultSet(real, expected));
				}
				catch (final IOException e)
				{
					_logger.log(Level.SEVERE, e.getMessage(), e);
					throw new RuntimeException(e);
				}
				try (var out = new FileOutputStream("expected-real"))
				{
					ResultSetFormatter.out(out, new DifferenceResultSet(expected, real));
				}
				catch (final IOException e)
				{
					_logger.log(Level.SEVERE, e.getMessage(), e);
					throw new RuntimeException(e);
				}

				// final Set<ResultBinding> rMinusE = SetUtils.difference(
				// new HashSet<ResultBinding>(realList),
				// new HashSet<ResultBinding>(expectedList));
				//
				// final FileWriter fwre = new FileWriter("real-_expected");
				// _writeResults(resultVars,
				// (Collection<ResultBinding>) rMinusE, fwre);
				//
				// final FileWriter fwer = new FileWriter("_expected-real");
				// final Set<ResultBinding> eMinusR = SetUtils.difference(
				// new HashSet<ResultBinding>(expectedList),
				// new HashSet<ResultBinding>(realList));
				//
				// _writeResults(resultVars,
				// (Collection<ResultBinding>) eMinusR, fwer);
			}
		}

		return ok;
	}

	@SuppressWarnings("unused")
	private static void writeResults(final List<ATermAppl> resultVars, final Collection<ResultBinding> bindingCollection, final FileWriter fwre) throws IOException
	{
		for (final ATermAppl var : resultVars)
			fwre.write(var.getName() + "\t");
		for (final ResultBinding b : bindingCollection)
		{
			for (final ATermAppl var : resultVars)
				fwre.write(b.getValue(var) + "\t");
			fwre.write("\n");
		}
	}

	private final ResultSetRewindable realResultsHandler(final QueryResult bindings)
	{
		final ResultSetRewindable real = ResultSetFactory.makeRewindable(new SparqlDLResultSet(bindings, _model.getRawModel()));

		final List<?> realList = ResultSetFormatter.toList(real);
		if (realList.size() > 10)
		{
			if (_logger.isLoggable(Level.INFO))
				_logger.log(Level.INFO, "Real=" + realList.subList(0, 9) + " ... " + realList.size());
		}
		else
			if (_logger.isLoggable(Level.INFO))
				_logger.info("Real=" + realList);
		real.reset();

		return real;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isApplicable(final String uri)
	{
		return !uri.startsWith("http://www.w3.org/2001/sw/DataAccess/tests/data-r2/syntax-sparql1/manifest#") && !uri.startsWith("http://www.w3.org/2001/sw/DataAccess/tests/data-r2/syntax-sparql2/manifest#") && !uri.startsWith("http://www.w3.org/2001/sw/DataAccess/tests/data-r2/syntax-sparql3/manifest#") && !uri.startsWith("http://www.w3.org/2001/sw/DataAccess/tests/data-r2/syntax-sparql4/manifest#");
	}

	private static class DifferenceResultSet implements ResultSet
	{

		private final List<Binding> solutions = new ArrayList<>();

		private final List<String> vars;

		private int index;

		public DifferenceResultSet(final ResultSet rs1, final ResultSet rs2)
		{
			vars = rs1.getResultVars();

			index = 0;

			final ResultSetRewindable real = ResultSetFactory.makeRewindable(rs1);
			final ResultSetRewindable expected = ResultSetFactory.makeRewindable(rs2);

			real.reset();
			while (real.hasNext())
			{
				final Binding b1 = real.nextBinding();
				expected.reset();
				boolean toAdd = true;
				while (expected.hasNext())
				{
					final Binding b2 = expected.nextBinding();
					if (b1.equals(b2))
					{
						toAdd = false;
						break;
					}
				}

				if (toAdd)
					solutions.add(b1);
			}
		}

		@Override
		public List<String> getResultVars()
		{
			return vars;
		}

		@Override
		public int getRowNumber()
		{
			return index;
		}

		@Override
		public boolean hasNext()
		{
			return index < solutions.size();
		}

		@Override
		public QuerySolution next()
		{
			throw new UnsupportedOperationException("Next is not supported.");
		}

		@Override
		public Binding nextBinding()
		{
			return solutions.get(index++);
		}

		@Override
		public QuerySolution nextSolution()
		{
			throw new UnsupportedOperationException("Next solution is not supported.");
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException("Removal is not supported.");
		}

		@Override
		public Model getResourceModel()
		{
			return null;
		}
	}

	@Override
	public String getName()
	{
		return "SparqlDLDawgTester";
	}
}
