package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunSubClassOf extends AFunOwl
{
	public FunSubClassOf(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 2;
	}
}
