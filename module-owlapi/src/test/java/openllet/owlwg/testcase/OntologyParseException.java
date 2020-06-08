package openllet.owlwg.testcase;

/**
 * <p>
 * Title: Ontology Parse Exception
 * </p>
 * <p>
 * Description:
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
public class OntologyParseException extends Exception
{

	private static final long serialVersionUID = 1L;

	public OntologyParseException()
	{
	}

	public OntologyParseException(final String message)
	{
		super(message);
	}

	public OntologyParseException(final Throwable cause)
	{
		super(cause);
	}

	public OntologyParseException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}
