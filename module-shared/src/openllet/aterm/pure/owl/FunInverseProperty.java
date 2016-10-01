package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunInverseProperty extends AFunOwl
{
	public FunInverseProperty(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 1;
	}

	@Override
	public boolean isQuoted()
	{
		return false;
	}
}
