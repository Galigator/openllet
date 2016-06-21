/**
 *
 */
package com.clarkparsia.pellet.test;

import static openllet.core.utils.TermFactory.term;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.PelletOptions;
import openllet.core.rules.model.AtomIConstant;
import openllet.core.rules.model.ClassAtom;
import openllet.core.rules.model.Rule;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.OntBuilder;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pavel Klinov
 */
public class OntBuilderTest
{

	// tests that the build looks up individuals in the original KB
	@Test
	public void testLookupIndividuals()
	{
		PelletOptions.KEEP_ABOX_ASSERTIONS = true;

		final KnowledgeBase kb = new KnowledgeBase();

		final ATermAppl C = term("C");
		final ATermAppl D = term("D");
		final ATermAppl i = term("i");
		final ATermAppl j = term("j");

		kb.addClass(C);
		kb.addClass(D);
		kb.addIndividual(i);
		kb.addIndividual(j);
		kb.addSubClass(C, D);
		kb.addType(i, C);
		kb.addType(j, D);
		final Rule rule = new Rule(Collections.singleton(new ClassAtom(C, new AtomIConstant(i))), Collections.singleton(new ClassAtom(D, new AtomIConstant(j))));

		kb.addRule(rule);

		final OntBuilder builder = new OntBuilder(kb);

		final Set<ATermAppl> rules = new HashSet<>();

		rules.add(ATermUtils.makeRule(new ATermAppl[] { ATermUtils.makeTypeAtom(i, C) }, new ATermAppl[] { ATermUtils.makeTypeAtom(j, D) }));

		final KnowledgeBase copy = builder.build(rules);

		Assert.assertEquals(1, copy.getRules().size());
	}

}
