package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunInv extends AFunOwl
{
	public FunInv(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 1;
	}
}
