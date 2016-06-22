package openllet.pellint.test.lintpattern.axiom;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import openllet.owlapi.OWL;
import openllet.pellint.lintpattern.axiom.EquivalentToAllValuePattern;
import openllet.pellint.model.Lint;
import openllet.pellint.model.LintFixer;
import openllet.pellint.test.PellintTestCase;
import openllet.pellint.util.CollectionUtil;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Harris Lin
 */
public class EquivalentToAllValuePatternTest extends PellintTestCase
{

	private EquivalentToAllValuePattern _pattern;

	@Override
	@Before
	public void setUp() throws OWLOntologyCreationException
	{
		super.setUp();
		_pattern = new EquivalentToAllValuePattern();
	}

	@Test
	public void testNone()
	{
		assertTrue(_pattern.isFixable());

		OWLAxiom axiom = OWL.subClassOf(_cls[0], _P0AllC0);
		assertNull(_pattern.match(_ontology, axiom));

		axiom = OWL.disjointClasses(_cls[1], _P0AllC0);
		assertNull(_pattern.match(_ontology, axiom));

		axiom = OWL.equivalentClasses(CollectionUtil.asSet(_cls[0], _P0AllC0, _cls[2]));
		assertNull(_pattern.match(_ontology, axiom));
	}

	@Test
	public void testOne()
	{
		final OWLAxiom axiom = OWL.equivalentClasses(_cls[0], _P0AllC0);

		final Lint lint = _pattern.match(_ontology, axiom);
		assertNotNull(lint);
		assertTrue(lint.getParticipatingClasses().contains(_cls[0]));

		final LintFixer fixer = lint.getLintFixer();
		assertTrue(fixer.getAxiomsToRemove().contains(axiom));
		final OWLAxiom expectedAxiom = OWL.subClassOf(_cls[0], _P0AllC0);
		assertTrue(fixer.getAxiomsToAdd().contains(expectedAxiom));

		assertNull(lint.getSeverity());
		assertSame(_ontology, lint.getParticipatingOntology());
	}
}
