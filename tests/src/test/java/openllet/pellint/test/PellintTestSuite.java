package openllet.pellint.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import openllet.pellint.test.lintpattern.LintPatternLoaderTest;
import openllet.pellint.test.lintpattern.axiom.EquivalentToAllValuePatternTest;
import openllet.pellint.test.lintpattern.axiom.EquivalentToComplementPatternTest;
import openllet.pellint.test.lintpattern.axiom.EquivalentToTopPatternTest;
import openllet.pellint.test.lintpattern.axiom.GCIPatternTest;
import openllet.pellint.test.lintpattern.axiom.LargeCardinalityPatternTest;
import openllet.pellint.test.lintpattern.axiom.LargeDisjunctionPatternTest;
import openllet.pellint.test.lintpattern.ontology.EquivalentAndSubclassAxiomPatternTest;
import openllet.pellint.test.lintpattern.ontology.ExistentialExplosionPatternTest;
import openllet.pellint.test.lintpattern.ontology.TooManyDifferentIndividualsPatternTest;
import openllet.pellint.test.model.LintFixerTest;
import openllet.pellint.test.model.LintTest;
import openllet.pellint.test.model.OntologyLintsTest;
import openllet.pellint.test.rdfxml.DoubtfulSetTest;
import openllet.pellint.test.rdfxml.OWLDatatypeTest;
import openllet.pellint.test.rdfxml.OWLSyntaxCheckerTest;
import openllet.pellint.test.rdfxml.RDFModelTest;
import openllet.pellint.test.util.OWL2DLProfileViolationsTest;
import openllet.pellint.test.util.OptimizedDirectedMultigraphTest;

/**
 * <p>
 * Title: Pellint test suite
 * </p>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Harris Lin
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ //
		DoubtfulSetTest.class, //
		RDFModelTest.class, //
		OptimizedDirectedMultigraphTest.class, //
		LintTest.class, //
		LintFixerTest.class, //
		OntologyLintsTest.class, //
		LintPatternLoaderTest.class, //
		EquivalentToAllValuePatternTest.class, //
		EquivalentToComplementPatternTest.class, //
		EquivalentToTopPatternTest.class, //
		GCIPatternTest.class, //
		LargeCardinalityPatternTest.class, //
		LargeDisjunctionPatternTest.class, //
		EquivalentAndSubclassAxiomPatternTest.class, //
		ExistentialExplosionPatternTest.class, //
		TooManyDifferentIndividualsPatternTest.class, //
		OWLSyntaxCheckerTest.class, //
		OWLDatatypeTest.class, //
		OWL2DLProfileViolationsTest.class })
public class PellintTestSuite
{
	public static Test suite()
	{
		return new JUnit4TestAdapter(PellintTestSuite.class);
	}
}
