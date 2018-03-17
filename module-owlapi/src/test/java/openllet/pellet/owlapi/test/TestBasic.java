package openllet.pellet.owlapi.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import openllet.core.rules.builtins.BuiltInRegistry;
import openllet.core.rules.builtins.FunctionBuiltIn;
import openllet.core.rules.builtins.NumericAdapter;
import openllet.core.rules.builtins.NumericFunction;
import openllet.core.utils.SetUtils;
import openllet.owlapi.OWL;
import openllet.owlapi.OWLGenericTools;
import openllet.owlapi.OWLHelper;
import openllet.owlapi.OWLManagerGroup;
import openllet.owlapi.SWRL;
import openllet.owlapi.XSD;
import openllet.shared.tools.Log;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.vocab.OWLFacet;

/**
 * Test basic of openllet-owlapi
 *
 * @since 2.6.0
 */
public class TestBasic
{
	static
	{
		System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "WARN");
		Log.setLevel(Level.WARNING, OWLGenericTools.class);
		Log._defaultLevel = Level.INFO;
	}
	private static final String NS = "http://test.org#";
	private static final Function<String, OWLNamedIndividual> i = s -> OWL.Individual(NS + s);
	private static final Function<String, OWLObjectProperty> o = s -> OWL.ObjectProperty(NS + s);
	private static final Function<String, OWLDataProperty> d = s -> OWL.DataProperty(NS + s);
	private static final Function<String, OWLClass> c = s -> OWL.Class(NS + s);

	private final OWLClass ClsA = c.apply("ClsA");
	private final OWLClass ClsB = c.apply("ClsB");
	private final OWLClass ClsC = c.apply("ClsC");
	private final OWLClass ClsD = c.apply("ClsD");
	private final OWLClass ClsE = c.apply("ClsE");
	private final OWLClass ClsF = c.apply("ClsF");
	private final OWLClass ClsG = c.apply("ClsG");
	private final OWLNamedIndividual Ind1 = i.apply("Ind1");
	private final OWLObjectProperty propA = o.apply("mimiroux");
	private final OWLDataProperty propB = d.apply("propB");
	private final SWRLVariable varA = SWRL.variable(IRI.create(NS + "a"));

	@Test
	public void rule() throws OWLOntologyCreationException
	{
		try (final OWLManagerGroup group = new OWLManagerGroup())
		{
			final OWLOntologyID ontId = OWLHelper.getVersion(IRI.create(NS + "owlapi.tests"), 1.0);

			final OWLHelper owl = new OWLGenericTools(group, ontId, true);

			owl.declareIndividual(ClsA, Ind1);

			{
				final List<OWLClass> entities = owl.getReasoner().getTypes(Ind1).entities().collect(Collectors.toList());
				assertTrue(entities.size() == 2);
				assertTrue(entities.contains(ClsA));
				assertTrue(entities.contains(OWL.Thing));
			}

			owl.addAxiom(SWRL.rule(//
					SWRL.antecedent(SWRL.classAtom(ClsA, varA)), //
					SWRL.consequent(SWRL.classAtom(ClsB, varA))//
			));

			{
				final List<OWLClass> entities = owl.getReasoner().getTypes(Ind1).entities().collect(Collectors.toList());
				assertTrue(entities.size() == 3);
				assertTrue(entities.contains(ClsA));
				assertTrue(entities.contains(ClsB));
				assertTrue(entities.contains(OWL.Thing));
			}

			owl.addAxiom(SWRL.rule(//
					SWRL.antecedent(SWRL.classAtom(ClsB, varA)), //
					SWRL.consequent(SWRL.classAtom(ClsC, varA))//
			));

			{
				final List<OWLClass> entities = owl.getReasoner().getTypes(Ind1).entities().collect(Collectors.toList());
				assertTrue(entities.size() == 4);
				assertTrue(entities.contains(ClsA));
				assertTrue(entities.contains(ClsB));
				assertTrue(entities.contains(ClsC));
				assertTrue(entities.contains(OWL.Thing));
			}

			owl.addClass(Ind1, ClsD);

			owl.addAxiom(SWRL.rule(//
					SWRL.antecedent(SWRL.classAtom(OWL.and(ClsD, ClsC), varA)), //
					SWRL.consequent(SWRL.classAtom(ClsE, varA))//
			));

			{
				final List<OWLClass> entities = owl.getReasoner().getTypes(Ind1).entities().collect(Collectors.toList());
				assertTrue(entities.size() == 6);
				assertTrue(entities.contains(ClsA));
				assertTrue(entities.contains(ClsB));
				assertTrue(entities.contains(ClsC));
				assertTrue(entities.contains(ClsD));
				assertTrue(entities.contains(ClsE));
				assertTrue(entities.contains(OWL.Thing));
			}

			owl.addClass(Ind1, OWL.not(ClsF)); // Mark the negation to enforce the open world assertion.

			final SWRLRule D_and_NotF = SWRL.rule(//
					SWRL.antecedent(SWRL.classAtom(OWL.and(ClsD, OWL.not(ClsF)), varA)), //
					SWRL.consequent(SWRL.classAtom(ClsG, varA))//
			);

			{
				owl.addAxiom(D_and_NotF);
				final List<OWLClass> entities = owl.getReasoner().getTypes(Ind1).entities().collect(Collectors.toList());
				assertTrue(entities.contains(ClsG));
				owl.removeAxiom(D_and_NotF);
			}

			final SWRLRule D_and_F = SWRL.rule(//
					SWRL.antecedent(SWRL.classAtom(OWL.and(ClsD, ClsF), varA)), //
					SWRL.consequent(SWRL.classAtom(ClsG, varA))//
			);

			{
				owl.addAxiom(D_and_F);
				final List<OWLClass> entities = owl.getReasoner().getTypes(Ind1).entities().collect(Collectors.toList());
				assertFalse(entities.contains(ClsG));
				owl.removeAxiom(D_and_F);
			}
		}
	}

	@Test
	public void incrementalStorage() throws OWLOntologyCreationException
	{ // RDFJsonLDDocumentFormatFactory // TurtleDocumentFormatFactory // doesn't work with : RDFXMLDocumentFormatFactory

		final String ontologyName = "owlapi.inc.storage";
		final File file = new File("target/" + "http___test.org#" + ontologyName + "-" + "http___test.org#" + ontologyName + "_1.0.owl");

		final String hardString = ");alpha\"#\\\n \t\n\rbeta<xml></xml>";

		try (final OWLManagerGroup group = new OWLManagerGroup())
		{
			group.setOntologiesDirectory(new File("target"));
			group.getPersistentManager();

			final OWLOntologyID ontId = OWLHelper.getVersion(IRI.create(NS + ontologyName), 1.0);
			final OWLHelper owl = new OWLGenericTools(group, ontId, false);

			owl.addAxiom(OWL.declaration(ClsA));
			owl.addAxiom(OWL.declaration(ClsB));
			owl.addAxiom(OWL.propertyAssertion(Ind1, propA, Ind1));
			owl.addAxiom(OWL.propertyAssertion(Ind1, propB, OWL.constant(hardString)));

			// This test is good but a little too slow when building the project ten time a day.
			//			try
			//			{
			//				System.out.println("Waiting begin");
			//				Thread.sleep(65 * 1000);
			//				System.out.println("Waiting end");
			//			}
			//			catch (final Exception e)
			//			{
			//				e.printStackTrace();
			//			}
			group.flushIncrementalStorage();

			assertTrue("file doesn't exist : " + file, file.exists());
			file.delete();
			assertTrue(owl.getObject(Ind1, propA).get().getIRI().equals(Ind1.getIRI()));
		}

		final Set<OWLAxiom> expected = new HashSet<>();
		expected.add(OWL.declaration(ClsA));
		expected.add(OWL.declaration(ClsB));
		expected.add(OWL.propertyAssertion(Ind1, propA, Ind1));
		expected.add(OWL.propertyAssertion(Ind1, propB, OWL.constant(hardString)));

		try (final OWLManagerGroup group = new OWLManagerGroup())
		{
			group.setOntologiesDirectory(new File("target"));
			group.getPersistentManager();

			final OWLOntologyID ontId = OWLHelper.getVersion(IRI.create(NS + ontologyName), 1.0);
			final OWLHelper owl = new OWLGenericTools(group, ontId, false);

			owl.addAxioms(expected.stream());
		} // Autoclose force a flush.

		// RDF/XML add force the addition of missing declaration.
		expected.add(OWL.declaration(Ind1));
		expected.add(OWL.declaration(propA));
		expected.add(OWL.declaration(propB));

		try (final OWLManagerGroup group = new OWLManagerGroup()) // Force the manager to read the file.
		{
			assertTrue(file.exists());
			group.setOntologiesDirectory(new File("target"));
			group.getPersistentManager();

			final OWLOntologyID ontId = OWLHelper.getVersion(IRI.create(NS + ontologyName), 1.0);
			final OWLHelper owl = new OWLGenericTools(group, ontId, false);

			final String[] foundStrings = owl.getOntology().axioms().map(OWLAxiom::toString).sorted().toArray(String[]::new);
			final String[] expectedStrings = expected.stream().map(OWLAxiom::toString).sorted().toArray(String[]::new);

			assertTrue("hard String !", owl.getValue(Ind1, propB).map(x -> x.getLiteral().equals(hardString)).orElse(false));

			assertTrue(foundStrings.length == expectedStrings.length);

			for (int j = 0, l = expectedStrings.length; j < l; j++)
			{
				final String es = expectedStrings[j];
				final String fs = foundStrings[j];

				if (!es.equals(fs))
				{
					assertTrue("Axiom size different", fs.length() == es.length());
					System.out.println(es);
					System.out.println(fs);
					final byte[] bE = es.getBytes();
					final byte[] bF = fs.getBytes();
					for (int k = 0, kl = bE.length; k < kl; k++)
						if (bE[k] != bF[k])
							System.out.println("Byte[" + k + "] -> " + bE[k] + " != " + bF[k]);
				}

				assertTrue(es + "!=" + fs, es.equals(fs));

			}
		} // Autoclose force a flush.
		finally
		{ // Delete the file.
			assertTrue(file.exists());
			file.delete();
		}
	}

	@Test
	public void testRegexRestriction() throws OWLOntologyCreationException
	{
		try (final OWLManagerGroup group = new OWLManagerGroup())
		{
			final OWLOntologyID ontId = OWLHelper.getVersion(IRI.create(NS + "owlapi.inc.regex.restriction"), 1.0);
			final OWLHelper owl = new OWLGenericTools(group, ontId, true);

			final OWLNamedIndividual x1 = i.apply("I1");
			final OWLNamedIndividual x2 = i.apply("I2");

			owl.addAxiom(OWL.equivalentClasses(ClsA, OWL.some(propB, OWL.restrict(XSD.STRING, OWL.facetRestriction(OWLFacet.PATTERN, OWL.constant("A.A"))))));
			owl.addAxiom(OWL.propertyAssertion(x1, propB, OWL.constant("AAA")));
			owl.addAxiom(OWL.propertyAssertion(x2, propB, OWL.constant("BBB")));

			owl.addAxiom(OWL.differentFrom(x1, x2));

			final OWLReasoner r = owl.getReasoner();
			assertTrue(r.isEntailed(OWL.classAssertion(x1, ClsA)));
			assertFalse(r.isEntailed(OWL.classAssertion(x2, ClsA)));
		}
	}

	/*
		@Test
		public void testDurationRestriction() throws OWLOntologyCreationException
		{
			try (final OWLManagerGroup group = new OWLManagerGroup())
			{
				final OWLOntologyID ontId = OWLHelper.getVersion(IRI.create(NS + "owlapi.inc.regex.restriction"), 1.0);
				final OWLHelper owl = new OWLGenericTools(group, ontId, true);
	
				final OWLNamedIndividual x1 = i.apply("I1");
				final OWLNamedIndividual x2 = i.apply("I2");
	
				final javax.xml.datatype.Duration duration = DatatypeFactory.newInstance().newDuration(1000);// Duration of 1000ms
	
				System.out.println(duration.getXMLSchemaType());
	
				//final OWL2Datatype durationDataType = OWL2Datatype.getDatatype(IRI.create(Namespaces.XSD + "duration"));
				final OWL2Datatype durationDataType = OWL2Datatype.XSD_DATE_TIME;
				assertTrue(owl.getReasoner().isConsistent());
				owl.addAxiom(//
						OWL.equivalentClasses(ClsA, //
								OWL.some(propB, //
										OWL.restrict(XSD.DURATION, //
												OWL.facetRestriction(//
														OWLFacet.MAX_INCLUSIVE, //
														OWL._factory.getOWLLiteral("1000", durationDataType)//  javax.xml.datatype.Duration parser convert that into 1000ms
												)//
										)//
								)//
						)//
				);
				assertTrue(owl.getReasoner().isConsistent());
	
				owl.addAxiom(OWL.propertyAssertion(x1, propB, OWL._factory.getOWLLiteral("500", durationDataType)));
				owl.getReasoner().isConsistent();
				final KnowledgeBase kb = ((PelletReasoner) owl.getReasoner()).getKB();
				System.out.println(kb.getExplanation());
	
				assertTrue(owl.getReasoner().isConsistent());
				owl.addAxiom(OWL.propertyAssertion(x2, propB, OWL._factory.getOWLLiteral("1500", durationDataType)));
				assertTrue(owl.getReasoner().isConsistent());
	
				owl.addAxiom(OWL.differentFrom(x1, x2));
				assertTrue(owl.getReasoner().isConsistent());
	
				final OWLReasoner r = owl.getReasoner();
				assertTrue(r.isEntailed(OWL.classAssertion(x1, ClsA)));
				assertFalse(r.isEntailed(OWL.classAssertion(x2, ClsA)));
			}
			catch (final DatatypeConfigurationException exception)
			{
				throw new OpenError(exception);
			}
		}
	*/

	@Test
	public void testRestrictionConjonction() throws OWLOntologyCreationException
	{
		for (int loop = 0; loop < 20; loop++)
			try (final OWLManagerGroup group = new OWLManagerGroup())
			{
				final OWLOntologyID ontId = OWLHelper.getVersion(IRI.create(NS + "owlapi.inc.integer-min-max.restriction"), 1.0);
				final OWLHelper owl = new OWLGenericTools(group, ontId, true);

				final OWLNamedIndividual x1 = i.apply("I1");
				final OWLNamedIndividual x2 = i.apply("I2");
				final OWLNamedIndividual x3 = i.apply("I3");
				final OWLNamedIndividual x4 = i.apply("I4");

				owl.addAxiom(OWL.equivalentClasses(ClsA, OWL.some(propB, //
						OWL.restrict(XSD.INTEGER, //
								OWL.facetRestriction(OWLFacet.MIN_INCLUSIVE, OWL.constant(100)), //
								OWL.facetRestriction(OWLFacet.MAX_INCLUSIVE, OWL.constant(250))//
						))));//

				owl.addAxiom(OWL.equivalentClasses(ClsB, OWL.some(propB, //
						OWL.restrict(XSD.INTEGER, //
								OWL.facetRestriction(OWLFacet.MIN_INCLUSIVE, OWL.constant(250)), //
								OWL.facetRestriction(OWLFacet.MAX_INCLUSIVE, OWL.constant(252))//
						))));//

				owl.addAxiom(OWL.equivalentClasses(ClsD, OWL.and(ClsC, ClsA)));
				owl.addAxiom(OWL.equivalentClasses(ClsE, OWL.and(ClsC, ClsB)));

				owl.addAxiom(OWL.propertyAssertion(x1, propB, OWL.constant(15)));
				owl.addAxiom(OWL.propertyAssertion(x2, propB, OWL.constant(150)));
				owl.addAxiom(OWL.propertyAssertion(x3, propB, OWL.constant(250)));
				owl.addAxiom(OWL.propertyAssertion(x4, propB, OWL.constant(300)));

				owl.addAxiom(OWL.classAssertion(x1, ClsC));
				owl.addAxiom(OWL.classAssertion(x2, ClsC));
				owl.addAxiom(OWL.classAssertion(x3, ClsC));
				owl.addAxiom(OWL.classAssertion(x4, ClsC));

				{
					final OWLReasoner r = owl.getReasoner();
					r.types(x1).forEach(cx -> System.out.println("x1 : " + cx));
					r.types(x2).forEach(cx -> System.out.println("x2 : " + cx));
					r.types(x3).forEach(cx -> System.out.println("x3 : " + cx));
					r.types(x4).forEach(cx -> System.out.println("x4 : " + cx));

				}
				//
				//
				//			owl.addAxiom(OWL.classAssertion(x2, ClsB));
				//
				//			{
				//				final OWLReasoner r = owl.getReasoner();
				//				assertTrue(r.isEntailed(OWL.classAssertion(x2, ClsC)));
				//				assertFalse(r.isEntailed(OWL.classAssertion(x3, ClsC)));
				//			}
			}
	}

	@Test
	public void testMultipleStringRestriction() throws OWLOntologyCreationException
	{
		try (final OWLManagerGroup group = new OWLManagerGroup())
		{
			final OWLOntologyID ontId = OWLHelper.getVersion(IRI.create(NS + "owlapi.inc.maxLength.restriction"), 1.0);
			final OWLHelper owl = new OWLGenericTools(group, ontId, true);

			final OWLNamedIndividual x0 = i.apply("I0");
			final OWLNamedIndividual x1 = i.apply("I1");
			final OWLNamedIndividual x2 = i.apply("I2");
			final OWLNamedIndividual x3 = i.apply("I1");
			final OWLNamedIndividual x4 = i.apply("I2");

			owl.addAxiom(//
					OWL.equivalentClasses(//
							ClsA, //
							OWL.some(//
									propB, //
									OWL.restrict(//
											XSD.STRING, //
											OWL.facetRestriction(OWLFacet.MAX_LENGTH, OWL.constant(5L)), //
											OWL.facetRestriction(OWLFacet.MIN_LENGTH, OWL.constant(1L)), //
											OWL.facetRestriction(OWLFacet.PATTERN, OWL.constant("A.[XYZ]A+"))//
									)//
							)//
					)//
			);

			owl.addAxiom(OWL.propertyAssertion(x0, propB, OWL.constant("")));
			owl.addAxiom(OWL.propertyAssertion(x1, propB, OWL.constant("ABXA")));
			owl.addAxiom(OWL.propertyAssertion(x2, propB, OWL.constant("ABBA")));
			owl.addAxiom(OWL.propertyAssertion(x3, propB, OWL.constant("AXYAA")));
			owl.addAxiom(OWL.propertyAssertion(x4, propB, OWL.constant("AAAAAAAAA")));

			owl.addAxiom(OWL.differentFrom(SetUtils.create(x0, x1, x2, x3, x4)));

			final OWLReasoner r = owl.getReasoner();
			assertFalse(r.isEntailed(OWL.classAssertion(x0, ClsA))); // > 1
			assertTrue(r.isEntailed(OWL.classAssertion(x1, ClsA))); // match regexp
			assertFalse(r.isEntailed(OWL.classAssertion(x2, ClsA))); // doesn't match regexp
			assertTrue(r.isEntailed(OWL.classAssertion(x3, ClsA))); // match regexp
			assertFalse(r.isEntailed(OWL.classAssertion(x4, ClsA))); // < 5
		}
	}

	@Test
	public void testMaxLengthRestriction() throws OWLOntologyCreationException
	{
		try (final OWLManagerGroup group = new OWLManagerGroup())
		{
			final OWLOntologyID ontId = OWLHelper.getVersion(IRI.create(NS + "owlapi.inc.maxLength.restriction"), 1.0);
			final OWLHelper owl = new OWLGenericTools(group, ontId, true);

			final OWLNamedIndividual x0 = i.apply("I0");
			final OWLNamedIndividual x1 = i.apply("I1");
			final OWLNamedIndividual x2 = i.apply("I2");

			owl.addAxiom(OWL.equivalentClasses(ClsA, OWL.some(propB, OWL.restrict(XSD.STRING, OWL.facetRestriction(OWLFacet.MAX_LENGTH, OWL.constant(3L))))));
			owl.addAxiom(OWL.propertyAssertion(x0, propB, OWL.constant("")));
			owl.addAxiom(OWL.propertyAssertion(x1, propB, OWL.constant("AA")));
			owl.addAxiom(OWL.propertyAssertion(x2, propB, OWL.constant("AAAAA")));

			owl.addAxiom(OWL.differentFrom(SetUtils.create(x0, x1, x2)));

			final OWLReasoner r = owl.getReasoner();
			assertTrue(r.isEntailed(OWL.classAssertion(x0, ClsA)));
			assertTrue(r.isEntailed(OWL.classAssertion(x1, ClsA)));
			assertFalse(r.isEntailed(OWL.classAssertion(x2, ClsA)));
		}
	}

	@Test
	public void testRestriction() throws OWLOntologyCreationException
	{
		try (final OWLManagerGroup group = new OWLManagerGroup())
		{
			final OWLOntologyID ontId = OWLHelper.getVersion(IRI.create(NS + "owlapi.inc.integer-float.restriction"), 1.0);
			final OWLHelper owl = new OWLGenericTools(group, ontId, true);

			final OWLNamedIndividual x1 = i.apply("I1");
			final OWLNamedIndividual x2 = i.apply("I2");

			owl.addAxiom(OWL.equivalentClasses(ClsA, OWL.some(propB, XSD.INTEGER)));
			owl.addAxiom(OWL.propertyAssertion(x1, propB, OWL.constant(1)));
			owl.addAxiom(OWL.propertyAssertion(x2, propB, OWL.constant(1.)));

			final OWLReasoner r = owl.getReasoner();
			assertTrue(r.isEntailed(OWL.classAssertion(x1, ClsA)));
			assertFalse(r.isEntailed(OWL.classAssertion(x2, ClsA)));
		}
	}

	@Test
	public void testOnlyProperties() throws OWLOntologyCreationException
	{
		try (final OWLManagerGroup group = new OWLManagerGroup())
		{
			final OWLOntologyID ontId = OWLHelper.getVersion(IRI.create(NS + "owlapi.only.properties"), 1.0);
			final OWLHelper owl = new OWLGenericTools(group, ontId, true);

			//			final OWLNamedIndividual x1 = i.apply("I1");
			//			final OWLNamedIndividual x2 = i.apply("I2");

			//owl.addAxiom(OWL.propertyAssertion(x1, propA, x2));
			owl.addAxiom(OWL.inverseProperties(o.apply("A"), o.apply("B")));
			//owl.addAxiom(OWL.declaration(ClsA));

			owl.getReasoner().getObjectPropertyDomains(o.apply("A"));
		} // The test is just about not crash.
	}

	@Test
	public void testAddAndRemove() throws OWLOntologyCreationException
	{
		try (final OWLManagerGroup group = new OWLManagerGroup())
		{
			final OWLOntologyID ontId = OWLHelper.getVersion(IRI.create(NS + "owlapi.add.remove"), 1.0);
			final OWLHelper owl = new OWLGenericTools(group, ontId, true);

			owl.addAxiom(OWL.declaration(OWL.DataProperty(NS + "propA")));
			owl.addAxiom(OWL.declaration(OWL.Class(NS + "clsA")));
			owl.addAxiom(OWL.equivalentClasses(OWL.Class(NS + "clsA"), //
					OWL.value(OWL.DataProperty(NS + "propA"), OWL.constant(12))//
			));
			assertTrue(owl.getReasoner().instances(OWL.Class(NS + "clsA")).count() == 0);

			final OWLNamedIndividual x1 = OWL.Individual(NS + "I1");

			owl.addAxiom(OWL.classAssertion(x1, OWL.Class(NS + "clsA")));
			assertTrue(owl.getReasoner().instances(OWL.Class(NS + "clsA")).count() == 1);

			owl.removeAxiom(OWL.classAssertion(x1, OWL.Class(NS + "clsA")));
			assertTrue(owl.getReasoner().instances(OWL.Class(NS + "clsA")).count() == 0);

		} // The test is just about not crash.
	}

	@Test
	public void testSubProperties() throws OWLOntologyCreationException
	{
		try (final OWLManagerGroup group = new OWLManagerGroup())
		{
			final OWLOntologyID ontId = OWLHelper.getVersion(IRI.create(NS + "owlapi.inc.properties"), 1.0);
			final OWLHelper owl = new OWLGenericTools(group, ontId, true);

			owl.addAxiom(OWL.subPropertyOf(o.apply("P2"), o.apply("P1"))); // p2 extends p1

			owl.addAxiom(OWL.propertyAssertion(i.apply("I1"), o.apply("P1"), i.apply("I2")));
			owl.addAxiom(OWL.propertyAssertion(i.apply("I3"), o.apply("P2"), i.apply("I4")));

			assertFalse(owl.getObject(i.apply("I1"), o.apply("P2")).isPresent());
			assertTrue(owl.getObject(i.apply("I3"), o.apply("P1")).get().equals(i.apply("I4")));
		}
	}

	@Test
	public void testTransitiveSubProperties() throws OWLOntologyCreationException
	{
		try (final OWLManagerGroup group = new OWLManagerGroup())
		{
			final OWLOntologyID ontId = OWLHelper.getVersion(IRI.create(NS + "owlapi.inc.transtive.properties"), 1.0);
			final OWLHelper owl = new OWLGenericTools(group, ontId, true);
			final OWLObjectProperty p1 = OWL.ObjectProperty(NS + "P1");
			final OWLObjectProperty p2 = OWL.ObjectProperty(NS + "P2");
			final OWLObjectProperty p3 = OWL.ObjectProperty(NS + "P3");
			final OWLObjectProperty p4 = OWL.ObjectProperty(NS + "P4");

			{
				owl.addAxiom(OWL.subPropertyOf(p1, p2)); // p2 extends [P1]
				final OWLObjectProperty[] chain = { p2, p1 };
				owl.addAxiom(OWL.subPropertyOf(chain, p2)); // p2 extends [P2, P1]
			}
			{
				owl.addAxiom(OWL.inverseProperties(p1, p3)); // p3 inverse of p1
			}
			{
				owl.addAxiom(OWL.subPropertyOf(p3, p4)); // p4 extends [P3]
				final OWLObjectProperty[] chain = { p4, p3 };
				owl.addAxiom(OWL.subPropertyOf(chain, p4)); // p4 extends [P4, P3]
			}

			final OWLNamedIndividual i1 = i.apply("I1");
			final OWLNamedIndividual i2 = i.apply("I2");
			final OWLNamedIndividual i3 = i.apply("I3");
			final OWLNamedIndividual i4 = i.apply("I4");
			final OWLNamedIndividual i5 = i.apply("I5");
			final OWLNamedIndividual i6 = i.apply("I6");
			final OWLNamedIndividual i7 = i.apply("I7");
			final OWLNamedIndividual i8 = i.apply("I8");

			final OWLNamedIndividual ia = i.apply("IA");
			final OWLNamedIndividual ib = i.apply("IB");

			owl.addAxiom(OWL.propertyAssertion(i1, p1, i2));
			owl.addAxiom(OWL.propertyAssertion(i2, p1, i3));
			owl.addAxiom(OWL.propertyAssertion(i3, p1, i4));
			owl.addAxiom(OWL.propertyAssertion(i4, p1, i5));
			owl.addAxiom(OWL.propertyAssertion(i5, p1, i6));
			owl.addAxiom(OWL.propertyAssertion(i6, p1, i7));
			owl.addAxiom(OWL.propertyAssertion(i7, p1, i8));
			owl.addAxiom(OWL.propertyAssertion(i8, p1, ia));
			owl.addAxiom(OWL.propertyAssertion(ia, p1, ib));

			assertTrue("direct", owl.getObjects(i5, p1).map(x -> x.toString()).sorted().collect(Collectors.joining("")).equals("" + i6));
			assertTrue("transitive", owl.getObjects(i5, p2).map(x -> x.toString()).sorted().collect(Collectors.joining("")).equals("" + i6 + i7 + i8 + ia + ib));
			assertTrue("inverse", owl.getObjects(i5, p3).map(x -> x.toString()).sorted().collect(Collectors.joining("")).equals("" + i4));
			assertTrue("inverse transitive", owl.getObjects(i5, p4).map(x -> x.toString()).sorted((a, b) -> -a.compareTo(b)).collect(Collectors.joining("")).equals("" + i4 + i3 + i2 + i1));
		}
	}

	@Test
	public void testSwrlBuildInByVariable() throws OWLOntologyCreationException
	{
		try (final OWLManagerGroup group = new OWLManagerGroup())
		{
			final OWLOntologyID ontId = OWLHelper.getVersion(IRI.create(NS + "swrl-build-in"), 1.00);
			final OWLHelper owl = new OWLGenericTools(group, ontId, true);

			final OWLDataProperty dpA = OWL.DataProperty(NS + "dpA");
			final OWLDataProperty dpB = OWL.DataProperty(NS + "dpB");
			final OWLNamedIndividual a = OWL.Individual(NS + "A");
			final OWLNamedIndividual b = OWL.Individual(NS + "B");
			final SWRLIndividualArgument swrlIndA = SWRL.individual(a);
			final SWRLIndividualArgument swrlIndB = SWRL.individual(b);
			final OWLLiteral ten = OWL.constant(10.);
			final OWLLiteral eleven = OWL.constant(11.);
			final SWRLVariable varX = SWRL.variable(NS + "x");
			final SWRLVariable varY = SWRL.variable(NS + "y");
			final SWRLLiteralArgument sup = SWRL.constant("sup");
			final SWRLLiteralArgument inf = SWRL.constant("inf");

			owl.addAxiom(OWL.propertyAssertion(a, dpA, ten));
			owl.addAxiom(OWL.propertyAssertion(b, dpA, eleven));
			owl.addAxiom(SWRL.rule(//
					SWRL.antecedent(//
							SWRL.propertyAtom(dpA, swrlIndA, varX), //
							SWRL.propertyAtom(dpA, swrlIndB, varY), //
							SWRL.greaterThan(varX, varY)), //
					SWRL.consequent(SWRL.propertyAtom(dpB, swrlIndA, sup))));

			owl.addAxiom(SWRL.rule(//
					SWRL.antecedent(//
							SWRL.propertyAtom(dpA, swrlIndA, varX), //
							SWRL.propertyAtom(dpA, swrlIndB, varY), //
							SWRL.lessThan(varX, varY)), //
					SWRL.consequent(SWRL.propertyAtom(dpB, swrlIndA, inf))));

			final OWLReasoner reasoner = owl.getReasoner();
			assertFalse(reasoner.isEntailed(OWL.propertyAssertion(a, dpB, OWL.constant("sup"))));
			assertTrue(reasoner.isEntailed(OWL.propertyAssertion(a, dpB, OWL.constant("inf"))));
		}
	}

	@Test
	public void testSwrlBuildInByConstants() throws OWLOntologyCreationException
	{
		try (final OWLManagerGroup group = new OWLManagerGroup())
		{
			final OWLOntologyID ontId = OWLHelper.getVersion(IRI.create(NS + "swrl-build-in"), 1.01);
			final OWLHelper owl = new OWLGenericTools(group, ontId, true);

			final OWLDataProperty dpA = OWL.DataProperty(NS + "dpA");
			final OWLDataProperty dpB = OWL.DataProperty(NS + "dpB");
			final OWLNamedIndividual a = OWL.Individual(NS + "A");
			final OWLNamedIndividual b = OWL.Individual(NS + "B");
			final SWRLIndividualArgument swrlIndA = SWRL.individual(a);
			final OWLLiteral ten = OWL.constant(10.);
			final OWLLiteral eleven = OWL.constant(11.);
			final SWRLVariable varX = SWRL.variable(NS + "x");
			final SWRLLiteralArgument sup = SWRL.constant("sup");
			final SWRLLiteralArgument inf = SWRL.constant("inf");

			owl.addAxiom(OWL.propertyAssertion(a, dpA, ten));
			owl.addAxiom(OWL.propertyAssertion(b, dpA, eleven));
			owl.addAxiom(SWRL.rule(//
					SWRL.antecedent(//
							SWRL.propertyAtom(dpA, swrlIndA, varX), //
							SWRL.greaterThan(varX, SWRL.constant(11.))), //
					SWRL.consequent(SWRL.propertyAtom(dpB, swrlIndA, sup))));

			owl.addAxiom(SWRL.rule(//
					SWRL.antecedent(//
							SWRL.propertyAtom(dpA, swrlIndA, varX), //
							SWRL.lessThan(varX, SWRL.constant(11.))), //
					SWRL.consequent(SWRL.propertyAtom(dpB, swrlIndA, inf))));

			final OWLReasoner reasoner = owl.getReasoner();
			assertFalse(reasoner.isEntailed(OWL.propertyAssertion(a, dpB, OWL.constant("sup"))));
			assertTrue(reasoner.isEntailed(OWL.propertyAssertion(a, dpB, OWL.constant("inf"))));
		}
	}

	class RandomBuildIn implements NumericFunction
	{
		public volatile int _callCountBigDecimal = 0;
		public volatile int _callCountBigInteger = 0;
		public volatile int _callCountDouble = 0;
		public volatile int _callCountFloat = 0;
		public volatile Object _object = null;
		private final Random _rand = new Random();

		@Override
		public BigDecimal apply(final BigDecimal... args)
		{
			_callCountBigDecimal++;
			return (BigDecimal) (_object = new BigDecimal(_rand.nextFloat()));
		}

		@Override
		public BigInteger apply(final BigInteger... args)
		{
			_callCountBigInteger++;
			return (BigInteger) (_object = new BigInteger(Long.toString(_rand.nextInt()))); // I am using only a 'int' not an BigInt.
		}

		@Override
		public Double apply(final Double... args)
		{
			_callCountDouble++;
			return (Double) (_object = _rand.nextDouble());
		}

		@Override
		public Float apply(final Float... args)
		{
			_callCountFloat++;
			return (Float) (_object = _rand.nextFloat());
		}
	}

	@Test
	public void testSpecialBuitIn() throws OWLOntologyCreationException
	{
		final RandomBuildIn myRandomFunction = new RandomBuildIn();
		BuiltInRegistry.instance.registerBuiltIn("MyRandomFunction", new FunctionBuiltIn(new NumericAdapter(myRandomFunction)));

		try (final OWLManagerGroup group = new OWLManagerGroup())
		{
			final OWLOntologyID ontId = OWLHelper.getVersion(IRI.create(NS + "swrl-special-build-in"), 1.02);
			final OWLHelper owl = new OWLGenericTools(group, ontId, true);

			final OWLDataProperty property = OWL.DataProperty(NS + "property");
			final OWLNamedIndividual individual = OWL.Individual(NS + "individual");
			final OWLClass clazz = OWL.Class(NS + "clazz");

			final SWRLVariable varX = SWRL.variable(NS + "x");
			final SWRLVariable varY = SWRL.variable(NS + "y");

			owl.addAxiom(OWL.classAssertion(individual, clazz));
			// owl.addAxiom(OWL.range(property, XSD.DOUBLE)); // TODO : uncomment this to show a bug.

			owl.addAxiom(SWRL.rule(//
					SWRL.antecedent(//
							SWRL.classAtom(clazz, varX), //
							OWL._factory.getSWRLBuiltInAtom(IRI.create("MyRandomFunction"), Arrays.asList(varY))), //
					SWRL.consequent(//
							SWRL.propertyAtom(property, varX, varY))//
			));

			final Set<OWLLiteral> results = owl.getReasoner().getDataPropertyValues(individual, property);
			assertTrue(results.size() == 1);
			final OWLLiteral literal = results.iterator().next();
			assertTrue(literal.isInteger());
			assertTrue(0 == myRandomFunction._callCountBigDecimal);
			assertTrue(1 == myRandomFunction._callCountBigInteger); // Nothing in the code or spec specify to use this datatype.
			assertTrue(0 == myRandomFunction._callCountDouble);
			assertTrue(0 == myRandomFunction._callCountFloat);

			assertTrue(myRandomFunction._object.toString().equals(Integer.toString(literal.parseInteger())));
		}
	}
}
