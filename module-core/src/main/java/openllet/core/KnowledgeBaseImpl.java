// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC.
// <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms
// of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com
//
// ---
// Portions Copyright (c) 2003 Ron Alford, Mike Grove, Bijan Parsia, Evren Sirin
// Alford, Grove, Parsia, Sirin parts of this source code are available under
// the terms of the MIT License.
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

package openllet.core;

import static java.lang.String.format;
import static openllet.core.utils.TermFactory.BOTTOM;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.aterm.ATermList;
import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.abox.ABoxImpl;
import openllet.core.boxes.abox.Edge;
import openllet.core.boxes.abox.Individual;
import openllet.core.boxes.abox.IndividualIterator;
import openllet.core.boxes.abox.Literal;
import openllet.core.boxes.abox.Node;
import openllet.core.boxes.rbox.RBox;
import openllet.core.boxes.rbox.RBoxImpl;
import openllet.core.boxes.rbox.Role;
import openllet.core.boxes.tbox.TBox;
import openllet.core.boxes.tbox.TBoxFactory;
import openllet.core.datatypes.exceptions.InvalidLiteralException;
import openllet.core.datatypes.exceptions.UnrecognizedDatatypeException;
import openllet.core.exceptions.InconsistentOntologyException;
import openllet.core.exceptions.UnsupportedFeatureException;
import openllet.core.expressivity.Expressivity;
import openllet.core.expressivity.ExpressivityChecker;
import openllet.core.knowledge.Base;
import openllet.core.knowledge.DatatypeVisitor;
import openllet.core.knowledge.FullyDefinedClassVisitor;
import openllet.core.rules.ContinuousRulesStrategy;
import openllet.core.rules.UsableRuleFilter;
import openllet.core.rules.model.AtomDVariable;
import openllet.core.rules.model.AtomIObject;
import openllet.core.rules.model.AtomIVariable;
import openllet.core.rules.model.ClassAtom;
import openllet.core.rules.model.DatavaluedPropertyAtom;
import openllet.core.rules.model.IndividualPropertyAtom;
import openllet.core.rules.model.Rule;
import openllet.core.rules.model.RuleAtom;
import openllet.core.rules.model.SameIndividualAtom;
import openllet.core.tableau.branch.Branch;
import openllet.core.tableau.completion.CompletionStrategy;
import openllet.core.tableau.completion.EmptySRIQStrategy;
import openllet.core.tableau.completion.SROIQStrategy;
import openllet.core.tableau.completion.incremental.DependencyIndex;
import openllet.core.tableau.completion.incremental.IncrementalRestore;
import openllet.core.taxonomy.Taxonomy;
import openllet.core.taxonomy.TaxonomyBuilder;
import openllet.core.taxonomy.TaxonomyNode;
import openllet.core.taxonomy.printer.ClassTreePrinter;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.AnnotationClasses;
import openllet.core.utils.Bool;
import openllet.core.utils.MultiMapUtils;
import openllet.core.utils.MultiValueMap;
import openllet.core.utils.SetUtils;
import openllet.core.utils.SizeEstimate;
import openllet.core.utils.Timer;
import openllet.core.utils.Timers;
import openllet.core.utils.progress.ProgressMonitor;
import openllet.shared.tools.Log;

public class KnowledgeBaseImpl implements KnowledgeBase
{
	public final static Logger _logger = Log.getLogger(KnowledgeBaseImpl.class);

	static
	{
		// Ensure memory profiler will first process ATermFactory which makes it easier to analyze the results
		ATermUtils.getFactory();
	}

	/**
	 * Timers used in various different parts of KB. There may be many different _timers created here depending on the level of debugging or application
	 * requirements. However, there are three major timers that are guaranteed to exist.
	 * <ul>
	 * <li><b>main</b> - This is the main timer that exists in any Timers objects. All the other _timers defined in here will have this timer as its dependant
	 * so setting a timeout on this timer will put a limit on every operation done inside KB.</li>
	 * <li><b>preprocessing</b> - This is the operation where TBox creation, absorbtion and normalization is done. It also includes computing hierarchy of
	 * properties in RBox and merging the _individuals in ABox if there are explicit sameAs assertions.</li>
	 * <li><b>consistency</b> - This is the timer for ABox consistency check. Putting a timeout will mean that any single consistency check should be completed
	 * in a certain amount of time.</li>
	 * </ul>
	 */
	public final Timers _timers;

	protected final MultiValueMap<AssertionType, ATermAppl> _aboxAssertions = new MultiValueMap<>();
	private final Set<ATermAppl> _individuals = SetUtils.create();
	private final Map<ATermAppl, Map<ATermAppl, Set<ATermAppl>>> _annotations;

	@Override
	public Map<ATermAppl, Map<ATermAppl, Set<ATermAppl>>> getAnnotations()
	{
		return _annotations;
	}

	private final Map<ATermAppl, Set<ATermAppl>> _instances = new ConcurrentHashMap<>();

	@Override
	public Map<ATermAppl, Set<ATermAppl>> getInstances()
	{
		return _instances;
	}

	private final FullyDefinedClassVisitor _fullyDefinedVisitor = new FullyDefinedClassVisitor()
	{

		@Override
		public boolean isProperty(final ATerm p)
		{
			return KnowledgeBaseImpl.this.isProperty(p);
		}

		@Override
		public boolean isDatatype(final ATermAppl p)
		{
			return KnowledgeBaseImpl.this.isDatatype(p);
		}

		@Override
		public TBox getTBox()
		{
			return _tbox;
		}

		@Override
		public Set<ATermAppl> getIndividuals()
		{
			return _individuals;
		}
	};
	private final DatatypeVisitor _datatypeVisitor = new DatatypeVisitor()
	{
		@Override
		public ABox getABox()
		{
			return _abox;
		}

		@Override
		public TBox getTBox()
		{
			return _tbox;
		}

		@Override
		public RBox getRBox()
		{
			return _rbox;
		}

		@Override
		public Timers getTimers()
		{
			return _timers;
		}
	};

	@Override
	public ProgressMonitor getBuilderProgressMonitor()
	{
		return _builderProgressMonitor;
	}

	@Override
	public void setBuilderProgressMonitor(final ProgressMonitor builderProgressMonitor)
	{
		_builderProgressMonitor = builderProgressMonitor;
	}

	@Override
	public FullyDefinedClassVisitor getFullyDefinedVisitor()
	{
		return _fullyDefinedVisitor;
	}

	@Override
	public DatatypeVisitor getDatatypeVisitor()
	{
		return _datatypeVisitor;
	}

	protected volatile ABox _abox;
	protected volatile TBox _tbox;
	protected volatile RBox _rbox;

	protected volatile Optional<TaxonomyBuilder> _builder = Optional.empty();

	@Override
	public Optional<TaxonomyBuilder> getOptTaxonomyBuilder()
	{
		return _builder;
	}

	@Override
	public void setOptTaxonomyBuilder(final Optional<TaxonomyBuilder> builder)
	{
		_builder = builder;
	}

	protected volatile EnumSet<ChangeType> _changes;

	@Override
	public EnumSet<ChangeType> getChanges()
	{
		return _changes;
	}

	public void setChanges(final EnumSet<ChangeType> changes)
	{
		_changes = changes;
	}

	protected volatile boolean _canUseIncConsistency = false;

	protected volatile EnumSet<ReasoningState> _state = EnumSet.noneOf(ReasoningState.class);
	private volatile boolean _consistent = false;
	private volatile boolean _explainOnlyInconsistency = false;
	private volatile ProgressMonitor _builderProgressMonitor;
	private volatile SizeEstimate _estimate;
	private volatile ExpressivityChecker _expChecker;

	@Override
	public ExpressivityChecker getExpChecker()
	{
		return _expChecker;
	}

	public void setExpChecker(final ExpressivityChecker expChecker)
	{
		_expChecker = expChecker;
	}

	/**
	 * Rules added to this KB. The key is the asserted rule,
	 */
	private final Map<Rule, Rule> _rules = new HashMap<>(); // All operations are atomic (and we must allow null normalized rules).

	/** Structure for tracking which assertions are deleted */
	private final Set<ATermAppl> _deletedAssertions = SetUtils.create();

	/** set of syntactic assertions */
	private final Set<ATermAppl> _syntacticAssertions = SetUtils.create();

	/** Index used for abox deletions */
	private volatile DependencyIndex _dependencyIndex;

	/**
	 * The state of KB w.r.t. reasoning. The state is not valid if KB is changed, i.e. !changes.isEmpty(). These states are added in the _order CONSISTENCY <
	 * CLASSIFY < REALIZE when the corresponding functions are called. If KB is modified after classification, calling prepare might remove CONSISTENCY but
	 * leave CLASSIFY.
	 */
	protected enum ReasoningState
	{
		CONSISTENCY, CLASSIFY, REALIZE
	}

	public enum AssertionType
	{
		TYPE, OBJ_ROLE, DATA_ROLE
	}

	public KnowledgeBaseImpl()
	{
		clear();

		_timers = new Timers();
		_timers.createTimer("preprocessing");
		_timers.createTimer("consistency");
		_timers.createTimer("complete");
		_state = EnumSet.noneOf(ReasoningState.class);

		if (OpenlletOptions.USE_INCREMENTAL_DELETION)
			_dependencyIndex = new DependencyIndex(this);

		_annotations = new ConcurrentHashMap<>();
	}

	/**
	 * Create a KB based on an existing one. New KB has a copy of the ABox but TBox and RBox is openllet.shared.hash between two.
	 *
	 * @param kb
	 */
	protected KnowledgeBaseImpl(final KnowledgeBaseImpl kb, final boolean emptyABox)
	{
		_timers = kb._timers;
		_tbox = kb._tbox;
		_rbox = kb._rbox;
		_rules.clear();
		_rules.putAll(kb._rules);

		_annotations = kb._annotations;

		_expChecker = new ExpressivityChecker(this, kb.getExpressivity());

		_changes = kb._changes.clone();

		if (OpenlletOptions.USE_INCREMENTAL_DELETION)
			_dependencyIndex = new DependencyIndex(this);

		if (emptyABox)
		{
			_abox = new ABoxImpl(this);

			_instances.clear();

			// even though we don't copy the _individuals over to the new KB
			// we should still create _individuals for the
			for (final ATermAppl nominal : kb.getExpressivity().getNominals())
				addIndividual(nominal);
		}
		else
		{
			_abox = kb._abox.copy(this);

			if (OpenlletOptions.KEEP_ABOX_ASSERTIONS)
				for (final AssertionType assertionType : AssertionType.values())
				{
					final Set<ATermAppl> assertions = kb._aboxAssertions.get(assertionType);
					if (!assertions.isEmpty())
						_aboxAssertions.put(assertionType, SetUtils.create(assertions));
				}

			_individuals.addAll(kb._individuals);
			_instances.clear();
			_instances.putAll(kb._instances);

			_deletedAssertions.addAll(kb.getDeletedAssertions()); // copy deleted assertions
			_syntacticAssertions.addAll(kb._syntacticAssertions); // copy syntactic assertions

			if (OpenlletOptions.USE_INCREMENTAL_CONSISTENCY && OpenlletOptions.USE_INCREMENTAL_DELETION)
				_dependencyIndex = new DependencyIndex(this, kb._dependencyIndex); // copy the dependency _index
		}

		if (kb.isConsistencyDone())
		{
			prepare();

			_state = EnumSet.of(ReasoningState.CONSISTENCY);
			_consistent = kb._consistent;

			_abox.setComplete(true);

			_estimate = new SizeEstimate(this);
		}
		else
			_state = EnumSet.noneOf(ReasoningState.class);
	}

	@Override
	public KnowledgeBase getKnowledgeBase()
	{
		return this;
	}

	@Override
	public Timers getTimers()
	{
		return _timers;
	}

	@Override
	public ABox getABox()
	{
		return _abox;
	}

	@Override
	public TBox getTBox()
	{
		return _tbox;
	}

	@Override
	public RBox getRBox()
	{
		return _rbox;
	}

	@Override
	public TaxonomyBuilder getBuilder()
	{
		return getTaxonomyBuilder();
	}

	@Override
	public Logger getLogger()
	{
		return _logger;
	}

	@Override
	public Expressivity getExpressivity()
	{
		return getExpressivityChecker().getExpressivity();
	}

	@Override
	public ExpressivityChecker getExpressivityChecker()
	{
		// if we can use incremental reasoning then expressivity has been
		// updated as only the ABox was incrementally changed
		if (canUseIncConsistency())
			return _expChecker;

		prepare();

		return _expChecker;
	}

	@Override
	public void clear()
	{
		if (_abox == null)
			_abox = new ABoxImpl(this);
		else
		{
			final boolean doExplanation = _abox.doExplanation();
			final boolean keepLastCompletion = _abox.isKeepLastCompletion();
			_abox = new ABoxImpl(this);
			_abox.setDoExplanation(doExplanation);
			_abox.setKeepLastCompletion(keepLastCompletion);
		}

		_tbox = TBoxFactory.createTBox(this);
		_rbox = new RBoxImpl();
		_rules.clear(); // All operations are atomic (and we must allow null normalized rules).
		_expChecker = new ExpressivityChecker(this);
		_individuals.clear();
		_aboxAssertions.clear();
		_instances.clear();
		_builder = Optional.empty();

		_state.clear();
		_changes = EnumSet.of(ChangeType.ABOX_ADD, ChangeType.TBOX_ADD, ChangeType.RBOX_ADD);
	}

	@Override
	public void clearABox()
	{
		_aboxAssertions.clear();

		_annotations.clear();

		if (OpenlletOptions.USE_INCREMENTAL_DELETION)
		{
			_deletedAssertions.clear();
			_syntacticAssertions.clear();
			_dependencyIndex = new DependencyIndex(this);
		}

		_abox = new ABoxImpl(this, true); // copy abox & cache.

		_individuals.clear();

		_changes = EnumSet.of(ChangeType.ABOX_DEL);

		prepare();

		// even though we don't copy the _individuals over to the new KB
		// we should still create _individuals for the
		for (final ATermAppl nominal : getExpressivity().getNominals())
			addIndividual(nominal);
	}

	/**
	 * Create a copy of this KB. Depending on the value of <code>emptyABox</code> either a completely new copy of ABox will be created or the new KB will have
	 * an empty ABox. If <code>emptyABox</code> parameter is true but the original KB contains nominals in its RBox or TBox the new KB will have the definition
	 * of those _individuals (but not ) In either case, the new KB will point to the same RBox and TBox so changing one KB's RBox or TBox will affect other.
	 *
	 * @param emptyABox If <code>true</code> ABox is not copied to the new KB
	 * @return A copy of this KB
	 */
	@Override
	public KnowledgeBase copy(final boolean emptyABox)
	{
		return new KnowledgeBaseImpl(this, emptyABox);
	}

	@Override
	public void addClass(final ATermAppl c)
	{
		if (null == c || c.equals(ATermUtils.TOP) || ATermUtils.isComplexClass(c))
			return;

		final boolean added = _tbox.addClass(c);

		if (added)
		{
			_changes.add(ChangeType.TBOX_ADD);

			_logger.finer(() -> "class " + c);
		}
	}

	@Override
	public void addSubClass(final ATermAppl sub, final ATermAppl sup)
	{
		if (null == sub || null == sup || sub.equals(sup))
			return;

		_changes.add(ChangeType.TBOX_ADD);

		_tbox.addAxiom(ATermUtils.makeSub(sub, sup));

		_logger.finer(() -> "sub-class " + sub + " " + sup);
	}

	@Override
	public void addEquivalentClass(final ATermAppl c1, final ATermAppl c2)
	{
		if (null == c1 || null == c2 || c1.equals(c2))
			return;

		_changes.add(ChangeType.TBOX_ADD);

		_tbox.addAxiom(ATermUtils.makeEqClasses(c1, c2));

		_logger.finer(() -> "eq-class " + c1 + " " + c2);
	}

	@Override
	public void addKey(final ATermAppl c, final Set<ATermAppl> properties)
	{
		if (null == c || null == properties)
			return;

		int varId = 0;
		final List<RuleAtom> head = new ArrayList<>();
		final List<RuleAtom> body = new ArrayList<>();

		final AtomIVariable x = new AtomIVariable("x");
		final AtomIVariable y = new AtomIVariable("y");

		head.add(new SameIndividualAtom(x, y));

		// Process the body
		// First add the property atom pairs for each property
		for (final ATermAppl property : properties)
		{
			if (isObjectProperty(property))
			{
				final AtomIVariable z = new AtomIVariable("z" + varId);
				body.add(new IndividualPropertyAtom(property, x, z));
				body.add(new IndividualPropertyAtom(property, y, z));
			}
			else
				if (isDatatypeProperty(property))
				{
					final AtomDVariable z = new AtomDVariable("z" + varId);
					body.add(new DatavaluedPropertyAtom(property, x, z));
					body.add(new DatavaluedPropertyAtom(property, y, z));
				}

			varId++;
		}

		// Then add the class atoms for the two subject variables
		body.add(new ClassAtom(c, x));
		body.add(new ClassAtom(c, y));

		addRule(new Rule(head, body));
	}

	@Override
	public void addDisjointClasses(final ATermList classes)
	{
		if (null == classes)
			return;

		_changes.add(ChangeType.TBOX_ADD);

		_tbox.addAxiom(ATermUtils.makeDisjoints(classes));

		_logger.finer(() -> "disjoints " + classes);
	}

	@Override
	public void addDisjointClasses(final List<ATermAppl> classes)
	{
		if (null == classes)
			return;

		addDisjointClasses(ATermUtils.toSet(classes));
	}

	@Override
	public void addDisjointClass(final ATermAppl c1, final ATermAppl c2)
	{
		if (null == c1 || null == c2)
			return;

		_changes.add(ChangeType.TBOX_ADD);

		_tbox.addAxiom(ATermUtils.makeDisjoint(c1, c2));

		_logger.finer(() -> "disjoint " + c1 + " " + c2);
	}

	@Override
	public void addComplementClass(final ATermAppl c1, final ATermAppl c2)
	{
		if (null == c1 || null == c2)
			return;

		_changes.add(ChangeType.TBOX_ADD);
		final ATermAppl notC2 = ATermUtils.makeNot(c2);

		if (c1.equals(notC2))
			return;

		_tbox.addAxiom(ATermUtils.makeEqClasses(c1, notC2));

		_logger.finer(() -> "complement " + c1 + " " + c2);
	}

	@Override
	public Individual addIndividual(final ATermAppl i)
	{
		if (null == i)
			return null;

		final Node node = _abox.getNode(i);
		if (node != null)
		{
			if (node instanceof Literal)
				throw new UnsupportedFeatureException("Trying to use a literal as an _individual: " + ATermUtils.toString(i));

			return (Individual) node;
		}
		else
			if (ATermUtils.isLiteral(i))
				throw new UnsupportedFeatureException("Trying to use a literal as an _individual: " + ATermUtils.toString(i));

		final int remember = _abox.getBranchIndex();
		_abox.setBranchIndex(DependencySet.NO_BRANCH);

		_abox.setSyntacticUpdate(true);
		final Individual ind = _abox.addIndividual(i, DependencySet.INDEPENDENT);
		_individuals.add(i);

		_logger.finer(() -> "individual " + i);

		_abox.setSyntacticUpdate(false);

		if (!OpenlletOptions.USE_PSEUDO_NOMINALS)
		{
			// add value(x) for nominal _node but do not apply UC yet
			// because it might not be complete. it will be added
			// by CompletionStrategy.initialize()
			final ATermAppl nominal = ATermUtils.makeValue(i);
			_abox.addType(i, nominal, DependencySet.INDEPENDENT);
		}

		// set addition flag
		_changes.add(ChangeType.ABOX_ADD);

		// if we can use inc reasoning then update incremental completion
		// structures
		if (canUseIncConsistency())
		{
			_abox.setSyntacticUpdate(true);

			// need to update the _branch _node count as this is _node has been
			// added otherwise during back jumping this _node can be removed
			for (final Branch branch : _abox.getBranches())
			{
				branch.setNodeCount(branch.getNodeCount() + 1);
			}

			// track updated and new _individuals; this is needed for the
			// incremental completion _strategy
			_abox.getIncrementalChangeTracker().addUpdatedIndividual(_abox.getIndividual(i));
			_abox.getIncrementalChangeTracker().addNewIndividual(_abox.getIndividual(i));
			_abox.setSyntacticUpdate(false);
		}

		_abox.setBranchIndex(remember);

		return ind;
	}

	@Override
	public void addType(final ATermAppl i, final ATermAppl c)
	{
		if (null == i || null == c)
			return;

		if (AnnotationClasses.contains(c))
			return;

		final ATermAppl typeAxiom = ATermUtils.makeTypeAtom(i, c);
		final DependencySet ds = OpenlletOptions.USE_TRACING ? new DependencySet(typeAxiom) : DependencySet.INDEPENDENT;

		// add type assertion to syntactic assertions and update dependency
		// _index
		if (OpenlletOptions.USE_INCREMENTAL_DELETION)
		{
			_syntacticAssertions.add(typeAxiom);
			_dependencyIndex.addTypeDependency(i, c, ds);
		}

		if (OpenlletOptions.KEEP_ABOX_ASSERTIONS)
			_aboxAssertions.add(AssertionType.TYPE, typeAxiom);

		addType(i, c, ds);
	}

	@Override
	public void addType(final ATermAppl i, final ATermAppl c, final DependencySet ds)
	{
		if (null == i || null == c || null == ds)
			return;

		// set addition flag
		_changes.add(ChangeType.ABOX_ADD);

		// if use incremental reasoning then update the cached pseudo model as
		// well
		if (canUseIncConsistency())
			// TODO: refactor the access to the updatedIndividuals and
			// newIndividuals - add get method
			// add this _individuals to the affected list - used for inc.
			// consistency checking
			_abox.getIncrementalChangeTracker().addUpdatedIndividual(_abox.getIndividual(i));

		synchronized (_abox)
		{
			_abox.setSyntacticUpdate(true); // TODO : find another way to alter behavior of abox
			_abox.addType(i, c, ds);
			_abox.setSyntacticUpdate(false);
		}

		if (canUseIncConsistency())
			// incrementally update the expressivity of the KB, so that we do
			// not have to reperform if from scratch!
			updateExpressivity(i, c);

		_logger.finer(() -> "type " + i + " " + c);
	}

	@Override
	public void addSame(final ATermAppl i1, final ATermAppl i2)
	{
		if (null == i1 || null == i2)
			return;

		// set addition flag
		_changes.add(ChangeType.ABOX_ADD);

		if (canUseIncConsistency())
		{
			// TODO: refactor the access to the updatedIndividuals and
			// newIndividuals - add get method
			_abox.getIncrementalChangeTracker().addUpdatedIndividual(_abox.getIndividual(i1));
			_abox.getIncrementalChangeTracker().addUpdatedIndividual(_abox.getIndividual(i2));

			// add to pseudomodel - note _branch is not set to zero - this is
			// done in SHOIQIncStrategy, prior
			// to merging _nodes
			_abox.addSame(i1, i2);
		}

		_abox.addSame(i1, i2);
		_logger.finer(() -> "same " + i1 + " " + i2);
	}

	@Override
	public void addAllDifferent(final ATermList list)
	{
		if (null == list)
			return;

		// set addition flag
		_changes.add(ChangeType.ABOX_ADD);

		// if we can use incremental consistency checking then add to pseudomodel
		if (canUseIncConsistency())
		{
			ATermList outer = list;
			// add to updated inds
			while (!outer.isEmpty())
			{
				ATermList inner = outer.getNext();
				while (!inner.isEmpty())
				{
					// TODO: refactor the access to the updatedIndividuals and
					// newIndividuals - add get method
					_abox.getIncrementalChangeTracker().addUpdatedIndividual(_abox.getIndividual(outer.getFirst()));
					_abox.getIncrementalChangeTracker().addUpdatedIndividual(_abox.getIndividual(inner.getFirst()));
					inner = inner.getNext();
				}
				outer = outer.getNext();
			}

			// add to pseudomodel - note _branch must be temporarily set to 0 to
			// ensure that asssertion will not be restored during backtracking
			final int branch = _abox.getBranchIndex();
			_abox.setBranchIndex(0);
			// update pseudomodel
			_abox.addAllDifferent(list);
			_abox.setBranchIndex(branch);
		}

		_abox.addAllDifferent(list);

		_logger.finer(() -> "all diff " + list);
	}

	@Override
	public void addDifferent(final ATermAppl i1, final ATermAppl i2)
	{
		if (null == i1 || null == i2)
			return;

		// set addition flag
		_changes.add(ChangeType.ABOX_ADD);

		// if we can use incremental consistency checking then add to
		// pseudomodel
		if (canUseIncConsistency())
		{
			// TODO: refactor the access to the updatedIndividuals and
			// newIndividuals - add get method
			_abox.getIncrementalChangeTracker().addUpdatedIndividual(_abox.getIndividual(i1));
			_abox.getIncrementalChangeTracker().addUpdatedIndividual(_abox.getIndividual(i2));

			// add to pseudomodel - note _branch must be temporarily set to 0 to
			// ensure that asssertion
			// will not be restored during backtracking
			final int branch = _abox.getBranchIndex();
			_abox.setBranchIndex(0);
			_abox.addDifferent(i1, i2);
			_abox.setBranchIndex(branch);
		}

		_abox.addDifferent(i1, i2);
		_logger.finer(() -> "diff " + i1 + " " + i2);
	}

	/**
	 * @param p
	 * @param s
	 * @param o
	 * @deprecated 2.5.1 Use addPropertyValue instead
	 */
	@Deprecated
	public void addObjectPropertyValue(final ATermAppl p, final ATermAppl s, final ATermAppl o)
	{
		addPropertyValue(p, s, o);
	}

	@Override
	public boolean addPropertyValue(final ATermAppl p, final ATermAppl s, final ATermAppl o)
	{
		if (null == p || null == s || null == o)
			return false;

		final Individual subj = _abox.getIndividual(s);
		final Role role = getRole(p);
		Node obj = null;

		if (subj == null)
		{
			_logger.warning(s + _isNotAnKnowIndividual);
			return false;
		}

		if (role == null)
		{
			_logger.warning(p + _isNotAnKnowProperty);
			return false;
		}

		if (!role.isObjectRole() && !role.isDatatypeRole())
			return false;

		final ATermAppl propAxiom = ATermUtils.makePropAtom(p, s, o);

		DependencySet ds = OpenlletOptions.USE_TRACING ? new DependencySet(propAxiom) : DependencySet.INDEPENDENT;

		if (role.isObjectRole())
		{
			obj = _abox.getIndividual(o);
			if (obj == null)
				if (ATermUtils.isLiteral(o))
				{
					_logger.warning("Ignoring literal value " + o + " for object property " + p);
					return false;
				}
				else
				{
					_logger.warning(o + _isNotAnKnowIndividual);
					return false;
				}
			if (OpenlletOptions.KEEP_ABOX_ASSERTIONS)
				_aboxAssertions.add(AssertionType.OBJ_ROLE, propAxiom);
		}
		else
			if (role.isDatatypeRole())
			{
				if (!ATermUtils.isLiteral(o))
				{
					_logger.warning("Ignoring non-literal value " + o + " for _data property " + p);
					return false;
				}
				obj = _abox.addLiteral(o, ds);
				if (OpenlletOptions.KEEP_ABOX_ASSERTIONS)
					_aboxAssertions.add(AssertionType.DATA_ROLE, propAxiom);
			}

		// set addition flag
		_changes.add(ChangeType.ABOX_ADD);

		if (obj != null && !canUseIncConsistency())
		{
			Edge edge = _abox.addEdge(p, s, obj.getName(), ds);

			if (edge == null)
			{
				_abox.reset();
				edge = _abox.addEdge(p, s, obj.getName(), ds);

				assert edge != null;
			}

			if (OpenlletOptions.USE_INCREMENTAL_DELETION)
			{
				// add to syntactic assertions
				_syntacticAssertions.add(propAxiom);

				// add to dependency _index
				_dependencyIndex.addEdgeDependency(edge, edge.getDepends());
			}
		}
		else
			if (canUseIncConsistency())
			{
				// TODO: refactor the access to the updatedIndividuals and
				// newIndividuals - add get method
				// add this _individual to the affected list
				_abox.getIncrementalChangeTracker().addUpdatedIndividual(_abox.getIndividual(s));

				if (role.isObjectRole())
				{
					// if this is an object property then add the object to the
					// affected list
					_abox.getIncrementalChangeTracker().addUpdatedIndividual(_abox.getIndividual(o));

					obj = _abox.getIndividual(o);
					if (obj.isPruned() || obj.isMerged())
						obj = obj.getSame();
				}

				// get the subject
				Individual subj2 = _abox.getIndividual(s);
				if (subj2.isPruned() || subj2.isMerged())
					subj2 = subj2.getSame();

				// generate dependency for new edge
				ds = OpenlletOptions.USE_TRACING ? new DependencySet(ATermUtils.makePropAtom(p, s, o)) : DependencySet.INDEPENDENT;

				// add to pseudomodel - note _branch must be temporarily set to 0 to
				// ensure that assertion
				// will not be restored during backtracking
				final int branch = _abox.getBranchIndex();
				_abox.setBranchIndex(DependencySet.NO_BRANCH);
				// add the edge
				final Edge newEdge = subj2.addEdge(role, obj, ds);
				_abox.setBranchIndex(branch);

				// add new edge to affected set
				if (newEdge != null)
					_abox.getIncrementalChangeTracker().addNewEdge(newEdge);
			}

		_logger.finer(() -> "prop-value " + s + " " + p + " " + o);

		return true;
	}

	@Override
	public boolean addNegatedPropertyValue(final ATermAppl p, final ATermAppl s, final ATermAppl o)
	{
		if (null == p || null == s || null == o)
			return false;

		_changes.add(ChangeType.ABOX_ADD);

		final Individual subj = _abox.getIndividual(s);
		final Role role = getRole(p);

		if (subj == null)
		{
			_logger.warning(s + _isNotAnKnowIndividual);
			return false;
		}

		if (role == null)
		{
			_logger.warning(p + _isNotAnKnowProperty);
			return false;
		}

		final ATermAppl propAxiom = ATermUtils.makeNot(ATermUtils.makePropAtom(p, s, o));

		final DependencySet ds = OpenlletOptions.USE_TRACING ? new DependencySet(propAxiom) : DependencySet.INDEPENDENT;

		if (role.isObjectRole())
		{
			if (_abox.getIndividual(o) == null)
				if (ATermUtils.isLiteral(o))
				{
					_logger.warning("Ignoring literal value " + o + " for object property " + p);
					return false;
				}
				else
				{
					_logger.warning(o + _isNotAnKnowIndividual);
					return false;
				}
		}
		else
			if (role.isDatatypeRole())
				_abox.addLiteral(o, ds);

		final ATermAppl C = ATermUtils.makeNot(ATermUtils.makeHasValue(p, o));

		addType(s, C, ds);

		_logger.finer(() -> "not-prop-value " + s + " " + p + " " + o);

		return true;
	}

	@Override
	public void addProperty(final ATermAppl p)
	{
		_changes.add(ChangeType.RBOX_ADD);
		_rbox.addRole(p);
		_logger.finer(() -> "prop " + p);
	}

	/**
	 * Add a new object property. If property was earlier defined to be a datatype property then this function will simply return without changing the KB.
	 *
	 * @param p Name of the property
	 * @return True if property is added, false if not
	 */
	@Override
	public boolean addObjectProperty(final ATerm p)
	{
		if (null == p)
			return false;

		final boolean exists = getPropertyType(p) == PropertyType.OBJECT;

		final Role role = _rbox.addObjectRole((ATermAppl) p);

		if (!exists)
		{
			_changes.add(ChangeType.RBOX_ADD);

			_logger.finer(() -> "object-prop " + p);
		}

		return role != null;
	}

	/**
	 * Add a new object property. If property was earlier defined to be a datatype property then this function will simply return without changing the KB.
	 *
	 * @param p
	 * @return True if property is added, false if not
	 */
	@Override
	public boolean addDatatypeProperty(final ATerm p)
	{
		if (null == p)
			return false;

		final boolean exists = getPropertyType(p) == PropertyType.DATATYPE;

		final Role role = _rbox.addDatatypeRole((ATermAppl) p);

		if (!exists)
		{
			_changes.add(ChangeType.RBOX_ADD);
			_logger.finer(() -> "data-prop " + p);
		}

		return role != null;
	}

	@Deprecated
	public void addOntologyProperty(final ATermAppl p)
	{
		addAnnotationProperty(p);
	}

	@Override
	public boolean addAnnotationProperty(final ATerm p)
	{
		if (null == p)
			return false;

		final boolean exists = getPropertyType(p) == PropertyType.ANNOTATION;

		final Role role = _rbox.addAnnotationRole((ATermAppl) p);

		if (!exists)
		{
			_changes.add(ChangeType.RBOX_ADD);
			_logger.finer(() -> "annotation-prop " + p);
		}

		return role != null;
	}

	@Override
	public boolean addAnnotation(final ATermAppl s, final ATermAppl p, final ATermAppl o)
	{
		if (null == p || null == s || null == o//
				|| !OpenlletOptions.USE_ANNOTATION_SUPPORT//
				|| !isAnnotationProperty(p))//
			return false;

		synchronized (_annotations)
		{
			Map<ATermAppl, Set<ATermAppl>> pidx = _annotations.get(s);

			if (pidx == null)
				pidx = new HashMap<>();

			Set<ATermAppl> oidx = pidx.get(p);

			if (oidx == null)
				oidx = new HashSet<>();

			oidx.add(o);
			pidx.put(p, oidx);
			_annotations.put(s, pidx);
		}

		_logger.finer(() -> "annotation " + s + " " + p + " " + o);

		return true;
	}

	@Override
	public boolean isAnnotation(final ATermAppl s, final ATermAppl p, final ATermAppl o)
	{
		final Set<ATermAppl> oidx = getAnnotations(s, p);

		if (oidx == null)
			return false;

		return oidx.contains(o);
	}

	@Override
	public void addDomain(final ATerm p, final ATermAppl c)
	{
		if (null == p || null == c)
			return;

		_changes.add(ChangeType.RBOX_ADD);

		_rbox.addDomain(p, c);

		_logger.finer(() -> "domain " + p + " " + c);
	}

	/**
	 * For internal use when domain axioms come from TBox absorption
	 */
	@Override
	public void addDomain(final ATerm p, final ATermAppl c, final Set<ATermAppl> explain)
	{
		_changes.add(ChangeType.RBOX_ADD);

		_rbox.addDomain(p, c, explain);

		_logger.finer(() -> "domain " + p + " " + c + " " + explain);
	}

	@Override
	public void addRange(final ATerm p, final ATermAppl c)
	{
		_changes.add(ChangeType.RBOX_ADD);

		_rbox.addRange(p, c);

		_logger.finer(() -> "range " + p + " " + c);
	}

	/**
	 * For internal use when range axioms come from TBox absorption
	 */
	@Override
	public void addRange(final ATerm p, final ATermAppl c, final Set<ATermAppl> explain)
	{
		if (null == p || null == c || null == explain)
			return;

		_changes.add(ChangeType.RBOX_ADD);

		_rbox.addRange(p, c, explain);

		_logger.finer(() -> "range " + p + " " + c + " " + explain);
	}

	@Override
	public void addDatatype(final ATermAppl p)
	{
		if (null == p)
			return;

		getDatatypeReasoner().declare(p);
	}

	/**
	 * Adds a new datatype defined to be equivalent to the given data range expression.
	 *
	 * @param name name of the datatype
	 * @param datarange a data range expression
	 * @return true if the add success
	 */
	@Override
	public boolean addDatatypeDefinition(final ATermAppl name, final ATermAppl datarange)
	{
		if (null == name || null == datarange)
			return false;

		return getDatatypeReasoner().define(name, datarange);
	}

	/**
	 * Removes (if possible) the given property domain axiom from the KB and return <code>true</code> if removal was successful. See also
	 * {@link #addDomain(ATerm, ATermAppl)}.
	 *
	 * @param p Property in domain axiom
	 * @param c Class in domain axiom
	 * @return <code>true</code> if axiom is removed, <code>false</code> if removal failed
	 */
	@Override
	public boolean removeDomain(final ATerm p, final ATermAppl c)
	{
		if (null == p || null == c)
			return false;

		final Role role = getRole(p);
		if (role == null)
		{
			Base.handleUndefinedEntity(p + _isNotAnProperty);
			return false;
		}
		if (!isClass(c))
		{
			Base.handleUndefinedEntity(c + _isNotAValidClassExpression);
			return false;
		}

		final boolean removed = getRBox().removeDomain(p, c);

		if (removed)
			_changes.add(ChangeType.RBOX_DEL);

		_logger.finer(() -> "Remove domain " + p + " " + c);

		return removed;
	}

	@Override
	public boolean removePropertyValue(final ATermAppl p, final ATermAppl i1, final ATermAppl i2)
	{
		if (null == p || null == i1 || null == i2)
			return false;

		final ATermAppl ind1 = i1;
		final ATermAppl ind2;
		if (ATermUtils.isLiteral(i2))
			try
			{
				ind2 = _abox.getDatatypeReasoner().getCanonicalRepresentation(i2);
			}
			catch (final InvalidLiteralException e)
			{
				_logger.warning(format("Unable to remove property value (%s,%s,%s) due to invalid literal: %s", p, i1, i2, e.getMessage()));
				return false;
			}
			catch (final UnrecognizedDatatypeException e)
			{
				_logger.warning(format("Unable to remove property value (%s,%s,%s) due to unrecognized datatype for literal: %s", p, i1, i2, e.getMessage()));
				return false;
			}
		else
			ind2 = i2;

		final Individual subj = _abox.getIndividual(ind1);
		final Node obj = _abox.getNode(ind2);
		final Role role = getRole(p);

		if (subj == null)
			if (OpenlletOptions.SILENT_UNDEFINED_ENTITY_HANDLING)
				throw new UnsupportedFeatureException(ind1 + _isNotAnIndividual);
			else
				return false;

		if (obj == null)
		{
			Base.handleUndefinedEntity(ind2 + _isNotAnIndividual);
			return false;
		}

		if (role == null)
		{
			Base.handleUndefinedEntity(p + _isNotAnProperty);
			return false;
		}

		_logger.finer(() -> "Remove ObjectPropertyValue " + ind1 + " " + p + " " + ind2);

		// make sure edge exists in assertions
		Edge edge = subj.getOutEdges().getExactEdge(subj, role, obj);

		if (edge == null && obj.isMerged())
			edge = obj.getInEdges().getExactEdge(subj, role, obj);

		if (edge == null)
			return false;

		// set deletion flag
		_changes.add(ChangeType.ABOX_DEL);

		if (!canUseIncConsistency())
		{
			_abox.reset();

			subj.removeEdge(edge);
			obj.removeInEdge(edge);
		}
		else
		{
			// if use inc. reasoning then we need to track the deleted
			// assertion.
			// Note that the actual edge will be deleted when
			// undo all dependent
			// structures in ABox.isIncConsistent()

			// add to deleted assertions
			getDeletedAssertions().add(ATermUtils.makePropAtom(p, ind1, ind2));

			// add this _individual to the affected list
			_abox.getIncrementalChangeTracker().addUpdatedIndividual(subj);

			// if this is an object property then add the object to the affected
			// list
			if (!role.isDatatypeRole())
				_abox.getIncrementalChangeTracker().addUpdatedIndividual((Individual) obj);
		}

		if (OpenlletOptions.KEEP_ABOX_ASSERTIONS)
		{
			final ATermAppl propAxiom = ATermUtils.makePropAtom(p, ind1, ind2);
			if (ATermUtils.isLiteral(ind2))
				_aboxAssertions.remove(AssertionType.DATA_ROLE, propAxiom);
			else
				_aboxAssertions.remove(AssertionType.OBJ_ROLE, propAxiom);
		}

		return true;
	}

	/**
	 * Removes (if possible) the given property range axiom from the KB and return <code>true</code> if removal was successful. See also
	 * {@link #addRange(ATerm, ATermAppl)}.
	 *
	 * @param p Property in range axiom
	 * @param c Class or datatype in range axiom
	 * @return <code>true</code> if axiom is removed, <code>false</code> if removal failed
	 */
	@Override
	public boolean removeRange(final ATerm p, final ATermAppl c)
	{
		if (null == p || null == c)
			return false;

		final Role role = getRole(p);
		if (role == null)
		{
			Base.handleUndefinedEntity(p + _isNotAnProperty);
			return false;
		}
		if (!isClass(c) && !isDatatype(c))
		{
			Base.handleUndefinedEntity(c + _isNotAValidClassExpressionOrDataRange);
			return false;
		}

		final boolean removed = getRBox().removeRange(p, c);

		if (removed)
			_changes.add(ChangeType.RBOX_DEL);

		_logger.finer(() -> "Remove range" + p + " " + c);

		return removed;
	}

	@Override
	public boolean removeType(final ATermAppl ind, final ATermAppl c)
	{
		if (null == ind || null == c)
			return false;

		final Individual subj = _abox.getIndividual(ind);

		if (subj == null)
			if (OpenlletOptions.SILENT_UNDEFINED_ENTITY_HANDLING)
				return false;
			else
				throw new UnsupportedFeatureException(ind + _isNotAnIndividual);

		final ATermAppl normC = ATermUtils.normalize(c);
		final DependencySet ds = subj.getDepends(normC);

		if (ds == null || !ds.isIndependent())
			return false;

		boolean removed = true;

		if (!canUseIncConsistency() || !OpenlletOptions.USE_INCREMENTAL_DELETION)
		{
			_abox.reset();

			removed = subj.removeType(normC);
		}
		else
		{
			// if use inc. reasoning then we need to track the deleted assertion.
			// Note that the actual edge type be deleted when undo all dependent
			// structures in ABox.isIncConsistent()

			// add axiom to deletion set
			getDeletedAssertions().add(ATermUtils.makeTypeAtom(ind, c));

			// add this _individuals to the affected list - used for inc.
			// consistency checking
			_abox.getIncrementalChangeTracker().addUpdatedIndividual(subj);

			// we may need to update the expressivity here, however so far it does not seem necessary!
			// updateExpressivity(i, c);
		}

		if (OpenlletOptions.KEEP_ABOX_ASSERTIONS)
		{
			final ATermAppl typeAxiom = ATermUtils.makeTypeAtom(ind, c);
			_aboxAssertions.remove(AssertionType.TYPE, typeAxiom);
		}

		// set deletion flag
		_changes.add(ChangeType.ABOX_DEL);

		_logger.finer(() -> "Remove Type " + ind + " " + c);

		return removed;
	}

	/**
	 * Removes (if possible) the given TBox axiom from the KB and return <code>true</code> if removal was successful.
	 *
	 * @param axiom TBox axiom to remove
	 * @return <code>true</code> if axiom is removed, <code>false</code> if removal failed
	 */
	@Override
	public boolean removeAxiom(final ATermAppl axiom)
	{
		if (null == axiom)
			return false;

		boolean removed = false;

		try
		{
			removed = _tbox.removeAxiom(axiom);
		}
		catch (final Exception e)
		{
			_logger.log(Level.SEVERE, "Removal failed for axiom " + axiom, e);
		}

		if (removed)
			_changes.add(ChangeType.TBOX_DEL);

		if (_logger.isLoggable(Level.FINER))
			_logger.finer("Remove " + axiom + ": " + removed);

		return removed;
	}

	@Override
	public void prepare()
	{
		if (!isChanged())
			return;

		final boolean explain = _abox.doExplanation();
		_abox.setDoExplanation(true);

		final Optional<Timer> timer = _timers.startTimer("preprocessing");

		// consistency need to be repeated after modifications
		_state.remove(ReasoningState.CONSISTENCY);
		// realization need to be repeated after modifications
		_state.remove(ReasoningState.REALIZE);

		// classification may notbve repeated if ...
		final boolean reuseTaxonomy =
				// classification has been previously done
				_state.contains(ReasoningState.CLASSIFY)
						// TBox did not change since classification
						&& !isTBoxChanged()
						// RBox did not change since classification
						&& !isRBoxChanged()
						// there are no nominals
						&& (!_expChecker.getExpressivity().hasNominal() || OpenlletOptions.USE_PSEUDO_NOMINALS);

		if (isRBoxChanged())
			_timers.execute("rbox", x -> _rbox.prepare());

		if (isTBoxChanged())
			_timers.execute("normalize", x -> _tbox.prepare());

		if (isRBoxChanged())
			_rbox.propagateDomainRange();

		_canUseIncConsistency = canUseIncConsistency();

		if (_abox.isComplete())
			if (_changes.contains(ChangeType.TBOX_DEL) || _changes.contains(ChangeType.RBOX_DEL) || !_canUseIncConsistency && _changes.contains(ChangeType.ABOX_DEL))
				_abox.reset();
			else
				if (_changes.contains(ChangeType.TBOX_ADD) || _changes.contains(ChangeType.RBOX_ADD))
					_abox.resetQueue();
				else
					if (_canUseIncConsistency && _changes.contains(ChangeType.ABOX_DEL))
						IncrementalRestore.restoreDependencies(this);

		// reset flags
		_changes.clear();

		_instances.clear();

		_estimate = new SizeEstimate(this);
		_abox.setDoExplanation(explain);

		if (!_canUseIncConsistency)
		{
			_logger.finer(() -> "Expressivity...");
			_expChecker.prepare();
		}

		_abox.clearCaches(!reuseTaxonomy);
		_abox.getCache().setMaxSize(OpenlletOptions.MAX_ANONYMOUS_CACHE);

		if (!reuseTaxonomy)
		{
			_state.remove(ReasoningState.CLASSIFY);
			_builder = Optional.empty();
			// taxonomy = null;
		}

		timer.ifPresent(t -> t.stop());

		if (_logger.isLoggable(Level.FINE))
		{
			final StringBuffer info = new StringBuffer();
			info.append("Expressivity: " + _expChecker.getExpressivity() + ", ");
			info.append("Classes: " + getClasses().size() + " ");
			info.append("Properties: " + getProperties().size() + " ");
			info.append("Individuals: " + _individuals.size());
			// info.append( " Strategy: " + chooseStrategy( _abox ) );
			_logger.fine(info.toString());
		}
	}

	/**
	 * This method is used for incremental reasoning. We do not want to recompute the expressivity from scratch.
	 *
	 * @param i
	 * @param c
	 */
	public void updateExpressivity(final ATermAppl i, final ATermAppl c)
	{
		if (null == i || null == c)
			return;

		// if the _tbox or _rbox changed then we cannot use incremental reasoning!
		if (!isChanged() || isTBoxChanged() || isRBoxChanged())
			return;

		// update expressivity given this _individual
		_expChecker.updateWithIndividual(i, c);

		// update the size _estimate as this could be a new _individual
		_estimate = new SizeEstimate(this);
	}

	public String getInfo()
	{
		prepare();

		final StringBuffer buffer = new StringBuffer();
		buffer.append("Expressivity: " + _expChecker.getExpressivity() + " ");
		buffer.append("Classes: " + getClasses().size() + " ");
		buffer.append("Properties: " + getProperties().size() + " ");
		buffer.append("Individuals: " + _individuals.size() + " ");

		final Expressivity expressivity = _expChecker.getExpressivity();
		if (expressivity.hasNominal())
			buffer.append("Nominals: " + expressivity.getNominals().size() + " ");

		return buffer.toString();
	}

	/**
	 * Returns true if the consistency check has been done and nothing in the KB has changed after that.
	 */
	@Override
	public boolean isConsistencyDone()
	{
		return !isChanged() && _state.contains(ReasoningState.CONSISTENCY);
	}

	/**
	 * Returns true if the classification check has been done and nothing in the KB has changed after that.
	 */
	@Override
	public boolean isClassified()
	{
		return !isChanged() && _state.contains(ReasoningState.CLASSIFY);
	}

	@Override
	public boolean isRealized()
	{
		return !isChanged() && _state.contains(ReasoningState.REALIZE);
	}

	public boolean isChanged()
	{
		return !_changes.isEmpty();
	}

	@Override
	public boolean isChanged(final ChangeType change)
	{
		return _changes.contains(change);
	}

	public boolean isTBoxChanged()
	{
		return _changes.contains(ChangeType.TBOX_ADD) || _changes.contains(ChangeType.TBOX_DEL);
	}

	public boolean isRBoxChanged()
	{
		return _changes.contains(ChangeType.RBOX_ADD) || _changes.contains(ChangeType.RBOX_DEL);
	}

	public boolean isABoxChanged()
	{
		return _changes.contains(ChangeType.ABOX_ADD) || _changes.contains(ChangeType.ABOX_DEL);
	}

	/**
	 * Returns all unsatisfiable classes in the KB excluding the BOTTOM concept. The result may be empty if there is no user-defined concept in the KB that is
	 * unsatisfiable.
	 *
	 * @return all unsatisfiable classes in the KB excluding the BOTTOM concept
	 */
	@Override
	public Set<ATermAppl> getUnsatisfiableClasses()
	{
		return getUnsatisfiableClasses(false);
	}

	/**
	 * Returns all unsatisfiable classes in the KB including the BOTTOM concept. Since BOTTOM concept is built-in the result will always have at least one
	 * element.
	 *
	 * @return all unsatisfiable classes in the KB including the BOTTOM concept
	 */
	@Override
	public Set<ATermAppl> getAllUnsatisfiableClasses()
	{
		return getUnsatisfiableClasses(true);
	}

	private Set<ATermAppl> getUnsatisfiableClasses(final boolean includeBottom)
	{
		Set<ATermAppl> aUnsatClasses = new HashSet<>();

		if (isClassified())
			// if the kb is already classified we can get them this way
			aUnsatClasses = includeBottom ? getAllEquivalentClasses(ATermUtils.BOTTOM) : getEquivalentClasses(ATermUtils.BOTTOM);
		else
		{
			if (includeBottom)
				aUnsatClasses.add(BOTTOM);

			// if not, check for them like this, without triggering classification
			final Set<ATermAppl> aClasses = getClasses();
			for (final ATermAppl aClass : aClasses)
				if (!isSatisfiable(aClass))
					aUnsatClasses.add(aClass);
		}

		return aUnsatClasses;
	}

	private void consistency()
	{
		if (isConsistencyDone())
			return;

		_abox.setInitialized(false);

		// prepare the KB
		prepare();

		for (final Entry<Rule, Rule> normalizedRule : _rules.entrySet())
			if (normalizedRule.getValue() == null)
			{
				final Rule rule = normalizedRule.getKey();
				final String msg = UsableRuleFilter.explainNotUsable(rule);
				_logger.warning("Ignoring rule " + rule + ": " + msg);
			}

		final Optional<Timer> timer = _timers.startTimer("consistency");

		final boolean doExplanation = _abox.doExplanation();

		if (OpenlletOptions.USE_TRACING && !_explainOnlyInconsistency)
			_abox.setDoExplanation(true);

		// perform the consistency check
		_consistent = _canUseIncConsistency ? _abox.isIncConsistent() : _abox.isConsistent();

		// final clean up
		if (OpenlletOptions.USE_INCREMENTAL_CONSISTENCY)
			_abox.getIncrementalChangeTracker().clear();

		if (OpenlletOptions.USE_INCREMENTAL_DELETION)
			getDeletedAssertions().clear();

		if (!_consistent)
		{
			// the behavior of Pellet 1.5.1 (and prior versions) was to generate
			// explanations for inconsistent ontologies even if the
			// doExplanation
			// was not set. this was causing an overhead for repeated
			// consistency
			// tests that mostly turn out to be _consistent. the new _strategy is
			// to repeat the consistency test for inconsistent ontologies by
			// manually setting the doExplanation flag. this will generate more
			// overhead for inconsistent ontologies but inconsistent ontologies
			// are much less frequent so this trade-off is preferred

			// create explanation by default for the ABox consistency check
			// but only if we can generate it (i.e. tracing is turned on) and
			// we haven't already done so (i.e. doExplanation flag was false at
			// the beginning)
			if (OpenlletOptions.USE_TRACING && _explainOnlyInconsistency && !_abox.doExplanation())
			{
				_abox.setDoExplanation(true);

				_abox.reset();
				_abox.isConsistent();

				_abox.setDoExplanation(false);
			}

			if (_logger.isLoggable(Level.FINE))
				_logger.fine("Inconsistent ontology. Reason: " + getExplanation());

			if (OpenlletOptions.USE_TRACING && _logger.isLoggable(Level.FINE))
				_logger.fine(renderExplanationSet());
		}

		_abox.setDoExplanation(doExplanation);

		_state.add(ReasoningState.CONSISTENCY);

		timer.ifPresent(t -> t.stop());

		if (_logger.isLoggable(Level.FINE) && timer.isPresent())
			_logger.fine("Consistent: " + _consistent + " (" + timer.get().getLast() + "ms)");

		assert isConsistencyDone() : "Consistency flag not set";
	}

	private String renderExplanationSet()
	{
		final StringBuilder msg = new StringBuilder("ExplanationSet: [");
		final Set<ATermAppl> explanation = getExplanationSet();
		for (final ATermAppl axiom : explanation)
		{
			msg.append(ATermUtils.toString(axiom));
			msg.append(",");
		}
		if (explanation.isEmpty())
			msg.append(']');
		else
			msg.setCharAt(msg.length() - 1, ']');

		return msg.toString();
	}

	@Override
	public boolean isConsistent()
	{
		consistency();

		return _consistent;
	}

	@Override
	public Taxonomy<ATermAppl> getToldTaxonomy()
	{
		return getTaxonomyBuilder().getToldTaxonomy();
	}

	@Override
	public Map<ATermAppl, Set<ATermAppl>> getToldDisjoints()
	{
		return getTaxonomyBuilder().getToldDisjoints();
	}

	@Override
	public void ensureConsistency()
	{
		if (!isConsistent())
			throw new InconsistentOntologyException("Cannot do reasoning with inconsistent ontologies!\n"//
					+ "Reason for inconsistency: " + getExplanation()//
					+ (OpenlletOptions.USE_TRACING ? "\n" + renderExplanationSet() : ""));
	}

	@Override
	public void classify()
	{
		ensureConsistency();

		if (isClassified())
			return;

		_logger.fine("Classifying...");

		final Optional<Timer> timer = _timers.startTimer("classify"); // TODO : coordination of the name of the classifier with the Taxonomy builders..

		final TaxonomyBuilder builder = getTaxonomyBuilder();
		final boolean isClassified;
		synchronized (builder)
		{
			isClassified = builder.classify();
		}

		timer.ifPresent(t -> t.stop());

		if (!isClassified)
			return;

		_state.add(ReasoningState.CLASSIFY);

		_estimate.computKBCosts();
	}

	@Override
	public void realize()
	{
		if (isRealized())
			return;

		classify();

		if (!isClassified())
			return;

		final Optional<Timer> timer = _timers.startTimer("realize");

		// This is false if the progress monitor is canceled
		final boolean isRealized;
		final TaxonomyBuilder builder = getTaxonomyBuilder();
		synchronized (builder)
		{
			isRealized = builder.realize();
		}

		timer.ifPresent(t -> t.stop());

		if (!isRealized)
			return;

		_state.add(ReasoningState.REALIZE);

		_estimate.computKBCosts();
	}

	/**
	 * @return the actual set individuals.
	 */
	@Override
	public Set<ATermAppl> currentIndividuals()
	{
		return _individuals;
	}

	/**
	 * @return the set of all _individuals. Returned set is unmodifiable!
	 */
	@Override
	public Set<ATermAppl> getIndividuals()
	{
		return Collections.unmodifiableSet(_individuals);
	}

	@Override
	public int getIndividualsCount()
	{
		return _individuals.size();
	}

	@Override
	public Stream<ATermAppl> individuals()
	{
		return _individuals.stream();
	}

	@Override
	public boolean isSatisfiable(final ATermAppl c)
	{
		if (null == c)
			return false;

		ensureConsistency();

		if (!isClass(c))
		{
			Base.handleUndefinedEntity(c + _isNotAnKnowClass);
			return false;
		}

		final ATermAppl normalClass = ATermUtils.normalize(c);

		if (isClassified() && !doExplanation())
		{
			final Bool equivToBottom = getTaxonomyBuilder().getTaxonomy().isEquivalent(ATermUtils.BOTTOM, normalClass);
			if (equivToBottom.isKnown())
				return equivToBottom.isFalse();
		}

		return _abox.isSatisfiable(normalClass);
	}

	/**
	 * @param d
	 * @return true if there is at least one named individual that belongs to the given class
	 */
	@Override
	public boolean hasInstance(final ATerm d)
	{
		if (null == d)
			return false;

		if (!isClass(d))
		{
			Base.handleUndefinedEntity(d + _isNotAnClass);
			return false;
		}

		ensureConsistency();

		final ATermAppl c = ATermUtils.normalize((ATermAppl) d);

		final List<ATermAppl> unknowns = new ArrayList<>();
		final Iterator<Individual> i = new IndividualIterator(_abox);
		while (i.hasNext())
		{
			final ATermAppl x = i.next().getName();

			final Bool knownType = _abox.isKnownType(x, c);
			if (knownType.isTrue())
				return true;
			else
				if (knownType.isUnknown())
					unknowns.add(x);
		}

		final boolean hasInstance = !unknowns.isEmpty() && _abox.existType(unknowns, c);

		return hasInstance;
	}

	@Override
	public boolean isSameAs(final ATermAppl t1, final ATermAppl t2)
	{
		if (null == t1 || null == t2)
			return false;

		ensureConsistency();

		if (!isIndividual(t1))
		{
			Base.handleUndefinedEntity(t1 + _isNotAnIndividual);
			return false;
		}
		if (!isIndividual(t2))
		{
			Base.handleUndefinedEntity(t2 + _isNotAnIndividual);
			return false;
		}

		if (t1.equals(t2))
			return true;

		final Set<ATermAppl> knowns = new HashSet<>();
		final Set<ATermAppl> unknowns = new HashSet<>();

		final Individual ind = _abox.getIndividual(t1);
		if (ind.isMerged() && !ind.getMergeDependency(true).isIndependent())
			_abox.getSames(ind.getSame(), unknowns, unknowns);
		else
			_abox.getSames(ind.getSame(), knowns, unknowns);

		if (knowns.contains(t2))
		{
			if (!doExplanation())
				return true;
		}
		else
			if (!unknowns.contains(t2))
				return false;

		return _abox.isSameAs(t1, t2);
	}

	@Override
	public boolean isDifferentFrom(final ATermAppl t1, final ATermAppl t2)
	{
		if (null == t1 || null == t2)
			return false;

		final Individual ind1 = _abox.getIndividual(t1);
		final Individual ind2 = _abox.getIndividual(t2);

		if (ind1 == null)
		{
			Base.handleUndefinedEntity(t1 + _isNotAnIndividual);
			return false;
		}

		if (ind2 == null)
		{
			Base.handleUndefinedEntity(t2 + _isNotAnIndividual);
			return false;
		}

		if (ind1.isDifferent(ind2) && !doExplanation())
			return true;

		final ATermAppl c = ATermUtils.makeNot(ATermUtils.makeValue(t2));

		return isType(t1, c);
	}

	@Override
	public Set<ATermAppl> getDifferents(final ATermAppl name)
	{
		if (null == name)
			return Collections.emptySet();

		ensureConsistency();

		Individual ind = _abox.getIndividual(name);

		if (ind == null)
		{
			Base.handleUndefinedEntity(name + _isNotAnIndividual);
			return Collections.emptySet();
		}

		boolean isIndependent = true;
		if (ind.isMerged())
		{
			isIndependent = ind.getMergeDependency(true).isIndependent();
			ind = ind.getSame();
		}

		final ATermAppl c = ATermUtils.makeNot(ATermUtils.makeValue(name));

		final Set<ATermAppl> differents = new HashSet<>();
		for (final ATermAppl x : _individuals)
		{
			final Bool isType = _abox.isKnownType(x, c);
			if (isIndependent && isType.isKnown())
			{
				if (isType.isTrue())
					differents.add(x);
			}
			else
				if (isType(x, c))
					differents.add(x);
		}

		return differents;
	}

	@Override
	public boolean hasPropertyValue(final ATermAppl s, final ATermAppl p, final ATermAppl o)
	{
		if (null == s || null == p)
			return false;

		ensureConsistency();

		if (!isIndividual(s))
		{
			Base.handleUndefinedEntity(s + _isNotAnIndividual);
			return false;
		}

		if (!isProperty(p))
		{
			Base.handleUndefinedEntity(p + _isNotAnKnowProperty);
			return false;
		}

		if (o != null)
			if (isDatatypeProperty(p))
			{
				if (!ATermUtils.isLiteral(o))
					return false;
			}
			else
				if (!isIndividual(o))
					return false;

		return _abox.hasPropertyValue(s, p, o);
	}

	@Override
	public Bool hasKnownPropertyValue(final ATermAppl s, final ATermAppl p, final ATermAppl o)
	{
		if (null == s || null == p || null == o)
			return Bool.FALSE;

		ensureConsistency();

		return _abox.hasObviousPropertyValue(s, p, o);
	}

	public Set<Set<ATermAppl>> getDisjoints(final ATermAppl c)
	{
		if (null == c)
			return Collections.emptySet();

		if (isClass(c))
			return getDisjointClasses(c);
		else
			if (isProperty(c))
				return getDisjointProperties(c);
			else
				Base.handleUndefinedEntity(c + _isNotAnPropertyNorAClass);
		return Collections.emptySet();
	}

	@Override
	public Set<Set<ATermAppl>> getDisjointProperties(final ATermAppl p)
	{
		if (null == p)
			return Collections.emptySet();

		return getDisjointProperties(p, false);
	}

	public Set<Set<ATermAppl>> getDisjointProperties(final ATermAppl p, final boolean direct)
	{
		if (null == p)
			return Collections.emptySet();

		if (!isProperty(p))
		{
			Base.handleUndefinedEntity(p + _isNotAnProperty);
			return Collections.emptySet();
		}

		final Role role = _rbox.getRole(p);

		if (!role.isObjectRole() && !role.isDatatypeRole())
			return Collections.emptySet();

		final Set<Set<ATermAppl>> disjoints = new HashSet<>();

		TaxonomyNode<ATermAppl> node = getRoleTaxonomy(role.isObjectRole()).getTop();

		final Set<TaxonomyNode<ATermAppl>> marked = new HashSet<>();
		final List<TaxonomyNode<ATermAppl>> visit = new ArrayList<>();
		visit.add(node);

		for (int i = 0; i < visit.size(); i++)
		{
			node = visit.get(i);

			if (node.isHidden() || node.getEquivalents().isEmpty() || marked.contains(node))
				continue;

			final ATermAppl r = node.getName();
			if (isDisjointProperty(p, r))
			{
				final Set<ATermAppl> eqs = getAllEquivalentProperties(r);
				if (!eqs.isEmpty())
					disjoints.add(eqs);
				if (direct)
					mark(node, marked);
				else
					disjoints.addAll(getSubProperties(r));
			}
			else
				visit.addAll(node.getSubs());

		}

		return disjoints;
	}

	private void mark(final TaxonomyNode<ATermAppl> node, final Set<TaxonomyNode<ATermAppl>> marked)
	{
		marked.add(node);

		for (final TaxonomyNode<ATermAppl> next : node.getSubs())
			mark(next, marked);
	}

	@Override
	public Set<ATermAppl> getComplements(final ATermAppl c)
	{
		if (null == c)
			return Collections.emptySet();

		if (!isClass(c))
		{
			Base.handleUndefinedEntity(c + _isNotAnClass);
			return Collections.emptySet();
		}

		final ATermAppl notC = ATermUtils.normalize(ATermUtils.makeNot(c));
		final Set<ATermAppl> complements = getAllEquivalentClasses(notC);

		if (notC.equals(ATermUtils.BOTTOM))
			complements.add(ATermUtils.BOTTOM);

		return complements;
	}

	@Override
	public Set<ATermAppl> getSames(final ATermAppl name)
	{
		if (null == name)
			return Collections.emptySet();

		final Set<ATermAppl> sames = getAllSames(name);
		sames.remove(name);

		return sames;
	}

	public void printClassTree(final PrintWriter out)
	{
		if (null == out)
			return;

		classify();

		new ClassTreePrinter().print(getTaxonomyBuilder().getTaxonomy(), out);
	}

	/**
	 * @param doDepAxioms
	 * @deprecated Use setDoExplanation instead
	 */
	@Deprecated
	public void setDoDependencyAxioms(final boolean doDepAxioms)
	{
		_logger.finer(() -> "Setting DoDependencyAxioms = " + doDepAxioms);
	}

	/**
	 * @return DO NOT USE
	 * @deprecated Use getExplanation instead
	 */
	@Deprecated
	public boolean getDoDependencyAxioms()
	{
		return false;
	}

	@Override
	public CompletionStrategy chooseStrategy(final ABox abox)
	{
		if (null == abox)
			return null;

		return chooseStrategy(abox, getExpressivity());
	}

	/**
	 * Choose a completion strategy based on the expressivity of the KB. The _abox given is not necessarily the ABox that belongs to this KB but can be a
	 * derivative.
	 *
	 * @return the completion strategy choosen
	 */
	@Override
	public CompletionStrategy chooseStrategy(final ABox abox, final Expressivity expressivity)
	{
		if (null == abox || null == expressivity)
			return null;

		final boolean conceptSatisfiability = abox.size() == 1 && new IndividualIterator(abox).next().isConceptRoot();

		// We don't need to use rules _strategy if we are checking concept satisfiability unless
		// there are nominals because then rules may affect concept satisfiability and we need
		// to use _rules _strategy
		if (getRules().size() > 0 && (expressivity.hasNominal() || !conceptSatisfiability))
			return new ContinuousRulesStrategy(abox);

		final boolean fullDatatypeReasoning = OpenlletOptions.USE_FULL_DATATYPE_REASONING && (expressivity.hasCardinalityD() || expressivity.hasKeys());

		if (!fullDatatypeReasoning)
			if (conceptSatisfiability && !expressivity.hasNominal())
				return new EmptySRIQStrategy(abox);

		return new SROIQStrategy(abox);
	}

	/**
	 * Set a timeout for the main timer. Used to stop an automated test after a reasonable amount of time has passed.
	 *
	 * @param timeout
	 */
	@Override
	public void setTimeout(final long timeout)
	{
		_logger.info(() -> "Timeout @ " + timeout + "ms");
		_timers._mainTimer.setTimeout(timeout);
	}

	/**
	 * Get the classification results.
	 */
	@Override
	public Taxonomy<ATermAppl> getTaxonomy()
	{
		classify();

		return getTaxonomyBuilder().getTaxonomy();
	}

	@Override
	public void setTaxonomyBuilderProgressMonitor(final ProgressMonitor progressMonitor)
	{
		if (null == progressMonitor)
			return;

		_builderProgressMonitor = progressMonitor;

		if (_builder.isPresent())
			getTaxonomyBuilder().setProgressMonitor(progressMonitor);
	}

	@Override
	public SizeEstimate getSizeEstimate()
	{
		return _estimate;
	}

	/**
	 * Add a rule to the KB.
	 */
	@Override
	public boolean addRule(final Rule rule)
	{
		if (null == rule)
			return false;

		// DL-safe _rules affects the ABox so we might redo the reasoning
		_changes.add(ChangeType.ABOX_ADD);

		_rules.put(rule, normalize(rule));

		_logger.finer(() -> "rule " + rule);

		return true;
	}

	private Rule normalize(final Rule rule)
	{
		if (!UsableRuleFilter.isUsable(rule))
			return null;

		final List<RuleAtom> head = rule.getHead().stream().map(atom ->
		{
			if (atom instanceof ClassAtom)
			{
				final ClassAtom ca = (ClassAtom) atom;
				final ATermAppl rawCls = ca.getPredicate();
				final ATermAppl normCls = ATermUtils.normalize(rawCls);
				if (rawCls != normCls)
					return new ClassAtom(normCls, ca.getArgument());
			}
			return atom;
		}).collect(Collectors.toList());

		final Map<AtomIObject, Set<ATermAppl>> types = new HashMap<>();

		for (final RuleAtom atom : rule.getBody())
			if (atom instanceof IndividualPropertyAtom)
			{
				final IndividualPropertyAtom propAtom = (IndividualPropertyAtom) atom;
				final ATermAppl prop = propAtom.getPredicate();

				final AtomIObject subj = propAtom.getArgument1();
				if (subj instanceof AtomIVariable)
				{
					final Set<ATermAppl> domains = getRole(prop).getDomains();
					if (domains != null)
						MultiMapUtils.addAll(types, subj, domains);
				}

				final AtomIObject obj = propAtom.getArgument2();
				if (obj instanceof AtomIVariable)
				{
					final Set<ATermAppl> ranges = getRole(prop).getRanges();
					if (ranges != null)
						MultiMapUtils.addAll(types, obj, ranges);
				}
			}

		final List<RuleAtom> body = new ArrayList<>(rule.getBody().size());
		for (final RuleAtom atom : rule.getBody())
			if (atom instanceof ClassAtom)
			{
				final ClassAtom ca = (ClassAtom) atom;
				final AtomIObject arg = ca.getArgument();
				final ATermAppl rawCls = ca.getPredicate();
				final ATermAppl normCls = ATermUtils.normalize(rawCls);
				if (!MultiMapUtils.contains(types, arg, normCls))
					body.add(rawCls == normCls ? atom : new ClassAtom(normCls, arg));
				// else the class is drop.
			}
			else
				body.add(atom);

		return new Rule(rule.getName(), head, body);
	}

	/**
	 * Return all the asserted rules.
	 */
	@Override
	public Set<Rule> getRules()
	{
		return _rules.keySet();
	}

	/**
	 * Return the asserted rules with their normalized form. A normalized rule is a rule where any class expression occurring in the rules is in normalized
	 * form.
	 *
	 * @return set of rules where
	 */
	@Override
	public Map<Rule, Rule> getNormalizedRules()
	{
		return _rules;
	}

	/**
	 * @return true if we can use incremental consistency checking
	 */
	protected boolean canUseIncConsistency()
	{
		// can we do incremental consistency checking
		final Expressivity expressivity = _expChecker.getExpressivity();
		if (expressivity == null)
			return false;

		return !(expressivity.hasNominal() && expressivity.hasInverse())//
				&& getRules().isEmpty() //
				&& !isTBoxChanged() //
				&& !isRBoxChanged() //
				&& _abox.isComplete() //
				&& OpenlletOptions.USE_INCREMENTAL_CONSISTENCY //
				// support additions only; also support deletions with or with additions, however tracing must be on to support incremental deletions
				&& (!_changes.contains(ChangeType.ABOX_DEL) || OpenlletOptions.USE_INCREMENTAL_DELETION);
	}

	public void ensureIncConsistency(final boolean aboxDeletion)
	{
		if (canUseIncConsistency())
			return;

		final Expressivity expressivity = _expChecker.getExpressivity();

		String msg = "ABox " + (aboxDeletion ? "deletion" : "addition") + " failed because ";
		if (expressivity == null)
			msg += "an initial consistency check has not been performed on this KB";
		else
			if (expressivity.hasNominal())
				msg += "KB has nominals";
			else
				if (expressivity.hasInverse())
					msg += "KB has inverse properties";
				else
					if (isTBoxChanged())
						msg += "TBox changed";
					else
						if (isRBoxChanged())
							msg += "RBox changed";
						else
							if (OpenlletOptions.USE_INCREMENTAL_CONSISTENCY)
								msg += "configuration option USE_INCREMENTAL_CONSISTENCY is enabled";
							else
								if (aboxDeletion)
									msg += "configuration option USE_INCREMENTAL_DELETION is not enabled";
								else
									msg += "of an unknown reason";

		throw new UnsupportedOperationException(msg);
	}

	/**
	 * @return the dependency index for syntactic assertions in this kb
	 */
	@Override
	public DependencyIndex getDependencyIndex()
	{
		return _dependencyIndex;
	}

	/**
	 * @return syntactic assertions in the kb
	 */
	@Override
	public Set<ATermAppl> getSyntacticAssertions()
	{
		return _syntacticAssertions;
	}

	public Set<ATermAppl> getABoxAssertions(final AssertionType assertionType)
	{
		if (null == assertionType)
			return Collections.emptySet();

		final Set<ATermAppl> assertions = _aboxAssertions.get(assertionType);

		if (assertions == null)
			return Collections.emptySet();
		else
			return Collections.unmodifiableSet(assertions);
	}

	/**
	 * @return the deletedAssertions
	 */
	@Override
	public Set<ATermAppl> getDeletedAssertions()
	{
		return _deletedAssertions;
	}

	/**
	 * Returns _current value of explainOnlyInconsistency option.
	 *
	 * @see #setExplainOnlyInconsistency(boolean)
	 * @return current value of explainOnlyInconsistency option
	 */
	public boolean isExplainOnlyInconsistency()
	{
		return _explainOnlyInconsistency;
	}

	/**
	 * Controls what kind of explanations can be generated using this KB. With this option enabled explanations for inconsistent ontologies will be returned.
	 * But if the ontology is _consistent, it will not be possible to retrieve explanations for inferences about _instances. This option is disabled by default.
	 * It should be turned on if explanations are only needed for inconsistencies but not other inferences. Turning this option on improves the performance of
	 * consistency checking for _consistent ontologies.
	 *
	 * @param explainOnlyInconsistency new value for _explainOnlyInconsistency option
	 */
	public void setExplainOnlyInconsistency(final boolean explainOnlyInconsistency)
	{
		_explainOnlyInconsistency = explainOnlyInconsistency;
	}
}
