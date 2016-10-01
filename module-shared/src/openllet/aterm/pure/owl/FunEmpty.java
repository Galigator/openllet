package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunEmpty extends AFunOwl
{
	public FunEmpty(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 0;
	}

	@Override
	public boolean isQuoted()
	{
		return false;
	}
}
