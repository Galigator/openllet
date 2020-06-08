package openllet.owlwg.testcase;

/**
 * <p>
 * Title: Test Case Visitor
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
 * @author     Mike Smith &lt;msmith@clarkparsia.com&gt;
 * @param  <O>
 */

public interface TestCaseVisitor<O>
{

	void visit(ConsistencyTest<O> testcase);

	void visit(InconsistencyTest<O> testcase);

	void visit(PositiveEntailmentTest<O> testcase);

	void visit(NegativeEntailmentTest<O> testcase);
}
