package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunSubProperty extends AFunOwl
{
	public FunSubProperty(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 2;
	}
}
