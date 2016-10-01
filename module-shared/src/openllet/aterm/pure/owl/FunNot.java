package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunNot extends AFunOwl
{
	public FunNot(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 1;
	}
}
