package openllet.owlapi;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import openllet.owlapi.facet.FacetFactoryOWL;
import openllet.owlapi.facet.FacetManagerOWL;
import openllet.owlapi.facet.FacetOntologyOWL;
import openllet.owlapi.facet.FacetReasonerOWL;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.parameters.ChangeApplied;

public interface OWLManagementObject extends FacetFactoryOWL, FacetManagerOWL, FacetOntologyOWL, FacetReasonerOWL
{
	// Basics

	/**
	 * @param  axiom to change
	 * @return       true changes list
	 * @since        2.5.1
	 */
	default ChangeApplied addAxiom(final OWLAxiom axiom)
	{
		return getOntology().add(axiom);
	}

	/**
	 * @param  axioms to change
	 * @return        true changes list
	 * @since         2.5.1
	 */
	default ChangeApplied addAxioms(final Stream<OWLAxiom> axioms)
	{
		return getOntology().addAxioms(axioms);
	}

	/**
	 * @param  axiom to change
	 * @return       true changes list
	 * @since        2.5.1
	 */
	default ChangeApplied removeAxiom(final OWLAxiom axiom)
	{
		return getOntology().remove(axiom);
	}

	/**
	 * @param  axioms to change
	 * @return        true changes list
	 * @since         2.5.1
	 */
	default ChangeApplied removeAxioms(final Stream<OWLAxiom> axioms)
	{
		return getOntology().removeAxioms(axioms);
	}

	/**
	 * add axiom into the ontology.
	 *
	 * @param property that carry the data
	 * @param subject  is the root of the property
	 * @param value    is the target of the property
	 * @since          2.5.1
	 */
	default void addObjectPropertyAxiom(final OWLObjectPropertyExpression property, final OWLIndividual subject, final OWLIndividual value)
	{
		addAxiom(getFactory().getOWLObjectPropertyAssertionAxiom(property, subject, value));
	}

	/**
	 * add axiom into the ontology.
	 *
	 * @param property that carry the data
	 * @param subject  is the root of the property
	 * @param literal  is the target of the property
	 * @since          2.5.1
	 */
	default void addDataPropertyAxiom(final OWLDataProperty property, final OWLIndividual subject, final OWLLiteral literal)
	{
		addAxiom(getFactory().getOWLDataPropertyAssertionAxiom(property, subject, literal));
	}

	/**
	 * add axiom into the ontology.
	 *
	 * @param property that carry the data
	 * @param subject  is the root of the property
	 * @param value    is the target of the property
	 * @since          2.5.1
	 */
	default void addDataPropertyAxiom(final OWLDataProperty property, final OWLIndividual subject, final String value)
	{
		addDataPropertyAxiom(property, subject, getFactory().getOWLLiteral(value));
	}

	/**
	 * add axiom into the ontology.
	 *
	 * @param property that carry the data
	 * @param subject  is the root of the property
	 * @param value    is the target of the property
	 * @since          2.5.1
	 */
	default void addDataPropertyAxiom(final OWLDataProperty property, final OWLIndividual subject, final double value)
	{
		addDataPropertyAxiom(property, subject, getFactory().getOWLLiteral(value));
	}

	/**
	 * add axiom into the ontology.
	 *
	 * @param property that carry the data
	 * @param subject  is the root of the property
	 * @param value    is the target of the property
	 * @since          2.5.1
	 */
	default void addDataPropertyAxiom(final OWLDataProperty property, final OWLIndividual subject, final long value)
	{
		addDataPropertyAxiom(property, subject, getFactory().getOWLLiteral((double) value)); // This is also the default java behavior but here I want to make it explicit.
	}

	/**
	 * add axiom into the ontology.
	 *
	 * @param property that carry the data
	 * @param subject  is the root of the property
	 * @param value    is the target of the property
	 * @since          2.5.1
	 */
	default void addDataPropertyAxiom(final OWLDataProperty property, final OWLIndividual subject, final int value)
	{
		addDataPropertyAxiom(property, subject, getFactory().getOWLLiteral(value));
	}

	/**
	 * add axiom into the ontology.
	 *
	 * @param clazz   that will have the individual
	 * @param subject is the individual that will receive the assertion.
	 * @since         2.5.1
	 */
	default void addClassPropertyAxiom(final OWLClassExpression clazz, final OWLIndividual subject)
	{
		addAxiom(getFactory().getOWLClassAssertionAxiom(clazz, subject));
	}

	/**
	 * remove axiom from the ontology
	 *
	 * @param property that carry the data
	 * @param subject  is the root of the property
	 * @param value    is the target of the property
	 * @since          2.5.1
	 */
	default void removeObjectPropertyAxiom(final OWLObjectPropertyExpression property, final OWLIndividual subject, final OWLIndividual value)
	{
		removeAxiom(getFactory().getOWLObjectPropertyAssertionAxiom(property, subject, value));
	}

	/**
	 * remove axiom from the ontology
	 *
	 * @param property that carry the data
	 * @param subject  is the root of the property
	 * @param literal  is the target of the property
	 * @since          2.5.1
	 */
	default void removeDataPropertyAxiom(final OWLDataProperty property, final OWLIndividual subject, final OWLLiteral literal)
	{
		removeAxiom(getFactory().getOWLDataPropertyAssertionAxiom(property, subject, literal));
	}

	/**
	 * Remove all the value of an data property that are related to an individual. This function is slow and should be optmized.
	 *
	 * @param property to remove
	 * @param subject  to consider.
	 * @since          2.5.1
	 */
	default void removeDataPropertyAxiom(final OWLDataProperty property, final OWLNamedIndividual subject)
	{
		removeAxioms(getValues(subject, property).stream().map(value -> getFactory().getOWLDataPropertyAssertionAxiom(property, subject, value)));
	}

	/**
	 * Remove all the value of an data property that are related to an individual. This function is slow and should be optmized.
	 *
	 * @param property to remove
	 * @param subject  to consider.
	 * @since          2.5.1
	 */
	default void removeDataPropertyAxiom(final IRI property, final OWLNamedIndividual subject)
	{
		removeDataPropertyAxiom(getFactory().getOWLDataProperty(property), subject);
	}

	/**
	 * Remove all the target of an object property that are related to an individual. This function is slow and should be optimized.
	 *
	 * @param property to remove
	 * @param subject  to consider.
	 * @since          2.5.1
	 */
	default void removeObjectPropertyAxiom(final OWLObjectProperty property, final OWLNamedIndividual subject)
	{
		removeAxioms(getObjects(subject, property).map(object -> getFactory().getOWLObjectPropertyAssertionAxiom(property, subject, object)));
	}

	/**
	 * Remove all the target of an object property that are related to an individual. This function is slow and should be optimized.
	 *
	 * @param property to remove
	 * @param subject  to consider.
	 * @since          2.5.1
	 */
	default void removeObjectPropertyAxiom(final IRI property, final OWLNamedIndividual subject)
	{
		removeObjectPropertyAxiom(getFactory().getOWLObjectProperty(property), subject);
	}

	/**
	 * @param  named   is the individual with the given name that will be test against the facette.
	 * @param  facette is an class expression that represent an acceptable type for the individual.
	 * @return         true if the individual have the given facette
	 * @since          2.5.1
	 */
	default boolean individualHaveFacet(final OWLNamedIndividual named, final OWLClassExpression facette)
	{
		return getReasoner().isEntailed(getFactory().getOWLClassAssertionAxiom(facette, named));
	}

	/**
	 * @param  property is the property that support the given range. In fact can be all 'simple' DataProperty you may want.
	 * @param  range    like [1..3] or more complex if you want.
	 * @param  literal  to check, 2 is include [1..3], 4 isn't include in [1..3].
	 * @return          true if the literal is in the range.
	 * @since           2.6.1
	 */
	default boolean isLiteralIncludeInRange(final OWLDataProperty property, final OWLDataRange range, final OWLLiteral literal)
	{
		return getReasoner().isSatisfiable(//
				OWL.and(// You must be of all the following class
						OWL.some(property, OWL.oneOf(literal)), // The class of the 'literal'
						OWL.some(property, range), // The class of the range.
						OWL.max(property, 1))// But you can have the property only once.
		);
	}

	// Declarations

	/**
	 * @param  clazz      is a java class
	 * @param  individual that will be map to an owl class generated from the java class.
	 * @return            the owl class of the individual
	 * @since             2.5.1
	 */
	default OWLClass declareClassOfIndividual(final Class<?> clazz, final OWLNamedIndividual individual)
	{
		final OWLClass owlClazz = toClass(IRIUtils.clazz(clazz));
		addClass(individual, owlClazz);
		return owlClazz;
	}

	/**
	 * @param  iri to declare
	 * @return     the iri as an individual after inserting it as a declaration in the ontology.
	 * @since      2.5.1
	 */
	default OWLNamedIndividual declareIndividual(final IRI iri)
	{
		final OWLNamedIndividual baby = toIndividual(iri);
		addAxiom(getFactory().getOWLDeclarationAxiom(baby));
		return baby;
	}

	/**
	 * Declare the individual and add it a given class
	 *
	 * @param  owlClazz   already declare in this ontology.
	 * @param  individual to declare in this ontology.
	 * @return            the owl individual
	 * @since             2.6.0
	 */
	default OWLNamedIndividual declareIndividual(final OWLClass owlClazz, final OWLNamedIndividual individual)
	{
		addAxiom(getFactory().getOWLDeclarationAxiom(individual));
		addClass(individual, owlClazz);
		return individual;
	}

	/**
	 * Declare the individual and add it a given class
	 *
	 * @param  owlClazz   already declare in this ontology.
	 * @param  individual as full iri
	 * @return            the owl individual
	 * @since             2.5.1
	 */
	default OWLNamedIndividual declareIndividual(final OWLClass owlClazz, final IRI individual)
	{
		final OWLNamedIndividual owlIndividual = declareIndividual(individual);
		addClass(owlIndividual, owlClazz);
		return owlIndividual;
	}

	/**
	 * To avoid problem on declaration of the individual, this method get template of individual : class + namespace + name. And add some random around the
	 * name.
	 *
	 * @param      owlClazz  that must be bind to the individual
	 * @param      namespace of the individual to create.
	 * @param      name      of the individual to create.
	 * @return               A new named individual.
	 * @Deprecated           2.6.1 This method should take a fully qualified label that depend of the context. No more next int usage.
	 */
	@Deprecated
	default OWLNamedIndividual declareIndividual(final OWLClass owlClazz, final String namespace, final String name)
	{
		return declareIndividual(owlClazz, IRI.create(IRIUtils.isIRI(name) ? name : namespace + IRIUtils.randId(name)));
	}

	/**
	 * @return an annonymous individual (this is just a shortcut to the owlapi).
	 * @since  2.5.1
	 */
	default OWLAnonymousIndividual declareIndividual()
	{
		return getFactory().getOWLAnonymousIndividual();
	}

	/**
	 * @param  iri the desired property
	 * @return     the owl property
	 * @since      2.5.1
	 */
	default OWLObjectProperty declareObjectProperty(final IRI iri)
	{
		final OWLObjectProperty baby = toObjectProperty(iri);
		addAxiom(getFactory().getOWLDeclarationAxiom(baby));
		return baby;
	}

	/**
	 * @param  iri the desired property
	 * @return     the owl property
	 * @since      2.5.1
	 */
	default OWLDataProperty declareDataProperty(final IRI iri)
	{
		final OWLDataProperty baby = toDataProperty(iri);
		addAxiom(getFactory().getOWLDeclarationAxiom(baby));
		return baby;
	}

	/**
	 * @param  clazz is the iri of the desired classes
	 * @return       the owl class
	 * @since        2.5.1
	 */
	default OWLClass declareClass(final IRI clazz)
	{
		final OWLClass owlClazz = toClass(clazz);
		addAxiom(getFactory().getOWLDeclarationAxiom(owlClazz));
		return owlClazz;
	}

	// Converters

	/**
	 * @param  iri property
	 * @return     property
	 * @since      2.5.1
	 */
	default OWLObjectProperty toObjectProperty(final IRI iri)
	{
		return getFactory().getOWLObjectProperty(iri);
	}

	/**
	 * @param  iri property
	 * @return     property
	 * @since      2.5.1
	 */
	default OWLDataProperty toDataProperty(final IRI iri)
	{
		return getFactory().getOWLDataProperty(iri);
	}

	/**
	 * @param  iri class
	 * @return     class
	 * @since      2.5.1
	 */
	default OWLClass toClass(final IRI iri)
	{
		return getFactory().getOWLClass(iri);
	}

	/**
	 * @param  iri individual
	 * @return     individual
	 * @since      2.5.1
	 */
	default OWLNamedIndividual toIndividual(final IRI iri)
	{
		return getFactory().getOWLNamedIndividual(iri);
	}

	// Assoc

	/**
	 * @param  subject  subject
	 * @param  property property
	 * @return          all objects
	 * @since           2.5.1
	 */
	default Stream<OWLNamedIndividual> getObjects(final OWLNamedIndividual subject, final OWLObjectPropertyExpression property)
	{
		return getReasoner().getObjectPropertyValues(subject, property).entities();
	}

	/**
	 * @param  subject  subject
	 * @param  property property
	 * @return          all objects
	 * @since           2.5.1
	 */
	default Stream<OWLNamedIndividual> getObjects(final OWLNamedIndividual subject, final IRI property)
	{
		return getObjects(subject, toObjectProperty(property));
	}

	/**
	 * @param  subject  subject
	 * @param  property property
	 * @return          all values
	 * @since           2.5.1
	 */
	default Set<OWLLiteral> getValues(final OWLNamedIndividual subject, final OWLDataProperty property)
	{
		return getReasoner().getDataPropertyValues(subject, property);
	}

	/**
	 * @param  subject  subject
	 * @param  property property
	 * @return          all values
	 * @since           2.5.1
	 */
	default Set<OWLLiteral> getValues(final OWLNamedIndividual subject, final IRI property)
	{
		return getValues(subject, toDataProperty(property));
	}

	/**
	 * @param  subject  subject
	 * @param  property property
	 * @return          one object
	 * @since           2.5.1
	 */
	default Optional<OWLNamedIndividual> getObject(final OWLNamedIndividual subject, final OWLObjectPropertyExpression property)
	{
		return getObjects(subject, property).findAny();
	}

	/**
	 * @param  subject  subject
	 * @param  property property
	 * @return          one object
	 * @since           2.5.1
	 */
	default Optional<OWLNamedIndividual> getObject(final OWLNamedIndividual subject, final IRI property)
	{
		return getObject(subject, toObjectProperty(property));
	}

	/**
	 * @param  subject  subject
	 * @param  property property
	 * @return          one value
	 * @since           2.5.1
	 */
	default Optional<OWLLiteral> getValue(final OWLNamedIndividual subject, final OWLDataProperty property)
	{
		final Set<OWLLiteral> values = getValues(subject, property);
		return values.isEmpty() ? Optional.empty() : Optional.of(values.iterator().next());
	}

	/**
	 * @param  subject  subject
	 * @param  property property
	 * @return          one value
	 * @since           2.5.1
	 */
	default Optional<OWLLiteral> getValue(final OWLNamedIndividual subject, final IRI property)
	{
		return getValue(subject, toDataProperty(property));
	}

	// Addings

	/**
	 * @param subject  root of property
	 * @param property identifier of the property
	 * @param object   target
	 * @since          2.5.1
	 */
	default void addObject(final OWLNamedIndividual subject, final OWLObjectPropertyExpression property, final OWLNamedIndividual object)
	{
		addObjectPropertyAxiom(property, subject, object);
	}

	/**
	 * @param subject  root of property
	 * @param property identifier of the property
	 * @param object   target
	 * @since          2.5.1
	 */
	default void addObject(final OWLNamedIndividual subject, final IRI property, final OWLNamedIndividual object)
	{
		addObject(subject, toObjectProperty(property), object);
	}

	/**
	 * @param subject  root of property
	 * @param property identifier of the property
	 * @param literal  target
	 * @since          2.5.1
	 */
	default void addValue(final OWLNamedIndividual subject, final OWLDataProperty property, final OWLLiteral literal)
	{
		addDataPropertyAxiom(property, subject, literal);
	}

	/**
	 * @param subject  root of property
	 * @param property identifier of the property
	 * @param literal  target
	 * @since          2.5.1
	 */
	default void addValue(final OWLNamedIndividual subject, final IRI property, final OWLLiteral literal)
	{
		addValue(subject, toDataProperty(property), literal);
	}

	/**
	 * @param subject  root of property
	 * @param property identifier of the property
	 * @param literal  target
	 * @since          2.5.1
	 */
	default void addValue(final OWLNamedIndividual subject, final OWLDataProperty property, final String literal)
	{
		addDataPropertyAxiom(property, subject, literal);
	}

	/**
	 * @param subject  root of property
	 * @param property identifier of the property
	 * @param literal  target
	 * @since          2.5.1
	 */
	default void addValue(final OWLNamedIndividual subject, final IRI property, final String literal)
	{
		addValue(subject, toDataProperty(property), literal);
	}

	/**
	 * @param subject  root of property
	 * @param property identifier of the property
	 * @param literal  target
	 * @since          2.5.1
	 */
	default void addValue(final OWLNamedIndividual subject, final OWLDataProperty property, final int literal)
	{
		addDataPropertyAxiom(property, subject, literal);
	}

	/**
	 * @param subject  root of property
	 * @param property identifier of the property
	 * @param literal  target
	 * @since          2.5.1
	 */
	default void addValue(final OWLNamedIndividual subject, final IRI property, final int literal)
	{
		addValue(subject, toDataProperty(property), literal);
	}

	/**
	 * @param subject  root of property
	 * @param property identifier of the property
	 * @param literal  target
	 * @since          2.5.1
	 */
	default void addValue(final OWLNamedIndividual subject, final OWLDataProperty property, final double literal)
	{
		addDataPropertyAxiom(property, subject, literal);
	}

	/**
	 * @param subject  root of property
	 * @param property identifier of the property
	 * @param literal  target
	 * @since          2.5.1
	 */
	default void addValue(final OWLNamedIndividual subject, final IRI property, final double literal)
	{
		addValue(subject, toDataProperty(property), literal);
	}

	// Updating
	/**
	 * @param subject  root of the property
	 * @param property identifier of the property
	 * @param object   target of the property
	 * @since          2.5.1
	 */
	default void updateObject(final OWLNamedIndividual subject, final OWLObjectProperty property, final OWLNamedIndividual object)
	{
		removeObjectPropertyAxiom(property, subject);
		addObjectPropertyAxiom(property, subject, object);
	}

	/**
	 * @param subject  root of the property
	 * @param property identifier of the property
	 * @param object   target of the property
	 * @since          2.5.1
	 */
	default void updateObject(final OWLNamedIndividual subject, final IRI property, final OWLNamedIndividual object)
	{
		updateObject(subject, toObjectProperty(property), object);
	}

	/**
	 * @param subject  root of the property
	 * @param property identifier of the property
	 * @param literal  target of the property
	 * @since          2.5.1
	 */
	default void updateValue(final OWLNamedIndividual subject, final OWLDataProperty property, final OWLLiteral literal)
	{
		removeDataPropertyAxiom(property, subject);
		addDataPropertyAxiom(property, subject, literal);
	}

	/**
	 * @param subject  root of the property
	 * @param property identifier of the property
	 * @param literal  target of the property
	 * @since          2.5.1
	 */
	default void updateValue(final OWLNamedIndividual subject, final IRI property, final OWLLiteral literal)
	{
		updateValue(subject, toDataProperty(property), literal);
	}

	/**
	 * @param subject  root of the property
	 * @param property identifier of the property
	 * @param literal  target of the property
	 * @since          2.5.1
	 */
	default void updateValue(final OWLNamedIndividual subject, final OWLDataProperty property, final String literal)
	{
		removeDataPropertyAxiom(property, subject);
		addDataPropertyAxiom(property, subject, literal);
	}

	/**
	 * @param subject  root of the property
	 * @param property identifier of the property
	 * @param literal  target of the property
	 * @since          2.5.1
	 */
	default void updateValue(final OWLNamedIndividual subject, final IRI property, final String literal)
	{
		updateValue(subject, toDataProperty(property), literal);
	}

	/**
	 * @param subject  root of the property
	 * @param property identifier of the property
	 * @param literal  target of the property
	 * @since          2.5.1
	 */
	default void updateValue(final OWLNamedIndividual subject, final OWLDataProperty property, final int literal)
	{
		removeDataPropertyAxiom(property, subject);
		addDataPropertyAxiom(property, subject, literal);
	}

	/**
	 * @param subject  root of the property
	 * @param property identifier of the property
	 * @param literal  target of the property
	 * @since          2.5.1
	 */
	default void updateValue(final OWLNamedIndividual subject, final IRI property, final int literal)
	{
		updateValue(subject, toDataProperty(property), literal);
	}

	/**
	 * @param subject  root of the property
	 * @param property identifier of the property
	 * @param literal  target of the property
	 * @since          2.5.1
	 */
	default void updateValue(final OWLNamedIndividual subject, final OWLDataProperty property, final double literal)
	{
		removeDataPropertyAxiom(property, subject);
		addDataPropertyAxiom(property, subject, literal);
	}

	/**
	 * @param subject  root of the property
	 * @param property identifier of the property
	 * @param literal  target of the property
	 * @since          2.5.1
	 */
	default void updateValue(final OWLNamedIndividual subject, final IRI property, final double literal)
	{
		updateValue(subject, toDataProperty(property), literal);
	}

	// Classing

	/**
	 * @param subject that will receive the class
	 * @param clazz   that will be add
	 * @since         2.5.1
	 */
	default void addClass(final OWLNamedIndividual subject, final OWLClassExpression clazz)
	{
		addAxiom(getFactory().getOWLClassAssertionAxiom(clazz, subject));
	}

	/**
	 * @param subject that will receive the class
	 * @param clazz   that will be add
	 * @since         2.5.1
	 */
	default void addClass(final OWLNamedIndividual subject, final IRI clazz)
	{
		addClass(subject, toClass(clazz));
	}
}
