package openllet.owlwg.testcase;

import java.util.Set;
import org.semanticweb.owlapi.model.IRI;

/**
 * <p>
 * Title: Test Case
 * </p>
 * <p>
 * Description: Interface based on test cases described at <a href="http://www.w3.org/TR/owl2-test/">http://www.w3.org/TR/owl2-test/</a>. Parameterized based on
 * the object returned when parsing an ontology.
 * </p>
 * <p>
 * Copyright: Copyright &copy; 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <a href="http://clarkparsia.com/"/>http://clarkparsia.com/</a>
 * </p>
 *
 * @author     Mike Smith &lt;msmith@clarkparsia.com&gt;
 * @param  <O>
 */
public interface TestCase<O>
{

	void accept(final TestCaseVisitor<O> visitor);

	void dispose();

	Set<Semantics> getApplicableSemantics();

	String getIdentifier();

	Set<IRI> getImportedOntologies();

	String getImportedOntology(final IRI iri, final SerializationFormat format);

	Set<SerializationFormat> getImportedOntologyFormats(final IRI iri);

	Set<Semantics> getNotApplicableSemantics();

	Set<SyntaxConstraint> getSatisfiedConstraints();

	Status getStatus();

	Set<SyntaxConstraint> getUnsatisfiedConstraints();

	IRI getIRI();
}
