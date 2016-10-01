package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunAllDifferent extends AFunOwl
{
	public FunAllDifferent(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 1;
	}
}
