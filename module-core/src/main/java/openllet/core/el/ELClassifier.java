// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.el;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import openllet.aterm.AFun;
import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.aterm.ATermList;
import openllet.core.KnowledgeBase;
import openllet.core.boxes.rbox.Role;
import openllet.core.taxonomy.CDOptimizedTaxonomyBuilder;
import openllet.core.taxonomy.PartialOrderTaxonomyBuilder;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.CollectionUtils;
import openllet.core.utils.MultiValueMap;
import openllet.core.utils.PartialOrderComparator;
import openllet.core.utils.PartialOrderRelation;
import openllet.core.utils.SetUtils;
import openllet.core.utils.Timer;
import openllet.core.utils.Timers;
import openllet.shared.tools.Log;

/**
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin
 */
public class ELClassifier extends CDOptimizedTaxonomyBuilder
{
	@SuppressWarnings("hiding")
	public static final Logger _logger = Log.getLogger(ELClassifier.class);

	public final Timers _timers = new Timers();

	public volatile ConceptInfo TOP;
	public volatile ConceptInfo BOTTOM;

	private boolean _hasComplexRoles;

	private MultiValueMap<ConceptInfo, Trigger> _queue;

	private Map<ATermAppl, ConceptInfo> _concepts;

	private RoleChainCache _roleChains;
	private RoleRestrictionCache _roleRestrictions;

	private final PartialOrderComparator<ATermAppl> subsumptionComparator = (a, b) ->
	{
		final ConceptInfo aInfo = getInfo(a);
		final ConceptInfo bInfo = getInfo(b);

		return aInfo.hasSuperClass(bInfo) ? //
		bInfo.hasSuperClass(aInfo) ? PartialOrderRelation.EQUAL : PartialOrderRelation.LESS//
				: //
		bInfo.hasSuperClass(aInfo) ? PartialOrderRelation.GREATER : PartialOrderRelation.INCOMPARABLE;
	};

	public ELClassifier(final KnowledgeBase kb)
	{
		super(kb);
	}

	@Override
	protected void reset()
	{
		super.reset();

		_hasComplexRoles = _kb.getExpressivity().hasTransitivity() || _kb.getExpressivity().hasComplexSubRoles();

		_queue = new MultiValueMap<>();
		_concepts = CollectionUtils.makeMap();

		_roleChains = new RoleChainCache(_kb);
		_roleRestrictions = new RoleRestrictionCache(_kb.getRBox());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean classify()
	{
		reset();

		_kb.prepare();

		Timer t = _timers.startTimer("createConcepts");
		_logger.info("Creating structures");
		createConcepts();
		_logger.info("Created structures");
		t.stop();

		_monitor.setProgressTitle("Classifiying");
		_monitor.setProgressLength(_queue.size());
		_monitor.taskStarted();

		printStructures();

		_logger.info("Processing _queue");
		t = _timers.startTimer("processQueue");
		processQueue();
		t.stop();
		_logger.info("Processed _queue");

		if (_logger.isLoggable(Level.FINE))
			print();

		_logger.info("Building hierarchy");
		t = _timers.startTimer("buildHierarchy");

		_taxonomyImpl = new ELTaxonomyBuilder().build(_concepts);
		//		buildTaxonomyWithPO();

		t.stop();
		_logger.info("Builded hierarchy");

		_monitor.taskFinished();

		return true;
	}

	@SuppressWarnings("unused")
	private void buildPartialOrderTaxonomy()
	{
		final PartialOrderTaxonomyBuilder builder = new PartialOrderTaxonomyBuilder(_kb, subsumptionComparator);
		_taxonomyImpl = builder.getTaxonomy();

		for (final ConceptInfo ci : _concepts.values())
			classify(ci);
	}

	private void classify(final ConceptInfo ci)
	{
		final ATermAppl c = ci.getConcept();
		if (!ATermUtils.isPrimitive(c))
			return;

		if (ci.getSuperClasses().contains(BOTTOM))
		{
			_taxonomyImpl.addEquivalentNode(c, _taxonomyImpl.getBottomNode());
			return;
		}

		final Set<ATermAppl> equivalents = new HashSet<>();
		for (final ConceptInfo subsumer : ci.getSuperClasses())
		{
			if (!ATermUtils.isPrimitive(subsumer.getConcept()))
				continue;

			if (ci.equals(subsumer))
				continue;
			else
				if (subsumer.hasSuperClass(ci))
					equivalents.add(subsumer.getConcept());
				else
					classify(subsumer);
		}

		_taxonomyImpl.addEquivalents(c, equivalents);
	}

	private void addExistential(final ConceptInfo ci, final ATermAppl prop, final ConceptInfo qi)
	{
		if (ci.hasSuccessor(prop, qi))
			return;

		addExistentialP(ci, prop, qi);

		final Set<Set<ATermAppl>> supEqs = _kb.getSuperProperties(prop);
		for (final Set<ATermAppl> supEq : supEqs)
			for (final ATermAppl sup : supEq)
				addExistentialP(ci, sup, qi);
	}

	private void addExistentialP(final ConceptInfo ci, final ATermAppl prop, final ConceptInfo qi)
	{
		if (ci.hasSuccessor(prop, qi))
			return;

		for (final ConceptInfo supInfo : qi.getSuperClasses())
			if (ci.addSuccessor(prop, supInfo))
			{
				if (supInfo.equals(BOTTOM))
				{
					addSubsumer(ci, BOTTOM);

					final Iterator<ConceptInfo> preds = ci.getPredecessors().flattenedValues();
					while (preds.hasNext())
						addSubsumer(preds.next(), BOTTOM);
				}

				final ATermAppl some = ATermUtils.makeSomeValues(prop, supInfo.getConcept());
				final ConceptInfo si = getInfo(some);
				if (si != null)
					addToQueue(ci, si.getTriggers());
			}

		final ATermAppl q = qi.getConcept();
		if (ATermUtils.isAnd(q))
		{
			ATermList list = (ATermList) q.getArgument(0);
			while (!list.isEmpty())
			{
				final ATermAppl conj = (ATermAppl) list.getFirst();
				final ConceptInfo conjInfo = createConcept(conj);
				if (ci.addSuccessor(prop, conjInfo))
				{
					final ATermAppl some = ATermUtils.makeSomeValues(prop, conj);
					final ConceptInfo si = createConcept(some);
					addToQueue(ci, si.getTriggers());
				}

				list = list.getNext();
			}
		}

		final ATermAppl propRange = _roleRestrictions.getRange(prop);
		if (propRange != null)
		{
			final ATermAppl some = ATermUtils.makeSomeValues(prop, propRange);
			final ConceptInfo si = createConcept(some);
			addSubsumer(ci, si);
		}

		if (_hasComplexRoles)
		{
			for (final Entry<ATermAppl, Set<ConceptInfo>> entry : ci.getPredecessors().entrySet())
			{
				final ATermAppl predProp = entry.getKey();
				for (final ConceptInfo pred : entry.getValue())
					for (final ATermAppl supProp : _roleChains.getAllSuperRoles(predProp, prop))
						addExistential(pred, supProp, qi);
			}

			for (final Entry<ATermAppl, Set<ConceptInfo>> entry : qi.getSuccessors().entrySet())
			{
				final ATermAppl succProp = entry.getKey();
				for (final ConceptInfo succ : entry.getValue())
					for (final ATermAppl supProp : _roleChains.getAllSuperRoles(prop, succProp))
						addExistential(ci, supProp, succ);
			}
		}
	}

	private void addToQueue(final ConceptInfo ci, final Set<Trigger> triggers)
	{
		if (_queue.addAll(ci, triggers))
			if (_logger.isLoggable(Level.FINE))
				_logger.fine("Add to _queue: " + ci + " " + triggers);
	}

	private void addSubsumer(final ConceptInfo ci, final ConceptInfo supInfo)
	{
		if (ci.hasSuperClass(supInfo))
			return;

		ci.addSuperClass(supInfo);

		if (ATermUtils.isBottom(supInfo.getConcept()))
		{
			final Iterator<ConceptInfo> preds = ci.getPredecessors().flattenedValues();
			while (preds.hasNext())
				addSubsumer(preds.next(), supInfo);
			return;
		}

		addToQueue(ci, supInfo.getTriggers());

		final ATermAppl c = supInfo.getConcept();
		if (_logger.isLoggable(Level.FINE))
			_logger.fine("Adding subsumers to " + ci + " " + c + " " + ci.getSuperClasses());

		if (ATermUtils.isAnd(c))
		{
			ATermList list = (ATermList) c.getArgument(0);
			while (!list.isEmpty())
			{
				final ATermAppl conj = (ATermAppl) list.getFirst();

				addSubsumer(ci, createConcept(conj));

				list = list.getNext();
			}
		}
		else
			if (ATermUtils.isSomeValues(c))
			{
				final ATermAppl p = (ATermAppl) c.getArgument(0);
				final ATermAppl qualification = (ATermAppl) c.getArgument(1);
				final ConceptInfo q = createConcept(qualification);

				addExistential(ci, p, q);
			}
			else
				assert ATermUtils.isPrimitive(c);

		for (final Map.Entry<ATermAppl, Set<ConceptInfo>> e : ci.getPredecessors().entrySet())
		{
			final ATermAppl prop = e.getKey();
			for (final ConceptInfo pred : e.getValue())
			{
				final ATermAppl some = ATermUtils.makeSomeValues(prop, c);
				final ConceptInfo si = getInfo(some);
				if (si != null)
					addToQueue(pred, si.getTriggers());
			}
		}
	}

	private ConceptInfo createConcept(final ATermAppl c)
	{
		ConceptInfo ci = getInfo(c);
		if (ci == null)
		{
			ci = new ConceptInfo(c, _hasComplexRoles, false);
			_concepts.put(c, ci);
			ci.addSuperClass(TOP);
			addSubsumer(ci, ci);

			if (ATermUtils.isAnd(c))
			{
				final ConceptInfo[] conjuncts = createConceptArray((ATermList) c.getArgument(0));
				for (final ConceptInfo conjInfo : conjuncts)
					conjInfo.addTrigger(new Trigger(conjuncts, ci));
			}
			else
				if (ATermUtils.isSomeValues(c))
				{
					final ATermAppl q = (ATermAppl) c.getArgument(1);
					createConcept(q);
					ci.addTrigger(new Trigger(ci));
				}
		}

		return ci;
	}

	private ConceptInfo[] createConceptArray(final ATermList list)
	{
		final ConceptInfo[] a = new ConceptInfo[list.getLength()];

		int i = 0;
		for (final ATerm term : list)
		{
			a[i] = createConcept((ATermAppl) term);
			i++;
		}

		return a;
	}

	private void createConceptsFromAxiom(final ATermAppl sub, final ATermAppl sup)
	{
		final ConceptInfo ciSub = createConcept(sub);
		final ConceptInfo ciSup = createConcept(sup);

		final Trigger trigger = new Trigger(ciSup);

		ciSub.addTrigger(trigger);
	}

	private void toELSubClassAxioms(final ATermAppl axiom)
	{
		final AFun fun = axiom.getAFun();
		final ATermAppl sub = (ATermAppl) axiom.getArgument(0);
		final ATermAppl sup = (ATermAppl) axiom.getArgument(1);

		final ATermAppl subEL = ELSyntaxUtils.simplify(sub);
		if (fun.equals(ATermUtils.SUBFUN))
		{
			if (ATermUtils.isPrimitive(sup) || ATermUtils.isBottom(sup))
			{
				createConceptsFromAxiom(subEL, sup);
				return;
			}

			final ATermAppl supEL = ELSyntaxUtils.simplify(sup);
			createConceptsFromAxiom(subEL, supEL);
		}
		else
			if (fun.equals(ATermUtils.EQCLASSFUN))
			{
				final ATermAppl supEL = ELSyntaxUtils.simplify(sup);
				createConceptsFromAxiom(subEL, supEL);
				createConceptsFromAxiom(supEL, subEL);
			}
			else
				throw new IllegalArgumentException("Axiom " + axiom + " is not EL.");
	}

	private void normalizeAxioms()
	{
		//EquivalentClass -> SubClasses
		//Disjoint Classes -> SubClass
		//Normalize ATerm lists to sets
		final Collection<ATermAppl> assertedAxioms = _kb.getTBox().getAssertedAxioms();
		for (final ATermAppl assertedAxiom : assertedAxioms)
			toELSubClassAxioms(assertedAxiom);

		//Convert Role Domains to axioms
		for (final Entry<ATermAppl, ATermAppl> entry : _roleRestrictions.getDomains().entrySet())
		{
			final ATermAppl roleName = entry.getKey();
			final ATermAppl domain = entry.getValue();
			createConceptsFromAxiom(ATermUtils.makeSomeValues(roleName, ATermUtils.TOP), domain);
		}

		//Convert Reflexive Roles to axioms
		for (final Role role : _kb.getRBox().getRoles().values())
			if (role.isReflexive())
			{
				final ATermAppl range = _roleRestrictions.getRange(role.getName());
				if (range == null)
					continue;

				createConceptsFromAxiom(ATermUtils.TOP, range);
			}
	}

	private void createConcepts()
	{
		TOP = new ConceptInfo(ATermUtils.TOP, _hasComplexRoles, false);
		_concepts.put(ATermUtils.TOP, TOP);
		TOP.addSuperClass(TOP);

		BOTTOM = new ConceptInfo(ATermUtils.BOTTOM, _hasComplexRoles, false);
		_concepts.put(ATermUtils.BOTTOM, BOTTOM);
		BOTTOM.addSuperClass(BOTTOM);

		for (final ATermAppl c : _kb.getClasses())
			createConcept(c);

		normalizeAxioms();

		final Set<Trigger> TOP_TRIGGERS = TOP.getTriggers();
		for (final ConceptInfo ci : _concepts.values())
		{
			final Set<Trigger> queueList = SetUtils.create(TOP_TRIGGERS);
			queueList.addAll(ci.getTriggers());

			if (!queueList.isEmpty())
				_queue.addAll(ci, queueList);
		}
	}

	private ConceptInfo getInfo(final ATermAppl concept)
	{
		return _concepts.get(concept);
	}

	public void print()
	{
		for (final ConceptInfo ci : _concepts.values())
			System.out.println(ci + " " + ci.getSuperClasses());
		System.out.println();
		_roleChains.print();
	}

	public void printStructures()
	{
		if (_logger.isLoggable(Level.FINE))
			for (final ConceptInfo ci : _concepts.values())
				_logger.fine(ci + "\t" + ci.getTriggers() + "\t" + ci.getSuperClasses());
	}

	private void processQueue()
	{
		final int startingSize = _queue.size();
		while (!_queue.isEmpty())
		{
			final int processed = startingSize - _queue.size();
			if (_monitor.getProgress() < processed)
				_monitor.setProgress(processed);

			final MultiValueMap<ConceptInfo, Trigger> localQueue = _queue;
			_queue = new MultiValueMap<>();

			for (final Entry<ConceptInfo, Set<Trigger>> entry : localQueue.entrySet())
			{
				final ConceptInfo ci = entry.getKey();
				for (final Trigger trigger : entry.getValue())
					processTrigger(ci, trigger);
			}
		}
	}

	private void processTrigger(final ConceptInfo ci, final Trigger trigger)
	{
		if (trigger.isTriggered(ci))
			addSubsumer(ci, trigger.getConsequence());
	}
}
