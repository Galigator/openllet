package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunDisjointProperties extends AFunOwl
{
	public FunDisjointProperties(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 1;
	}
}
