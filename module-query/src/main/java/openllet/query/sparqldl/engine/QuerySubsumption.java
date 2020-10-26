package openllet.query.sparqldl.engine;

import java.util.List;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.exceptions.UnsupportedQueryException;
import openllet.core.utils.TermFactory;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.QueryAtom;
import openllet.query.sparqldl.model.QueryResult;

/**
 * Implements various methods regarding conjunctive query subsumption based on the ABox freezing method.
 *
 * @author Hector Perez-Urbina
 */
public class QuerySubsumption
{
	/**
	 * @param q1 sub
	 * @param q2 sup
	 * @return Checks whether sub is equivalent to sup
	 */
	public static boolean isEquivalentTo(final Query q1, final Query q2)
	{
		return isSubsumedBy(q1, q2) && isSubsumedBy(q2, q1);
	}

	/**
	 * @param sub
	 * @param sup
	 * @return Checks whether sub is subsumed by sup
	 */
	public static boolean isSubsumedBy(final Query sub, final Query sup)
	{
		return !getSubsumptionMappings(sub, sup).isEmpty();
	}

	/**
	 * @param sub
	 * @param sup
	 * @return Computes the subsumption mappings between sub and sup
	 */
	public static QueryResult getSubsumptionMappings(final Query sub, final Query sup)
	{
		final KnowledgeBase kb = sup.getKB().copy(true);

		final List<QueryAtom> queryAtoms = sub.getAtoms();
		for (final QueryAtom queryAtom2 : queryAtoms)
		{
			final QueryAtom queryAtom = queryAtom2;
			final List<ATermAppl> arguments = queryAtom.getArguments();

			ATermAppl ind1 = null;
			ATermAppl ind2 = null;
			ATermAppl pr = null;
			ATermAppl cl = null;

			switch (queryAtom.getPredicate())
			{
				case Type:
					ind1 = TermFactory.term(arguments.get(0).toString());
					cl = arguments.get(1);
					kb.addIndividual(ind1);
					kb.addType(ind1, cl);
					break;
				case PropertyValue:
					ind1 = TermFactory.term(arguments.get(0).toString());
					pr = arguments.get(1);
					ind2 = TermFactory.term(arguments.get(2).toString());
					kb.addIndividual(ind1);
					kb.addIndividual(ind2);
					kb.addPropertyValue(pr, ind1, ind2);
					break;
				case SameAs:
					ind1 = TermFactory.term(arguments.get(0).toString());
					ind2 = TermFactory.term(arguments.get(1).toString());
					kb.addIndividual(ind1);
					kb.addIndividual(ind2);
					kb.addSame(ind1, ind2);
					break;
				case DifferentFrom:
					ind1 = TermFactory.term(arguments.get(0).toString());
					ind2 = TermFactory.term(arguments.get(1).toString());
					kb.addIndividual(ind1);
					kb.addIndividual(ind2);
					kb.addDifferent(ind1, ind2);
					break;
				default:
					throw new UnsupportedQueryException("Unsupported atom type : " + queryAtom.getPredicate().toString());
			}
		}

		kb.isConsistent();

		sup.setKB(kb);
		final QueryResult results = QueryEngine.exec(sup);
		sup.setKB(sup.getKB());

		return results;
	}

}
