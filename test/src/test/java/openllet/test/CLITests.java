package openllet.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import junit.framework.JUnit4TestAdapter;
import openllet.OpenlletClassify;
import openllet.OpenlletCmdApp;
import openllet.OpenlletConsistency;
import openllet.OpenlletEntailment;
import openllet.OpenlletExplain;
import openllet.OpenlletExtractInferences;
import openllet.OpenlletInfo;
import openllet.OpenlletModularity;
import openllet.OpenlletQuery;
import openllet.OpenlletRealize;
import openllet.OpenlletTransTree;
import openllet.OpenlletUnsatisfiable;
import openllet.Openllint;
import openllet.owlapi.OWL;
import org.junit.Test;

public class CLITests
{

	private abstract class CLIMaker
	{

		protected abstract OpenlletCmdApp create();

		public void run(final String... args)
		{
			OWL._manager.ontologies().forEach(OWL._manager::removeOntology);
			final OpenlletCmdApp app = create();
			app.parseArgs(prepend(args, app.getAppCmd()));
			app.run();
		}
	}

	private static String[] prepend(final String[] strs, final String... prefix)
	{
		final String[] value = Arrays.copyOf(prefix, strs.length + prefix.length);
		for (int i = prefix.length; i < value.length; i++)
			value[i] = strs[i - prefix.length];
		return value;
	}

	private static void runAppSimple(final CLIMaker app, final String... args)
	{
		app.run(args);
	}

	private static void runAppVerbose(final CLIMaker app, final String... args)
	{
		app.run(args);
		app.run(prepend(args, "-v"));
		app.run(prepend(args, "--verbose"));
	}

	private static void runWithLoaders(final CLIMaker app, final String... args)
	{
		runAppVerbose(app, args);
		app.run(prepend(args, "-l", "OWLAPI"));
		app.run(prepend(args, "-l", "Jena"));
	}

	private static void runWithIgnore(final CLIMaker app, final String... args)
	{
		runWithLoaders(app, args);
		runWithLoaders(app, prepend(args, "--ignore-imports"));
	}

	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter(CLITests.class);
	}

	@Test
	public void classify()
	{
		runWithIgnore(new CLIMaker()
		{

			@Override
			protected OpenlletCmdApp create()
			{
				return new OpenlletClassify();
			}
		}, fileIRI("test/data/misc/family.owl"));
	}

	private String fileIRI(final String s)
	{
		return getClass().getResource("/" + s).toString();
	}

	@Test
	public void classifyWithPersist()
	{
		runWithIgnore(new CLIMaker()
		{

			@Override
			protected OpenlletCmdApp create()
			{
				return new OpenlletClassify();
			}
		}, "--persist", fileIRI("test/data/misc/family.owl"));
		final File folder = new File(System.getProperty("user.dir"));
		final File[] persistenceFiles = folder.listFiles((FilenameFilter) (dir, name) -> name.startsWith("persisted-state-"));
		// check that persistence generated the proper file
		// the file name contains a hash code of the ontology's IRI
		assertTrue(persistenceFiles.length > 0);
		// run again (to test operation from a persisted state)
		runWithIgnore(new CLIMaker()
		{

			@Override
			protected OpenlletCmdApp create()
			{
				return new OpenlletClassify();
			}
		}, "--persist", fileIRI("test/data/misc/family.owl"));
		for (final File file : persistenceFiles)
			file.delete();
	}

	@Test
	public void consistency()
	{
		runWithIgnore(new CLIMaker()
		{

			@Override
			protected OpenlletCmdApp create()
			{
				return new OpenlletConsistency();
			}
		}, fileIRI("test/data/misc/family.owl"));
	}

	// //DIG doesn't terminate - hard to test.
	// @Test
	// public void dig() {
	//
	// }
	@Test
	public void entailment()
	{
		runAppVerbose(new CLIMaker()
		{
			@Override
			protected OpenlletCmdApp create()
			{
				return new OpenlletEntailment();
			}
		}, "-e", fileIRI("test/data/modularity/koala-conclusions.owl"), fileIRI("test/data/modularity/koala.owl"));
	}

	@Test
	public void explain()
	{
		runAppVerbose(new CLIMaker()
		{
			@Override
			protected OpenlletCmdApp create()
			{
				return new OpenlletExplain();
			}
		}, fileIRI("test/data/modularity/koala.owl"));
	}

	@Test
	public void extract()
	{
		runWithIgnore(new CLIMaker()
		{
			@Override
			protected OpenlletCmdApp create()
			{
				return new OpenlletExtractInferences();
			}
		}, fileIRI("test/data/misc/family.owl"));
	}

	@Test
	public void info()
	{
		runAppSimple(new CLIMaker()
		{
			@Override
			protected OpenlletCmdApp create()
			{
				return new OpenlletInfo();
			}
		}, //
				fileIRI("test/data/modularity/koala.owl"), //
				fileIRI("test/data/modularity/galen.owl"), //
				fileIRI("test/data/modularity/miniTambis.owl"), //
				fileIRI("test/data/modularity/SUMO.owl"), //
				fileIRI("test/data/modularity/SWEET.owl"), //
				fileIRI("test/data/modularity/wine.owl")//
		);
	}

	@Test
	public void modularity()
	{
		runAppVerbose(new CLIMaker()
		{
			@Override
			protected OpenlletCmdApp create()
			{
				return new OpenlletModularity();
			}
		}, "-s", "Koala", fileIRI("test/data/modularity/koala.owl"));
	}

	@Test
	public void pellint()
	{
		runAppVerbose(new CLIMaker()
		{
			@Override
			protected OpenlletCmdApp create()
			{
				return new Openllint();
			}
		}, fileIRI("test/data/misc/family.owl"));
	}

	@Test
	public void query()
	{
		runAppVerbose(new CLIMaker()
		{
			@Override
			protected OpenlletCmdApp create()
			{
				return new OpenlletQuery();
			}
		}, "-q", fileIRI("test/data/query/sameAs/sameAs-01.rq"), fileIRI("test/data/query/sameAs/data-01.ttl"));
	}

	@Test
	public void realize()
	{
		runWithIgnore(new CLIMaker()
		{
			@Override
			protected OpenlletCmdApp create()
			{
				return new OpenlletRealize();
			}
		}, fileIRI("test/data/misc/family.owl"));
	}

	@Test
	public void transTree()
	{
		runAppVerbose(new CLIMaker()
		{
			@Override
			protected OpenlletCmdApp create()
			{
				return new OpenlletTransTree();
			}
		}, "-p", "http://www.co-ode.org/ontologies/test/pellet/transitive.owl#p", fileIRI("test/data/misc/transitiveSub.owl"));
	}

	@Test
	public void transTree2()
	{
		runAppVerbose(new CLIMaker()
		{

			@Override
			protected OpenlletCmdApp create()
			{
				return new OpenlletTransTree();
			}
		}, "-p", "http://www.co-ode.org/ontologies/test/pellet/transitive.owl#p", "-f", "http://www.co-ode.org/ontologies/test/pellet/transitive.owl#A", "--individuals", fileIRI("test/data/misc/transitiveSub.owl"));
	}

	@Test
	public void unsatisfiable()
	{
		runWithLoaders(new CLIMaker()
		{
			@Override
			protected OpenlletCmdApp create()
			{
				return new OpenlletUnsatisfiable();
			}
		}, fileIRI("test/data/modularity/koala.owl"));
	}
	// @Test
	// public void validate() {
	// runWithIgnore( new CLIMaker(){
	// @Override
	// protected PelletCmdApp create(String[] args) {
	// return new PelletValidate();
	// }
	// } );
	// }
}
