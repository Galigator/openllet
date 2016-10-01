package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunRule extends AFunOwl
{
	public FunRule(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 3;
	}
}
