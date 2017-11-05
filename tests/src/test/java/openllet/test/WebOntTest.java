// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC.
// <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms
// of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com
//
// ---
// Portions Copyright (c) 2003 Ron Alford, Mike Grove, Bijan Parsia, Evren Sirin
// Alford, Grove, Parsia, Sirin parts of this source code are available under
// the terms of the MIT License.
//
// The MIT License
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.

package openllet.test;

import static openllet.test.OWLTestVocabulary.Approved;
import static openllet.test.OWLTestVocabulary.ClassificationTest;
import static openllet.test.OWLTestVocabulary.ConsistencyTest;
import static openllet.test.OWLTestVocabulary.DL;
import static openllet.test.OWLTestVocabulary.ExtraCredit;
import static openllet.test.OWLTestVocabulary.Full;
import static openllet.test.OWLTestVocabulary.ImportEntailmentTest;
import static openllet.test.OWLTestVocabulary.ImportLevelTest;
import static openllet.test.OWLTestVocabulary.InconsistencyTest;
import static openllet.test.OWLTestVocabulary.Lite;
import static openllet.test.OWLTestVocabulary.NegativeEntailmentTest;
import static openllet.test.OWLTestVocabulary.NotOwlFeatureTest;
import static openllet.test.OWLTestVocabulary.OWLforOWLTest;
import static openllet.test.OWLTestVocabulary.Obsoleted;
import static openllet.test.OWLTestVocabulary.PositiveEntailmentTest;
import static openllet.test.OWLTestVocabulary.Proposed;
import static openllet.test.OWLTestVocabulary.TrueTest;
import static openllet.test.OWLTestVocabulary.conclusionDocument;
import static openllet.test.OWLTestVocabulary.inputDocument;
import static openllet.test.OWLTestVocabulary.level;
import static openllet.test.OWLTestVocabulary.premiseDocument;
import static openllet.test.OWLTestVocabulary.status;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import openllet.atom.OpenError;
import openllet.core.OpenlletOptions;
import openllet.core.exceptions.TimeoutException;
import openllet.core.exceptions.UnsupportedFeatureException;
import openllet.shared.tools.Log;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class WebOntTest
{
	public static Logger _logger = Log.getLogger(WebOntTest.class);

	// each test case should be handled in timeout seconds
	// or otherwise is assumed to fail that test case
	// if timeout is set to 0 then test case will not be stopped
	// default value is 10 seconds
	public int _timeout = 10;

	// At the end of tests stats about the results are displayed
	// There are couple of options here; short means only total
	// number of passes are shown, long means individual results
	// for each test is also displayed, all means a GUI window is
	// popped up to show these stats
	public final static int NO_STATS = 0;
	public final static int SHORT_STATS = 1;
	public final static int LONG_STATS = 2;
	public final static int ALL_STATS = 3;
	public int _showStats = ALL_STATS;

	private static String _base = "http://www.w3.org/2002/03owlt/";
	private final String _localBase = PelletTestSuite.base;
	private String _newBase;

	public static boolean DOUBLE_CHECK_ENTAILMENTS = false;

	public final static List<Resource> TYPES = Arrays.asList(new Resource[] { //
			NotOwlFeatureTest, //
			PositiveEntailmentTest, //
			NegativeEntailmentTest, //
			TrueTest, //
			OWLforOWLTest, //
			ConsistencyTest, //
			InconsistencyTest, //
			ImportEntailmentTest, //
			ImportLevelTest, //
			ClassificationTest//
	});

	public final static List<Resource> LEVELS = Arrays.asList(new Resource[] { Lite, DL, Full });
	public final static List<RDFNode> STATUS = Arrays.asList(new RDFNode[] { Approved, Proposed, ExtraCredit, Obsoleted });

	public boolean _avoidFailTests = false;

	/**
	 * Pellet is known to fail the following test cases either because they are not in OWL DL or they are extremely hard, e.g. more than 100 GCI's, or a very
	 * large number restriction. Such cases are not very realistic and it is not considered to be a problem to fail those test cases. But if Pellet fails on one
	 * of the other test cases then it indicates a problem.
	 */
	private static final List<Resource> AVOID = Arrays.asList(new Resource[] { //
			ResourceFactory.createResource(_base + "/AnnotationProperty/Manifest001#test"), //
			ResourceFactory.createResource(_base + "/AnnotationProperty/Manifest002#test"), //
			ResourceFactory.createResource(_base + "/Class/Manifest005#test"), //
			ResourceFactory.createResource(_base + "/DatatypeProperty/Manifest001#test"), //
			ResourceFactory.createResource(_base + "/description-logic/Manifest661#test"), //
			ResourceFactory.createResource(_base + "/description-logic/Manifest662#test"), //
			ResourceFactory.createResource(_base + "/description-logic/Manifest663#test"), //
			ResourceFactory.createResource(_base + "/description-logic/Manifest664#test"), //
			ResourceFactory.createResource(_base + "/description-logic/Manifest903#test"), //
			ResourceFactory.createResource(_base + "/description-logic/Manifest905#test"), //
			ResourceFactory.createResource(_base + "/description-logic/Manifest906#test"), //
			ResourceFactory.createResource(_base + "/description-logic/Manifest907#test"), //
			ResourceFactory.createResource(_base + "/description-logic/Manifest908#test"), //
			ResourceFactory.createResource(_base + "/description-logic/Manifest909#test"), //
			ResourceFactory.createResource(_base + "/description-logic/Manifest910#test"), //
			ResourceFactory.createResource(_base + "/disjointWith/Manifest010#test"), //
			ResourceFactory.createResource(_base + "/equivalentClass/Manifest008#test"), //
			ResourceFactory.createResource(_base + "/equivalentProperty/Manifest005#test"), //
			ResourceFactory.createResource(_base + "/extra-credit/Manifest002#test"), //
			ResourceFactory.createResource(_base + "/extra-credit/Manifest003#test"), //
			ResourceFactory.createResource(_base + "/extra-credit/Manifest004#test"), //
			ResourceFactory.createResource(_base + "/I4.6/Manifest003#test"), //
			ResourceFactory.createResource(_base + "/I4.6/Manifest005#test"), //
			ResourceFactory.createResource(_base + "/I5.1/Manifest001#test"), //
			ResourceFactory.createResource(_base + "/I5.24/Manifest002#test"), //
			ResourceFactory.createResource(_base + "/I5.26/Manifest006#test"), //
			ResourceFactory.createResource(_base + "/I5.26/Manifest007#test"), //
			ResourceFactory.createResource(_base + "/I5.3/Manifest014#test"), //
			ResourceFactory.createResource(_base + "/I5.3/Manifest015#test"), //
			ResourceFactory.createResource(_base + "/I5.5/Manifest003#test"), //
			ResourceFactory.createResource(_base + "/I5.5/Manifest004#test"), //
			ResourceFactory.createResource(_base + "/I5.5/Manifest006#test"), //
			ResourceFactory.createResource(_base + "/I5.5/Manifest007#test"), //
			ResourceFactory.createResource(_base + "/I5.8/Manifest012#test"), //
			ResourceFactory.createResource(_base + "/I5.8/Manifest017#test"), //
			ResourceFactory.createResource(_base + "/miscellaneous/Manifest202#test"), //
			ResourceFactory.createResource(_base + "/miscellaneous/Manifest205#test"), //
			ResourceFactory.createResource(_base + "/miscellaneous/Manifest302#test"), //
			ResourceFactory.createResource(_base + "/Ontology/Manifest003#test"), //
			ResourceFactory.createResource(_base + "/Restriction/Manifest005#test"), //
			ResourceFactory.createResource(_base + "/Restriction/Manifest006#test"), //
			ResourceFactory.createResource(_base + "/sameAs/Manifest001#test"), //
			ResourceFactory.createResource(_base + "/someValuesFrom/Manifest001#test"), //
			ResourceFactory.createResource(_base + "/someValuesFrom/Manifest003#test"), //
			ResourceFactory.createResource(_base + "/Thing/Manifest005#test") });

	final public static int TEST_PASS = 0;
	final public static int TEST_FAIL = 1;
	final public static int TEST_SKIP = 2;
	final public static List<String> RESULTS = Arrays.asList(new String[] { "PASS", "FAIL", "SKIP" });

	// There are three test levels Lite, DL, Full. There are three test
	// status: Approved, Proposed, Obsolete. There are three different
	// test result that can happen: Fail, Pass, Skip. We'll keep the
	// statistics for each possibility. The following two dimensional
	// array is used for this purpose. Each line stores information
	// about one level. First three columns are for Approved tests and
	// first column of these is count of passed tests, second column is
	// count of failed tests, third column is for skipped tests

	private final int[][][][] _stats = new int[LEVELS.size()][STATUS.size()][TYPES.size()][RESULTS.size()];

	// a table showing the test results for each individual test case
	private final Vector<Vector<Object>> results = new Vector<>();

	// maximum number of test cases to process
	private static int MAX_TEST_COUNT = Integer.MAX_VALUE;

	private static WebOntTester[] _testers = {
			/** new JenaWebOntTester(), new OWLAPIWebOntTester() */
	};

	public final static void main(final String[] args)
	{
		new WebOntTest().run(args);
	}

	public WebOntTest()
	{
		for (final WebOntTester tester : _testers)
		{
			tester.registerURIMapping(_base + "/imports/support001-A", _localBase + "owl-test/imports/support001-A.rdf");
			tester.registerURIMapping(_base + "/imports/support003-A", _localBase + "owl-test/imports/support003-A.rdf");
			tester.registerURIMapping(_base + "/imports/support011-A", _localBase + "owl-test/imports/support011-A.rdf");
			tester.registerURIMapping(_base + "/imports/support003-B", _localBase + "owl-test/imports/support003-B.rdf");
			tester.registerURIMapping(_base + "/imports/consistent012", _localBase + "owl-test/imports/consistent012.rdf");
			tester.registerURIMapping(_base + "/miscellaneous/consistent002", _localBase + "owl-test/miscellaneous/consistent002.rdf");
			tester.registerURIMapping(_base + "/miscellaneous/consistent001", _localBase + "owl-test/miscellaneous/consistent001.rdf");
		}
	}

	public void setBase(final String newBase)
	{
		setBase(_base, newBase);
	}

	public void setBase(final String base, final String newBase)
	{
		_base = base;
		_newBase = newBase;
	}

	public void setAvoidFailTests(final boolean avoidFailTests)
	{
		_avoidFailTests = avoidFailTests;
	}

	public void setShowStats(final int showStats)
	{
		_showStats = showStats;
	}

	public void run(final String[] args)
	{
		String manifestFile = _base + "testOntology.rdf"; // "Manifest.rdf";

		for (int i = 0; i < args.length; i++)
		{
			final String arg = args[i];

			switch (arg)
			{
				case "-timeout":
				{
					_timeout = Integer.parseInt(args[++i]);
					break;
				}
				case "-avoidFail":
				{
					_avoidFailTests = true;
					break;
				}
				case "-manifest":
				{
					manifestFile = args[++i];
					if (!manifestFile.startsWith("http://"))
						manifestFile = _base + "/" + manifestFile;
					break;
				}
				case "-validate":
				{
					OpenlletOptions.VALIDATE_ABOX = true;
					break;
				}
				case "-n":
				{
					try
					{
						MAX_TEST_COUNT = Integer.parseInt(args[++i]);
					}
					catch (final Exception e)
					{
						System.err.println(e);
					}
					break;
				}
				case "-stats":
				{
					final String stats = args[++i].toLowerCase();
					switch (stats)
					{
						case "no":
						{
							_showStats = NO_STATS;
							break;
						}
						case "short":
						{
							_showStats = SHORT_STATS;
							break;
						}
						case "long":
						{
							_showStats = LONG_STATS;
							break;
						}
						default:
							_showStats = ALL_STATS;
					}
					break;
				}
				default:
					System.err.println("Unknow parameter " + arg);
			}
		}

		System.out.println("Reading manifest file " + manifestFile);
		System.out.println();

		long time = System.currentTimeMillis();
		doTest(manifestFile);
		time = System.currentTimeMillis() - time;
		System.out.println("Total time: " + time);
	}

	public boolean doTest(final String manifestFile)
	{
		boolean allPassed = true;
		int testCount = 0;

		try
		{
			final Model model = ModelFactory.createDefaultModel();
			final Model outputModel = ModelFactory.createDefaultModel();

			// Create system description
			final Resource system = ResourceFactory.createResource();
			outputModel.add(system, RDFS.label, "Openllet");
			outputModel.add(system, RDFS.comment, "This was generated by the Openllet reasoner which can be found at https://github.com/Galigator/openllet");

			model.read(manifestFile, "");

			final StmtIterator i = model.listStatements(null, RDF.type, (Resource) null);
			while (i.hasNext() && testCount <= MAX_TEST_COUNT)
			{
				final Statement stmt = i.nextStatement();
				if (!TYPES.contains(stmt.getObject()))
					continue;

				final Resource testCase = stmt.getSubject();

				if (_avoidFailTests && AVOID.contains(testCase))
					continue;

				// Output statements
				Statement levelStmt = testCase.getProperty(level);
				final Statement statusStmt = testCase.getProperty(status);

				final Vector<String> levels = new Vector<>();
				// Changes according to the editors-draft. For each test there can
				// be more than one level. Basically each Lite test is also a Full
				// test and now this information is given explicitly in Manifest
				// So loop through all the levels and find the tight bound
				final int reasoningLevel = 3; // 0 - Lite, 1 - DL, 2 - Full
				final StmtIterator si = testCase.listProperties(level);
				while (si.hasNext())
				{
					final Statement s = si.nextStatement();
					final String levelName = s.getResource().getLocalName();
					if (reasoningLevel > LEVELS.indexOf(s.getResource()))
					{
						levelStmt = s;
						levels.add(levelName);
					}
				}

				final Resource testType = stmt.getResource();
				final Resource testLevel = levelStmt.getResource();
				final RDFNode testStatus = statusStmt.getObject();

				// don't do or report anything about obsolete test cases
				if (testStatus.equals(Obsoleted))
					continue;
				else
					if (testStatus.toString().startsWith("OBSOLETE"))
						continue;
					else
						if (testType.equals(OWLforOWLTest))
							continue;

				testCount++;

				final String fileName = testCase.toString();
				System.out.println("Test  : " + testCount);
				System.out.println("Name  : " + testCase);
				System.out.print("Info  : ");
				System.out.print(testType.getLocalName() + " ");
				System.out.print(levels + " ");
				System.out.print(testStatus);
				System.out.println();

				final Vector<Object> result = new Vector<>();
				result.add(Integer.valueOf(testCount - 1));
				result.add(fileName.substring(1 + fileName.substring(1, fileName.lastIndexOf("/") - 1).lastIndexOf("/")));
				result.add(testType.getLocalName());
				result.add(testLevel.getLocalName());
				result.add(testStatus);

				long time = System.currentTimeMillis();

				int testResult = TEST_FAIL;

				try
				{
					testResult = doTestCase(testCase, testType);
				}
				catch (final UnsupportedFeatureException e)
				{
					System.err.println("Skip  : " + e.getMessage());
				}
				catch (final TimeoutException e1)
				{
					System.err.println("Fail  : Timeout - Couldn't find answer after " + _timeout + " seconds");
					Log.error(_logger, e1);
				}
				catch (final Exception e)
				{
					System.err.println("Fail  : " + e.getMessage());
					printStackTrace(e);
				}
				catch (final StackOverflowError e)
				{
					// reporting failure for the bugs in our code causes problem for the approval of tests in webont wg
					System.err.println("Fail  : Stack overflow");
					printStackTrace(e);
				}
				catch (final OutOfMemoryError e)
				{
					System.err.println("Fail  : Out of Memory");
					Log.error(_logger, e);
				}

				time = System.currentTimeMillis() - time;

				// Insert test results;
				final Resource testRun = ResourceFactory.createResource();
				final Property type = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
				final Property tcProp = ResourceFactory.createProperty(_base + "/resultsOntology#test");
				final Resource runtype = ResourceFactory.createResource(_base + "/resultsOntology#TestRun");
				final Property systemProp = ResourceFactory.createProperty(_base + "/resultsOntology#system");
				Resource resultType = null;
				if (testResult == TEST_FAIL)
				{
					if (time >= _timeout * 1000)
						resultType = ResourceFactory.createResource(_base + "/resultsOntology#IncompleteRun");
					else
						resultType = ResourceFactory.createResource(_base + "/resultsOntology#FailingRun");
				}
				else
					if (testResult == TEST_PASS)
						resultType = ResourceFactory.createResource(_base + "/resultsOntology#PassingRun");
					else
						if (testResult == TEST_SKIP)
						{
							/* empty because skip ! */
						}

				outputModel.add(ResourceFactory.createStatement(testRun, type, runtype));
				outputModel.add(ResourceFactory.createStatement(testRun, tcProp, testCase));
				outputModel.add(ResourceFactory.createStatement(testRun, systemProp, system));
				if (resultType != null)
					outputModel.add(ResourceFactory.createStatement(testRun, type, resultType));
				// Insert test time;
				final Property timeProp = ResourceFactory.createProperty("http://owl.mindswap.org/2003/ont/owlweb.rdf#testTime");
				final Literal timeLiteral = model.createLiteral("" + time);
				outputModel.add(ResourceFactory.createStatement(testRun, timeProp, timeLiteral));

				result.add(Long.valueOf(time));
				result.add(RESULTS.get(testResult));

				results.add(result);

				final int i1 = LEVELS.indexOf(testLevel);
				final int i2 = STATUS.indexOf(testStatus);
				final int i3 = TYPES.indexOf(testType);
				final int i4 = testResult;

				_stats[i1][i2][i3][i4]++;

				System.out.println("Result: " + RESULTS.get(testResult));
				System.out.println("--------------------------------------------------------------------");

				allPassed &= testResult == TEST_PASS;
			}

			printStatistics();
		}
		catch (final Exception e)
		{
			System.out.println(e);
			e.printStackTrace();
			allPassed = false;

			if (testCount == 2)
				throw new OpenError(e);
		}

		return allPassed;
	}

	public int doSingleTest(final String manifestFile)
	{
		final Model model = ModelFactory.createDefaultModel();

		model.read(manifestFile, "");

		final StmtIterator i = model.listStatements(null, RDF.type, (Resource) null);
		Statement stmt;
		do
			stmt = i.nextStatement();
		while (!TYPES.contains(stmt.getObject()));

		final Resource testCase = stmt.getSubject();
		final Statement statusStmt = testCase.getProperty(status);

		final Resource testType = stmt.getResource();
		final RDFNode testStatus = statusStmt.getObject();

		if (_avoidFailTests && AVOID.contains(testCase))
			return TEST_SKIP;
		else
			if (testStatus.equals(Obsoleted))
				return TEST_SKIP;
			else
				if (testStatus.toString().startsWith("OBSOLETE"))
					return TEST_SKIP;
				else
					if (testType.equals(OWLforOWLTest))
						return TEST_SKIP;

		_logger.fine("Name  : " + testCase);

		int testResult = TEST_FAIL;

		testResult = doTestCase(testCase, testType);

		return testResult;
	}

	public void printStatistics()
	{
		final ArrayList<Object[][]> dataArray = new ArrayList<>();
		for (int s = 0; s < STATUS.size() - 1; s++)
		{
			final Object[][] data = new Object[LEVELS.size() * (TYPES.size() + 1) + 1][RESULTS.size()];

			for (int r = 0; r < 3; r++) // level,status,type,result
			{
				int columnTotal = 0;
				for (int l = 0; l < LEVELS.size(); l++)
				{
					int total = 0;
					for (int t = 0; t < TYPES.size(); t++)
					{
						final int count = _stats[l][s][t][r];
						data[l * (TYPES.size() + 1) + t + 1][r] = Integer.valueOf(count);

						total += count;
					}
					data[l * (TYPES.size() + 1)][r] = Integer.valueOf(total);
					columnTotal += total;
				}
				data[LEVELS.size() * (TYPES.size() + 1)][r] = Integer.valueOf(columnTotal);
			}

			dataArray.add(data);
		}

		if (_showStats >= LONG_STATS)
		{
			System.out.println();
			for (int i = 0; i < results.size(); i++)
			{
				final Vector<?> result = results.get(i);
				final boolean fail = result.get(result.size() - 1).equals("FAIL");
				if (fail)
				{
					for (int j = 0; j < result.size(); j++)
						System.out.print(result.get(j) + " ");
					System.out.println();
				}
			}
		}

		if (_showStats >= SHORT_STATS)
		{
			System.out.println();
			for (int s = 0; s < STATUS.size() - 1; s++)
			{
				final Object[][] data = dataArray.get(s);

				System.out.println(STATUS.get(s).toString());
				for (int c = 0; c < 3; c++)
					System.out.print(data[data.length - 1][c] + " ");
				System.out.println();
			}
		}

		if (_showStats >= ALL_STATS)
			showStatistics(dataArray);
	}

	private void showStatistics(final ArrayList<Object[][]> dataArray)
	{
		final Object[][] types = new String[LEVELS.size() * (TYPES.size() + 1) + 1][1];
		for (int l = 0; l < LEVELS.size(); l++)
		{
			types[l * (TYPES.size() + 1)][0] = LEVELS.get(l).getLocalName();
			for (int t = 0; t < TYPES.size(); t++)
				types[l * (TYPES.size() + 1) + t + 1][0] = "     " + TYPES.get(t).getLocalName();
			types[LEVELS.size() * (TYPES.size() + 1)][0] = "TOTAL";
		}

		final JFrame info = new JFrame("Result");
		info.getContentPane().setLayout(new BoxLayout(info.getContentPane(), BoxLayout.Y_AXIS));

		final Box mainPanel = Box.createHorizontalBox();
		final JTable table = new JTable(types, new String[] { "TYPE" });

		final Box p1 = Box.createVerticalBox();
		p1.add(new JLabel(" "));
		p1.add(new JScrollPane(table));
		mainPanel.add(p1);

		final JTable[] tables = new JTable[STATUS.size()];
		for (int s = 0; s < STATUS.size() - 1; s++)
		{
			final Box p = Box.createVerticalBox();

			final Object[][] data = dataArray.get(s);
			tables[s] = new JTable(data, RESULTS.toArray());

			final JLabel label = new JLabel(STATUS.get(s).toString());
			label.setAlignmentX(Component.CENTER_ALIGNMENT);
			p.add(label);
			p.add(new JScrollPane(tables[s]));
			mainPanel.add(p);
		}

		final Box optionsPanel = Box.createVerticalBox();
		final JCheckBox details = new JCheckBox("Show Details", true);
		details.addActionListener(e ->
		{
			final int rowHeight = details.isSelected() ? table.getRowHeight(0) : 1;

			for (int l = 0; l < LEVELS.size(); l++)
				for (int t = 0; t < TYPES.size(); t++)
				{
					table.setRowHeight(l * (TYPES.size() + 1) + t + 1, rowHeight);
					tables[0].setRowHeight(l * (TYPES.size() + 1) + t + 1, rowHeight);
					tables[1].setRowHeight(l * (TYPES.size() + 1) + t + 1, rowHeight);
					tables[2].setRowHeight(l * (TYPES.size() + 1) + t + 1, rowHeight);
				}
		});
		details.doClick();
		optionsPanel.add(details);

		final Vector<String> columnNames = new Vector<>();
		columnNames.add("No");
		columnNames.add("Name");
		columnNames.add("Type");
		columnNames.add("Level");
		columnNames.add("Status");
		columnNames.add("Time (ms)");
		columnNames.add("Result");

		final JTable resultsTable = new JTable(results, columnNames);

		resultsTable.getColumnModel().getColumn(0).setPreferredWidth(15);
		resultsTable.getColumnModel().getColumn(1).setPreferredWidth(250);
		resultsTable.getColumnModel().getColumn(2).setPreferredWidth(150);
		resultsTable.getColumnModel().getColumn(3).setPreferredWidth(30);
		resultsTable.getColumnModel().getColumn(5).setPreferredWidth(30);
		resultsTable.getColumnModel().getColumn(6).setPreferredWidth(30);

		info.getContentPane().add(mainPanel);
		info.getContentPane().add(optionsPanel);
		info.getContentPane().add(new JScrollPane(resultsTable));
		info.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		info.setSize(800, 600);
		info.setVisible(true);
	}

	/**
	 * Given a filename converts it to file path on local machine if use_cache option is set. This is used when testing is done without network connection
	 */
	private String getFileName(final String fileNameParam)
	{
		String fileName = fileNameParam;
		if (_newBase != null)
			fileName = fileName.replaceFirst(_base, _newBase);

		if (!fileName.endsWith(".rdf") && //
				!fileName.endsWith(".owl") && //
				!fileName.endsWith(".n3"))
			fileName += ".rdf";

		return fileName;
	}

	private int doConsistencyTest(final Resource testCase, final boolean isConsistent)
	{
		String inputFile = testCase.hasProperty(inputDocument) ? testCase.getProperty(inputDocument).getObject().toString() : null;

		if (inputFile != null)
		{
			inputFile = getFileName(inputFile);

			_logger.fine("Input : " + inputFile);
		}

		for (final WebOntTester tester : _testers)
		{
			_logger.fine("Tester: " + tester.getClass().getName());

			tester.setInputOntology(inputFile);

			if (tester.isConsistent() != isConsistent)
			{
				_logger.severe("Fail  : Consistency error");
				return TEST_FAIL;
			}
		}

		_logger.fine("Result: PASS");

		return TEST_PASS;
	}

	private int doEntailmentTest(final Resource testCase, final boolean isEntailed, final boolean classifyFirst)
	{
		String inputFile = testCase.hasProperty(premiseDocument) ? testCase.getProperty(premiseDocument).getObject().toString() : null;
		String conclusionsFile = testCase.getProperty(conclusionDocument).getObject().toString();

		if (inputFile != null)
		{
			inputFile = getFileName(inputFile);

			_logger.fine("Input : " + inputFile);
		}
		conclusionsFile = getFileName(conclusionsFile);
		_logger.fine("Conc. : " + conclusionsFile);

		for (final WebOntTester tester2 : _testers)
		{
			final WebOntTester tester = tester2;

			_logger.fine("Tester: " + tester.getClass().getName());
			tester.setInputOntology(inputFile);

			if (!tester.isConsistent())
			{
				_logger.severe("Fail  : Premises file is not consistent!");
				return TEST_FAIL;
			}

			// This is for the ClassificationTest used for galen, koala, SUMO, SWEET, wine
			if (classifyFirst)
				tester.classify();

			tester.testEntailment(conclusionsFile, isEntailed);

			// double check for entailments after classification and realization to test for possible bugs in caching and taxonomy
			if (WebOntTest.DOUBLE_CHECK_ENTAILMENTS)
			{
				tester.classify();

				tester.testEntailment(conclusionsFile, isEntailed);
			}
		}

		_logger.fine("Result: PASS");

		return TEST_PASS;
	}

	private int doTestCase(final Resource testCase, final Resource testType)
	{
		if (testType.equals(PositiveEntailmentTest))
			return doEntailmentTest(testCase, true, false);
		else
			if (testType.equals(NegativeEntailmentTest))
				return doEntailmentTest(testCase, false, false);
			else
				if (testType.equals(ConsistencyTest))
					return doConsistencyTest(testCase, true);
				else
					if (testType.equals(InconsistencyTest))
						return doConsistencyTest(testCase, false);
					else
						if (testType.equals(ImportEntailmentTest))
							return doEntailmentTest(testCase, true, false);
						else
							if (testType.equals(NotOwlFeatureTest))
								return TEST_SKIP;
							else
								if (testType.equals(ImportLevelTest))
									return TEST_SKIP;
								else
									if (testType.equals(OWLforOWLTest))
										return TEST_SKIP;
									else
										if (testType.equals(ClassificationTest))
											return doEntailmentTest(testCase, true, true);

		throw new OpenError("Unknown test type " + testType.getLocalName() + " for " + testCase);
	}

	public static void printStackTrace(final Throwable e)
	{
		final StackTraceElement[] ste = e.getStackTrace();

		System.err.println(e);
		if (ste.length > 25)
		{
			for (int i = 0; i < 15 && i < ste.length; i++)
				System.err.println("   " + ste[i]);
			System.err.println("   ...");
			for (int i = ste.length - 10; i < ste.length; i++)
				System.err.println("   " + ste[i]);
		}
		else
			for (final StackTraceElement element : ste)
				System.err.println("   " + element);
	}
}
