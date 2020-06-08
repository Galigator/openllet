// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.model;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;

/**
 * <p>
 * Title: Query Interface
 * </p>
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Petr Kremen
 */
public interface Query
{

	public enum VarType
	{
		CLASS, PROPERTY, INDIVIDUAL, LITERAL
	}

	/**
	 * @param filter to sets for this query.
	 */
	void setFilter(final Filter filter);

	/**
	 * @return the filter for this query.
	 */
	Filter getFilter();

	/**
	 * @return true if distinct results are required.
	 */
	boolean isDistinct();

	/**
	 * @param  queryType #VarType
	 * @return           variables that occur in the subquery specified by the given type.
	 */
	Set<ATermAppl> getDistVarsForType(final VarType queryType);

	/**
	 * Adds an query atom to the query.
	 *
	 * @param atom
	 */
	void add(final QueryAtom atom);

	/**
	 * Adds a distinguished variable to the query with its type - there can be more variable types to support punning.
	 *
	 * @param a
	 * @param type
	 */
	void addDistVar(final ATermAppl a, final VarType type);

	/**
	 * @param a is the distinguished variable to add that appears in the result projection to the query;
	 */
	void addResultVar(final ATermAppl a);

	/**
	 * @return all the variables used in this query.
	 */
	Set<ATermAppl> getVars();

	/**
	 * Return all undistinguished variables used in this query.
	 *
	 * @return Set of variables
	 */
	Set<ATermAppl> getUndistVars();

	/**
	 * @return individuals and literals used in this query.
	 */
	Set<ATermAppl> getConstants();

	/**
	 * Return all the variables that will be in the results. For SPARQL, these are the variables in the SELECT clause.
	 *
	 * @return Set of variables
	 */
	List<ATermAppl> getResultVars();

	/**
	 * Return all the distinguished variables. These are variables that will be bound to individuals (or _data values) existing in the KB.
	 *
	 * @return Set of variables
	 */
	Set<ATermAppl> getDistVars();

	/**
	 * @return all the atoms in the query.
	 */
	List<QueryAtom> getAtoms();

	/**
	 * @return The KB that will be used to answer this query.
	 */
	KnowledgeBase getKB();

	/**
	 * Sets the KB that will be used to answer this query.
	 *
	 * @param kb KB that will be used to answer this query
	 */
	void setKB(KnowledgeBase kb);

	/**
	 * Checks whether the query is ground.
	 *
	 * @return true iff the query is ground
	 */
	boolean isGround();

	/**
	 * Replace the variables in the query with the values specified in the binding and return a new query instance (without modifying this query).
	 *
	 * @param  binding
	 * @return         the query changed
	 */
	Query apply(ResultBinding binding);

	/**
	 * @param  distVar
	 * @param  avoidList
	 * @param  stopOnConstants
	 * @return                 Rolls up the query to the given variable.
	 */
	ATermAppl rollUpTo(final ATermAppl distVar, final Collection<ATermAppl> avoidList, final boolean stopOnConstants);

	/**
	 * Creates a subquery from the given query. Atoms are listed according to the 'atoms' parameter.
	 *
	 * @param  atoms selected atom indices
	 * @return       subquery
	 */
	Query reorder(int[] atoms);

	void remove(final QueryAtom atom);

	/**
	 * Searches for given atom pattern. This also might be used for different types of rolling-up, involving various sets of allowed atom types.
	 *
	 * @param  predicate
	 * @param  arguments
	 *
	 * @return           query atoms in the order as they appear in the query
	 */
	List<QueryAtom> findAtoms(final QueryPredicate predicate, final ATermAppl... arguments);

	/**
	 * @param parameters to set for the query parameterization
	 */
	void setQueryParameters(QueryParameters parameters);

	/**
	 * Get the query parameterization values
	 *
	 * @return QueryParameters
	 */
	QueryParameters getQueryParameters();

	/**
	 * Return the name of this query
	 *
	 * @return name of the query
	 */
	ATermAppl getName();

	/**
	 * Sets the name of this query
	 *
	 * @param name name of the query
	 */
	void setName(ATermAppl name);
}
