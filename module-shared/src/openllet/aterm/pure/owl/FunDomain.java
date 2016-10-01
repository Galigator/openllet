package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunDomain extends AFunOwl
{
	public FunDomain(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 2;
	}
}
