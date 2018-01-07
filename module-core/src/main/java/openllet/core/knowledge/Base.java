package openllet.core.knowledge;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.KnowledgeBase.ChangeType;
import openllet.core.OpenlletOptions;
import openllet.core.PropertyType;
import openllet.core.boxes.rbox.Role;
import openllet.core.el.SimplifiedELClassifier;
import openllet.core.exceptions.UndefinedEntityException;
import openllet.core.expressivity.ExpressivityChecker;
import openllet.core.taxonomy.CDOptimizedTaxonomyBuilder;
import openllet.core.taxonomy.TaxonomyBuilder;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.progress.ProgressMonitor;

/**
 * @since 2.6.4
 */
public interface Base extends Boxes
{
	/**
	 * @return the set of all individuals. Returned set is unmodifiable!
	 */
	public Set<ATermAppl> getIndividuals();

	public default boolean isIndividual(final ATerm ind)
	{
		return getIndividuals().contains(ind);
	}

	public Map<ATermAppl, Set<ATermAppl>> getInstances();

	public TaxonomyBuilder getBuilder();

	public ExpressivityChecker getExpChecker();

	public EnumSet<ChangeType> getChanges();

	public Map<ATermAppl, Map<ATermAppl, Set<ATermAppl>>> getAnnotations();

	public static void handleUndefinedEntity(final String s)
	{
		if (!OpenlletOptions.SILENT_UNDEFINED_ENTITY_HANDLING)
			throw new UndefinedEntityException(s);
	}

	public Optional<TaxonomyBuilder> getOptTaxonomyBuilder();

	public void setOptTaxonomyBuilder(final Optional<TaxonomyBuilder> builder);

	public void ensureConsistency();

	public boolean isSatisfiable(final ATermAppl c);

	public void realize();

	public void prepare();

	public void classify();

	public KnowledgeBase getKnowledgeBase();

	/**
	 * @return true if the classification check has been done and nothing in the KB has changed after that.
	 */
	public boolean isClassified();

	public boolean isRealized();

	public ProgressMonitor getBuilderProgressMonitor();

	public void setBuilderProgressMonitor(ProgressMonitor builderProgressMonitor);

	public FullyDefinedClassVisitor getFullyDefinedVisitor();

	public DatatypeVisitor getDatatypeVisitor();

	public default TaxonomyBuilder getTaxonomyBuilder()
	{
		if (!getOptTaxonomyBuilder().isPresent())
		{
			prepare();
			TaxonomyBuilder builder;

			if (getExpChecker().getExpressivity().isEL() && !OpenlletOptions.DISABLE_EL_CLASSIFIER)
				builder = new SimplifiedELClassifier(getKnowledgeBase());
			else
				builder = new CDOptimizedTaxonomyBuilder(getKnowledgeBase());
			//builder = new CDOptimizedTaxonomyBuilderProb(this, Optional.ofNullable(_builderProgressMonitor));

			if (getBuilderProgressMonitor() != null)
				builder.setProgressMonitor(getBuilderProgressMonitor());

			setOptTaxonomyBuilder(Optional.of(builder));
		}

		return getOptTaxonomyBuilder().get();
	}

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
	 * @param subCls
	 * @param supCls
	 * @return true if class subCls is subclass of class supCls.
	 */
	public boolean isSubClassOf(final ATermAppl subCls, final ATermAppl supCls);

	/**
	 * @return the set of all properties.
	 */
	public default Set<ATermAppl> getProperties()
	{
		final Set<ATermAppl> set = new HashSet<>();
		for (final Role role : getRBox().getRoles().values())
		{
			final ATermAppl p = role.getName();
			if (ATermUtils.isPrimitive(p) && (role.isObjectRole() || role.isDatatypeRole() || role.isAnnotationRole()))
				set.add(p);
		}
		return set;
	}

	/**
	 * @return the set of key values of the annotations map
	 */
	public default Set<ATermAppl> getAnnotationSubjects()
	{
		return getAnnotations().keySet();
	}

	public default PropertyType getPropertyType(final ATerm r)
	{
		final Role role = getProperty(r);
		return role == null ? PropertyType.UNTYPED : role.getType();
	}

	public default boolean isClass(final ATerm c)
	{

		if (getTBox().getClasses().contains(c) || c.equals(ATermUtils.TOP))
			return true;
		else
			if (ATermUtils.isComplexClass(c))
				return getFullyDefinedVisitor().isFullyDefined((ATermAppl) c);
			else
				return false;
	}

	public default boolean isDatatypeProperty(final ATerm p)
	{
		return null != p && getPropertyType(p) == PropertyType.DATATYPE;
	}

	public default boolean isDatatype(final ATermAppl c)
	{
		if (null == c)
			return false;

		return getDatatypeVisitor().isDatatype(c);
	}

	public default boolean isObjectProperty(final ATerm p)
	{
		return null != p && getPropertyType(p) == PropertyType.OBJECT;
	}

	public default boolean isABoxProperty(final ATerm p)
	{
		if (null == p)
			return false;

		final PropertyType type = getPropertyType(p);
		return type == PropertyType.OBJECT || type == PropertyType.DATATYPE;
	}

	public default boolean isAnnotationProperty(final ATerm p)
	{
		return p != null && getPropertyType(p) == PropertyType.ANNOTATION;
	}

	public default boolean isProperty(final ATerm p)
	{
		return getRBox().isRole(p);
	}

	/**
	 * @return the set of all named classes. Returned set is unmodifiable!
	 */
	public default Set<ATermAppl> getClasses()
	{
		return Collections.unmodifiableSet(getTBox().getClasses());
	}

	/**
	 * @return the set of all named classes including TOP and BOTTOM. Returned set is modifiable.
	 */
	public default Set<ATermAppl> getAllClasses()
	{
		return Collections.unmodifiableSet(getTBox().getAllClasses());
	}

	/**
	 * @return same as getAllClasses but can be lazy.
	 * @since 2.6.2
	 */
	public default Stream<ATermAppl> allClasses()
	{
		return getTBox().allClasses();
	}

	/**
	 * @param term
	 * @return a role
	 */
	public default Role getRole(final ATerm term)
	{
		return getRBox().getRole(term);
	}

	public default Role getProperty(final ATerm r)
	{
		return getRBox().getRole(r);
	}

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

	public void binaryInstanceRetrieval(final ATermAppl c, final List<ATermAppl> candidates, final Collection<ATermAppl> results);
}
