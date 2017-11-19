// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com
//
// ---
// Portions Copyright (c) 2003 Ron Alford, Mike Grove, Bijan Parsia, Evren Sirin
// Alford, Grove, Parsia, Sirin parts of this source code are available under the terms of the MIT License.
//
// The MIT License
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.

package openllet.core.utils;

import java.io.PrintWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import openllet.core.OpenlletOptions;
import openllet.core.output.TableData;

/**
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin
 */
public class Timers
{
	private final Map<String, Timer> _timers = new LinkedHashMap<>();

	public final Timer _mainTimer;

	public Timers()
	{
		_mainTimer = createTimer("main");
		_mainTimer.start();
	}

	private static Function<String, UnsupportedOperationException> _doesNotExist = name -> new UnsupportedOperationException("Timer " + name + " does not exist!");

	public void addAll(final Timers other)
	{
		for (final Entry<String, Timer> entry : other._timers.entrySet())
		{
			final String name = entry.getKey();
			final Timer otherTimer = entry.getValue();

			final Optional<Timer> thisTimer = getTimer(name);

			if (thisTimer.isPresent())
				thisTimer.get().add(otherTimer);
			else
				_timers.put(name, otherTimer);
		}
	}

	public Timer createTimer(final String name)
	{
		final Timer t = new Timer(name, _mainTimer);
		_timers.put(name, t);
		return t;
	}

	public Optional<Timer> startTimer(final String name)
	{
		if (OpenlletOptions.USE_THREADED_KERNEL)
			return Optional.empty();

		final Timer t = getTimer(name).orElseGet(() -> createTimer(name));
		t.start();
		return Optional.of(t);
	}

	public void execute(final String name, final Consumer<Timers> consumer)
	{
		if (OpenlletOptions.USE_THREADED_KERNEL)
		{
			consumer.accept(this);
			return;
		}

		final Optional<Timer> timer = startTimer(name);
		consumer.accept(this);
		timer.ifPresent(t -> t.stop());
	}

	public <RESULT> RESULT execute(final String name, final Supplier<RESULT> producer)
	{
		if (OpenlletOptions.USE_THREADED_KERNEL)
			return producer.get();

		final Optional<Timer> timer = startTimer(name);
		try
		{
			return producer.get();
		}
		finally
		{
			timer.ifPresent(Timer::stop);
		}
	}

	public void checkTimer(final String name)
	{
		getTimer(name).orElseThrow(() -> _doesNotExist.apply(name))//
				.check();
	}

	public void resetTimer(final String name)
	{
		getTimer(name).orElseThrow(() -> _doesNotExist.apply(name))//
				.reset();
	}

	public void interrupt()
	{
		_mainTimer.interrupt();
	}

	public void setTimeout(final String name, final long timeout)
	{
		getTimer(name).orElseGet(() -> createTimer(name))//
				.setTimeout(timeout);
	}

	public void stopTimer(final String name)
	{
		getTimer(name).orElseThrow(() -> _doesNotExist.apply(name))//
				.stop();
	}

	public void resetAll()
	{
		for (final Timer timer : _timers.values())
			timer.reset();
		_mainTimer.start();
	}

	public long getTimerTotal(final String name)
	{
		return getTimer(name).map(t -> t.getTotal()).orElse(0L);
	}

	public double getTimerAverage(final String name)
	{
		return getTimer(name).map(t -> t.getAverage()).orElse(0.);
	}

	public Optional<Timer> getTimer(final String name)
	{
		return Optional.ofNullable(_timers.get(name));
	}

	public Collection<Timer> getTimers()
	{
		return _timers.values();
	}

	public void print()
	{
		print(false);
	}

	public void print(final Writer pw)
	{
		print(pw, false, "Total");
	}

	public void print(final boolean shortForm)
	{
		print(shortForm, "Total");
	}

	public void print(final boolean shortForm, final String sortBy)
	{
		print(new PrintWriter(System.out), shortForm, sortBy);
	}

	public void print(final Writer pw, final boolean shortForm, final String sortBy)
	{

		final String[] colNames = shortForm ? new String[] { "Name", "Total (ms)" } : new String[] { "Name", "Count", "Avg", "Total (ms)" };

		final boolean[] alignment = shortForm ? new boolean[] { false, true } : new boolean[] { false, true, true, true };

		final List<Timer> list = new ArrayList<>(_timers.values());
		if (sortBy != null)
			Collections.sort(list, (o1, o2) ->
			{
				if (sortBy.equalsIgnoreCase("Total"))
				{
					long t1 = o1.getTotal();
					long t2 = o2.getTotal();
					if (t1 == 0)
						t1 = o1.getElapsed();
					if (t2 == 0)
						t2 = o2.getElapsed();
					return (int) (t2 - t1);
				}
				else
					if (sortBy.equalsIgnoreCase("Avg"))
						return (int) (o2.getAverage() - o1.getAverage());
					else
						if (sortBy.equalsIgnoreCase("Count"))
							return (int) (o2.getCount() - o1.getCount());
						else
							return AlphaNumericComparator.CASE_INSENSITIVE.compare(o1, o2);
			});

		final NumberFormat nf = new DecimalFormat("0.00");

		final TableData table = new TableData(Arrays.asList(colNames));
		table.setAlignment(alignment);
		for (final Timer timer : list)
		{
			//			if(timer.getCount() == 0)
			//			    continue;
			final List<Object> row = new ArrayList<>();
			row.add(timer.getName());
			if (!shortForm)
			{
				row.add(String.valueOf(timer.getCount()));
				row.add(nf.format(timer.getAverage()));
			}
			if (timer.isStarted())
				row.add(String.valueOf(timer.getElapsed()));
			else
				row.add(String.valueOf(timer.getTotal()));
			table.add(row);
		}

		table.print(pw);
	}

	@Override
	public String toString()
	{
		return _timers.values().toString();
	}
}
