package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunEquivalentClasses extends AFunOwl
{
	public FunEquivalentClasses(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 2;
	}
}
