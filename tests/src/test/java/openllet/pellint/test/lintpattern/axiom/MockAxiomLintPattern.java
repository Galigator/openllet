package openllet.pellint.test.lintpattern.axiom;

import openllet.pellint.format.LintFormat;
import openllet.pellint.lintpattern.axiom.AxiomLintPattern;

/**
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Harris Lin
 */
public class MockAxiomLintPattern extends AxiomLintPattern
{
	private int _intParam;
	private String _stringParam;

	@Override
	public String getName()
	{
		return toString();
	}

	@Override
	public String getDescription()
	{
		return toString();
	}

	@Override
	public boolean isFixable()
	{
		return false;
	}

	@Override
	public LintFormat getDefaultLintFormat()
	{
		return null;
	}

	public void setIntParam(final int v)
	{
		_intParam = v;
	}

	public int getIntParam()
	{
		return _intParam;
	}

	public void setStringParam(final String v)
	{
		_stringParam = v;
	}

	public String getStringParam()
	{
		return _stringParam;
	}

}
