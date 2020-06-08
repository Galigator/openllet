package openllet.core.knowledge;

import openllet.aterm.ATermAppl;
import openllet.aterm.ATermList;
import openllet.core.output.ATermBaseVisitor;
import openllet.core.utils.ATermUtils;

public abstract class DatatypeVisitor extends ATermBaseVisitor implements Boxes
{

	private boolean _isDatatype = false;

	public boolean isDatatype(final ATermAppl term)
	{
		_isDatatype = false;
		visit(term);

		return _isDatatype;
	}

	@Override
	public void visit(final ATermAppl term)
	{
		super.visit(term);
	}

	@Override
	public void visitOr(final ATermAppl term)
	{
		visitList((ATermList) term.getArgument(0));
	}

	@Override
	public void visitValue(final ATermAppl term)
	{
		final ATermAppl nominal = (ATermAppl) term.getArgument(0);

		if (ATermUtils.isLiteral(nominal)) _isDatatype = true;
	}

	@Override
	public void visitTerm(final ATermAppl term)
	{
		if (getDatatypeReasoner().isDeclared(term)) _isDatatype = true;
	}

	@Override
	public void visitNot(final ATermAppl term)
	{
		visit((ATermAppl) term.getArgument(0));
	}

	@Override
	public void visitAnd(final ATermAppl term)
	{
		visitList((ATermList) term.getArgument(0));
	}

	@Override
	public void visitOneOf(final ATermAppl term)
	{
		visitList((ATermList) term.getArgument(0));
	}

	@Override
	public void visitRestrictedDatatype(final ATermAppl dt)
	{
		isDatatype((ATermAppl) dt.getArgument(0));
	}
}
