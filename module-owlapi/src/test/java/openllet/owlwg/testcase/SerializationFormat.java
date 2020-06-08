package openllet.owlwg.testcase;

import static openllet.owlwg.testcase.TestVocabulary.DatatypeProperty.FUNCTIONAL_CONCLUSION_ONTOLOGY;
import static openllet.owlwg.testcase.TestVocabulary.DatatypeProperty.FUNCTIONAL_INPUT_ONTOLOGY;
import static openllet.owlwg.testcase.TestVocabulary.DatatypeProperty.FUNCTIONAL_NONCONCLUSION_ONTOLOGY;
import static openllet.owlwg.testcase.TestVocabulary.DatatypeProperty.FUNCTIONAL_PREMISE_ONTOLOGY;
import static openllet.owlwg.testcase.TestVocabulary.DatatypeProperty.OWLXML_CONCLUSION_ONTOLOGY;
import static openllet.owlwg.testcase.TestVocabulary.DatatypeProperty.OWLXML_INPUT_ONTOLOGY;
import static openllet.owlwg.testcase.TestVocabulary.DatatypeProperty.OWLXML_NONCONCLUSION_ONTOLOGY;
import static openllet.owlwg.testcase.TestVocabulary.DatatypeProperty.OWLXML_PREMISE_ONTOLOGY;
import static openllet.owlwg.testcase.TestVocabulary.DatatypeProperty.RDFXML_CONCLUSION_ONTOLOGY;
import static openllet.owlwg.testcase.TestVocabulary.DatatypeProperty.RDFXML_INPUT_ONTOLOGY;
import static openllet.owlwg.testcase.TestVocabulary.DatatypeProperty.RDFXML_NONCONCLUSION_ONTOLOGY;
import static openllet.owlwg.testcase.TestVocabulary.DatatypeProperty.RDFXML_PREMISE_ONTOLOGY;

import openllet.owlwg.testcase.TestVocabulary.Individual;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * <p>
 * Title: Serialization Formant
 * </p>
 * <p>
 * Description: See <a
 * href="http://www.w3.org/TR/owl2-test/#Normative_Syntax">OWL 2 Conformance:
 * Normative Syntax</a> and <a
 * href="http://www.w3.org/TR/owl2-test/#Input_Ontologies">Input Ontologies</a>.
 * </p>
 * <p>
 * Copyright: Copyright &copy; 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <a
 * href="http://clarkparsia.com/"/>http://clarkparsia.com/</a>
 * </p>
 *
 * @author Mike Smith &lt;msmith@clarkparsia.com&gt;
 */
public enum SerializationFormat
{

	/**
	 * OWL 2 Functional-Style Syntax
	 */
	FUNCTIONAL(Individual.FUNCTIONAL, FUNCTIONAL_INPUT_ONTOLOGY, FUNCTIONAL_PREMISE_ONTOLOGY, FUNCTIONAL_CONCLUSION_ONTOLOGY, FUNCTIONAL_NONCONCLUSION_ONTOLOGY),
	/**
	 * OWL 2 XML Syntax
	 */
	OWLXML(Individual.OWLXML, OWLXML_INPUT_ONTOLOGY, OWLXML_PREMISE_ONTOLOGY, OWLXML_CONCLUSION_ONTOLOGY, OWLXML_NONCONCLUSION_ONTOLOGY),
	/**
	 * OWL 2 RDF/XML Syntax
	 */
	RDFXML(Individual.RDFXML, RDFXML_INPUT_ONTOLOGY, RDFXML_PREMISE_ONTOLOGY, RDFXML_CONCLUSION_ONTOLOGY, RDFXML_NONCONCLUSION_ONTOLOGY);

	private final TestVocabulary.DatatypeProperty	_conclusion;
	private final TestVocabulary.DatatypeProperty	_input;
	private final TestVocabulary.Individual			_i;
	private final TestVocabulary.DatatypeProperty	_nonconclusion;
	private final TestVocabulary.DatatypeProperty	_premise;

	private SerializationFormat(final TestVocabulary.Individual i, final TestVocabulary.DatatypeProperty input, final TestVocabulary.DatatypeProperty premise,
			final TestVocabulary.DatatypeProperty conclusion, final TestVocabulary.DatatypeProperty nonconclusion)
	{
		_i = i;
		_input = input;
		_premise = premise;
		_conclusion = conclusion;
		_nonconclusion = nonconclusion;
	}

	public OWLDataProperty getConclusionOWLDataProperty()
	{
		return _conclusion.getOWLDataProperty();
	}

	public OWLDataProperty getNonConclusionOWLDataProperty()
	{
		return _nonconclusion.getOWLDataProperty();
	}

	public OWLIndividual getOWLIndividual()
	{
		return _i.getOWLIndividual();
	}

	public OWLDataProperty getPremiseOWLDataProperty()
	{
		return _premise.getOWLDataProperty();
	}

	public OWLDataProperty getInputOWLDataProperty()
	{
		return _input.getOWLDataProperty();
	}
}
