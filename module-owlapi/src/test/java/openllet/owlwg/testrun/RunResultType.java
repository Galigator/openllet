package openllet.owlwg.testrun;

import static openllet.owlwg.testrun.ResultVocabulary.Class.FAILING_RUN;
import static openllet.owlwg.testrun.ResultVocabulary.Class.INCOMPLETE_RUN;
import static openllet.owlwg.testrun.ResultVocabulary.Class.PASSING_RUN;

import org.semanticweb.owlapi.model.OWLClass;

/**
 * <p>
 * Title: Run Result Type
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
public enum RunResultType
{

	FAILING(FAILING_RUN), INCOMPLETE(INCOMPLETE_RUN), PASSING(PASSING_RUN);

	private final OWLClass _c;

	private RunResultType(final ResultVocabulary.Class c)
	{
		_c = c.getOWLClass();
	}

	public OWLClass getOWLClass()
	{
		return _c;
	}
}
