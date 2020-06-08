package openllet.aterm.pure.binary;

import openllet.aterm.ATerm;

/**
 * A structure that contains all information we need for reconstructing a term.
 *
 * @author Arnold Lankamp
 */
class ATermConstruct
{
	public int		type;

	public int		termIndex		= 0;
	public ATerm	tempTerm		= null;

	public int		subTermIndex	= 0;
	public ATerm[]	subTerms		= null;

	ATermConstruct(final int type, final int termIndex)
	{
		this.type = type;
		this.termIndex = termIndex;
	}
}
