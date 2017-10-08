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

import java.util.Collection;
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
import openllet.core.boxes.tbox.TBox;
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

public class KnowledgeBaseImplFullSync extends KnowledgeBaseImpl
{
	public KnowledgeBaseImplFullSync()
	{
		super();
	}

	protected KnowledgeBaseImplFullSync(final KnowledgeBaseImpl kb, final boolean emptyABox)
	{
		super(kb, emptyABox);
	}

	@Override
	public KnowledgeBase copy(final boolean emptyABox)
	{
		return new KnowledgeBaseImplFullSync(this, emptyABox);
	}

	@Override
	public synchronized TaxonomyBuilder getBuilder()
	{
		return super.getBuilder();
	}

	@Override
	public synchronized Expressivity getExpressivity()
	{
		return super.getExpressivity();
	}

	@Override
	public synchronized ExpressivityChecker getExpressivityChecker()
	{
		return super.getExpressivityChecker();
	}

	@Override
	public synchronized void clear()
	{
		super.clear();
	}

	@Override
	public synchronized void clearABox()
	{
		super.clearABox();
	}

	@Override
	public synchronized void addClass(final ATermAppl c)
	{
		super.addClass(c);
	}

	@Override
	public synchronized void addSubClass(final ATermAppl sub, final ATermAppl sup)
	{
		super.addSubClass(sub, sup);
	}

	@Override
	public synchronized void addEquivalentClass(final ATermAppl c1, final ATermAppl c2)
	{
		super.addEquivalentClass(c1, c2);
	}

	@Override
	public synchronized void addKey(final ATermAppl c, final Set<ATermAppl> properties)
	{
		super.addKey(c, properties);
	}

	@Override
	public synchronized void addDisjointClasses(final ATermList classes)
	{
		super.addDisjointClasses(classes);
	}

	@Override
	public synchronized void addDisjointClasses(final List<ATermAppl> classes)
	{
		super.addDisjointClasses(classes);
	}

	@Override
	public synchronized void addDisjointClass(final ATermAppl c1, final ATermAppl c2)
	{
		super.addDisjointClass(c1, c2);
	}

	@Override
	public synchronized void addComplementClass(final ATermAppl c1, final ATermAppl c2)
	{
		super.addComplementClass(c1, c2);
	}

	@Override
	public synchronized Individual addIndividual(final ATermAppl i)
	{
		return super.addIndividual(i);
	}

	@Override
	public synchronized void addType(final ATermAppl i, final ATermAppl c)
	{
		super.addType(i, c);
	}

	@Override
	public synchronized void addType(final ATermAppl i, final ATermAppl c, final DependencySet ds)
	{
		super.addType(i, c, ds);
	}

	@Override
	public synchronized void addSame(final ATermAppl i1, final ATermAppl i2)
	{
		super.addSame(i1, i2);
	}

	@Override
	public synchronized void addAllDifferent(final ATermList list)
	{
		super.addAllDifferent(list);
	}

	@Override
	public synchronized void addDifferent(final ATermAppl i1, final ATermAppl i2)
	{
		super.addDifferent(i1, i2);
	}

	@Override
	@Deprecated
	public synchronized void addObjectPropertyValue(final ATermAppl p, final ATermAppl s, final ATermAppl o)
	{
		super.addObjectPropertyValue(p, s, o);
	}

	@Override
	public synchronized boolean addPropertyValue(final ATermAppl p, final ATermAppl s, final ATermAppl o)
	{
		return super.addPropertyValue(p, s, o);
	}

	@Override
	public synchronized boolean addNegatedPropertyValue(final ATermAppl p, final ATermAppl s, final ATermAppl o)
	{
		return super.addNegatedPropertyValue(p, s, o);
	}

	@Override
	public synchronized void addProperty(final ATermAppl p)
	{
		super.addProperty(p);
	}

	@Override
	public synchronized boolean addObjectProperty(final ATerm p)
	{
		return super.addObjectProperty(p);
	}

	@Override
	public synchronized boolean addDatatypeProperty(final ATerm p)
	{
		return super.addDatatypeProperty(p);
	}

	@Override
	@Deprecated
	public synchronized void addOntologyProperty(final ATermAppl p)
	{
		super.addOntologyProperty(p);
	}

	@Override
	public synchronized boolean addAnnotationProperty(final ATerm p)
	{
		return super.addAnnotationProperty(p);
	}

	@Override
	public synchronized boolean addAnnotation(final ATermAppl s, final ATermAppl p, final ATermAppl o)
	{
		return super.addAnnotation(s, p, o);
	}

	@Override
	public synchronized Set<ATermAppl> getAnnotations(final ATermAppl s, final ATermAppl p)
	{
		return super.getAnnotations(s, p);
	}

	@Override
	public synchronized Set<ATermAppl> getIndividualsWithAnnotation(final ATermAppl p, final ATermAppl o)
	{
		return super.getIndividualsWithAnnotation(p, o);
	}

	@Override
	public synchronized boolean isAnnotation(final ATermAppl s, final ATermAppl p, final ATermAppl o)
	{
		return super.isAnnotation(s, p, o);
	}

	@Override
	public synchronized void addSubProperty(final ATerm sub, final ATermAppl sup)
	{
		super.addSubProperty(sub, sup);
	}

	@Override
	public synchronized void addEquivalentProperty(final ATermAppl p1, final ATermAppl p2)
	{
		super.addEquivalentProperty(p1, p2);
	}

	@Override
	public synchronized void addDisjointProperties(final ATermList properties)
	{
		super.addDisjointProperties(properties);
	}

	@Override
	public synchronized void addDisjointProperty(final ATermAppl p1, final ATermAppl p2)
	{
		super.addDisjointProperty(p1, p2);
	}

	@Override
	public synchronized void addDisjointProperty(final ATermAppl p1, final ATermAppl p2, final DependencySet ds)
	{
		super.addDisjointProperty(p1, p2, ds);
	}

	@Override
	public synchronized void addInverseProperty(final ATermAppl p1, final ATermAppl p2)
	{
		super.addInverseProperty(p1, p2);
	}

	@Override
	public synchronized void addTransitiveProperty(final ATermAppl p)
	{
		super.addTransitiveProperty(p);
	}

	@Override
	public synchronized void addSymmetricProperty(final ATermAppl p)
	{
		super.addSymmetricProperty(p);
	}

	@Override
	@Deprecated
	public synchronized void addAntisymmetricProperty(final ATermAppl p)
	{
		super.addAntisymmetricProperty(p);
	}

	@Override
	public synchronized void addAsymmetricProperty(final ATermAppl p)
	{
		super.addAsymmetricProperty(p);
	}

	@Override
	public synchronized void prepare()
	{
		super.prepare();
	}

	@Override
	public synchronized Timers getTimers()
	{
		return super.getTimers();
	}

	@Override
	public synchronized SizeEstimate getSizeEstimate()
	{
		return super.getSizeEstimate();
	}

	@Override
	public synchronized ABox getABox()
	{
		return super.getABox();
	}

	@Override
	public synchronized TBox getTBox()
	{
		return super.getTBox();
	}

	@Override
	public synchronized RBox getRBox()
	{
		return super.getRBox();
	}

	@Override
	public synchronized int getIndividualsCount()
	{
		return super.getIndividualsCount();
	}

	@Override
	public synchronized Set<ATermAppl> getIndividuals()
	{
		return super.getIndividuals();
	}

	@Override
	public synchronized Stream<ATermAppl> individuals()
	{
		return super.individuals();
	}

	@Override
	public synchronized void classify()
	{
		super.classify();
	}

	@Override
	public synchronized void realize()
	{
		super.realize();
	}

	@Override
	public synchronized boolean isClassified()
	{
		return super.isClassified();
	}

	@Override
	public synchronized boolean isConsistent()
	{
		return super.isConsistent();
	}

	@Override
	public synchronized void ensureConsistency()
	{
		super.ensureConsistency();
	}

	@Override
	public synchronized boolean isConsistencyDone()
	{
		return super.isConsistencyDone();
	}

	@Override
	public synchronized Taxonomy<ATermAppl> getTaxonomy()
	{
		return super.getTaxonomy();
	}

	@Override
	public synchronized boolean isDatatypeProperty(final ATerm p)
	{
		return super.isDatatypeProperty(p);
	}

	@Override
	public synchronized DependencyIndex getDependencyIndex()
	{
		return super.getDependencyIndex();
	}

	@Override
	public synchronized Set<ATermAppl> getSyntacticAssertions()
	{
		return super.getSyntacticAssertions();
	}

	@Override
	public synchronized Set<ATermAppl> getDeletedAssertions()
	{
		return super.getDeletedAssertions();
	}

	@Override
	public synchronized CompletionStrategy chooseStrategy(final ABox abox, final Expressivity expressivity)
	{
		return super.chooseStrategy(abox, expressivity);
	}

	@Override
	public synchronized boolean isRealized()
	{
		return super.isRealized();
	}

	@Override
	public synchronized boolean isSatisfiable(final ATermAppl c)
	{
		return super.isSatisfiable(c);
	}

	@Override
	public synchronized Set<ATermAppl> getUnsatisfiableClasses()
	{
		return super.getUnsatisfiableClasses();
	}

	@Override
	public synchronized Set<ATermAppl> getAllUnsatisfiableClasses()
	{
		return super.getAllUnsatisfiableClasses();
	}

	@Override
	public synchronized boolean isDisjointClass(final ATermAppl c1, final ATermAppl c2)
	{
		return super.isDisjointClass(c1, c2);
	}

	@Override
	public synchronized Map<Rule, Rule> getNormalizedRules()
	{
		return super.getNormalizedRules();
	}

	@Override
	public synchronized Set<Rule> getRules()
	{
		return super.getRules();
	}

	@Override
	public synchronized boolean isComplement(final ATermAppl c1, final ATermAppl c2)
	{
		return super.isComplement(c1, c2);
	}

	@Override
	public synchronized Set<ATermAppl> getProperties()
	{
		return super.getProperties();
	}

	@Override
	public synchronized Set<ATermAppl> getObjectProperties()
	{
		return super.getObjectProperties();
	}

	@Override
	public synchronized Set<ATermAppl> getDataProperties()
	{
		return super.getDataProperties();
	}

	@Override
	public synchronized Set<Set<ATermAppl>> getAllSuperProperties(final ATermAppl prop)
	{
		return super.getAllSuperProperties(prop);
	}

	@Override
	public synchronized Set<Set<ATermAppl>> getSubProperties(final ATermAppl prop, final boolean direct)
	{
		return super.getSubProperties(prop, direct);
	}

	@Override
	public synchronized Set<Set<ATermAppl>> getSuperProperties(final ATermAppl prop, final boolean direct)
	{
		return super.getSuperProperties(prop, direct);
	}

	@Override
	public synchronized Set<Set<ATermAppl>> getAllSubProperties(final ATermAppl prop)
	{
		return super.getAllSubProperties(prop);
	}

	@Override
	public synchronized Set<ATermAppl> getEquivalentProperties(final ATermAppl prop)
	{
		return super.getEquivalentProperties(prop);
	}

	@Override
	public synchronized Set<ATermAppl> getFunctionalProperties()
	{
		return super.getFunctionalProperties();
	}

	@Override
	public synchronized Set<ATermAppl> getInverseFunctionalProperties()
	{
		return super.getInverseFunctionalProperties();
	}

	@Override
	public synchronized Set<ATermAppl> getTransitiveProperties()
	{
		return super.getTransitiveProperties();
	}

	@Override
	public synchronized Set<ATermAppl> getSymmetricProperties()
	{
		return super.getSymmetricProperties();
	}

	@Override
	public synchronized Set<ATermAppl> getAsymmetricProperties()
	{
		return null;
	}

	@Override
	public synchronized Set<ATermAppl> getInverses(final ATerm name)
	{
		return super.getInverses(name);
	}

	@Override
	public synchronized boolean isObjectProperty(final ATerm p)
	{
		return super.isObjectProperty(p);
	}

	@Override
	public synchronized Map<ATermAppl, List<ATermAppl>> getPropertyValues(final ATermAppl pred)
	{
		return super.getPropertyValues(pred);
	}

	@Override
	public synchronized List<ATermAppl> getProperties(final ATermAppl s, final ATermAppl o)
	{
		return super.getProperties(s, o);
	}

	@Override
	public synchronized PropertyType getPropertyType(final ATerm r)
	{
		return super.getPropertyType(r);
	}

	@Override
	public synchronized Set<ATermAppl> getReflexiveProperties()
	{
		return super.getReflexiveProperties();
	}

	@Override
	public synchronized Set<ATermAppl> getIrreflexiveProperties()
	{
		return super.getIrreflexiveProperties();
	}

	@Override
	public synchronized Bool hasKnownPropertyValue(final ATermAppl s, final ATermAppl p, final ATermAppl o)
	{
		return super.hasKnownPropertyValue(s, p, o);
	}

	@Override
	public synchronized boolean hasPropertyValue(final ATermAppl s, final ATermAppl p, final ATermAppl o)
	{
		return super.hasPropertyValue(s, p, o);
	}

	@Override
	public synchronized List<ATermAppl> getPropertyValues(final ATermAppl r, final ATermAppl x)
	{
		return super.getPropertyValues(r, x);
	}

	@Override
	public synchronized Set<ATermAppl> getAnnotationProperties()
	{
		return super.getAnnotationProperties();
	}

	@Override
	public synchronized Set<ATermAppl> getDomains(final ATermAppl name)
	{
		return super.getDomains(name);
	}

	@Override
	public synchronized Set<ATermAppl> getRanges(final ATerm name)
	{
		return super.getRanges(name);
	}

	@Override
	public synchronized boolean isAnnotationProperty(final ATerm p)
	{
		return super.isAnnotationProperty(p);
	}

	@Override
	public synchronized Set<ATermAppl> getAnnotationSubjects()
	{
		return super.getAnnotationSubjects();
	}

	@Override
	public synchronized Taxonomy<ATermAppl> getRoleTaxonomy(final boolean objectTaxonomy)
	{
		return super.getRoleTaxonomy(objectTaxonomy);
	}

	@Override
	public Taxonomy<ATermAppl> getToldTaxonomy()
	{
		return super.getToldTaxonomy();
	}

	@Override
	public synchronized TaxonomyBuilder getTaxonomyBuilder()
	{
		return super.getTaxonomyBuilder();
	}

	@Override
	public synchronized Map<ATermAppl, Set<ATermAppl>> getToldDisjoints()
	{
		return super.getToldDisjoints();
	}

	@Override
	public synchronized Set<Set<ATermAppl>> getTypes(final ATermAppl ind, final boolean direct)
	{
		return super.getTypes(ind, direct);
	}

	@Override
	public synchronized boolean isType(final ATermAppl x, final ATermAppl c)
	{
		return super.isType(x, c);
	}

	@Override
	public synchronized Bool isKnownType(final ATermAppl x, final ATermAppl c)
	{
		return super.isKnownType(x, c);
	}

	@Override
	public synchronized Set<Set<ATermAppl>> getSubClasses(final ATermAppl c, final boolean direct)
	{
		return super.getSubClasses(c, direct);
	}

	@Override
	public synchronized boolean isClass(final ATerm c)
	{
		return super.isClass(c);
	}

	@Override
	public synchronized boolean isSubClassOf(final ATermAppl c1, final ATermAppl c2)
	{
		return super.isSubClassOf(c1, c2);
	}

	@Override
	public synchronized Set<ATermAppl> retrieve(final ATermAppl d, final Collection<ATermAppl> individuals)
	{
		return super.retrieve(d, individuals);
	}

	@Override
	public synchronized List<ATermAppl> retrieveIndividualsWithProperty(final ATermAppl r)
	{
		return super.retrieveIndividualsWithProperty(r);
	}

	@Override
	public synchronized Set<ATermAppl> getInstances(final ATermAppl c)
	{
		return super.getInstances(c);
	}

	@Override
	public synchronized boolean hasInstance(final ATerm c)
	{
		return super.hasInstance(c);
	}

	@Override
	public synchronized Set<ATermAppl> getInstances(final ATermAppl c, final boolean direct)
	{
		return super.getInstances(c, direct);
	}

	@Override
	public synchronized Set<ATermAppl> getAllSames(final ATermAppl name)
	{
		return super.getAllSames(name);
	}

	@Override
	public synchronized List<ATermAppl> getIndividualsWithProperty(final ATermAppl r, final ATermAppl x)
	{
		return super.getIndividualsWithDataProperty(r, x);
	}

	@Override
	public synchronized void addDomain(final ATerm p, final ATermAppl c)
	{
		super.addDomain(p, c);
	}

	@Override
	public synchronized void addDomain(final ATerm p, final ATermAppl c, final Set<ATermAppl> explain)
	{
		super.addDomain(p, c, explain);
	}

	@Override
	public synchronized void addRange(final ATerm p, final ATermAppl c)
	{
		super.addRange(p, c);
	}

	@Override
	public synchronized void addRange(final ATerm p, final ATermAppl c, final Set<ATermAppl> explain)
	{
		super.addRange(p, c, explain);
	}

	@Override
	public synchronized void addFunctionalProperty(final ATermAppl p)
	{
		super.addFunctionalProperty(p);
	}

	@Override
	public synchronized void addInverseFunctionalProperty(final ATerm p)
	{
		super.addInverseFunctionalProperty(p);
	}

	@Override
	public synchronized void addReflexiveProperty(final ATermAppl p)
	{
		super.addReflexiveProperty(p);
	}

	@Override
	public synchronized void addIrreflexiveProperty(final ATermAppl p)
	{
		super.addIrreflexiveProperty(p);
	}

	@Override
	public synchronized void addDatatype(final ATermAppl p)
	{
		super.addDatatype(p);
	}

	@Override
	public synchronized boolean addDatatypeDefinition(final ATermAppl name, final ATermAppl datarange)
	{
		return super.addDatatypeDefinition(name, datarange);
	}

	@Override
	public synchronized boolean addRule(final Rule rule)
	{
		return super.addRule(rule);
	}

	@Override
	public synchronized boolean removeType(final ATermAppl ind, final ATermAppl c)
	{
		return super.removeType(ind, c);
	}

	@Override
	public synchronized boolean removeDomain(final ATerm p, final ATermAppl c)
	{
		return super.removeDomain(p, c);
	}

	@Override
	public synchronized boolean removeRange(final ATerm p, final ATermAppl c)
	{
		return super.removeRange(p, c);
	}

	@Override
	public synchronized boolean removePropertyValue(final ATermAppl p, final ATermAppl i1, final ATermAppl i2)
	{
		return super.removePropertyValue(p, i1, i2);
	}

	@Override
	public synchronized boolean removeAxiom(final ATermAppl axiom)
	{
		return super.removeAxiom(axiom);
	}

	@Override
	public synchronized void setTaxonomyBuilderProgressMonitor(final ProgressMonitor progressMonitor)
	{
		super.setTaxonomyBuilderProgressMonitor(progressMonitor);
	}

	@Override
	public synchronized Set<ATermAppl> getEquivalentClasses(final ATermAppl c)
	{
		return super.getEquivalentClasses(c);
	}

	@Override
	public synchronized Set<ATermAppl> getAllEquivalentClasses(final ATermAppl c)
	{
		return super.getAllEquivalentClasses(c);
	}

	@Override
	public synchronized Set<Set<ATermAppl>> getSuperClasses(final ATermAppl cParam, final boolean direct)
	{
		return super.getSuperClasses(cParam, direct);
	}

	@Override
	public synchronized Set<Set<ATermAppl>> getDisjointClasses(final ATermAppl c, final boolean direct)
	{
		return super.getDisjointClasses(c, direct);
	}

	@Override
	public synchronized Set<ATermAppl> getComplements(final ATermAppl c)
	{
		return super.getComplements(c);
	}

	@Override
	public synchronized Set<ATermAppl> getSames(final ATermAppl name)
	{
		return super.getSames(name);
	}

	@Override
	public synchronized Set<ATermAppl> getDifferents(final ATermAppl name)
	{
		return super.getDifferents(name);
	}

	@Override
	public synchronized List<ATermAppl> getDataPropertyValues(final ATermAppl r, final ATermAppl lang, final ATermAppl datatype)
	{
		return super.getDataPropertyValues(r, lang, datatype);
	}

	@Override
	public synchronized List<ATermAppl> getObjectPropertyValues(final ATermAppl r, final ATermAppl x)
	{
		return super.getObjectPropertyValues(r, x);
	}

	@Override
	public synchronized Stream<ATermAppl> objectPropertyValues(final ATermAppl r, final ATermAppl x)
	{
		return super.objectPropertyValues(r, x);
	}

	@Override
	public synchronized Set<ATermAppl> getAllEquivalentProperties(final ATermAppl prop)
	{
		return super.getAllEquivalentProperties(prop);
	}

	@Override
	public synchronized Set<Set<ATermAppl>> getDisjointProperties(final ATermAppl p)
	{
		return super.getDisjointProperties(p);
	}

	@Override
	public synchronized boolean isDatatype(final ATermAppl c)
	{
		return super.isDatatype(c);
	}

	@Override
	public synchronized boolean isAsymmetricProperty(final ATermAppl p)
	{
		return super.isAsymmetricProperty(p);
	}

	@Override
	public synchronized boolean isReflexiveProperty(final ATermAppl p)
	{
		return super.isReflexiveProperty(p);
	}

	@Override
	public synchronized boolean isDisjoint(final ATermAppl c1, final ATermAppl c2)
	{
		return super.isDisjoint(c1, c2);
	}

	@Override
	public synchronized boolean hasDomain(final ATermAppl p, final ATermAppl c)
	{
		return super.hasDomain(p, c);
	}

	@Override
	public synchronized boolean isEquivalentProperty(final ATermAppl p1, final ATermAppl p2)
	{
		return super.isEquivalentProperty(p1, p2);
	}

	@Override
	public synchronized boolean isDifferentFrom(final ATermAppl t1, final ATermAppl t2)
	{
		return super.isDifferentFrom(t1, t2);
	}

	@Override
	public synchronized boolean isDisjointProperty(final ATermAppl r1, final ATermAppl r2)
	{
		return super.isDisjointProperty(r1, r2);
	}

	@Override
	public synchronized boolean hasRange(final ATermAppl p, final ATermAppl c)
	{
		return super.hasRange(p, c);
	}

	@Override
	public synchronized boolean isFunctionalProperty(final ATermAppl p)
	{
		return super.isFunctionalProperty(p);
	}

	@Override
	public synchronized boolean isSubPropertyOf(final ATermAppl sub, final ATermAppl sup)
	{
		return super.isSubPropertyOf(sub, sup);
	}

	@Override
	public synchronized boolean isInverse(final ATermAppl r1, final ATermAppl r2)
	{
		return super.isInverse(r1, r2);
	}

	@Override
	public synchronized boolean isEquivalentClass(final ATermAppl c1, final ATermAppl c2)
	{
		return super.isEquivalentClass(c1, c2);
	}

	@Override
	public synchronized boolean isTransitiveProperty(final ATermAppl r)
	{
		return super.isTransitiveProperty(r);
	}

	@Override
	public synchronized boolean isIrreflexiveProperty(final ATermAppl p)
	{
		return super.isIrreflexiveProperty(p);
	}

	@Override
	public synchronized boolean isInverseFunctionalProperty(final ATermAppl p)
	{
		return super.isInverseFunctionalProperty(p);
	}

	@Override
	public synchronized boolean isSameAs(final ATermAppl t1, final ATermAppl t2)
	{
		return super.isSameAs(t1, t2);
	}

	@Override
	public synchronized void printClassTree()
	{
		super.printClassTree();
	}

	@Override
	public synchronized boolean isChanged(final ChangeType change)
	{
		return super.isChanged(change);
	}
}
