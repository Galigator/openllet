package openllet.pellint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLEntityRenamer;

import openllet.core.KnowledgeBase;
import openllet.owlapi.OWLGenericTools;
import openllet.owlapi.OWLHelper;
import openllet.owlapi.OWLManagerGroup;
import openllet.owlapi.OpenlletReasoner;

public class SearchMinimalBug
{
	private static String minimalOntologyFile = "minimalOntologyFile.owl";
	private static final int nCPU = Runtime.getRuntime().availableProcessors();
	private static ExecutorService _executor = Executors.newFixedThreadPool(nCPU);
	private final Random _random = new Random();
	private int _minimalAxiomsCount = -1;

	private List<OWLAxiom> randomizeAxioms(final OWLAxiom[] axioms)
	{
		for (int i = 0, l = axioms.length; i < l; i++)
		{
			final int rand = _random.nextInt(l);
			final OWLAxiom tmp = axioms[rand];
			axioms[rand] = axioms[i];
			axioms[i] = tmp;
		}
		return Arrays.asList(axioms);
	}

	private synchronized void registerOntology(final OWLOntology o)
	{
		final int axiomCount = o.getAxiomCount();
		if (axiomCount < _minimalAxiomsCount || _minimalAxiomsCount < 0)
		{
			_minimalAxiomsCount = axiomCount;
			try (OutputStream outputStream = new FileOutputStream(minimalOntologyFile))
			{
				o.getOWLOntologyManager().saveOntology(o, outputStream);
				System.out.println("We have a new minimum with : " + _minimalAxiomsCount + " axioms. Greating thread " + Thread.currentThread().getName() + " !");
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	class Worker implements Runnable
	{
		private final List<OWLAxiom> _allAxioms;
		private final List<OWLAxiom> _removedAxioms;
		private final String NS = OWLHelper._protocol + Thread.currentThread().getName();
		private final int _tryCount = 0;

		public Worker(final OWLAxiom[] axioms)
		{
			_allAxioms = new LinkedList<>(randomizeAxioms(axioms));
			_removedAxioms = new ArrayList<>(_allAxioms.size());
		}

		private void test(final OWLHelper owl)
		{
			final int index = _random.nextInt(_allAxioms.size());
			final OWLAxiom axiom = _allAxioms.remove(index);
			_removedAxioms.add(axiom);
			owl.getOntology().add(_allAxioms);

			final OpenlletReasoner r = (OpenlletReasoner) owl.getReasoner();
			try
			{
				r.isConsistent();
				final KnowledgeBase kb = r.getKB();
				kb.realize();
				kb.classify();
				// The error have disappear; lets roll back.
				final OWLAxiom correct = _removedAxioms.remove(_removedAxioms.size() - 1);
				System.out.println(correct + "\t may be part of the problem");
				_allAxioms.add(correct);
			}
			catch (@SuppressWarnings("unused") final Exception e)
			{ // So the error is still present. We have a new minimal ontology.
				registerOntology(owl.getOntology());
			}
		}

		@Override
		public void run()
		{
			while (!_allAxioms.isEmpty())
				try (final OWLManagerGroup group = new OWLManagerGroup())
				{
					test(new OWLGenericTools(group, OWLHelper.getVersion(IRI.create(NS), _tryCount), true));
				}
				catch (final Exception e)
				{
					e.printStackTrace();
				}
		}
	}

	private static void renameObjects(final OWLOntology o)
	{
		final OWLOntologyManager m = o.getOWLOntologyManager();
		final OWLEntityRenamer renamer = new OWLEntityRenamer(m, Collections.singleton(o));
		final Map<OWLEntity, IRI> entity2IRIMap = new HashMap<>();
		final AtomicInteger ai = new AtomicInteger();
		o.individualsInSignature().forEach(toRename -> entity2IRIMap.put(toRename, IRI.create("x:ind/" + ai.getAndIncrement())));
		o.classesInSignature().forEach(toRename -> entity2IRIMap.put(toRename, IRI.create("x:cls/" + ai.getAndIncrement())));
		o.annotationPropertiesInSignature().forEach(toRename -> entity2IRIMap.put(toRename, IRI.create("x:ap/" + ai.getAndIncrement())));
		o.dataPropertiesInSignature().forEach(toRename -> entity2IRIMap.put(toRename, IRI.create("x:dp/" + ai.getAndIncrement())));
		o.objectPropertiesInSignature().forEach(toRename -> entity2IRIMap.put(toRename, IRI.create("x:op/" + ai.getAndIncrement())));
		o.applyChanges(renamer.changeIRI(entity2IRIMap));
	}

	public void process(final File file, final boolean rename)
	{
		try
		{
			final OWLOntology o = OWLManager.createConcurrentOWLOntologyManager().loadOntologyFromOntologyDocument(file);

			if (rename)
				renameObjects(o);
			// o.add(OWL.classAssertion(OWL.Individual("y:impossible"), OWL.and(OWL.Class("y:cls/a"), OWL.not(OWL.Class("y:cls/a")))));

			final OWLAxiom[] axioms = o.axioms().toArray(OWLAxiom[]::new);

			for (int i = 0; i < nCPU; i++)
				_executor.execute(new Worker(axioms.clone()));
			// new Worker(axioms.clone()).run();
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}

	}

	public static void main(final String[] args)
	{
		new SearchMinimalBug().process(new File(args[0]), args.length > 1 ? Boolean.parseBoolean(args[1]) : false);
	}
}
