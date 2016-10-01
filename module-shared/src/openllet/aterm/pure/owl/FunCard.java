package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunCard extends AFunOwl
{
	public FunCard(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 3;
	}
}
