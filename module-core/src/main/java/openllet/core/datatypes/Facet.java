package openllet.core.datatypes;

import java.util.HashMap;
import java.util.Map;

import openllet.aterm.ATermAppl;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.Namespaces;

/**
 * <p>
 * Title: Facet
 * </p>
 * <p>
 * Description: Interface to centralize enumeration and query of supported constraining facets
 * </p>
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Mike Smith
 */
public interface Facet
{
	ATermAppl getName();

	public static class Registry
	{

		private static final Map<ATermAppl, Facet> map = new HashMap<>();
		static
		{
			for (final Facet f : XSD.values())
				map.put(f.getName(), f);
		}

		/**
		 * Get a Facet for a URI
		 *
		 * @param name the name of the facet, generally a URI
		 * @return A facet if the name is registered, <code>null</code> else
		 */
		public static Facet get(final ATermAppl name)
		{
			return map.get(name);
		}

	}

	/**
	 * Facets in the XSD name space (and documented in the XML Schema specifications)
	 */
	public enum XSD implements Facet
	{
		MAX_EXCLUSIVE("maxExclusive"), //
		MAX_INCLUSIVE("maxInclusive"), //
		MIN_EXCLUSIVE("minExclusive"), //
		MIN_INCLUSIVE("minInclusive"), //
		LENGTH("length"), //
		MIN_LENGTH("minLength"), //
		MAX_LENGTH("maxLength"), //
		PATTERN("pattern");

		private final ATermAppl _name;

		private XSD(final String localName)
		{
			_name = ATermUtils.makeTermAppl(Namespaces.XSD + localName);
		}

		@Override
		public ATermAppl getName()
		{
			return _name;
		}
	}
}
