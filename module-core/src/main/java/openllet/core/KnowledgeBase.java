package openllet.core;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.aterm.ATermList;
import openllet.core.boxes.abox.ABox;
import openllet.core.boxes.abox.Individual;
import openllet.core.boxes.rbox.RBox;
import openllet.core.boxes.rbox.Role;
import openllet.core.boxes.tbox.TBox;
import openllet.core.datatypes.DatatypeReasoner;
import openllet.core.expressivity.Expressivity;
import openllet.core.expressivity.ExpressivityChecker;
import openllet.core.rules.model.Rule;
import openllet.core.tableau.completion.CompletionStrategy;
import openllet.core.tableau.completion.incremental.DependencyIndex;
import openllet.core.taxonomy.Taxonomy;
import openllet.core.taxonomy.TaxonomyBuilder;
import openllet.core.utils.Bool;
import openllet.core.utils.SizeEstimate;
import openllet.core.utils.Timers;
import openllet.core.utils.progress.ProgressMonitor;
import openllet.shared.tools.Logging;

/**
 * Abstraction of operation expeteced from a knowledge base.
 *
 * @since 2.6.0
 */
public interface KnowledgeBase extends Logging
{
	// Error messages

	public static final String _isNotAnIndividual = " is not an individual!";
	public static final String _isNotAnKnowIndividual = " is not a known individual!";
	public static final String _isNotAnProperty = " is not a property!";
	public static final String _isNotAnPropertyNorAClass = " is not a property nor a class!";
	public static final String _isNotAnKnowProperty = " is not a known property!";
	public static final String _isNotAnClass = " is not a class!";
	public static final String _isNotAnKnowClass = " is not a known class!";
	public static final String _isNotAValidClassExpression = " is not a valid class expression";
	public static final String _isNotAValidClassExpressionOrDataRange = " is not a valid class expression or data range";
	public static final String _isNotAnKnowObjectProperty = " is not a known object property!";
	public static final String _isNotAnKnowDataProperty = " is not a known data property!";

	public void clear();

	public void prepare();

	public Timers getTimers();

	public SizeEstimate getSizeEstimate();

	/**
	 * Set a timeout for the main timer. Used to stop an automated test after a reasonable amount of time has passed.
	 *
	 * @param timeout
	 */
	public void setTimeout(final long timeout);

	public ABox getABox();

	public void clearABox();

	public TBox getTBox();

	public RBox getRBox();

	public TaxonomyBuilder getBuilder();

	public Expressivity getExpressivity();

	/**
	 * @return the set of all individuals. Returned set is unmodifiable!
	 */
	public Set<ATermAppl> getIndividuals();

	/**
	 * @return all individuals.
	 */
	public Stream<ATermAppl> individuals();

	public default boolean isIndividual(final ATerm ind)
	{
		return getIndividuals().contains(ind);
	}

	public void classify();

	public void realize();

	/**
	 * @return true if the classification check has been done and nothing in the KB has changed after that.
	 */
	public boolean isClassified();

	public boolean isConsistent();

	public void ensureConsistency();

	/**
	 * @return true if the consistency check has been done and nothing in the KB has changed after that.
	 */
	public boolean isConsistencyDone();

	public default Set<ATermAppl> getExplanationSet()
	{
		return getABox().getExplanationSet();
	}

	/**
	 * @param doExplanation The doExplanation to set.
	 */
	public default void setDoExplanation(final boolean doExplanation)
	{
		getABox().setDoExplanation(doExplanation);
	}

	public default boolean doExplanation()
	{
		return getABox().doExplanation();
	}

	public default String getExplanation()
	{
		return getABox().getExplanation();
	}

	/**
	 * @return the classification results.
	 */
	public Taxonomy<ATermAppl> getTaxonomy();

	public boolean isDatatypeProperty(final ATerm p);

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

	public boolean isRealized();

	public boolean isSatisfiable(final ATermAppl c);

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

	public boolean isDisjointClass(final ATermAppl c1, final ATermAppl c2);

	/**
	 * @param term
	 * @return a role
	 */
	public default Role getRole(final ATerm term)
	{
		return getRBox().getRole(term);
	}

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

	/**
	 * @return Returns the DatatypeReasoner
	 */
	public default DatatypeReasoner getDatatypeReasoner()
	{
		return getABox().getDatatypeReasoner();
	}

	/**
	 * @return the set of all named classes. Returned set is unmodifiable!
	 */
	public default Set<ATermAppl> getClasses()
	{
		return Collections.unmodifiableSet(getTBox().getClasses());
	}

	public boolean isComplement(final ATermAppl c1, final ATermAppl c2);

	/**
	 * @return the set of all properties.
	 */
	public Set<ATermAppl> getProperties();

	/**
	 * @return the set of all object properties.
	 */
	public Set<ATermAppl> getObjectProperties();

	/**
	 * @return the set of all object properties.
	 */
	public Set<ATermAppl> getDataProperties();

	public Set<Set<ATermAppl>> getAllSuperProperties(final ATermAppl prop);

	/**
	 * Return the sub properties of p. Depending on the second parameter the result will include either all subproperties or only the direct subproperties.
	 *
	 * @param prop
	 * @param direct If true return only the direct subproperties, otherwise return all the subproperties
	 * @return A set of sets, where each set in the collection represents a set of equivalent properties. The elements of the inner class are ATermAppl objects.
	 */
	public Set<Set<ATermAppl>> getSubProperties(final ATermAppl prop, final boolean direct);

	/**
	 * Return all the sub properties of p.
	 *
	 * @param prop
	 * @return A set of sets, where each set in the collection represents a set of equivalent properties. The elements of the inner class are ATermAppl objects.
	 */
	public default Set<Set<ATermAppl>> getSubProperties(final ATermAppl prop)
	{
		return getSubProperties(prop, false);
	}

	/**
	 * Return the super properties of p. Depending on the second parameter the result will include either all super properties or only the direct super
	 * properties.
	 *
	 * @param prop
	 * @param direct If true return only the direct super properties, otherwise return all the super properties
	 * @return A set of sets, where each set in the collection represents a set of equivalent properties. The elements of the inner class are Role objects.
	 */
	public Set<Set<ATermAppl>> getSuperProperties(final ATermAppl prop, final boolean direct);

	/**
	 * Return all the super properties of p.
	 *
	 * @param prop
	 * @return A set of sets, where each set in the collection represents a set of equivalent properties. The elements of the inner class are Role objects.
	 */
	public default Set<Set<ATermAppl>> getSuperProperties(final ATermAppl prop)
	{
		return getSuperProperties(prop, false);
	}

	public Set<Set<ATermAppl>> getAllSubProperties(final ATermAppl prop);

	/**
	 * Return all the properties that are equivalent to p.
	 *
	 * @param prop
	 * @return A set of ATermAppl objects.
	 */
	public Set<ATermAppl> getEquivalentProperties(final ATermAppl prop);

	public Set<ATermAppl> getFunctionalProperties();

	public Set<ATermAppl> getInverseFunctionalProperties();

	public Set<ATermAppl> getTransitiveProperties();

	public Set<ATermAppl> getSymmetricProperties();

	public Set<ATermAppl> getAsymmetricProperties();

	/**
	 * @param name
	 * @return the named inverse property and all its equivalent properties.
	 */
	public Set<ATermAppl> getInverses(final ATerm name);

	public boolean isObjectProperty(final ATerm p);

	public Map<ATermAppl, List<ATermAppl>> getPropertyValues(final ATermAppl pred);

	/**
	 * @param s
	 * @param o
	 * @return all properties asserted between a subject and object.
	 */
	public List<ATermAppl> getProperties(final ATermAppl s, final ATermAppl o);

	public PropertyType getPropertyType(final ATerm r);

	public default boolean isProperty(final ATerm p)
	{
		return getRBox().isRole(p);
	}

	public void addProperty(final ATermAppl p);

	public default Role getProperty(final ATerm r)
	{
		return getRBox().getRole(r);
	}

	public Set<ATermAppl> getReflexiveProperties();

	public Set<ATermAppl> getIrreflexiveProperties();

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

	/**
	 * Return all property values for a given property and subject value.
	 *
	 * @param r
	 * @param x
	 * @return List of ATermAppl objects.
	 */
	public List<ATermAppl> getPropertyValues(final ATermAppl r, final ATermAppl x);

	public Set<ATermAppl> getAnnotationProperties();

	/**
	 * The results of this function is not guaranteed to be complete. Use {@link #hasDomain(ATermAppl, ATermAppl)} to get complete answers.
	 *
	 * @param name
	 * @return the domain restrictions on the property.
	 */
	public Set<ATermAppl> getDomains(final ATermAppl name);

	/**
	 * The results of this function is not guaranteed to be complete. Use {@link #hasRange(ATermAppl, ATermAppl)} to get complete answers.
	 *
	 * @param name
	 * @return the domain restrictions on the property.
	 */
	public Set<ATermAppl> getRanges(final ATerm name);

	public boolean isAnnotationProperty(final ATerm p);

	public Set<ATermAppl> getIndividualsWithAnnotation(final ATermAppl p, final ATermAppl o);

	/**
	 * @return the set of key values of the annotations map
	 */
	public Set<ATermAppl> getAnnotationSubjects();

	public Set<ATermAppl> getAnnotations(final ATermAppl s, final ATermAppl p);

	public boolean isAnnotation(final ATermAppl s, final ATermAppl p, final ATermAppl o);

	public Taxonomy<ATermAppl> getRoleTaxonomy(final boolean objectTaxonomy);

	public Taxonomy<ATermAppl> getToldTaxonomy();

	public TaxonomyBuilder getTaxonomyBuilder();

	public Map<ATermAppl, Set<ATermAppl>> getToldDisjoints();

	// ----------------------------------------- Get Classification result -----------------------------------------------------

	/**
	 * Returns the (named) classes individual belongs to. Depending on the second parameter the result will include either all types or only the direct types.
	 *
	 * @param ind An individual name
	 * @param direct If true return only the direct types, otherwise return all types
	 * @return A set of sets, where each set in the collection represents an equivalence class. The elements of the inner class are ATermAppl objects.
	 */
	public Set<Set<ATermAppl>> getTypes(final ATermAppl ind, final boolean direct);

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

	public boolean isType(final ATermAppl x, final ATermAppl c);

	/**
	 * Answers the isType question without doing any satisfiability check. It might return <code>Bool.TRUE</code>, <code>Bool.FALSE</code>, or
	 * <code>Bool.UNKNOWN</code>. If <code>Bool.UNKNOWN</code> is returned <code>isType</code> function needs to be called to get the answer.
	 *
	 * @param x
	 * @param c
	 * @return true if the term x is of the know type c (class)
	 */
	public Bool isKnownType(final ATermAppl x, final ATermAppl c);

	/**
	 * @return the set of all named classes including TOP and BOTTOM. Returned set is modifiable.
	 */
	public default Set<ATermAppl> getAllClasses()
	{
		return Collections.unmodifiableSet(getTBox().getAllClasses());
	}

	/**
	 * Returns the (named) subclasses of class c. Depending on the second parameter the result will include either all subclasses or only the direct subclasses.
	 * A class d is a direct subclass of c iff
	 * <ol>
	 * <li>d is subclass of c</li>
	 * <li>there is no other class x different from c and d such that x is subclass of c and d is subclass of x</li>
	 * </ol>
	 * The class c itself is not included in the list but all the other classes that are sameAs c are put into the list. Also note that the returned list will
	 * always have at least one element. The list will either include one other concept from the hierarchy or the BOTTOM concept if no other class is subsumed
	 * by c. By definition BOTTOM concept is subclass of every concept.
	 * <p>
	 * *** This function will first classify the whole ontology ***
	 * </p>
	 *
	 * @param c class whose subclasses are returned
	 * @param direct If true return only the direct subclasses, otherwise return all the subclasses
	 * @return A set of sets, where each set in the collection represents an equivalence class. The elements of the inner class are ATermAppl objects.
	 */
	public Set<Set<ATermAppl>> getSubClasses(final ATermAppl c, final boolean direct);

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

	public boolean isClass(final ATerm c);

	/**
	 * @param c1
	 * @param c2
	 * @return true if class c1 is subclass of class c2.
	 */
	public boolean isSubClassOf(final ATermAppl c1, final ATermAppl c2);

	/**
	 * @param d
	 * @param individuals
	 * @return all the individuals that belong to the given class which is not necessarily a named class.
	 */
	public Set<ATermAppl> retrieve(final ATermAppl d, final Collection<ATermAppl> individuals);

	/**
	 * @param r
	 * @return individuals which possibly have a property value for the given property.
	 */
	public List<ATermAppl> retrieveIndividualsWithProperty(final ATermAppl r);

	/**
	 * Returns all the instances of concept c. If TOP concept is used every individual in the knowledge base will be returned
	 *
	 * @param c class whose instances are returned
	 * @return A set of ATerm objects
	 */
	public Set<ATermAppl> getInstances(final ATermAppl c);

	/**
	 * @param c is a classes
	 * @return true if there is at least one named individual that belongs to the given class
	 */
	public boolean hasInstance(final ATerm c);

	/**
	 * Returns the instances of class c. Depending on the second parameter the resulting list will include all or only the direct instances. An individual x is
	 * a direct instance of c iff x is of type c and there is no subclass d of c such that x is of type d.
	 * <p>
	 * *** This function will first realize the whole ontology ***
	 * </p>
	 *
	 * @param c class whose instances are returned
	 * @param direct if true return only the direct instances, otherwise return all the instances
	 * @return A set of ATerm objects
	 */
	public Set<ATermAppl> getInstances(final ATermAppl c, final boolean direct);

	/**
	 * @param name
	 * @return all the individuals asserted to be equal to the given individual including the individual itself.
	 */
	public Set<ATermAppl> getAllSames(final ATermAppl name);

	/**
	 * List all subjects with a given property and property value.
	 *
	 * @param r
	 * @param x If property is an object property an ATermAppl object that is the URI of the _individual, if the property is a _data property an ATerm object
	 *        that contains the literal value (See {#link #getIndividualsWithDataProperty(ATermAppl, ATermAppl)} for details)
	 * @return List of ATermAppl objects.
	 */
	public List<ATermAppl> getIndividualsWithProperty(final ATermAppl r, final ATermAppl x);

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

	public void addDisjointProperties(final ATermList properties);

	public void addDisjointProperty(final ATermAppl p1, final ATermAppl p2);

	public void addSubProperty(final ATerm sub, final ATermAppl sup);

	public void addEquivalentProperty(final ATermAppl p1, final ATermAppl p2);

	public void addDomain(final ATerm p, final ATermAppl c);

	public void addDomain(final ATerm p, final ATermAppl c, final Set<ATermAppl> explain);

	public void addRange(final ATerm p, final ATermAppl c);

	public void addRange(final ATerm p, final ATermAppl c, final Set<ATermAppl> explain);

	public void addInverseProperty(final ATermAppl p1, final ATermAppl p2);

	public void addTransitiveProperty(final ATermAppl p);

	public void addFunctionalProperty(final ATermAppl p);

	public void addInverseFunctionalProperty(final ATerm p);

	public void addSymmetricProperty(final ATermAppl p);

	public void addAsymmetricProperty(final ATermAppl p);

	public void addReflexiveProperty(final ATermAppl p);

	public void addIrreflexiveProperty(final ATermAppl p);

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
	 * Returns all the classes that are equivalent to class c, excluding c itself.
	 * <p>
	 * *** This function will first classify the whole ontology ***
	 * </p>
	 *
	 * @param c class whose equivalent classes are found
	 * @return A set of ATerm objects
	 */
	public Set<ATermAppl> getEquivalentClasses(final ATermAppl c);

	/**
	 * Returns all the classes that are equivalent to class c, including c itself.
	 * <p>
	 * *** This function will first classify the whole ontology ***
	 * </p>
	 *
	 * @param c class whose equivalent classes are found
	 * @return A set of ATerm objects
	 */
	public Set<ATermAppl> getAllEquivalentClasses(final ATermAppl c);

	/**
	 * Returns the (named) superclasses of class c. Depending on the second parameter the resulting list will include either all or only the direct
	 * superclasses. A class d is a direct superclass of c iff
	 * <ol>
	 * <li>d is superclass of c</li>
	 * <li>there is no other class x such that x is superclass of c and d is superclass of x</li>
	 * </ol>
	 * The class c itself is not included in the list but all the other classes that are sameAs c are put into the list. Also note that the returned list will
	 * always have at least one element. The list will either include one other concept from the hierarchy or the TOP concept if no other class subsumes c. By
	 * definition TOP concept is superclass of every concept.
	 * <p>
	 * *** This function will first classify the whole ontology ***
	 * </p>
	 *
	 * @param cParam class whose superclasses are returned
	 * @param direct
	 * @return A set of sets, where each set in the collection represents an equivalence class. The elements of the inner class are ATermAppl objects.
	 */
	public Set<Set<ATermAppl>> getSuperClasses(final ATermAppl cParam, final boolean direct);

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

	public Set<Set<ATermAppl>> getDisjointClasses(final ATermAppl c, final boolean direct);

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

	/**
	 * Return all literal values for a given dataproperty that belongs to the specified datatype.
	 *
	 * @param r
	 * @param lang
	 * @param datatype
	 * @return List of ATermAppl objects representing literals. These objects are in the form literal(value, lang, datatypeURI).
	 */
	public List<ATermAppl> getDataPropertyValues(final ATermAppl r, final ATermAppl lang, final ATermAppl datatype);

	/**
	 * Return all literal values for a given dataproperty and subject value.
	 *
	 * @param r
	 * @param x
	 * @return List of ATermAppl objects.
	 */
	public default List<ATermAppl> getDataPropertyValues(final ATermAppl r, final ATermAppl x)
	{
		return getDataPropertyValues(r, x, (ATermAppl) null);
	}

	/**
	 * Return all property values for a given object property and subject value.
	 *
	 * @param r
	 * @param x
	 * @return A list of ATermAppl objects
	 */
	public List<ATermAppl> getObjectPropertyValues(final ATermAppl r, final ATermAppl x);

	public Set<ATermAppl> getAllEquivalentProperties(final ATermAppl prop);

	public Set<Set<ATermAppl>> getDisjointProperties(final ATermAppl p);

	public boolean isDatatype(final ATermAppl c);

	// ----------------------------------------- Consulting State -----------------------------------------------------

	public boolean isAsymmetricProperty(final ATermAppl p);

	public boolean isReflexiveProperty(final ATermAppl p);

	public boolean isDisjoint(final ATermAppl c1, final ATermAppl c2);

	public boolean hasDomain(final ATermAppl p, final ATermAppl c);

	public boolean isEquivalentProperty(final ATermAppl p1, final ATermAppl p2);

	public boolean isDifferentFrom(final ATermAppl t1, final ATermAppl t2);

	public boolean isDisjointProperty(final ATermAppl r1, final ATermAppl r2);

	public boolean hasRange(final ATermAppl p, final ATermAppl c);

	public boolean isFunctionalProperty(final ATermAppl p);

	public boolean isSubPropertyOf(final ATermAppl sub, final ATermAppl sup);

	public boolean isInverse(final ATermAppl r1, final ATermAppl r2);

	public default boolean isSymmetricProperty(final ATermAppl p)
	{
		return isInverse(p, p);
	}

	/**
	 * @param c1
	 * @param c2
	 * @return true if class c1 is equivalent to class c2.
	 */
	public boolean isEquivalentClass(final ATermAppl c1, final ATermAppl c2);

	public boolean isTransitiveProperty(final ATermAppl r);

	public boolean isIrreflexiveProperty(final ATermAppl p);

	public boolean isInverseFunctionalProperty(final ATermAppl p);

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

	/**
	 * Print the class hierarchy on the standard output.
	 */
	public void printClassTree();

	public enum ChangeType
	{
		ABOX_ADD, ABOX_DEL, TBOX_ADD, TBOX_DEL, RBOX_ADD, RBOX_DEL
	}

	public boolean isChanged(final ChangeType change);
}
