// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Description: Essentially a set of OpenlletCmdOption
 * </p>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Markus Stocker
 */
public class OpenlletCmdOptions
{

	private final Map<String, OpenlletCmdOption> options;
	private final Map<String, OpenlletCmdOption> shortOptions;
	private final Set<OpenlletCmdOption> mandatory;

	public OpenlletCmdOptions()
	{
		options = new LinkedHashMap<>();
		shortOptions = new HashMap<>();
		mandatory = new HashSet<>();
	}

	public void add(final OpenlletCmdOption option)
	{
		final String shortOption = option.getShortOption();
		final String longOption = option.getLongOption();

		if (options.containsKey(longOption))
			throw new OpenlletCmdException("Duplicate long option for command: " + longOption);
		else
			if (shortOption != null && shortOptions.containsKey(shortOption))
				throw new OpenlletCmdException("Duplicate short option for command: " + shortOption);

		shortOptions.put(shortOption, option);
		options.put(longOption, option);

		if (option.isMandatory())
			mandatory.add(option);
	}

	public OpenlletCmdOption getOption(final String key)
	{
		// If key is short option then this matches
		OpenlletCmdOption option = shortOptions.get(key);

		// Else, key is long option, retrieve its short option
		if (option == null)
			option = options.get(key);

		return option;
	}

	public Set<OpenlletCmdOption> getMandatoryOptions()
	{
		return mandatory;
	}

	public Collection<OpenlletCmdOption> getOptions()
	{
		return options.values();
	}
}
