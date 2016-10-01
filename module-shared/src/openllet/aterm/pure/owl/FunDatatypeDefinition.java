package openllet.aterm.pure.owl;

import openllet.aterm.pure.PureFactory;

public class FunDatatypeDefinition extends AFunOwl
{
	public FunDatatypeDefinition(final PureFactory factory)
	{
		super(factory);
	}

	@Override
	public int getArity()
	{
		return 2;
	}
}
