package openllet.owlwg.testrun;

/**
 * <p>
 * Title: Test Run Result Visitor
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright &copy; 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <a href="http://clarkparsia.com/"/>http://clarkparsia.com/</a>
 * </p>
 *
 * @author Mike Smith &lt;msmith@clarkparsia.com&gt;
 */
public interface TestRunResultVisitor
{

	void visit(SyntaxConstraintRun result);

	void visit(ReasoningRun result);

	void visit(SyntaxTranslationRun result);

}
