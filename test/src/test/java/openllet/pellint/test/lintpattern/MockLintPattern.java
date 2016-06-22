package openllet.pellint.test.lintpattern;

import openllet.pellint.format.LintFormat;
import openllet.pellint.lintpattern.LintPattern;

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
public class MockLintPattern implements LintPattern
{
	private final boolean _isFixable;

	public MockLintPattern()
	{
		this(false);
	}

	public MockLintPattern(final boolean isFixable)
	{
		_isFixable = isFixable;
	}

	@Override
	public String getName()
	{
		return this.toString();
	}

	@Override
	public String getDescription()
	{
		return this.toString();
	}

	@Override
	public boolean isFixable()
	{
		return _isFixable;
	}

	@Override
	public LintFormat getDefaultLintFormat()
	{
		return null;
	}

}
