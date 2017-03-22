// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.utils;

import java.util.Properties;

/**
 * <p>
 * Description: Convenience class to build Properties objects.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin
 */
public class PropertiesBuilder
{
	private Properties _properties = new Properties();

	public PropertiesBuilder()
	{
		// nothing to do
	}

	public PropertiesBuilder(final Properties defaults)
	{
		_properties = new Properties(defaults);
	}

	public PropertiesBuilder set(final String key, final String value)
	{
		_properties.setProperty(key, value);
		return this;
	}

	public Properties build()
	{
		return _properties;
	}

	public static Properties singleton(final String key, final String value)
	{
		return new PropertiesBuilder().set(key, value).build();
	}
}
