package openllet.core;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.aterm.ATermList;
import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.abox.Individual;
import openllet.core.expressivity.Expressivity;
import openllet.core.expressivity.ExpressivityChecker;
import openllet.core.knowledge.ClassesBase;
import openllet.core.knowledge.InstancesBase;
import openllet.core.knowledge.PropertiesBase;
import openllet.core.rules.model.Rule;
import openllet.core.tableau.completion.CompletionStrategy;
import openllet.core.tableau.completion.incremental.DependencyIndex;
import openllet.core.taxonomy.Taxonomy;
import openllet.core.utils.Bool;
import openllet.core.utils.SizeEstimate;
import openllet.core.utils.progress.ProgressMonitor;

/**
 * Abstraction of operation expeteced from a knowledge base.
 *
 * @since 2.6.0
 */
public interface KnowledgeBase extends InstancesBase, PropertiesBase, ClassesBase
{

	public void clear();

	public SizeEstimate getSizeEstimate();

	/**
	 * Set a timeout for the main timer. Used to stop an automated test after a reasonable amount of time has passed.
	 *
	 * @param timeout
	 */
	public void setTimeout(final long timeout);

	public void clearABox();

	public Expressivity getExpressivity();

	/**
	 * @return the total number of individuals in kb.
	 * @since 2.6.2
	 */
	public int getIndividualsCount();

	/**
	 * @return all individuals.
	 */
	public Stream<ATermAppl> individuals();

	public boolean isConsistent();

	/**
	 * @return true if the consistency check has been done and nothing in the KB has changed after that.
	 */
	public boolean isConsistencyDone();

	/**
	 * @return the classification results.
	 */
	public Taxonomy<ATermAppl> getTaxonomy();

	public ExpressivityChecker getExpressivityChecker();

	/**
	 * @return the dependency index for syntactic assertions in this kb
	 */
	public DependencyIndex getDependencyIndex();

	/**
	 * @return syntactic assertions in the kb
	 */
	public Set<ATermAppl> getSyntacticAssertions();

	/**
	 * @return the deletedAssertions
	 */
	public Set<ATermAppl> getDeletedAssertions();

	/**
	 * Choose a completion strategy based on the expressivity of the KB. The abox given is not necessarily the ABox that belongs to this KB but can be a
	 * derivative.
	 *
	 * @param abox
	 * @param expressivity
	 * @return a Completion strategy choose.
	 */
	public CompletionStrategy chooseStrategy(final ABox abox, final Expressivity expressivity);

	public default CompletionStrategy chooseStrategy(final ABox abox)
	{
		return chooseStrategy(abox, getExpressivity());
	}

	/**
	 * Returns all unsatisfiable classes in the KB excluding the BOTTOM concept. The result may be empty if there is no user-defined concept in the KB that is
	 * unsatisfiable.
	 *
	 * @return all unsatisfiable classes in the KB excluding the BOTTOM concept
	 */
	public Set<ATermAppl> getUnsatisfiableClasses();

	/**
	 * Returns all unsatisfiable classes in the KB including the BOTTOM concept. Since BOTTOM concept is built-in the result will always have at least one
	 * element.
	 *
	 * @return all unsatisfiable classes in the KB including the BOTTOM concept
	 */
	public Set<ATermAppl> getAllUnsatisfiableClasses();

	/**
	 * Return the asserted rules with their normalized form. A normalized rule is a rule where any class expression occurring in the rules is in normalized
	 * form.
	 *
	 * @return set of rules where
	 */
	public Map<Rule, Rule> getNormalizedRules();

	/**
	 * @return all the asserted rules.
	 */
	public Set<Rule> getRules();

	public void addProperty(final ATermAppl p);

	/**
	 * Answers the hasPropertyValue question without doing any satisfiability check. It might return <code>Boolean.TRUE</code>, <code>Boolean.FALSE</code>, or
	 * <code>null</code> (unknown). If the null value is returned <code>hasPropertyValue</code> function needs to be called to get the answer.
	 *
	 * @param s Subject
	 * @param p Predicate
	 * @param o Object (<code>null</code> can be used as wildcard)
	 * @return true if the hasPropertyValue question without doing any satisfiability check.
	 */
	public Bool hasKnownPropertyValue(final ATermAppl s, final ATermAppl p, final ATermAppl o);

	public boolean hasPropertyValue(final ATermAppl s, final ATermAppl p, final ATermAppl o);

	public boolean isAnnotation(final ATermAppl s, final ATermAppl p, final ATermAppl o);

	public Taxonomy<ATermAppl> getToldTaxonomy();

	public Map<ATermAppl, Set<ATermAppl>> getToldDisjoints();

	// ----------------------------------------- Get Classification result -----------------------------------------------------

	/**
	 * Get all the (named) classes _individual belongs to.
	 * <p>
	 * *** This function will first realize the whole ontology ***
	 * </p>
	 *
	 * @param ind An individual name
	 * @return A set of sets, where each set in the collection represents an equivalence class. The elements of the inner class are ATermAppl objects.
	 */
	public default Set<Set<ATermAppl>> getTypes(final ATermAppl ind)
	{
		return getTypes(ind, /* direct = */false);
	}

	public void addType(final ATermAppl i, final ATermAppl c, final DependencySet ds);

	public void addType(final ATermAppl i, final ATermAppl c);

	/**
	 * Returns all the (named) subclasses of class c. The class c itself is not included in the list but all the other classes that are equivalent to c are put
	 * into the list. Also note that the returned list will always have at least one element, that is the BOTTOM concept. By definition BOTTOM concept is
	 * subclass of every concept. This function is equivalent to calling getSubClasses(c, true).
	 * <p>
	 * *** This function will first classify the whole ontology ***
	 * </p>
	 *
	 * @param c class whose subclasses are returned
	 * @return A set of sets, where each set in the collection represents an equivalence class. The elements of the inner class are ATermAppl objects.
	 */
	public default Set<Set<ATermAppl>> getSubClasses(final ATermAppl c)
	{
		return getSubClasses(c, false);
	}

	/**
	 * @param c is a classes
	 * @return true if there is at least one named individual that belongs to the given class
	 */
	public boolean hasInstance(final ATerm c);

	// -------------------------------------------------- ADD CHANGE --------------------------------------------------------------

	/**
	 * Add a new object property. If property was earlier defined to be a datatype property then this function will simply return without changing the KB.
	 *
	 * @param p Name of the property
	 * @return True if property is added, false if not
	 */
	public boolean addObjectProperty(final ATerm p);

	/**
	 * Add a new object property. If property was earlier defined to be a datatype property then this function will simply return without changing the KB.
	 *
	 * @param p
	 * @return True if property is added, false if not
	 */
	public boolean addDatatypeProperty(final ATerm p);

	public void addClass(final ATermAppl c);

	public Individual addIndividual(final ATermAppl i);

	public void addEquivalentClass(final ATermAppl c1, final ATermAppl c2);

	public void addSubClass(final ATermAppl sub, final ATermAppl sup);

	public void addDisjointClasses(final ATermList classes);

	public void addDisjointClasses(final List<ATermAppl> classes);

	public void addComplementClass(final ATermAppl c1, final ATermAppl c2);

	public void addDisjointClass(final ATermAppl c1, final ATermAppl c2);

	public void addDomain(final ATerm p, final ATermAppl c);

	public void addDomain(final ATerm p, final ATermAppl c, final Set<ATermAppl> explain);

	public void addRange(final ATerm p, final ATermAppl c);

	public void addRange(final ATerm p, final ATermAppl c, final Set<ATermAppl> explain);

	public boolean addPropertyValue(final ATermAppl p, final ATermAppl s, final ATermAppl o);

	public boolean addNegatedPropertyValue(final ATermAppl p, final ATermAppl s, final ATermAppl o);

	public void addSame(final ATermAppl i1, final ATermAppl i2);

	public void addDifferent(final ATermAppl i1, final ATermAppl i2);

	public void addAllDifferent(final ATermList list);

	public boolean addAnnotation(final ATermAppl s, final ATermAppl p, final ATermAppl o);

	public boolean addAnnotationProperty(final ATerm p);

	public void addDatatype(final ATermAppl p);

	public void addKey(final ATermAppl c, final Set<ATermAppl> properties);

	/**
	 * Adds a new datatype defined to be equivalent to the given data range expression.
	 *
	 * @param name name of the datatype
	 * @param datarange a data range expression
	 * @return true if the add success
	 */
	public boolean addDatatypeDefinition(final ATermAppl name, final ATermAppl datarange);

	/**
	 * Add a rule to the KB.
	 *
	 * @param rule
	 * @return true if the add success
	 */
	public boolean addRule(final Rule rule);

	// -------------------------------------------------- REMOVE CHANGE --------------------------------------------------------------

	public boolean removeType(final ATermAppl ind, final ATermAppl c);

	/**
	 * Removes (if possible) the given property domain axiom from the KB and return <code>true</code> if removal was successful. See also
	 * {@link #addDomain(ATerm, ATermAppl)}.
	 *
	 * @param p Property in domain axiom
	 * @param c Class in domain axiom
	 * @return <code>true</code> if axiom is removed, <code>false</code> if removal failed
	 */
	public boolean removeDomain(final ATerm p, final ATermAppl c);

	/**
	 * Removes (if possible) the given property range axiom from the KB and return <code>true</code> if removal was successful. See also
	 * {@link #addRange(ATerm, ATermAppl)}.
	 *
	 * @param p Property in range axiom
	 * @param c Class or datatype in range axiom
	 * @return <code>true</code> if axiom is removed, <code>false</code> if removal failed
	 */
	public boolean removeRange(final ATerm p, final ATermAppl c);

	public boolean removePropertyValue(final ATermAppl p, final ATermAppl i1, final ATermAppl i2);

	/**
	 * Removes (if possible) the given TBox axiom from the KB and return <code>true</code> if removal was successful.
	 *
	 * @param axiom TBox axiom to remove
	 * @return <code>true</code> if axiom is removed, <code>false</code> if removal failed
	 */
	public boolean removeAxiom(final ATermAppl axiom);

	// ----------------------------------------- Monitor -----------------------------------------------------

	public void setTaxonomyBuilderProgressMonitor(final ProgressMonitor progressMonitor);

	// ----------------------------------------- Consulting -----------------------------------------------------

	/**
	 * Returns all the superclasses (implicitly or explicitly defined) of class c. The class c itself is not included in the list. but all the other classes
	 * that are sameAs c are put into the list. Also note that the returned list will always have at least one element, that is TOP concept. By definition TOP
	 * concept is superclass of every concept. This function is equivalent to calling getSuperClasses(c, true).
	 * <p>
	 * *** This function will first classify the whole ontology ***
	 * </p>
	 *
	 * @param c class whose superclasses are returned
	 * @return A set of sets, where each set in the collection represents an equivalence class. The elements of the inner class are ATermAppl objects.
	 */
	public default Set<Set<ATermAppl>> getSuperClasses(final ATermAppl c)
	{
		return getSuperClasses(c, false);
	}

	public default Set<Set<ATermAppl>> getDisjointClasses(final ATermAppl c)
	{
		return getDisjointClasses(c, false);
	}

	public Set<ATermAppl> getComplements(final ATermAppl c);

	/**
	 * @param name
	 * @return all the individuals asserted to be equal to the given individual but not the the individual itself.
	 */
	public Set<ATermAppl> getSames(final ATermAppl name);

	public Set<ATermAppl> getDifferents(final ATermAppl name);

	public boolean isDifferentFrom(final ATermAppl t1, final ATermAppl t2);

	public Set<Set<ATermAppl>> getDisjointProperties(final ATermAppl p);

	// ----------------------------------------- Consulting State -----------------------------------------------------

	public boolean isSameAs(final ATermAppl t1, final ATermAppl t2);

	/**
	 * Create a copy of this KB with a completely new ABox copy but pointing to the same RBox and TBox.
	 *
	 * @return A copy of this KB
	 */
	public default KnowledgeBase copy()
	{
		return copy(false);
	}

	/**
	 * Create a copy of this KB. Depending on the value of <code>emptyABox</code> either a completely new copy of ABox will be created or the new KB will have
	 * an empty ABox. If <code>emptyABox</code> parameter is true but the original KB contains nominals in its RBox or TBox the new KB will have the definition
	 * of those _individuals (but not ) In either case, the new KB will point to the same RBox and TBox so changing one KB's RBox or TBox will affect other.
	 *
	 * @param emptyABox If <code>true</code> ABox is not copied to the new KB
	 * @return A copy of this KB
	 */
	public KnowledgeBase copy(final boolean emptyABox);

	public enum ChangeType
	{
		ABOX_ADD, ABOX_DEL, TBOX_ADD, TBOX_DEL, RBOX_ADD, RBOX_DEL
	}

	public boolean isChanged(final ChangeType change);
}
