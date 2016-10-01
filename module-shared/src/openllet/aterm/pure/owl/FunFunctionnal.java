package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunFunctionnal extends AFunOwl
{
	public FunFunctionnal(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 1;
	}
}
