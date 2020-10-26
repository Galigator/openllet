package openllet.aterm.pure.binary;

import openllet.aterm.ATerm;

/**
 * A structure that contains all information we need for reconstructing a term.
 *
 * @author Arnold Lankamp
 */
class ATermConstruct
{
	public int type;

	public int termIndex = 0;
	public ATerm tempTerm = null;

	public int subTermIndex = 0;
	public ATerm[] subTerms = null;

	ATermConstruct(final int type_, final int termIndex_)
	{
		type = type_;
		termIndex = termIndex_;
	}
}
