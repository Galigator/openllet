package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunBuiltin extends AFunOwl
{

	public FunBuiltin(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 1;
	}
}
