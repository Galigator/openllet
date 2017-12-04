package openllet.core.knowledge;

import java.util.Set;
import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.aterm.ATermList;
import openllet.core.boxes.tbox.TBox;
import openllet.core.output.ATermBaseVisitor;
import openllet.core.utils.ATermUtils;

public abstract class FullyDefinedClassVisitor extends ATermBaseVisitor
{
	public abstract boolean isProperty(ATerm p);

	public abstract boolean isDatatype(ATermAppl p);

	public abstract TBox getTBox();

	public abstract Set<ATermAppl> getIndividuals();

	private boolean _fullyDefined = true;

	public boolean isFullyDefined(final ATermAppl term)
	{
		_fullyDefined = true;
		visit(term);
		return _fullyDefined;
	}

	private void visitQCR(final ATermAppl term)
	{
		visitRestr(term);
		if (_fullyDefined)
		{
			final ATermAppl q = (ATermAppl) term.getArgument(2);
			if (!isDatatype(q))
				visit(q);
		}
	}

	private void visitQR(final ATermAppl term)
	{
		visitRestr(term);
		if (_fullyDefined)
		{
			final ATermAppl q = (ATermAppl) term.getArgument(1);
			if (!isDatatype(q))
				visit(q);
		}
	}

	private void visitRestr(final ATermAppl term)
	{
		_fullyDefined = _fullyDefined && isProperty(term.getArgument(0));
	}

	@Override
	public void visit(final ATermAppl term)
	{
		if (term.equals(ATermUtils.TOP) || term.equals(ATermUtils.BOTTOM) || term.equals(ATermUtils.TOP_LIT) || term.equals(ATermUtils.BOTTOM_LIT))
			return;

		super.visit(term);
	}

	@Override
	public void visitAll(final ATermAppl term)
	{
		visitQR(term);
	}

	@Override
	public void visitAnd(final ATermAppl term)
	{
		if (_fullyDefined)
			visitList((ATermList) term.getArgument(0));
	}

	@Override
	public void visitCard(final ATermAppl term)
	{
		visitQCR(term);
	}

	@Override
	public void visitHasValue(final ATermAppl term)
	{
		visitQR(term);
	}

	@Override
	public void visitLiteral(final ATermAppl term)
	{
		return;
	}

	@Override
	public void visitMax(final ATermAppl term)
	{
		visitQCR(term);
	}

	@Override
	public void visitMin(final ATermAppl term)
	{
		visitQCR(term);
	}

	@Override
	public void visitNot(final ATermAppl term)
	{
		visit((ATermAppl) term.getArgument(0));
	}

	@Override
	public void visitOneOf(final ATermAppl term)
	{
		if (_fullyDefined)
			visitList((ATermList) term.getArgument(0));
	}

	@Override
	public void visitOr(final ATermAppl term)
	{
		if (_fullyDefined)
			visitList((ATermList) term.getArgument(0));
	}

	@Override
	public void visitSelf(final ATermAppl term)
	{
		visitRestr(term);
	}

	@Override
	public void visitSome(final ATermAppl term)
	{
		visitQR(term);
	}

	@Override
	public void visitTerm(final ATermAppl term)
	{
		_fullyDefined = _fullyDefined && getTBox().getClasses().contains(term);
		if (!_fullyDefined)
			return;
	}

	@Override
	public void visitValue(final ATermAppl term)
	{
		final ATermAppl nominal = (ATermAppl) term.getArgument(0);
		if (ATermUtils.isLiteral(nominal))
			_fullyDefined = false;
		else
			if (!ATermUtils.isLiteral(nominal))
				_fullyDefined = _fullyDefined && getIndividuals().contains(nominal);
	}

	@Override
	public void visitInverse(final ATermAppl term)
	{
		final ATermAppl p = (ATermAppl) term.getArgument(0);
		if (ATermUtils.isPrimitive(p))
			_fullyDefined = _fullyDefined && isProperty(p);
		else
			visitInverse(p);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitRestrictedDatatype(final ATermAppl dt)
	{
		_fullyDefined = _fullyDefined && isDatatype((ATermAppl) dt.getArgument(0));
	}

}
