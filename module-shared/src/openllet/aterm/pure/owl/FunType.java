package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunType extends AFunOwl
{
	public FunType(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 2;
	}
}
