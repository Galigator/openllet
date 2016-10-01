package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunRange extends AFunOwl
{
	public FunRange(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 2;
	}
}
