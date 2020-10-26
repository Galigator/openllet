// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.el;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import openllet.aterm.AFun;
import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.aterm.ATermList;
import openllet.core.KnowledgeBase;
import openllet.core.boxes.rbox.Role;
import openllet.core.taxonomy.CDOptimizedTaxonomyBuilder;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.MultiValueMap;
import openllet.core.utils.Timer;
import openllet.core.utils.Timers;
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
public abstract class RuleBasedELClassifier extends CDOptimizedTaxonomyBuilder
{
	@SuppressWarnings("hiding")
	public static final Logger _logger = Log.getLogger(RuleBasedELClassifier.class);

	protected Timers _timers = new Timers();

	public RuleBasedELClassifier(final KnowledgeBase kb)
	{
		super(kb);
		_logger.fine("new RuleBasedELClassifier");
	}

	protected abstract void addSubclassRule(ATermAppl sub, ATermAppl sup);

	protected abstract void addRoleDomainRule(ATermAppl p, ATermAppl domain);

	protected abstract void addRoleRangeRule(ATermAppl p, ATermAppl range);

	protected abstract void addRoleChainRule(ATerm[] chain, ATermAppl sup);

	protected abstract void addRoleHierarchyRule(ATermAppl sub, ATermAppl sup);

	protected abstract MultiValueMap<ATermAppl, ATermAppl> run(Collection<ATermAppl> classes);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean classify()
	{
		reset();

		_monitor.setProgressTitle("Classifiying");
		_monitor.setProgressLength(_classes.size());
		_monitor.taskStarted();
		_monitor.setProgress(0);

		_logger.info("Creating structures");

		{
			final Optional<Timer> timer = _timers.startTimer("createConcepts");
			processAxioms();
			timer.ifPresent(Timer::stop);
		}

		_logger.info("Running rules");

		final MultiValueMap<ATermAppl, ATermAppl> subsumers = run(_kb.getAllClasses());

		_monitor.setProgress(_classes.size());

		_logger.info("Building hierarchy");
		{
			final Optional<Timer> timer = _timers.startTimer("buildHierarchy");
			buildTaxonomy(subsumers);
			timer.ifPresent(Timer::stop);
		}
		_monitor.setProgress(_classes.size());
		_monitor.taskFinished();

		return true;
	}

	protected void buildTaxonomy(final MultiValueMap<ATermAppl, ATermAppl> subsumers)
	{
		_taxonomyImpl = new GenericTaxonomyBuilder().build(subsumers);
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
				addSubclassRule(subEL, sup);
				return;
			}

			final ATermAppl supEL = ELSyntaxUtils.simplify(sup);
			addSubclassRule(subEL, supEL);
		}
		else
			if (fun.equals(ATermUtils.EQCLASSFUN))
			{
				final ATermAppl supEL = ELSyntaxUtils.simplify(sup);
				addSubclassRule(subEL, supEL);
				addSubclassRule(supEL, subEL);
			}
			else
				throw new IllegalArgumentException("Axiom " + axiom + " is not EL.");
	}

	private void processAxioms()
	{
		//EquivalentClass -> SubClasses
		//Disjoint Classes -> SubClass
		//Normalize ATerm lists to sets
		final Collection<ATermAppl> assertedAxioms = _kb.getTBox().getAssertedAxioms();
		for (final ATermAppl assertedAxiom : assertedAxioms)
			toELSubClassAxioms(assertedAxiom);

		//Role Hierarchies
		for (final Role r : _kb.getRBox().getRoles().values())
		{
			final ATermAppl role = r.getName();
			for (final Set<ATermAppl> supers : _kb.getSuperProperties(role))
				for (final ATermAppl sup : supers)
					addRoleHierarchyRule(role, sup);
		}

		//Role Chains
		for (final Role supRole : _kb.getRBox().getRoles().values())
			for (final ATermList chainList : supRole.getSubRoleChains())
			{
				final ATerm[] chain = ATermUtils.toArray(chainList);
				addRoleChainRule(chain, supRole.getName());
			}

		//Role Domain Restrictions
		final RoleRestrictionCache roleRestrictions = new RoleRestrictionCache(_kb.getRBox());
		for (final Entry<ATermAppl, ATermAppl> entry : roleRestrictions.getDomains().entrySet())
			addRoleDomainRule(entry.getKey(), entry.getValue());

		//Role Range Restrictions
		for (final Entry<ATermAppl, ATermAppl> entry : roleRestrictions.getRanges().entrySet())
			addRoleRangeRule(entry.getKey(), entry.getValue());

		//Reflexive Roles
		for (final Role role : _kb.getRBox().getRoles().values())
			if (role.isReflexive())
			{
				final ATermAppl range = roleRestrictions.getRange(role.getName());
				if (range == null)
					continue;

				addSubclassRule(ATermUtils.TOP, range);
			}
	}
}
