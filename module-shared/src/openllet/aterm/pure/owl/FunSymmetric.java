package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunSymmetric extends AFunOwl
{
	public FunSymmetric(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 1;
	}
}
