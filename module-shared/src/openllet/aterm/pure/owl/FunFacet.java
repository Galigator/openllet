package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunFacet extends AFunOwl
{
	public FunFacet(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 2;
	}
}
