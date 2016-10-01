package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunComplementOf extends AFunOwl
{
	public FunComplementOf(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 2;
	}
}
