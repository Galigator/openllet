package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunAsymmetric extends AFunOwl
{
	public FunAsymmetric(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 1;
	}
}
