package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunDisjointWith extends AFunOwl
{
	public FunDisjointWith(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 2;
	}
}
