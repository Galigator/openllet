package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunRestrictedDatatype extends AFunOwl
{
	public FunRestrictedDatatype(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 2;
	}
}
