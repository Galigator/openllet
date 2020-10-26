package openllet.owlwg.testcase;

import java.util.Set;

/**
 * <p>
 * Title: Premised Test Case
 * </p>
 * <p>
 * Description: Shared interface of all premised tests.
 * </p>
 * <p>
 * Copyright: Copyright &copy; 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <a href="http://clarkparsia.com/"/>http://clarkparsia.com/</a>
 * </p>
 *
 * @author Mike Smith &lt;msmith@clarkparsia.com&gt;
 * @param <O>
 */
public interface PremisedTest<O> extends TestCase<O>
{

	Set<SerializationFormat> getPremiseFormats();

	String getPremiseOntology(final SerializationFormat format);

	O parsePremiseOntology(final SerializationFormat format) throws OntologyParseException;
}
