package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunLiteral extends AFunOwl
{
	public FunLiteral(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 3;
	}

	@Override
	public boolean isQuoted()
	{
		return false;
	}
}
