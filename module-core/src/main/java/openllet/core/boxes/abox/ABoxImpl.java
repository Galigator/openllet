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

package openllet.core.boxes.abox;

import static java.lang.String.format;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.aterm.ATermList;
import openllet.atom.OpenError;
import openllet.core.DependencySet;
import openllet.core.KnowledgeBase;
import openllet.core.OpenlletOptions;
import openllet.core.boxes.rbox.RBox;
import openllet.core.boxes.rbox.Role;
import openllet.core.boxes.tbox.TBox;
import openllet.core.datatypes.DatatypeReasoner;
import openllet.core.datatypes.DatatypeReasonerImpl;
import openllet.core.datatypes.exceptions.DatatypeReasonerException;
import openllet.core.datatypes.exceptions.InvalidLiteralException;
import openllet.core.datatypes.exceptions.UnrecognizedDatatypeException;
import openllet.core.exceptions.InternalReasonerException;
import openllet.core.expressivity.Expressivity;
import openllet.core.tableau.branch.Branch;
import openllet.core.tableau.cache.CachedNode;
import openllet.core.tableau.cache.CachedNodeFactory;
import openllet.core.tableau.cache.ConceptCache;
import openllet.core.tableau.cache.ConceptCacheLRU;
import openllet.core.tableau.completion.CompletionStrategy;
import openllet.core.tableau.completion.SROIQIncStrategy;
import openllet.core.tableau.completion.queue.BasicCompletionQueue;
import openllet.core.tableau.completion.queue.CompletionQueue;
import openllet.core.tableau.completion.queue.NodeSelector;
import openllet.core.tableau.completion.queue.OptimizedBasicCompletionQueue;
import openllet.core.tableau.completion.queue.QueueElement;
import openllet.core.tracker.BranchEffectTracker;
import openllet.core.tracker.IncrementalChangeTracker;
import openllet.core.tracker.SimpleBranchEffectTracker;
import openllet.core.tracker.SimpleIncrementalChangeTracker;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.Bool;
import openllet.core.utils.CandidateSet;
import openllet.core.utils.MultiMapUtils;
import openllet.core.utils.SetUtils;
import openllet.core.utils.Timer;
import openllet.core.utils.fsm.State;
import openllet.core.utils.fsm.Transition;
import openllet.core.utils.fsm.TransitionGraph;
import openllet.core.utils.iterator.MultiListIterator;
import openllet.shared.tools.Log;

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
public class ABoxImpl implements ABox
{
	private final static Logger _logger = Log.getLogger(ABoxImpl.class);

	private final ABoxStats _stats = new ABoxStats();

	/**
	 * datatype reasoner used for checking the satisfiability of datatypes
	 */
	private final DatatypeReasoner _dtReasoner;

	/** The KB to which this ABox belongs */
	private final KnowledgeBase _kb;
	private final BranchEffectTracker _branchEffects;
	private final CompletionQueue _completionQueue;
	private final IncrementalChangeTracker _incChangeTracker;

	private final List<Branch> _branches;

	/**
	 * This is a list of node names. This list stores the individuals in the order they are created
	 */
	private final List<ATermAppl> _nodeList;

	// pseudo model for this Abox. This is the ABox that results from
	// completing to the original Abox
	// private ABox pseudoModel;

	private final Set<Clash> _assertedClashes;

	private final List<NodeMerge> _toBeMerged;

	private final Map<ATermAppl, int[]> _disjBranchStats;

	/**
	 * This is a list of _nodes. Each _node has a name expressed as an ATerm which is used as the key in the Hashtable. The value is the actual _node object
	 */
	private final Map<ATermAppl, Node> _nodes;

	/** the current branch number */
	private volatile int _branchIndex;

	/** the last clash recorded */
	private volatile Clash _clash;

	/** if we are using copy on write, this is where to copy from */
	private volatile ABox _sourceABox; // FIXME : sourceBox actively use null.

	/**
	 * cache of the last completion. it may be different from the pseudo model, e.g. type checking for individual adds one extra assertion last completion is
	 * stored for caching the root nodes that was the result of
	 */
	private volatile ABox _lastCompletion;

	private volatile Clash _lastClash;

	/**
	 * following two variables are used to generate names for newly generated individuals. so during rules are applied anon1, anon2, etc. will be generated.
	 * This prefix will also make sure that any node whose name starts with this prefix is not a root node
	 */
	private volatile int _anonCount = 0;

	private volatile boolean _keepLastCompletion;

	// complete ABox means no more tableau rules are applicable
	private volatile boolean _isComplete = false;

	/**
	 * Indicates if any of the completion rules has been applied to modify ABox
	 */
	private volatile boolean _changed = false;

	private volatile boolean _doExplanation = false;

	/**
	 * return true if init() function is called. This indicates parsing is completed and ABox is ready for completion
	 */
	private volatile boolean _initialized = false;

	private volatile boolean _rulesNotApplied = false;

	/** flag set when incrementally updating the abox with explicit assertions */
	private volatile boolean _syntacticUpdate = false;

	/**
	 * cached satisfiability results the table maps every atomic concept A (and also its negation not(A)) to the root node of its completed tree. If a concept
	 * is mapped to null value it means it is not satisfiable
	 */
	private volatile ConceptCache _cache;

	@Override
	public Logger getLogger()
	{
		return _logger;
	}

	@Override
	public ABoxStats getStats()
	{
		return _stats;
	}

	@Override
	public List<ATermAppl> getNodeList()
	{
		return _nodeList;
	}

	@Override
	public ABox getSourceABox()
	{
		return _sourceABox;
	}

	@Override
	public void setSourceABox(final ABox sourceABox)
	{
		_sourceABox = sourceABox;
	}

	@Override
	public boolean isRulesNotApplied()
	{
		return _rulesNotApplied;
	}

	@Override
	public void setRulesNotApplied(final boolean rulesNotApplied)
	{
		_rulesNotApplied = rulesNotApplied;
	}

	public ABoxImpl(final KnowledgeBase kb)
	{
		_kb = kb;
		_nodes = Collections.synchronizedMap(new IdentityHashMap<>());
		_nodeList = new ArrayList<>();
		_clash = null;
		_assertedClashes = SetUtils.create();
		_doExplanation = false;
		_dtReasoner = new DatatypeReasonerImpl();
		_keepLastCompletion = false;

		setBranchIndex(DependencySet.NO_BRANCH);
		_branches = new ArrayList<>();
		_disjBranchStats = Collections.synchronizedMap(new IdentityHashMap<>());

		_toBeMerged = new ArrayList<>();
		_rulesNotApplied = true;

		if (OpenlletOptions.TRACK_BRANCH_EFFECTS)
			_branchEffects = new SimpleBranchEffectTracker();
		else
			_branchEffects = null;

		if (OpenlletOptions.USE_COMPLETION_QUEUE)
		{
			if (OpenlletOptions.USE_OPTIMIZED_BASIC_COMPLETION_QUEUE)
				_completionQueue = new OptimizedBasicCompletionQueue(this);
			else
				_completionQueue = new BasicCompletionQueue(this);
		}
		else
			_completionQueue = null;

		if (OpenlletOptions.USE_INCREMENTAL_CONSISTENCY)
			_incChangeTracker = new SimpleIncrementalChangeTracker();
		else
			_incChangeTracker = null;
	}

	public ABoxImpl(final KnowledgeBase kb, final boolean copyCache)
	{
		this(kb);
		if (copyCache)
			_cache = kb.getABox().getCache();
	}

	public ABoxImpl(final KnowledgeBase kb, final ABoxImpl abox, final ATermAppl extraIndividual, final boolean copyIndividuals)
	{
		_kb = kb;
		final Optional<Timer> timer = kb.getTimers().startTimer("cloneABox");

		_rulesNotApplied = true;
		_initialized = abox._initialized;
		setChanged(abox.isChanged());
		setAnonCount(abox.getAnonCount());
		_cache = abox._cache;
		_clash = abox._clash;
		_dtReasoner = abox._dtReasoner;
		_doExplanation = abox._doExplanation;
		_disjBranchStats = abox.getDisjBranchStats();

		final int extra = extraIndividual == null ? 0 : 1;
		final int nodeCount = extra + (copyIndividuals ? abox._nodes.size() : 0);

		_nodes = Collections.synchronizedMap(new IdentityHashMap<>(nodeCount));
		_nodeList = new ArrayList<>(nodeCount);

		if (OpenlletOptions.TRACK_BRANCH_EFFECTS)
		{
			if (copyIndividuals)
				_branchEffects = abox._branchEffects.copy();
			else
				_branchEffects = new SimpleBranchEffectTracker();
		}
		else
			_branchEffects = null;

		// copy the _queue - this must be done early so that the effects of
		// adding the extra _individual do not get removed
		if (OpenlletOptions.USE_COMPLETION_QUEUE)
		{
			if (copyIndividuals)
			{
				_completionQueue = abox._completionQueue.copy();
				_completionQueue.setABox(this);
			}
			else
				if (OpenlletOptions.USE_OPTIMIZED_BASIC_COMPLETION_QUEUE)
					_completionQueue = new OptimizedBasicCompletionQueue(this);
				else
					_completionQueue = new BasicCompletionQueue(this);
		}
		else
			_completionQueue = null;

		if (extraIndividual != null)
		{
			final Individual n = new Individual(extraIndividual, this, null);
			n.setNominalLevel(Node.BLOCKABLE);
			n.setConceptRoot(true);
			n.addType(ATermUtils.TOP, DependencySet.INDEPENDENT);
			_nodes.put(extraIndividual, n);
			_nodeList.add(extraIndividual);

			if (OpenlletOptions.COPY_ON_WRITE)
				_sourceABox = abox;
		}

		if (copyIndividuals)
		{
			_toBeMerged = abox.getToBeMerged();
			if (_sourceABox == null)
			{
				for (int i = 0; i < nodeCount - extra; i++)
				{
					final ATermAppl x = abox._nodeList.get(i);
					final Node node = abox.getNode(x);
					final Node copy = node.copyTo(this);

					_nodes.put(x, copy);
					_nodeList.add(x);
				}

				for (final Node node : _nodes.values())
					node.updateNodeReferences();
			}
		}
		else
		{
			_toBeMerged = Collections.emptyList();
			_sourceABox = null;
			_initialized = false;
		}

		// Copy of the _incChangeTracker looks up _nodes in the new ABox, so this
		// copy must follow _node copying
		if (OpenlletOptions.USE_INCREMENTAL_CONSISTENCY)
		{
			if (copyIndividuals)
				_incChangeTracker = abox._incChangeTracker.copy(this);
			else
				_incChangeTracker = new SimpleIncrementalChangeTracker();
		}
		else
			_incChangeTracker = null;

		_assertedClashes = SetUtils.create();
		for (final Clash clash : abox._assertedClashes)
			_assertedClashes.add(clash.copyTo(this));

		if (extraIndividual == null || copyIndividuals)
		{
			setBranchIndex(abox._branchIndex);
			_branches = new ArrayList<>(abox._branches.size());
			for (int i = 0, n = abox._branches.size(); i < n; i++)
			{
				final Branch branch = abox._branches.get(i);
				Branch copy;

				if (_sourceABox == null)
				{
					copy = branch.copyTo(this);
					copy.setNodeCount(branch.getNodeCount() + extra);
				}
				else
					copy = branch;
				_branches.add(copy);
			}
		}
		else
		{
			setBranchIndex(DependencySet.NO_BRANCH);
			_branches = new ArrayList<>();
		}

		timer.ifPresent(t -> t.stop());
	}

	@Override
	public ABoxImpl copy()
	{
		return copy(_kb);
	}

	@Override
	public ABoxImpl copy(final KnowledgeBase kb)
	{
		return new ABoxImpl(kb, this, null, true);
	}

	@Override
	public ABoxImpl copy(final ATermAppl extraIndividual, final boolean copyIndividuals)
	{
		return new ABoxImpl(_kb, this, extraIndividual, copyIndividuals);
	}

	@Override
	public void copyOnWrite()
	{
		if (_sourceABox == null)
			return;

		final Optional<Timer> timer = _kb.getTimers().startTimer("copyOnWrite");

		final List<ATermAppl> currentNodeList = new ArrayList<>(_nodeList);
		final int currentSize = currentNodeList.size();
		final int nodeCount = getSourceABox().getNodes().size();

		_nodeList.clear();// reset cost less than reallocate a new array.
		_nodeList.add(currentNodeList.get(0));

		for (int i = 0; i < nodeCount; i++)
		{
			final ATermAppl x = getSourceABox().getNodeList().get(i);
			final Node node = _sourceABox.getNode(x);
			final Node copyNode = node.copyTo(this);
			_nodes.put(x, copyNode);
			_nodeList.add(x);
		}

		if (currentSize > 1)
			_nodeList.addAll(currentNodeList.subList(1, currentSize));

		for (final Node node : _nodes.values())
			if (getSourceABox().getNodes().containsKey(node.getName()))
				node.updateNodeReferences();

		for (int i = 0, n = _branches.size(); i < n; i++)
		{
			final Branch branch = _branches.get(i);
			final Branch copy = branch.copyTo(this);
			_branches.set(i, copy);

			if (i >= _sourceABox.getBranches().size())
				copy.setNodeCount(copy.getNodeCount() + nodeCount);
			else
				copy.setNodeCount(copy.getNodeCount() + 1);
		}

		timer.ifPresent(t -> t.stop());

		_sourceABox = null;
	}

	/**
	 * Clear the pseudo model created for the ABox and concept satisfiability.
	 *
	 * @param clearSatCache If true clear concept satisfiability _cache, if false only clear pseudo model.
	 */
	@Override
	public void clearCaches(final boolean clearSatCache)
	{
		_lastCompletion = null;

		if (clearSatCache)
			_cache = new ConceptCacheLRU(_kb);
	}

	@Override
	public Bool getCachedSat(final ATermAppl c)
	{
		return _cache.getSat(c);
	}

	@Override
	public ConceptCache getCache()
	{
		return _cache;
	}

	@Override
	public CachedNode getCached(final ATermAppl c)
	{
		if (ATermUtils.isNominal(c))
			return getIndividual(c.getArgument(0)).getSame();
		else
			return _cache.get(c);
	}

	private void cache(final Individual rootNode, final ATermAppl c, final boolean isConsistent)
	{

		if (!isConsistent)
		{
			if (_logger.isLoggable(Level.FINE))
			{
				_logger.fine("Unsatisfiable: " + ATermUtils.toString(c));
				_logger.fine("Equivalent to TOP: " + ATermUtils.toString(ATermUtils.negate(c)));
			}

			_cache.putSat(c, false);
		}
		else
		{
			_logger.fine(() -> "Cache " + rootNode.debugString());

			_cache.put(c, CachedNodeFactory.createNode(c, rootNode));
		}
	}

	@Override
	public Bool isKnownSubClassOf(final ATermAppl c1, final ATermAppl c2)
	{
		Bool isSubClassOf = Bool.UNKNOWN;
		final CachedNode cached = getCached(c1);
		if (cached != null)
			isSubClassOf = isType(cached, c2);

		return isSubClassOf;
	}

	@Override
	public boolean isSubClassOf(final ATermAppl c1, final ATermAppl c2)
	{
		if (!_doExplanation)
		{
			final Bool isKnownSubClass = isKnownSubClassOf(c1, c2);
			if (isKnownSubClass.isKnown())
				return isKnownSubClass.isTrue();
		}

		if (_logger.isLoggable(Level.FINE))
		{
			final long count = _kb.getTimers().getTimer("subClassSat").map(t -> t.getCount()).orElse(0L);
			_logger.fine(count + ") Checking subclass [" + ATermUtils.toString(c1) + " " + ATermUtils.toString(c2) + "]");
		}

		final ATermAppl notC2 = ATermUtils.negate(c2);
		final ATermAppl c = ATermUtils.makeAnd(c1, notC2);
		final Optional<Timer> timer = _kb.getTimers().startTimer("subClassSat");
		final boolean sub = !isSatisfiable(c, false);
		timer.ifPresent(t -> t.stop());

		_logger.fine(() -> " Result: " + sub + timer.map(t -> " (" + t.getLast() + "ms)").orElse(""));

		return sub;
	}

	@Override
	public boolean isSatisfiable(final ATermAppl c)
	{
		final boolean cacheModel = OpenlletOptions.USE_CACHING && (ATermUtils.isPrimitiveOrNegated(c) || OpenlletOptions.USE_ADVANCED_CACHING);
		return isSatisfiable(c, cacheModel);
	}

	@Override
	public boolean isSatisfiable(final ATermAppl cParam, final boolean cacheModel)
	{
		ATermAppl c = cParam;
		c = ATermUtils.normalize(c);

		// if normalization revealed an obvious unsatisfiability, return
		// immediately
		if (c.equals(ATermUtils.BOTTOM))
		{
			_lastClash = Clash.unexplained(null, DependencySet.INDEPENDENT, "Obvious contradiction in class expression: " + ATermUtils.toString(c));
			return false;
		}

		if (_logger.isLoggable(Level.FINE))
			_logger.fine("Satisfiability for " + ATermUtils.toString(c));

		if (cacheModel)
		{
			final CachedNode cached = getCached(c);
			if (cached != null)
			{
				final boolean satisfiable = !cached.isBottom();
				final boolean needToCacheModel = cacheModel && !cached.isComplete();
				if (_logger.isLoggable(Level.FINE))
					_logger.fine("Cached sat for " + ATermUtils.toString(c) + " is " + satisfiable);
				// if clashExplanation is enabled we should actually build the
				// tableau again to generate the _clash. we don't _cache the
				// clashExplanation up front because generating clashExplanation is costly
				// and we only want to do it when explicitly asked note that
				// when the concepts is satisfiable there is no clashExplanation to
				// be generated so we return the result immediately
				if (!needToCacheModel && (satisfiable || !_doExplanation))
					return satisfiable;
			}
		}

		_stats._satisfiabilityCount++;

		final Optional<Timer> timer = _kb.getTimers().startTimer("satisfiability");
		final boolean isSat = isConsistent(Collections.emptySet(), c, cacheModel);
		timer.ifPresent(t -> t.stop());

		return isSat;
	}

	@Override
	public CandidateSet<ATermAppl> getObviousInstances(final ATermAppl c)
	{
		return getObviousInstances(c, _kb.getIndividuals());
	}

	@Override
	public CandidateSet<ATermAppl> getObviousInstances(final ATermAppl cParam, final Collection<ATermAppl> individuals)
	{
		ATermAppl c = cParam;
		c = ATermUtils.normalize(c);
		final Set<ATermAppl> subs = _kb.isClassified() && _kb.getTaxonomy().contains(c) ? _kb.getTaxonomy().getFlattenedSubs(c, false) : Collections.<ATermAppl> emptySet();
		subs.remove(ATermUtils.BOTTOM);

		final CandidateSet<ATermAppl> cs = new CandidateSet<>();
		for (final ATermAppl x : individuals)
		{
			final Bool isType = isKnownType(x, c, subs);
			cs.add(x, isType);
		}

		return cs;
	}

	@Override
	public void getObviousTypes(final ATermAppl x, final List<ATermAppl> types, final List<ATermAppl> nonTypes)
	{
		assert isComplete() : "Initial consistency check has not been performed!";

		Individual pNode = getIndividual(x);
		if (!pNode.getMergeDependency(true).isIndependent())
			pNode = getIndividual(x);
		else
			pNode = pNode.getSame();

		pNode.getObviousTypes(types, nonTypes);
	}

	@Override
	public CandidateSet<ATermAppl> getObviousSubjects(final ATermAppl p, final ATermAppl o)
	{
		final CandidateSet<ATermAppl> candidates = new CandidateSet<>(_kb.getIndividuals());
		getObviousSubjects(p, o, candidates);

		return candidates;
	}

	@Override
	public void getSubjects(final ATermAppl p, final ATermAppl o, final CandidateSet<ATermAppl> candidates)
	{
		final Iterator<ATermAppl> i = candidates.iterator();
		while (i.hasNext())
		{
			final ATermAppl s = i.next();

			final Bool hasObviousValue = hasObviousPropertyValue(s, p, o);
			candidates.update(s, hasObviousValue);
		}
	}

	@Override
	public void getObviousSubjects(final ATermAppl p, final ATermAppl o, final CandidateSet<ATermAppl> candidates)
	{
		final Iterator<ATermAppl> i = candidates.iterator();
		while (i.hasNext())
		{
			final ATermAppl s = i.next();

			final Bool hasObviousValue = hasObviousPropertyValue(s, p, o);
			if (hasObviousValue.isFalse())
				i.remove();
			else
				candidates.update(s, hasObviousValue);
		}
	}

	@Override
	public void getObviousObjects(final ATermAppl pParam, final CandidateSet<ATermAppl> candidates)
	{
		ATermAppl p = pParam;
		p = getRole(p).getInverse().getName();
		final Iterator<ATermAppl> i = candidates.iterator();
		while (i.hasNext())
		{
			final ATermAppl s = i.next();

			final Bool hasObviousValue = hasObviousObjectPropertyValue(s, p, null);
			candidates.update(s, hasObviousValue);
		}
	}

	@Override
	public Bool isKnownType(final ATermAppl x, final ATermAppl c)
	{
		return isKnownType(x, c, Collections.emptySet());
	}

	@Override
	public Bool isKnownType(final ATermAppl x, final ATermAppl c, final Collection<ATermAppl> subs)
	{
		assert isComplete() : "Initial consistency check has not been performed!";

		Individual pNode = getIndividual(x);

		boolean isIndependent = true;
		if (pNode.isMerged())
		{
			isIndependent = pNode.getMergeDependency(true).isIndependent();
			pNode = pNode.getSame();
		}

		final Bool isType = isKnownType(pNode, c, subs);

		if (isIndependent)
			return isType;
		else
			if (isType.isTrue())
				return Bool.UNKNOWN;
			else
				return isType;
	}

	@Override
	public Bool isKnownType(final Individual pNode, final ATermAppl concept, final Collection<ATermAppl> subs)
	{
		Bool isType = isType(pNode, concept);
		if (isType.isUnknown())
		{
			final Set<ATermAppl> concepts = ATermUtils.isAnd(concept) ? ATermUtils.listToSet((ATermList) concept.getArgument(0)) : SetUtils.singleton(concept);

			isType = Bool.TRUE;
			for (final ATermAppl c : concepts)
			{
				Bool type = pNode.hasObviousType(c);

				if (type.isUnknown() && pNode.hasObviousType(subs))
					type = Bool.TRUE;

				if (type.isKnown())
					isType = isType.and(type);
				else
				{
					isType = Bool.UNKNOWN;

					final Collection<ATermAppl> axioms = _kb.getTBox().getAxioms(c);
					LOOP: for (final ATermAppl axiom : axioms)
					{
						ATermAppl term = (ATermAppl) axiom.getArgument(1);

						final boolean equivalent = axiom.getAFun().equals(ATermUtils.EQCLASSFUN);
						if (equivalent)
						{
							final Iterator<ATermAppl> i = ATermUtils.isAnd(term) ? //
									new MultiListIterator((ATermList) term.getArgument(0)) : //
									Collections.singleton(term).iterator();
							Bool knownType = Bool.TRUE;
							while (i.hasNext() && knownType.isTrue())
							{
								term = i.next();
								knownType = isKnownType(pNode, term, Collections.emptySet());
							}
							if (knownType.isTrue())
							{
								isType = Bool.TRUE;
								break LOOP;
							}
						}
					}

					// TODO following short-cut might be implemented correctly
					// the main problem here is that concept might be in the
					// types of the _individual with a dependency. In this case,
					// Node.hasObviousType returns unknown and changing it to
					// false here is wrong.

					if (isType.isUnknown())
						return Bool.UNKNOWN;
				}
			}
		}

		return isType;
	}

	private Bool isType(final CachedNode pNode, final ATermAppl c)
	{
		Bool isType = Bool.UNKNOWN;

		final boolean isPrimitive = _kb.getTBox().isPrimitive(c);

		if (isPrimitive && !pNode.isTop() && !pNode.isBottom() && pNode.isComplete())
		{
			final DependencySet ds = pNode.getDepends().get(c);
			if (ds == null)
				return Bool.FALSE;
			else
				if (ds.isIndependent() && pNode.isIndependent())
					return Bool.TRUE;
		}

		final ATermAppl notC = ATermUtils.negate(c);
		final CachedNode cached = getCached(notC);
		if (cached != null && cached.isComplete())
			isType = _cache.isMergable(_kb, pNode, cached).not();

		if (OpenlletOptions.CHECK_NOMINAL_EDGES && isType.isUnknown())
		{
			final CachedNode cNode = getCached(c);
			if (cNode != null)
				isType = _cache.checkNominalEdges(_kb, pNode, cNode);
		}

		return isType;
	}

	@Override
	public boolean isSameAs(final ATermAppl ind1, final ATermAppl ind2)
	{
		final ATermAppl c = ATermUtils.makeValue(ind2);

		return isType(ind1, c);
	}

	/**
	 * @param x is an individual
	 * @param cParam is a class
	 * @return true if individual x belongs to type c. This is a logical consequence of the KB if in all possible models x belongs to C. This is checked by
	 *         trying to construct a model where x belongs to not(c).
	 */
	@Override
	public boolean isType(final ATermAppl x, final ATermAppl cParam)
	{
		ATermAppl c = cParam;
		c = ATermUtils.normalize(c);

		if (!doExplanation())
		{
			Set<ATermAppl> subs;
			if (_kb.isClassified() && _kb.getTaxonomy().contains(c))
			{
				subs = _kb.getTaxonomy().getFlattenedSubs(c, false);
				subs.remove(ATermUtils.BOTTOM);
			}
			else
				subs = Collections.emptySet();

			final Bool type = isKnownType(x, c, subs);
			if (type.isKnown())
				return type.isTrue();
		}
		// List list = (List) _kb.instances.get( c );
		// if( list != null )
		// return list.contains( x );

		if (_logger.isLoggable(Level.FINE))
			_logger.fine("Checking type " + ATermUtils.toString(c) + " for individual " + ATermUtils.toString(x));

		final ATermAppl notC = ATermUtils.negate(c);

		final Optional<Timer> timer = _kb.getTimers().startTimer("isType");
		final boolean isType = !isConsistent(SetUtils.singleton(x), notC, false);
		timer.ifPresent(Timer::stop);

		if (_logger.isLoggable(Level.FINE))
			_logger.fine("Type " + isType + " " + ATermUtils.toString(c) + " for individual " + ATermUtils.toString(x));

		return isType;
	}

	@Override
	public boolean existType(final List<ATermAppl> inds, final ATermAppl cParam)
	{
		final ATermAppl c = ATermUtils.normalize(cParam);

		_logger.fine(() -> "Checking type " + ATermUtils.toString(c) + " for individuals " + inds.size());

		final ATermAppl notC = ATermUtils.negate(c);

		final boolean isType = !isConsistent(inds, notC, false);

		_logger.fine(() -> "Type " + isType + " " + ATermUtils.toString(c) + " for individuals " + inds.size());

		return isType;
	}

	@Override
	public Bool hasObviousPropertyValue(final ATermAppl s, final ATermAppl p, final ATermAppl o)
	{
		final Role prop = getRole(p);

		if (prop.isDatatypeRole())
			try
			{
				final Object value = o == null ? null : _dtReasoner.getValue(o);
				return hasObviousDataPropertyValue(s, p, value);
			}
			catch (final UnrecognizedDatatypeException e)
			{
				_logger.warning(format("Returning false for property value check (%s,%s,%s) due to datatype problem with input literal: %s", s, p, o, e.getMessage()));
				return Bool.FALSE;
			}
			catch (final InvalidLiteralException e)
			{
				_logger.warning(format("Returning false for property value check (%s,%s,%s) due to problem with input literal: %s", s, p, o, e.getMessage()));
				return Bool.FALSE;
			}
		else
			return hasObviousObjectPropertyValue(s, p, o);
	}

	@Override
	public Bool hasObviousDataPropertyValue(final ATermAppl s, final ATermAppl p, final Object value)
	{
		assert isComplete() : "Initial consistency check has not been performed!";

		Individual subj = getIndividual(s);
		final Role prop = getRole(p);

		if (prop.isTop())
			return Bool.TRUE;
		else
			if (prop.isBottom())
				return Bool.FALSE;

		// if onlyPositive is set then the answer returned is sound but not
		// complete so we cannot return negative answers
		boolean onlyPositive = false;

		if (!subj.getMergeDependency(true).isIndependent())
		{
			onlyPositive = true;
			subj = getIndividual(s);
		}
		else
			subj = subj.getSame();

		final Bool hasValue = subj.hasDataPropertyValue(prop, value);
		if (onlyPositive && hasValue.isFalse())
			return Bool.UNKNOWN;

		return hasValue;
	}

	@Override
	public Bool hasObviousObjectPropertyValue(final ATermAppl s, final ATermAppl p, final ATermAppl o)
	{
		final Role prop = getRole(p);

		if (prop.isTop())
			return Bool.TRUE;
		else
			if (prop.isBottom())
				return Bool.FALSE;

		final Set<ATermAppl> knowns = new HashSet<>();
		final Set<ATermAppl> unknowns = new HashSet<>();

		getObjectPropertyValues(s, prop, knowns, unknowns, true);

		if (o == null)
		{
			if (!knowns.isEmpty())
				return Bool.TRUE;
			else
				if (!unknowns.isEmpty())
					return Bool.UNKNOWN;
				else
					return Bool.FALSE;
		}
		else
			if (knowns.contains(o))
				return Bool.TRUE;
			else
				if (unknowns.contains(o))
					return Bool.UNKNOWN;
				else
					return Bool.FALSE;
	}

	@Override
	public boolean hasPropertyValue(final ATermAppl s, final ATermAppl p, final ATermAppl o)
	{
		final Bool hasObviousValue = hasObviousPropertyValue(s, p, o);
		if (hasObviousValue.isKnown())
			if (hasObviousValue.isFalse() || !doExplanation())
				return hasObviousValue.isTrue();

		ATermAppl c = null;
		if (o == null)
		{
			if (_kb.isDatatypeProperty(p))
				c = ATermUtils.makeMin(p, 1, ATermUtils.TOP_LIT);
			else
				c = ATermUtils.makeMin(p, 1, ATermUtils.TOP);
		}
		else
			c = ATermUtils.makeHasValue(p, o);

		final boolean isType = isType(s, c);

		return isType;
	}

	@Override
	public List<ATermAppl> getDataPropertyValues(final ATermAppl s, final Role role, final ATermAppl datatype)
	{
		return getDataPropertyValues(s, role, datatype, false);
	}

	@Override
	public List<ATermAppl> getDataPropertyValues(final ATermAppl s, final Role role, final ATermAppl datatype, final boolean onlyObvious)
	{
		assert isComplete() : "Initial consistency check has not been performed!";

		Individual subj = getIndividual(s);

		final List<ATermAppl> values = new ArrayList<>();

		boolean isIndependent = true;
		if (subj.isMerged())
		{
			isIndependent = subj.getMergeDependency(true).isIndependent();
			subj = subj.getSame();
		}

		final EdgeList edges = subj.getRSuccessorEdges(role);
		for (int i = 0; i < edges.size(); i++)
		{
			final Edge edge = edges.get(i);
			final DependencySet ds = edge.getDepends();
			final Literal literal = (Literal) edge.getTo();
			final ATermAppl literalValue = literal.getTerm();
			if (literalValue != null)
			{
				if (datatype != null)
					if (!literal.hasType(datatype))
						try
						{
							if (!_dtReasoner.isSatisfiable(Collections.singleton(datatype), literal.getValue()))
								continue;
						}
						catch (final DatatypeReasonerException e)
						{
							final String msg = format("Unexpected datatype reasoner exception while fetching property values (%s,%s,%s): %s", s, role, datatype, e.getMessage());
							_logger.severe(msg);
							throw new InternalReasonerException(msg);
						}

				if (isIndependent && ds.isIndependent())
					values.add(literalValue);
				else
					if (!onlyObvious)
					{
						final ATermAppl hasValue = ATermUtils.makeHasValue(role.getName(), literalValue);
						if (isType(s, hasValue))
							values.add(literalValue);
					}
			}
		}

		return values;
	}

	@Override
	public List<ATermAppl> getObviousDataPropertyValues(final ATermAppl s, final Role prop, final ATermAppl datatype)
	{
		return getDataPropertyValues(s, prop, datatype, true);
	}

	@Override
	public void getObjectPropertyValues(final ATermAppl s, final Role role, final Set<ATermAppl> knowns, final Set<ATermAppl> unknowns, final boolean getSames)
	{
		assert isComplete() : "Initial consistency check has not been performed!";

		Individual subj = getIndividual(s);

		boolean isIndependent = true;
		if (subj.isMerged())
		{
			isIndependent = subj.getMergeDependency(true).isIndependent();
			subj = subj.getSame();
		}

		if (role.isSimple())
			getSimpleObjectPropertyValues(subj, role, knowns, unknowns, getSames);
		else
			if (!role.hasComplexSubRole())
				getTransitivePropertyValues(subj, role, knowns, unknowns, getSames, new HashMap<Individual, Set<Role>>(), true);
			else
			{
				final TransitionGraph<Role> tg = role.getFSM();
				getComplexObjectPropertyValues(subj, tg.getInitialState(), tg, knowns, unknowns, getSames, new HashMap<Individual, Set<State<Role>>>(), true);
			}

		if (!isIndependent)
		{
			unknowns.addAll(knowns);
			knowns.clear();
		}
	}

	@Override
	public void getSimpleObjectPropertyValues(final Individual subj, final Role role, final Set<ATermAppl> knowns, final Set<ATermAppl> unknowns, final boolean getSames)
	{
		final EdgeList edges = subj.getRNeighborEdges(role);
		for (int i = 0; i < edges.size(); i++)
		{
			final Edge edge = edges.get(i);
			final DependencySet ds = edge.getDepends();
			final Individual value = (Individual) edge.getNeighbor(subj);

			if (value.isRootNominal())
				if (ds.isIndependent())
				{
					if (getSames)
						getSames(value, knowns, unknowns);
					else
						knowns.add(value.getName());
				}
				else
					if (getSames)
						getSames(value, unknowns, unknowns);
					else
						unknowns.add(value.getName());
		}
	}

	@Override
	public void getTransitivePropertyValues(final Individual subj, final Role prop, final Set<ATermAppl> knowns, final Set<ATermAppl> unknowns, final boolean getSames, final Map<Individual, Set<Role>> visited, final boolean isIndependent)
	{
		if (!MultiMapUtils.addAll(visited, subj, prop.getSubRoles()))
			return;

		final EdgeList edges = subj.getRNeighborEdges(prop);
		for (int i = 0; i < edges.size(); i++)
		{
			final Edge edge = edges.get(i);
			final DependencySet ds = edge.getDepends();
			final Individual value = (Individual) edge.getNeighbor(subj);
			final Role edgeRole = edge.getFrom().equals(subj) ? edge.getRole() : edge.getRole().getInverse();
			if (value.isRootNominal())
				if (isIndependent && ds.isIndependent())
				{
					if (getSames)
						getSames(value, knowns, unknowns);
					else
						knowns.add(value.getName());
				}
				else
					if (getSames)
						getSames(value, unknowns, unknowns);
					else
						unknowns.add(value.getName());

			if (!prop.isSimple())
			{
				// all the following roles might cause this property to
				// propagate
				final Set<Role> transRoles = SetUtils.intersection(edgeRole.getSuperRoles(), prop.getTransitiveSubRoles());
				for (final Role transRole : transRoles)
					getTransitivePropertyValues(value, transRole, knowns, unknowns, getSames, visited, isIndependent && ds.isIndependent());
			}
		}
	}

	@Override
	public void getComplexObjectPropertyValues(final Individual subj, final State<Role> st, final TransitionGraph<Role> tg, final Set<ATermAppl> knowns, final Set<ATermAppl> unknowns, final boolean getSames, final HashMap<Individual, Set<State<Role>>> visited, final boolean isIndependent)
	{
		if (!MultiMapUtils.add(visited, subj, st))
			return;

		if (tg.isFinal(st) && subj.isRootNominal())
		{
			_logger.fine("add " + subj);
			if (isIndependent)
			{
				if (getSames)
					getSames(subj, knowns, unknowns);
				else
					knowns.add(subj.getName());
			}
			else
				if (getSames)
					getSames(subj, unknowns, unknowns);
				else
					unknowns.add(subj.getName());
		}

		_logger.fine(subj.toString());

		for (final Transition<Role> t : st.getTransitions())
		{
			final Role r = t.getName();
			final EdgeList edges = subj.getRNeighborEdges(r);
			for (int i = 0; i < edges.size(); i++)
			{
				final Edge edge = edges.get(i);
				final DependencySet ds = edge.getDepends();
				final Individual value = (Individual) edge.getNeighbor(subj);

				getComplexObjectPropertyValues(value, t.getTo(), tg, knowns, unknowns, getSames, visited, isIndependent && ds.isIndependent());
			}
		}
	}

	@Override
	public void getSames(final Individual ind, final Set<ATermAppl> knowns, final Set<ATermAppl> unknowns)
	{
		knowns.add(ind.getName());

		final boolean thisMerged = ind.isMerged() && !ind.getMergeDependency(true).isIndependent();

		for (final Node other : ind.getMerged())
		{
			if (!other.isRootNominal())
				continue;

			final boolean otherMerged = other.isMerged() && !other.getMergeDependency(true).isIndependent();
			if (thisMerged || otherMerged)
			{
				unknowns.add(other.getName());
				getSames((Individual) other, unknowns, unknowns);
			}
			else
			{
				knowns.add(other.getName());
				getSames((Individual) other, knowns, unknowns);
			}
		}
	}

	/**
	 * @return true if this ABox is consistent. Consistent ABox means after applying all the tableau completion rules at least one _branch with no clashes was
	 *         found
	 */
	@Override
	public boolean isConsistent()
	{
		boolean isConsistent = false;

		checkAssertedClashes();

		isConsistent = isConsistent(Collections.emptySet(), null, false);

		if (isConsistent)
		{
			// put the BOTTOM concept into the _cache which will
			// also put TOP in there
			_cache.putSat(ATermUtils.BOTTOM, false);

			assert isComplete() : "ABox not marked complete!";
		}

		return isConsistent;
	}

	/**
	 * Checks if all the previous asserted clashes are resolved. If there is an unresolved _clash, the _clash will be set to the first such _clash found
	 * (selection is arbitrary). The _clash remains unchanged if all clashes are resolved. That is, the _clash might be non-null after this function even if all
	 * asserted clashes are This function is used when incremental deletion is disabled.
	 */
	private boolean checkAssertedClashes()
	{
		final Iterator<Clash> i = _assertedClashes.iterator();
		while (i.hasNext())
		{
			final Clash clash = i.next();
			final Node node = clash.getNode();
			final ATermAppl term = clash._args != null ? (ATermAppl) clash._args[0] : null;

			// check if _clash is resolved through deletions
			boolean resolved = true;
			switch (clash.getClashType())
			{
				case ATOMIC:
					final ATermAppl negation = ATermUtils.negate(term);
					resolved = !node.hasType(term) || !node.hasType(negation);
					break;
				case NOMINAL:
					resolved = !node.isSame(getNode(term));
					break;
				case INVALID_LITERAL:
					resolved = false;
					break;
				default:
					_logger.warning("Unexpected asserted _clash type: " + clash);
					break;
			}

			if (resolved)
				// discard resolved _clash
				i.remove();
			else
			{
				// this _clash is not resolved, no point in continuing
				setClash(clash);
				return false;
			}
		}

		return true;
	}

	/**
	 * Check the consistency of this ABox possibly after adding some type assertions. If <code>c_</code> is null then nothing is added to ABox (pure consistency
	 * test) and the individuals should be an empty collection. If <code>c_</code> is not null but <code>individuals</code> is empty, this is a satisfiability
	 * check for concept <code>c_</code> so a new individual will be added with type <code>c_</code>. If individuals is not empty, this means we will add type
	 * <code>c_</code> to each of the individuals in the collection and check the consistency.
	 * <p>
	 * The consistency checks will be done either on a copy of the ABox or its pseudo model depending on the situation. In either case this ABox will not be
	 * modified at all. After the consistency check lastCompletion points to the modified ABox.
	 *
	 * @param individuals
	 * @param c_
	 * @return true if consistent.
	 */
	private boolean isConsistent(final Collection<ATermAppl> individualsParam, final ATermAppl c_, final boolean cacheModel)
	{
		Collection<ATermAppl> individuals = individualsParam;
		ATermAppl c = c_;

		final Optional<Timer> timer = _kb.getTimers().startTimer("isConsistent");

		if (_logger.isLoggable(Level.FINE))
			if (c == null)
				_logger.fine("ABox consistency for " + individuals.size() + " individuals");
			else
			{
				final StringBuilder sb = new StringBuilder();
				sb.append("[");
				final Iterator<ATermAppl> it = individuals.iterator();
				for (int i = 0; i < 100 && it.hasNext(); i++)
				{
					if (i > 0)
						sb.append(", ");
					sb.append(ATermUtils.toString(it.next()));
				}
				if (it.hasNext())
					sb.append(", ...");
				sb.append("]");
				_logger.fine("Consistency " + ATermUtils.toString(c) + " for " + individuals.size() + " individuals " + sb);
			}

		final Expressivity expr = _kb.getExpressivityChecker().getExpressivityWith(c);

		// if c is null we are checking the consistency of this ABox as
		// it is and we will not add anything extra
		final boolean initialConsistencyCheck = c == null;

		final boolean emptyConsistencyCheck = initialConsistencyCheck && isEmpty();

		// if individuals is empty and we are not building the pseudo
		// model then this is concept satisfiability
		final boolean conceptSatisfiability = individuals.isEmpty() && (!initialConsistencyCheck || emptyConsistencyCheck);

		// Check if there are any nominals in the KB or nominal
		// reasoning is disabled
		final boolean hasNominal = expr.hasNominal() && !OpenlletOptions.USE_PSEUDO_NOMINALS;

		// Use empty model only if this is concept satisfiability for a KB
		// where there are no nominals
		final boolean canUseEmptyABox = conceptSatisfiability && !hasNominal;

		ATermAppl x = null;
		if (conceptSatisfiability)
		{
			x = ATermUtils.CONCEPT_SAT_IND;
			individuals = SetUtils.singleton(x);
		}

		if (emptyConsistencyCheck)
			c = ATermUtils.TOP;

		final ABox abox = canUseEmptyABox ? this.copy(x, false) : initialConsistencyCheck ? this : this.copy(x, true);

		for (final ATermAppl ind : individuals)
		{
			abox.setSyntacticUpdate(true);
			abox.addType(ind, c);
			abox.setSyntacticUpdate(false);
		}

		// synchronized (this) // We should not try to completion in the same time. -> only if parallel core is enable.
		{
			_logger.fine(() -> "Consistency check starts");

			final CompletionStrategy strategy = _kb.chooseStrategy(abox, expr);

			_logger.fine(() -> "Strategy: " + strategy.getClass().getName());

			_kb.getTimers().execute("complete", timers -> strategy.complete(expr));
		}

		final boolean consistent = !abox.isClosed();

		if (x != null && c != null && cacheModel)
			cache(abox.getIndividual(x), c, consistent);

		if (_logger.isLoggable(Level.FINE))
			_logger.fine("Consistent: " + consistent //
					+ " Time: " + timer.map(t -> t.getElapsed()).orElse(0L)//
					+ " Branches " + abox.getBranches().size()//
					+ " Tree depth: " + abox.getStats()._treeDepth//
					+ " Tree size: " + abox.getNodes().size()//
					+ " Restores " + abox.getStats()._globalRestores//
					+ " global " + abox.getStats()._localRestores//
					+ " local"// FIXME something missing here ?
					+ " Backtracks " + abox.getStats()._backtracks//
					+ " avg backjump " + abox.getStats()._backjumps / (double) abox.getStats()._backtracks);

		if (consistent)
		{
			if (initialConsistencyCheck && isEmpty())
				setComplete(true);
		}
		else
		{
			_lastClash = abox.getClash();
			_logger.fine(() -> "Clash: " + abox.getClash().detailedString());
			if (_doExplanation && OpenlletOptions.USE_TRACING)
			{
				if (individuals.size() == 1)
				{
					final ATermAppl ind = individuals.iterator().next();

					final ATermAppl tempAxiom = ATermUtils.makeTypeAtom(ind, c);
					final Set<ATermAppl> explanationSet = getExplanationSet();
					final boolean removed = explanationSet.remove(tempAxiom);
					if (!removed)
						if (_logger.isLoggable(Level.FINE))
							_logger.fine("Explanation set is missing an axiom.\n\tAxiom: " + tempAxiom + "\n\tExplantionSet: " + explanationSet);
				}
				if (_logger.isLoggable(Level.FINE))
				{
					final StringBuilder sb = new StringBuilder();
					for (final ATermAppl axiom : getExplanationSet())
					{
						sb.append("\n\t");
						sb.append(ATermUtils.toString(axiom));
					}
					_logger.fine("Explanation: " + sb);
				}
			}
		}

		_stats._consistencyCount++;

		if (_keepLastCompletion)
			_lastCompletion = abox;
		else
			_lastCompletion = null;

		timer.ifPresent(Timer::stop);

		return consistent;
	}

	/**
	 * Check the consistency of this ABox using the incremental consistency checking approach
	 */
	@Override
	public boolean isIncConsistent()
	{
		assert isComplete() : "Initial consistency check has not been performed!";

		final Optional<Timer> incT = _kb.getTimers().startTimer(IS_INC_CONSISTENT);
		final Optional<Timer> timer = _kb.getTimers().startTimer(IS_CONSISTENT);

		// throw away old information to let gc do its work
		_lastCompletion = null;

		_logger.fine("Consistency check starts");

		// currently there is only one incremental consistency _strategy
		final CompletionStrategy incStrategy = new SROIQIncStrategy(this);

		_logger.fine("Strategy: " + incStrategy.getClass().getName());

		// set _abox to not being complete
		setComplete(false);

		_kb.getTimers().execute("complete", timers -> incStrategy.complete(_kb.getExpressivityChecker().getExpressivity()));

		final boolean consistent = !isClosed();

		if (_logger.isLoggable(Level.FINE))
			_logger.fine("Consistent: " + consistent + " Tree depth: " + _stats._treeDepth + " Tree size: " + getNodes().size());

		if (!consistent)
		{
			_lastClash = getClash();
			if (_logger.isLoggable(Level.FINE))
				_logger.fine(getClash().detailedString());
		}

		_stats._consistencyCount++;

		_lastCompletion = this;

		timer.ifPresent(t -> t.stop());
		incT.ifPresent(t -> t.stop());

		// do not clear the _clash information

		return consistent;
	}

	@Override
	public EdgeList getInEdges(final ATerm x)
	{
		return getNode(x).getInEdges();
	}

	@Override
	public EdgeList getOutEdges(final ATerm x)
	{
		final Node node = getNode(x);
		if (node instanceof Literal)
			return new EdgeList();
		return ((Individual) node).getOutEdges();
	}

	@Override
	public Individual getIndividual(final ATerm x)
	{
		final Node o = _nodes.get(x);
		if (o instanceof Individual)
			return (Individual) o;
		return null;
	}

	@Override
	public Literal getLiteral(final ATerm x)
	{
		final Node o = _nodes.get(x);
		if (o instanceof Literal)
			return (Literal) o;
		return null;
	}

	@Override
	public Node getNode(final ATerm x)
	{
		return _nodes.get(x);
	}

	@Override
	public void addType(final ATermAppl x, final ATermAppl c)
	{
		final DependencySet ds = OpenlletOptions.USE_TRACING ? new DependencySet(ATermUtils.makeTypeAtom(x, c)) : DependencySet.INDEPENDENT;

		addType(x, c, ds);
	}

	@Override
	public void addType(final ATermAppl x, final ATermAppl cParam, final DependencySet dsParam)
	{
		ATermAppl c = cParam;
		DependencySet ds = dsParam;

		c = ATermUtils.normalize(c);

		// when a type is being added to
		// an ABox that has already been completed, the _branch
		// of the dependency set will automatically be set to
		// the _current _branch. We need to set it to the initial
		// _branch number to make sure that this type assertion
		// will not be removed during backtracking
		final int remember = _branchIndex;
		setBranchIndex(DependencySet.NO_BRANCH);

		Individual node = getIndividual(x);
		node.addType(c, ds, false);

		while (node.isMerged())
		{
			ds = ds.union(node.getMergeDependency(false), _doExplanation);
			node = (Individual) node.getMergedTo();
			node.addType(c, ds, !node.isMerged());
		}

		setBranchIndex(remember);
	}

	@Override
	public Edge addEdge(final ATermAppl p, final ATermAppl s, final ATermAppl o, final DependencySet dsParam)
	{
		DependencySet ds = dsParam;

		final Role role = getRole(p);
		Individual subj = getIndividual(s);
		Node obj = getNode(o);

		if (subj.isMerged() && obj.isMerged())
			return null;

		if (obj.isMerged())
		{
			obj.addInEdge(new DefaultEdge(role, subj, obj, ds));
			ds = ds.union(obj.getMergeDependency(true), true);
			ds = ds.copy(ds.max() + 1);
			obj = obj.getSame();
		}

		Edge edge = new DefaultEdge(role, subj, obj, ds);
		final Edge existingEdge = subj.getOutEdges().getExactEdge(subj, role, obj);
		if (existingEdge == null)
			subj.addOutEdge(edge);
		else
			if (!existingEdge.getDepends().isIndependent())
			{
				subj.removeEdge(existingEdge);
				subj.addOutEdge(edge);
			}

		if (subj.isMerged())
		{
			ds = ds.union(subj.getMergeDependency(true), true);
			ds = ds.copy(ds.max() + 1);
			subj = subj.getSame();
			edge = new DefaultEdge(role, subj, obj, ds);

			if (subj.getOutEdges().hasEdge(edge))
				return null;

			subj.addOutEdge(edge);
			obj.addInEdge(edge);
		}
		else
			if (existingEdge == null)
				obj.addInEdge(edge);
			else
				if (!existingEdge.getDepends().isIndependent())
				{
					obj.removeInEdge(existingEdge);
					obj.addInEdge(edge);
				}

		return edge;
	}

	/**
	 * Remove the given node from the node map which maps names to nodes. Does not remove the node from the node list or other nodes' edge lists.
	 *
	 * @param x is a node.
	 * @return true if something have been remove. false if there was nothing to remove.
	 */
	@Override
	public boolean removeNode(final ATermAppl x)
	{
		return _nodes.remove(x) != null;
	}

	@Override
	public void removeType(final ATermAppl x, final ATermAppl c)
	{
		getNode(x).removeType(ATermUtils.normalize(c));
	}

	@Override
	public Literal addLiteral(final DependencySet ds)
	{
		return createLiteral(ATermUtils.makeLiteral(createUniqueName(false)), ds);
	}

	/**
	 * Add a new literal to the ABox. Literal will be assigned a fresh unique name.
	 *
	 * @param dataValue A literal ATerm which should be constructed with one of ATermUtils.makeXXXLiteral functions
	 * @return Literal object that has been created
	 */
	@Override
	public Literal addLiteral(final ATermAppl dataValue)
	{
		final int remember = getBranchIndex();
		setBranchIndex(DependencySet.NO_BRANCH);

		final Literal lit = addLiteral(dataValue, DependencySet.INDEPENDENT);

		setBranchIndex(remember);

		return lit;
	}

	@Override
	public Literal addLiteral(final ATermAppl dataValue, final DependencySet ds)
	{
		if (dataValue == null || !ATermUtils.isLiteral(dataValue))
			throw new InternalReasonerException("Invalid value to create a literal. Value: " + dataValue);

		return createLiteral(dataValue, ds);
	}

	/**
	 * Helper function to add a literal.
	 *
	 * @param value The java object that represents the value of this literal
	 * @return
	 */
	private Literal createLiteral(final ATermAppl dataValue, final DependencySet ds)
	{
		ATermAppl name;
		/*
		 * No datatype means the literal is an anonymous variable created for a
		 * min cardinality or some values from restriction.
		 */
		if (ATermUtils.NO_DATATYPE.equals(dataValue.getArgument(ATermUtils.LIT_URI_INDEX)))
			name = dataValue;
		else
			try
			{
				name = getDatatypeReasoner().getCanonicalRepresentation(dataValue);
			}
			catch (final InvalidLiteralException e)
			{
				final String msg = format("Attempt to create an invalid literal (%s): %s", dataValue, e.getMessage());
				if (OpenlletOptions.INVALID_LITERAL_AS_INCONSISTENCY)
				{
					_logger.fine(msg);
					name = dataValue;
				}
				else
				{
					_logger.severe(msg);
					throw new InternalReasonerException(msg, e);
				}
			}
			catch (final UnrecognizedDatatypeException e)
			{
				final String msg = format("Attempt to create a literal with an unrecognized datatype (%s): %s", dataValue, e.getMessage());
				_logger.severe(msg);
				throw new InternalReasonerException(msg, e);
			}

		final Node node = getNode(name);
		if (node != null)
			if (node instanceof Literal)
			{

				if (((Literal) node).getValue() == null && OpenlletOptions.USE_COMPLETION_QUEUE)
				{
					// added for completion _queue
					final QueueElement newElement = new QueueElement(node);
					_completionQueue.add(newElement, NodeSelector.LITERAL);
				}

				if (getBranchIndex() >= 0 && OpenlletOptions.TRACK_BRANCH_EFFECTS)
					_branchEffects.add(getBranchIndex(), node.getName());

				return (Literal) node;
			}
			else
				throw new InternalReasonerException("Same term refers to both a literal and an _individual: " + name);

		final int remember = _branchIndex;
		setBranchIndex(DependencySet.NO_BRANCH);

		/*
		 * TODO Investigate the effects of storing asserted value
		 * The input version of the literal is not discarded, only the canonical
		 * versions are stored in the literal. This may cause problems in cases
		 * where the same value space object is presented in the _data in multiple
		 * forms.
		 */
		final Literal lit = new Literal(name, dataValue, this, ds);
		lit.addType(ATermUtils.TOP_LIT, ds);

		setBranchIndex(remember);

		_nodes.put(name, lit);
		_nodeList.add(name);

		if (lit.getValue() == null && OpenlletOptions.USE_COMPLETION_QUEUE)
		{
			// added for completion _queue
			final QueueElement newElement = new QueueElement(lit);
			_completionQueue.add(newElement, NodeSelector.LITERAL);
		}

		if (getBranchIndex() >= 0 && OpenlletOptions.TRACK_BRANCH_EFFECTS)
			_branchEffects.add(getBranchIndex(), lit.getName());

		return lit;
	}

	@Override
	public Individual addIndividual(final ATermAppl x, final DependencySet ds)
	{
		final Individual ind = addIndividual(x, null, ds);

		// update affected inds for this _branch
		if (getBranchIndex() >= 0 && OpenlletOptions.TRACK_BRANCH_EFFECTS)
			_branchEffects.add(getBranchIndex(), ind.getName());

		return ind;
	}

	@Override
	public Individual addFreshIndividual(final Individual parent, final DependencySet ds)
	{
		final boolean isNominal = parent == null;
		final ATermAppl name = createUniqueName(isNominal);
		final Individual ind = addIndividual(name, parent, ds);

		if (isNominal)
			ind.setNominalLevel(1);

		return ind;
	}

	private Individual addIndividual(final ATermAppl x, final Individual parent, final DependencySet ds)
	{
		if (_nodes.containsKey(x))
			throw new InternalReasonerException("adding a _node twice " + x);

		setChanged(true);

		final Individual n = new Individual(x, this, parent);

		_nodes.put(x, n);
		_nodeList.add(x);

		if (n.getDepth() > _stats._treeDepth)
		{
			_stats._treeDepth = n.getDepth();
			if (_logger.isLoggable(Level.FINER))
				_logger.finer("Depth: " + _stats._treeDepth + " Size: " + size());
		}

		//this must be performed after the _nodeList is updated as this call will update the completion queues
		n.addType(ATermUtils.TOP, ds);

		if (getBranchIndex() > 0 && OpenlletOptions.TRACK_BRANCH_EFFECTS)
			_branchEffects.add(getBranchIndex(), n.getName());

		return n;
	}

	@Override
	public void addSame(final ATermAppl x, final ATermAppl y)
	{
		final Individual ind1 = getIndividual(x);
		final Individual ind2 = getIndividual(y);

		// ind1.setSame(ind2, new DependencySet(explanationTable.getCurrent()));

		// ind1.setSame(ind2, DependencySet.INDEPENDENT);
		final ATermAppl sameAxiom = ATermUtils.makeSameAs(x, y);

		// update syntactic assertions - currently i do not add this to the
		// dependency _index
		// now, as it will be added during the actual merge when the completion
		// is performed
		if (OpenlletOptions.USE_INCREMENTAL_DELETION)
			_kb.getSyntacticAssertions().add(sameAxiom);

		final DependencySet ds = OpenlletOptions.USE_TRACING ? new DependencySet(sameAxiom) : DependencySet.INDEPENDENT;
		getToBeMerged().add(new NodeMerge(ind1, ind2, ds));
	}

	@Override
	public void addDifferent(final ATermAppl x, final ATermAppl y)
	{
		final Individual ind1 = getIndividual(x);
		final Individual ind2 = getIndividual(y);

		final ATermAppl diffAxiom = ATermUtils.makeDifferent(x, y);

		// update syntactic assertions - currently i do not add this to the
		// dependency _index
		// now, as it will simply be used during the completion _strategy
		if (OpenlletOptions.USE_INCREMENTAL_DELETION)
			_kb.getSyntacticAssertions().add(diffAxiom);

		// ind1.setDifferent(ind2, new
		// DependencySet(explanationTable.getCurrent()));
		final DependencySet ds = OpenlletOptions.USE_TRACING ? new DependencySet(diffAxiom) : DependencySet.INDEPENDENT;

		// Temporarily reset the _branch so that this assertion survives resets
		final int remember = _branchIndex;
		setBranchIndex(DependencySet.NO_BRANCH);

		ind1.setDifferent(ind2, ds);

		setBranchIndex(remember);
	}

	@Override
	public void addAllDifferent(final ATermList list)
	{
		final ATermAppl allDifferent = ATermUtils.makeAllDifferent(list);
		ATermList outer = list;
		while (!outer.isEmpty())
		{
			ATermList inner = outer.getNext();
			while (!inner.isEmpty())
			{
				final Individual ind1 = getIndividual(outer.getFirst());
				final Individual ind2 = getIndividual(inner.getFirst());

				// update syntactic assertions - currently i do not add this to the dependency index
				// now, as it will be added during the actual merge when the completion is performed
				if (OpenlletOptions.USE_INCREMENTAL_DELETION)
					_kb.getSyntacticAssertions().add(allDifferent);

				final DependencySet ds = OpenlletOptions.USE_TRACING ? new DependencySet(allDifferent) : DependencySet.INDEPENDENT;

				final int remember = _branchIndex;
				setBranchIndex(DependencySet.NO_BRANCH);

				ind1.setDifferent(ind2, ds);

				setBranchIndex(remember);

				inner = inner.getNext();
			}
			outer = outer.getNext();
		}
	}

	@Override
	public boolean isNode(final ATerm x)
	{
		return getNode(x) != null;
	}

	@Override
	final public ATermAppl createUniqueName(final boolean isNominal)
	{
		_anonCount++;

		final ATermAppl name = isNominal ? ATermUtils.makeAnonNominal(_anonCount) : ATermUtils.makeAnon(_anonCount);

		return name;
	}

	@Override
	final public Map<ATermAppl, Node> getNodes()
	{
		return _nodes;
	}

	@Override
	final public List<ATermAppl> getNodeNames()
	{
		return _nodeList;
	}

	@Override
	public String toString()
	{
		return "[size: " + _nodes.size() + " freeMemory: " + Runtime.getRuntime().freeMemory() / 1000000.0 + "mb]";
	}

	/**
	 * @return Returns the datatype reasoner.
	 */
	@Override
	public DatatypeReasoner getDatatypeReasoner()
	{
		return _dtReasoner;
	}

	/**
	 * @return Returns the isComplete.
	 */
	@Override
	public boolean isComplete()
	{
		return _isComplete;
	}

	@Override
	public void setComplete(final boolean isComplete)
	{
		_isComplete = isComplete;
	}

	@Override
	public boolean isClosed()
	{
		return !OpenlletOptions.SATURATE_TABLEAU && _initialized && _clash != null;
	}

	@Override
	public Clash getClash()
	{
		return _clash;
	}

	@Override
	public void setClash(final Clash clash)
	{
		if (clash != null)
		{
			if (_logger.isLoggable(Level.FINER))
			{
				_logger.finer("CLSH: " + clash);
				if (clash.getDepends().max() > _branchIndex && _branchIndex != -1)
					_logger.severe("Invalid _clash dependency " + clash + " > " + _branchIndex);
			}

			if (_branchIndex == DependencySet.NO_BRANCH && clash.getDepends().getBranch() == DependencySet.NO_BRANCH)
				_assertedClashes.add(clash);

			if (_clash != null)
			{
				_logger.finer(() -> "Clash was already set \nExisting: " + _clash + "\nNew     : " + clash);

				if (_clash.getDepends().max() < clash.getDepends().max())
					return;
			}
		}

		_clash = clash;
		// CHW - added for incremental deletions
		if (OpenlletOptions.USE_INCREMENTAL_DELETION)
			_kb.getDependencyIndex().setClashDependencies(_clash);

	}

	/**
	 * @return Returns the kb.
	 */
	@Override
	public KnowledgeBase getKB()
	{
		return _kb;
	}

	/**
	 * Convenience function to get the named role.
	 */
	@Override
	public Role getRole(final ATerm r)
	{
		return _kb.getRole(r);
	}

	/**
	 * Return the RBox
	 */
	@Override
	public RBox getRBox()
	{
		return _kb.getRBox();
	}

	/**
	 * Return the TBox
	 */
	@Override
	public TBox getTBox()
	{
		return _kb.getTBox();
	}

	/**
	 * Return the _current branch number. Branches are created when a non-deterministic rule, e.g. disjunction or max rule, is being applied.
	 *
	 * @return Returns the branch.
	 */
	@Override
	public int getBranchIndex()
	{
		return _branchIndex;
	}

	@Override
	public void setBranchIndex(final int branch)
	{
		_branchIndex = branch;
	}

	@Override
	public void incrementBranch()
	{

		if (OpenlletOptions.USE_COMPLETION_QUEUE)
			_completionQueue.incrementBranch(_branchIndex);

		_branchIndex++;
	}

	/**
	 * Check if the ABox is ready to be completed.
	 *
	 * @return Returns the initialized.
	 */
	@Override
	public boolean isInitialized()
	{
		return _initialized;
	}

	@Override
	public void setInitialized(final boolean initialized)
	{
		_initialized = initialized;
	}

	/**
	 * Checks if the clashExplanation is turned on.
	 *
	 * @return Returns the doExplanation.
	 */
	@Override
	final public boolean doExplanation()
	{
		return _doExplanation;
	}

	@Override
	public void setDoExplanation(final boolean doExplanation)
	{
		_doExplanation = doExplanation;
	}

	@Override
	public void setExplanation(final DependencySet ds)
	{
		_lastClash = Clash.unexplained(null, ds);
	}

	@Override
	public String getExplanation()
	{
		if (_lastClash == null)
			return "No inconsistency was found! There is no clashExplanation generated.";
		else
			return _lastClash.detailedString();
	}

	@Override
	public Set<ATermAppl> getExplanationSet()
	{
		if (_lastClash == null)
			throw new OpenError("No clashExplanation was generated!");

		return _lastClash.getDepends().getExplain();
	}

	@Override
	public BranchEffectTracker getBranchEffectTracker()
	{
		if (_branchEffects == null)
			throw new NullPointerException();

		return _branchEffects;
	}

	/**
	 * Returns the branches.
	 */
	@Override
	public List<Branch> getBranches()
	{
		return _branches;
	}

	@Override
	public IncrementalChangeTracker getIncrementalChangeTracker()
	{
		if (_incChangeTracker == null)
			throw new NullPointerException();

		return _incChangeTracker;
	}

	@Override
	public IndividualIterator getIndIterator()
	{
		return new IndividualIterator(this);
	}

	/**
	 * Validate all the edges in the ABox nodes. Used to find bugs in the copy and detach/attach functions.
	 */
	@Override
	public void validate()
	{
		if (!OpenlletOptions.VALIDATE_ABOX)
			return;
		System.out.print("VALIDATING...");
		final Iterator<Individual> n = getIndIterator();
		while (n.hasNext())
		{
			final Individual node = n.next();
			if (node.isPruned())
				continue;
			validate(node);
		}
	}

	@Override
	public void validateTypes(final Individual node, final List<ATermAppl> negatedTypes)
	{
		for (int i = 0, n = negatedTypes.size(); i < n; i++)
		{
			final ATermAppl a = negatedTypes.get(i);
			if (a.getArity() == 0)
				continue;
			final ATermAppl notA = (ATermAppl) a.getArgument(0);

			if (node.hasType(notA))
			{
				if (!node.hasType(a))
					throw new InternalReasonerException("Invalid type found: " + node + " " + " " + a + " " + node.debugString() + " " + node._depends);
				throw new InternalReasonerException("Clash found: " + node + " " + a + " " + node.debugString() + " " + node._depends);
			}
		}
	}

	@Override
	public void validate(final Individual node)
	{
		validateTypes(node, node.getTypes(Node.ATOM));
		validateTypes(node, node.getTypes(Node.SOME));
		validateTypes(node, node.getTypes(Node.OR));
		validateTypes(node, node.getTypes(Node.MAX));

		if (!node.isRoot())
		{
			final EdgeList preds = node.getInEdges();
			final boolean validPred = preds.size() == 1 || preds.size() == 2 && preds.hasEdgeFrom(node);
			if (!validPred)
				throw new InternalReasonerException("Invalid blockable node: " + node + " " + node.getInEdges());

		}
		else
			if (node.isNominal())
			{
				final ATermAppl nominal = ATermUtils.makeValue(node.getName());
				if (!ATermUtils.isAnonNominal(node.getName()) && !node.hasType(nominal))
					throw new InternalReasonerException("Invalid nominal node: " + node + " " + node.getTypes());
			}

		for (final ATermAppl c : node.getDepends().keySet())
		{
			final DependencySet ds = node.getDepends(c);
			if (ds.max() > _branchIndex || !OpenlletOptions.USE_SMART_RESTORE && ds.getBranch() > _branchIndex)
				throw new InternalReasonerException("Invalid ds found: " + node + " " + c + " " + ds + " " + _branchIndex);
		}
		for (final Node ind : node.getDifferents())
		{
			final DependencySet ds = node.getDifferenceDependency(ind);
			if (ds.max() > _branchIndex || ds.getBranch() > _branchIndex)
				throw new InternalReasonerException("Invalid ds: " + node + " != " + ind + " " + ds);
			if (ind.getDifferenceDependency(node) == null)
				throw new InternalReasonerException("Invalid difference: " + node + " != " + ind + " " + ds);
		}
		EdgeList edges = node.getOutEdges();
		for (int e = 0; e < edges.size(); e++)
		{
			final Edge edge = edges.get(e);
			final Node succ = edge.getTo();
			if (_nodes.get(succ.getName()) != succ)
				throw new InternalReasonerException("Invalid edge to a non-existing node: " + edge + " " + _nodes.get(succ.getName()) + "(" + _nodes.get(succ.getName()).hashCode() + ")" + succ + "(" + succ.hashCode() + ")");
			if (!succ.getInEdges().hasEdge(edge))
				throw new InternalReasonerException("Invalid edge: " + edge);
			if (succ.isMerged())
				throw new InternalReasonerException("Invalid edge to a removed node: " + edge + " " + succ.isMerged());
			final DependencySet ds = edge.getDepends();
			if (ds.max() > _branchIndex || ds.getBranch() > _branchIndex)
				throw new InternalReasonerException("Invalid ds: " + edge + " " + ds);
			final EdgeList allEdges = node.getEdgesTo(succ);
			if (allEdges.getRoles().size() != allEdges.size())
				throw new InternalReasonerException("Duplicate edges: " + allEdges);
		}
		edges = node.getInEdges();
		for (int e = 0; e < edges.size(); e++)
		{
			final Edge edge = edges.get(e);
			final DependencySet ds = edge.getDepends();
			if (ds.max() > _branchIndex || ds.getBranch() > _branchIndex)
				throw new InternalReasonerException("Invalid ds: " + edge + " " + ds);
		}
	}

	/**
	 * Print the ABox as a completion tree (child nodes are indented).
	 *
	 * @param stream is where to print
	 */
	@Override
	public void printTree(final PrintStream stream)
	{
		if (!OpenlletOptions.PRINT_ABOX)
			return;
		stream.println("PRINTING... " + DependencySet.INDEPENDENT);
		final Iterator<Node> n = _nodes.values().iterator();
		while (n.hasNext())
		{
			final Node node = n.next();
			if (!node.isRoot() || node instanceof Literal)
				continue;
			printNode(stream, (Individual) node, new HashSet<Individual>(), "   ");
		}
	}

	/**
	 * Print the node in the completion tree.
	 *
	 * @param stream
	 * @param node
	 * @param printed
	 * @param indentLvl
	 */
	private void printNode(final PrintStream stream, final Individual node, final Set<Individual> printed, final String indentLvl)
	{
		final boolean printOnlyName = node.isNominal() && !printed.isEmpty();

		stream.print(node);
		if (!printed.add(node))
		{
			stream.println();
			return;
		}
		if (node.isMerged())
		{
			stream.println(" -> " + node.getMergedTo() + " " + node.getMergeDependency(false));
			return;
		}
		else
			if (node.isPruned())
				throw new InternalReasonerException("Pruned node: " + node);

		stream.print(" = ");
		for (int i = 0; i < Node.TYPES; i++)
			for (final ATermAppl c : node.getTypes(i))
			{
				stream.print(ATermUtils.toString(c));
				stream.print(", ");
			}
		stream.println(node.getDifferents());

		if (printOnlyName)
			return;

		final String indent = indentLvl + "  ";
		final Iterator<Edge> i = node.getOutEdges().iterator();
		while (i.hasNext())
		{
			final Edge edge = i.next();
			final Node succ = edge.getTo();
			final EdgeList edges = node.getEdgesTo(succ);

			stream.print(indent + "[");
			for (int e = 0; e < edges.size(); e++)
			{
				if (e > 0)
					stream.print(", ");
				stream.print(edges.get(e).getRole());
			}
			stream.print("] ");
			if (succ instanceof Individual)
				printNode(stream, (Individual) succ, printed, indent);
			else
				stream.println(" (Literal) " + ATermUtils.toString(succ.getName()) + " " + ATermUtils.toString(succ.getTypes()));
		}
	}

	@Override
	public Clash getLastClash()
	{
		return _lastClash;
	}

	@Override
	public ABox getLastCompletion()
	{
		return _lastCompletion;
	}

	@Override
	public boolean isKeepLastCompletion()
	{
		return _keepLastCompletion;
	}

	@Override
	public void setKeepLastCompletion(final boolean keepLastCompletion)
	{
		_keepLastCompletion = keepLastCompletion;
	}

	@Override
	public int size()
	{
		return _nodes.size();
	}

	@Override
	public boolean isEmpty()
	{
		return _nodes.isEmpty();
	}

	@Override
	public void setLastCompletion(final ABox comp)
	{
		_lastCompletion = comp;
	}

	@Override
	public void setSyntacticUpdate(final boolean val)
	{
		_syntacticUpdate = val;
	}

	@Override
	public boolean isSyntacticUpdate()
	{
		return _syntacticUpdate;
	}

	@Override
	public CompletionQueue getCompletionQueue()
	{
		return _completionQueue;
	}

	/**
	 * Reset the ABox to contain only asserted information. Any ABox assertion added by tableau rules will be removed.
	 */
	@Override
	public void reset()
	{
		if (!isComplete())
			return;

		setComplete(false);

		final Iterator<ATermAppl> i = _nodeList.iterator();
		while (i.hasNext())
		{
			final ATermAppl nodeName = i.next();
			final Node node = _nodes.get(nodeName);
			if (!node.isRootNominal())
			{
				i.remove();
				_nodes.remove(nodeName);
			}
			else
				node.reset(false);
		}

		setComplete(false);
		setInitialized(false);
		// clear the _clash. we can safely clear the _clash because
		// either this was an asserted _clash and already stored in the
		// _assertedClashes (and will be verified before consistency change)
		// or this was a _clash that occurred during completion and will
		// reoccur (if no already resolved) since we will run the tableau
		// completion again
		setClash(null);

		setBranchIndex(DependencySet.NO_BRANCH);
		_branches.clear();
		_disjBranchStats.clear();
		_rulesNotApplied = true;
	}

	@Override
	public void resetQueue()
	{
		for (final Node node : _nodes.values())
			node.reset(true);
	}

	@Override
	public int setAnonCount(final int anonCount)
	{
		return _anonCount = anonCount;
	}

	/**
	 * @return the anonCount
	 */
	@Override
	public int getAnonCount()
	{
		return _anonCount;
	}

	/**
	 * @return the disjBranchStats
	 */
	@Override
	public Map<ATermAppl, int[]> getDisjBranchStats()
	{
		return _disjBranchStats;
	}

	@Override
	public void setChanged(final boolean changed)
	{
		_changed = changed;
	}

	/**
	 * @return the changed
	 */
	@Override
	public boolean isChanged()
	{
		return _changed;
	}

	/**
	 * @return the toBeMerged
	 */
	@Override
	public List<NodeMerge> getToBeMerged()
	{
		return _toBeMerged;
	}
}
