// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.utils.fsm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import openllet.atom.OpenError;
import openllet.core.exceptions.InternalReasonerException;
import openllet.core.utils.Pair;

/**
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author     Evren Sirin
 * @param  <T> kind of states
 */
public class TransitionGraph<T>
{
	private volatile State<T>		_initialState;	// the initial state for the TG

	private final Set<State<T>>		_allStates;		// set of all states in the TG

	private volatile Set<State<T>>	_finalStates;	// set of final states for the TG

	private final Set<T>			_alphabet;		// set of all characters in TG

	public TransitionGraph()
	{
		_initialState = null;
		_allStates = new HashSet<>();
		_finalStates = new HashSet<>();
		_alphabet = new HashSet<>();
	}

	/**
	 * @return the number of states in this transition graph
	 */
	public int size()
	{
		return _allStates.size();
	}

	// ---------------------------------------------------
	// adds a new state to the graph

	public State<T> newState()
	{
		final State<T> s = new State<>();
		_allStates.add(s);
		return s;
	}

	public Set<T> getAlpahabet()
	{
		return Collections.unmodifiableSet(_alphabet);
	}

	public Set<State<T>> getAllStates()
	{
		return Collections.unmodifiableSet(_allStates);
	}

	public void setInitialState(final State<T> s)
	{
		_initialState = s;
	}

	public State<T> getInitialState()
	{
		return _initialState;
	}

	public void addFinalState(final State<T> s)
	{
		_finalStates.add(s);
	}

	public Set<State<T>> getFinalStates()
	{
		return _finalStates;
	}

	public State<T> getFinalState()
	{
		final int size = _finalStates.size();

		if (size == 0)
			throw new OpenError("There are no final states!");
		else if (size > 1) throw new OpenError("There is more than one final state!");

		return _finalStates.iterator().next();
	}

	public void addTransition(final State<T> begin, final T transition, final State<T> end)
	{
		if (transition == null) throw new NullPointerException();

		begin.addTransition(transition, end);
		_alphabet.add(transition);
	}

	public void addTransition(final State<T> begin, final State<T> end)
	{
		begin.addTransition(end);
	}

	public List<Pair<State<T>, State<T>>> findTransitions(final T transition)
	{
		final List<Pair<State<T>, State<T>>> result = new ArrayList<>();

		for (final State<T> s1 : _allStates)
		{
			final State<T> s2 = s1.move(transition);

			if (s2 != null) result.add(new Pair<>(s1, s2));
		}

		return result;
	}

	public boolean isInitial(final State<T> st)
	{
		return _initialState.equals(st);
	}

	public boolean isFinal(final State<T> st)
	{
		return _finalStates.contains(st);
	}

	// ---------------------------------------------------
	// test whether Set<State<T>> is DFA final state (contains NFA final state)

	public boolean isAnyFinal(final Set<State<T>> ss)
	{
		for (final State<T> st : ss)
			if (_finalStates.contains(st)) return true;
		return false;
	}

	// ---------------------------------------------------
	// given a DFA, print it out

	@Override
	public String toString()
	{
		final StringBuffer buf = new StringBuffer();

		buf.append("[Transition Graph\n");

		// print all states and edges
		for (final State<T> st : _allStates)
		{
			buf.append(st.getName()).append(": ");
			final Iterator<Transition<T>> i = st.getTransitions().iterator();
			while (i.hasNext())
			{
				buf.append(i.next());
				if (i.hasNext()) buf.append(", ");
			}

			buf.append("\n");
		}

		// print start state
		buf.append("initial state: ");
		buf.append(_initialState.getName());
		buf.append("\n");

		// print final state(s)
		buf.append("final states: ");
		buf.append(_finalStates);
		buf.append("\n");

		// print alphabet
		buf.append("alphabet: ");
		buf.append(_alphabet);
		buf.append("\n");
		buf.append("]\n");

		return buf.toString();
	}

	// ---------------------------------------------------
	// renumber states of TG in preorder, beginning at start state

	public TransitionGraph<T> renumber()
	{
		final Set<State<T>> processed = new HashSet<>();

		final LinkedList<State<T>> workList = new LinkedList<>();

		int val = 0;
		workList.addFirst(_initialState);

		while (workList.size() > 0)
		{
			final State<T> s = workList.removeFirst();
			s.setName(val++);
			processed.add(s);

			for (final Transition<T> e : s.getTransitions())
				if (processed.add(e.getTo())) workList.addLast(e.getTo());
		}

		return this;
	}

	// -------------------------------------------------------------//
	// -------------------------------------------------------------//
	// --------------Make changes past this point-------------------//
	// -------------------------------------------------------------//
	// -------------------------------------------------------------//

	// ---------------------------------------------------

	public TransitionGraph<T> insert(final TransitionGraph<T> tg, final State<T> i, final State<T> f)
	{
		// combine the alphabets
		_alphabet.addAll(tg._alphabet);

		// map each state in the input tg to a state in this tg
		final Map<State<T>, State<T>> newStates = new HashMap<>();
		// initial state of input tg will be mapped to state i
		newStates.put(tg.getInitialState(), i);
		// all the final states of input tg will be mapped to state f
		for (final State<T> fs : tg.getFinalStates())
			newStates.put(fs, f);

		// for each transition in tg, create a new transition in this tg
		// creating new states as necessary
		for (final State<T> s1 : tg._allStates)
		{
			State<T> n1 = newStates.get(s1);
			if (n1 == null)
			{
				n1 = newState();
				newStates.put(s1, n1);
			}

			for (final Transition<T> t : s1.getTransitions())
			{
				final State<T> s2 = t.getTo();

				State<T> n2 = newStates.get(s2);
				if (n2 == null)
				{
					n2 = newState();
					newStates.put(s2, n2);
				}

				if (t.isEpsilon())
					n1.addTransition(n2);
				else
					n1.addTransition(t.getName(), n2);
			}
		}

		return this;
	}

	// ---------------------------------------------------
	// compute a NFA move from a set of states
	// to states that are reachable by one edge labeled c

	public Set<State<T>> move(final Set<State<T>> stateSet, final T c)
	{
		final Set<State<T>> result = new HashSet<>();

		// for all the states in the set SS
		for (final State<T> st : stateSet)
			// for all the edges from state st
			for (final Transition<T> e : st.getTransitions())
				// add the 'to' state if transition matches
				if (e.hasName(c)) result.add(e.getTo());

		return result;
	}

	// ---------------------------------------------------
	// USER DEFINED FUNCTION
	// compute from a set of states, the states that are
	// reachable by any number of edges labeled epsilon
	// from only one state

	public Set<State<T>> epsilonClosure(final State<T> s, final Set<State<T>> init)
	{
		Set<State<T>> result = init;

		// s is in the epsilon closure of itself
		result.add(s);

		// for each edge from s
		for (final Transition<T> e : s.getTransitions())
			// if this is an epsilon transition and the result
			// does not contain 'to' state then add the epsilon
			// closure of 'to' state to the result set
			if (e.isEpsilon() && !result.contains(e.getTo())) result = epsilonClosure(e.getTo(), result);

		return result;
	}

	// ---------------------------------------------------
	// compute from a set of states, the states that are
	// reachable by any number of edges labeled epsilon

	public Set<State<T>> epsilonClosure(final Set<State<T>> stateSet)
	{
		Set<State<T>> result = new HashSet<>();

		// for each state in SS add their epsilon closure to the result
		for (final State<T> s : stateSet)
			result = epsilonClosure(s, result);

		return result;
	}

	public boolean isDeterministic()
	{
		if (!_allStates.contains(_initialState)) throw new InternalReasonerException();

		for (final State<T> s : _allStates)
		{
			final Set<T> seenSymbols = new HashSet<>();
			for (final Transition<T> t : s.getTransitions())
			{
				final T symbol = t.getName();

				if (t.isEpsilon() || !seenSymbols.add(symbol)) return false;
			}
		}

		return true;
	}

	public boolean isConnected()
	{
		final Set<State<T>> visited = new HashSet<>();
		final Stack<State<T>> stack = new Stack<>();

		stack.push(_initialState);
		visited.add(_initialState);

		while (!stack.isEmpty())
		{
			final State<T> state = stack.pop();

			if (!_allStates.contains(state)) return false;

			for (final Transition<T> t : state.getTransitions())
				if (visited.add(t.getTo())) stack.push(t.getTo());

		}

		return visited.size() == _allStates.size();
	}

	// ---------------------------------------------------
	// convert NFA into equivalent DFA

	public TransitionGraph<T> determinize()
	{
		// Define a map for the new states in DFA. The key for the
		// elements in map is the set of NFA states and the value
		// is the new state in DFA
		final HashMap<Set<State<T>>, State<T>> dStates = new HashMap<>();

		// start state of DFA is epsilon closure of start state in NFA
		State<T> s = new State<>();
		Set<State<T>> ss = epsilonClosure(_initialState, new HashSet<State<T>>());

		_initialState = s;

		// unmarked states in dStates will be processed
		final Set<State<T>> processList = new HashSet<>();
		processList.add(s);
		dStates.put(ss, s);
		_initialState = s;

		// if there are unprocessed states continue
		boolean moreToProcess = true;
		while (moreToProcess)
		{
			State<T> u = null;
			Set<State<T>> U = null;

			moreToProcess = false;

			//find an unmarked state in mappings in dStates
			for (final Map.Entry<Set<State<T>>, State<T>> entry : dStates.entrySet())
			{
				s = entry.getValue();
				ss = entry.getKey();
				moreToProcess = processList.contains(s);

				if (moreToProcess) break;
			}

			if (moreToProcess)
			{
				for (final T a : _alphabet)
				{
					// find epsilon closure of move with a
					U = epsilonClosure(move(ss, a));
					// if result is empty continue
					if (U.size() == 0) continue;
					// check if this set of NFA states are
					// already in dStates
					u = dStates.get(U);

					// if the result is equal to NFA states
					// associated with the processed state
					// then add an edge from s to itself
					// else create a new state and add edge
					if (u == null)
					{
						u = new State<>();
						processList.add(u);
						dStates.put(U, u);
					}
					else if (u.equals(s)) u = s;
					s.addTransition(a, u);
				}
				// update s in dStates (since key is unchanged only
				// the changed value i.e state s is updated in dStates)
				processList.remove(s);
				dStates.put(ss, s);
			}
		}
		// a set of final states for DFA
		final Set<State<T>> acceptingStates = new HashSet<>();
		// clear all states
		_allStates.clear();

		for (final Map.Entry<Set<State<T>>, State<T>> entry : dStates.entrySet())
		{
			// find DFA state and corresponding set of NFA states
			s = entry.getValue();
			ss = entry.getKey();
			// add DFA state to state set
			_allStates.add(s);
			// if any of NFA states are final update accepting states
			if (isAnyFinal(ss)) acceptingStates.add(s);
		}
		// accepting states becomes final states
		_finalStates.clear();
		_finalStates = acceptingStates;

		return this;
	}

	public void setPartition(final Set<State<T>> stateSet, final int num, final Map<State<T>, Integer> partitions)
	{
		for (final State<T> s : stateSet)
			partitions.put(s, num);
	}

	// ---------------------------------------------------
	// given a DFA, produce an equivalent minimized DFA

	public TransitionGraph<T> minimize()
	{
		// partitions are set of states, where max # of sets = # of states
		final List<Set<State<T>>> partitions = new ArrayList<>(_allStates.size());
		final Map<State<T>, Integer> partitionNumbers = new HashMap<>();
		final Map<State<T>, State<T>> partitionRep = new HashMap<>();

		// first partition is the set of final states
		final Set<State<T>> firstPartition = new HashSet<>(_finalStates);
		partitions.add(firstPartition);
		setPartition(firstPartition, 0, partitionNumbers);

		// check if there are any states that are not final
		if (firstPartition.size() < _allStates.size())
		{
			// second partition is set of non-accepting states
			final Set<State<T>> secondPartition = new HashSet<>(_allStates);
			secondPartition.removeAll(_finalStates);
			partitions.add(secondPartition);
			setPartition(secondPartition, 1, partitionNumbers);
		}

		for (int p = 0; p < partitions.size(); p++)
		{
			final Iterator<State<T>> i = partitions.get(p).iterator();

			// store the first element of the set
			final State<T> s = i.next();

			Set<State<T>> newPartition = null;

			// for all the states in a partition
			while (i.hasNext())
			{
				final State<T> t = i.next();

				// for all the symbols in an alphabet
				for (final T a : _alphabet)
					// find move(a) for the first and _current state
					// if they go to different partitions
					if (!isEquivalentState(s.move(a), t.move(a), partitionNumbers))
					{
						// if a new partition was not created in this iteration
						// create a new partition
						if (newPartition == null)
						{
							newPartition = new HashSet<>();
							partitions.add(newPartition);
						}

						// remove _current state from this partition
						i.remove();
						// add it to the new partition
						newPartition.add(t);
						// set its partition number
						partitionNumbers.put(t, partitions.size() - 1);
						// done with this state
						break;
					}
			}

			if (newPartition != null)
				// start checking from the first partition
				p = -1;
		}

		// store the partition num of the start state
		final int startPartition = partitionNumbers.get(_initialState);

		// for each partition the first state is marked as the representative
		// of that partition and rest is removed from states
		for (int p = 0; p < partitions.size(); p++)
		{
			final Iterator<State<T>> i = partitions.get(p).iterator();
			final State<T> s = i.next();
			partitionRep.put(s, s);
			if (p == startPartition) _initialState = s;
			while (i.hasNext())
			{
				final State<T> t = i.next();
				_allStates.remove(t);
				_finalStates.remove(t);
				// set rep so that we can later update
				// edges to this state
				partitionRep.put(t, s);
			}
		}

		// correct any edges that are going to states that are removed,
		// by updating the target state to be the rep of partition which
		// dead state belonged to
		for (final State<T> t : _allStates)
			for (final Transition<T> edge : t.getTransitions())
				edge.setTo(partitionRep.get(edge.getTo()));

		return this;
	}

	protected boolean isEquivalentState(final State<T> s1, final State<T> s2, final Map<State<T>, Integer> partitionNum)
	{
		if (s1 == s2) return true;
		if (s1 == null || s2 == null) return false;
		return partitionNum.get(s1).equals(partitionNum.get(s2));
	}
}
