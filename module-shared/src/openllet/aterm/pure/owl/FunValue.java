package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunValue extends AFunOwl
{
	public FunValue(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 1;
	}
}
