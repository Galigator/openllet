package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunDisjointPropertyWith extends AFunOwl
{
	public FunDisjointPropertyWith(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 2;
	}
}
