package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunProp extends AFunOwl
{
	public FunProp(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 3;
	}
}
