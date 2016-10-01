package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunSameAs extends AFunOwl
{
	public FunSameAs(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 2;
	}
}
