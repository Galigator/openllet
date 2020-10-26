// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.boxes.abox;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.core.DependencySet;
import openllet.core.utils.ATermUtils;

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
public class Clash
{
	public final ATerm[] _args;
	private volatile DependencySet _depends; // Warn, some functions get the _depends then change its value behind us.
	private final Node _node;
	private final ClashType _type;
	private final String _clashExplanation;

	public enum ClashType
	{
		ATOMIC("An individual belongs to a type and its complement"), //
		MIN_MAX("An individual contains a minCardinality restriction that is greater than a maxCardinality restriction"), //
		MAX_CARD("The maxCardinality restriction is violated"), //
		FUNC_MAX_CARD("An individual contains a minCardinality restriction that is greater than a maxCardinality restriction"), //
		//		MAX_ZERO("The maxCardinality(0) restriction is violated"), // dead code.
		NOMINAL("An individual is sameAs and differentFrom another individual at the same time"), //
		EMPTY_DATATYPE("Range restrictions on a literal is inconsistent"), //
		VALUE_DATATYPE("The literal value does not satisfy the datatype restriction"), //
		MISSING_DATATYPE("Plain literal does not satisfy the datatype restriction (literal may be missing the rdf:datatype attribute)"), //
		INVALID_LITERAL("Invalid literal for the rdf:datatype attribute"), //
		DISJOINT_PROPS("Two disjoint properties have the same value"), //
		BOTTOM_PROP("An individual has a value for bottom property"), //
		UNEXPLAINED("Cannot explain");

		private final String _explanation;

		private ClashType(final String explanation)
		{
			_explanation = explanation;
		}

		public String getExplanation()
		{
			return _explanation;
		}
	}

	private Clash(final Node node, final ClashType type, final DependencySet depends)
	{
		_depends = depends;
		_node = node;
		_type = type;
		_args = null;
		_clashExplanation = type.getExplanation();
	}

	private Clash(final Node node, final ClashType type, final DependencySet depends, final ATerm[] args)
	{
		_depends = depends;
		_node = node;
		_type = type;
		_args = args;
		_clashExplanation = type.getExplanation();
	}

	private Clash(final Node node, final ClashType type, final DependencySet depends, final String explanation)
	{
		_depends = depends;
		_node = node;
		_type = type;
		_args = null;
		_clashExplanation = explanation;
	}

	public Clash copyTo(final ABoxImpl abox)
	{
		return new Clash(abox.getNode(getNode().getName()), getType(), getDepends(), _clashExplanation);
	}

	public ClashType getClashType()
	{
		return getType();
	}

	public static Clash unexplained(final Node node, final DependencySet depends)
	{
		return new Clash(node, ClashType.UNEXPLAINED, depends);
	}

	public static Clash unexplained(final Node node, final DependencySet depends, final String msg)
	{
		return new Clash(node, ClashType.UNEXPLAINED, depends, msg);
	}

	public static Clash atomic(final Node node, final DependencySet depends)
	{
		return new Clash(node, ClashType.ATOMIC, depends);
	}

	public static Clash atomic(final Node node, final DependencySet depends, final ATermAppl c)
	{
		return new Clash(node, ClashType.ATOMIC, depends, new ATerm[] { c });
	}

	public static Clash bottomProperty(final Node node, final DependencySet depends, final ATermAppl p)
	{
		return new Clash(node, ClashType.BOTTOM_PROP, depends, new ATerm[] { p });
	}

	public static Clash maxCardinality(final Node node, final DependencySet depends)
	{
		return new Clash(node, ClashType.MAX_CARD, depends);
	}

	public static Clash maxCardinality(final Node node, final DependencySet depends, final ATermAppl r, final int n)
	{
		return new Clash(node, ClashType.MAX_CARD, depends, new ATerm[] { r, ATermUtils.getFactory().makeInt(n) });
	}

	public static Clash minMax(final Node node, final DependencySet depends)
	{
		return new Clash(node, ClashType.MIN_MAX, depends);
	}

	//	public static Clash functionalCardinality(final Node node, final DependencySet depends)
	//	{
	//		return new Clash(node, ClashType.FUNC_MAX_CARD, depends);
	//	}

	public static Clash functionalCardinality(final Node node, final DependencySet depends, final ATermAppl r)
	{
		return new Clash(node, ClashType.FUNC_MAX_CARD, depends, new ATerm[] { r });
	}

	//	public static Clash missingDatatype(final Node node, final DependencySet depends)
	//	{
	//		return new Clash(node, ClashType.MISSING_DATATYPE, depends);
	//	}

	//	public static Clash missingDatatype(final Node node, final DependencySet depends, final ATermAppl value, final ATermAppl datatype)
	//	{
	//		return new Clash(node, ClashType.MISSING_DATATYPE, depends, new ATermAppl[] { value, datatype });
	//	}

	public static Clash nominal(final Node node, final DependencySet depends)
	{
		return new Clash(node, ClashType.NOMINAL, depends);
	}

	public static Clash nominal(final Node node, final DependencySet depends, final ATermAppl other)
	{
		return new Clash(node, ClashType.NOMINAL, depends, new ATermAppl[] { other });
	}

	//	public static Clash valueDatatype(final Node node, final DependencySet depends)
	//	{
	//		return new Clash(node, ClashType.VALUE_DATATYPE, depends);
	//	}

	public static Clash valueDatatype(final Node node, final DependencySet depends, final ATermAppl value, final ATermAppl datatype)
	{
		return new Clash(node, ClashType.VALUE_DATATYPE, depends, new ATermAppl[] { value, datatype });
	}

	public static Clash emptyDatatype(final Node node, final DependencySet depends)
	{
		return new Clash(node, ClashType.EMPTY_DATATYPE, depends);
	}

	public static Clash emptyDatatype(final Node node, final DependencySet depends, final ATermAppl[] datatypes)
	{
		return new Clash(node, ClashType.EMPTY_DATATYPE, depends, datatypes);
	}

	public static Clash invalidLiteral(final Node node, final DependencySet depends)
	{
		return new Clash(node, ClashType.INVALID_LITERAL, depends);
	}

	public static Clash invalidLiteral(final Node node, final DependencySet depends, final ATermAppl value)
	{
		return new Clash(node, ClashType.INVALID_LITERAL, depends, new ATermAppl[] { value });
	}

	//	public static Clash disjointProps(final Node node, final DependencySet depends)
	//	{
	//		return new Clash(node, ClashType.DISJOINT_PROPS, depends);
	//	}

	public static Clash disjointProps(final Node node, final DependencySet depends, final ATermAppl p1, final ATermAppl p2)
	{
		return new Clash(node, ClashType.DISJOINT_PROPS, depends, new ATermAppl[] { p1, p2 });
	}

	public String detailedString()
	{
		String str;

		if (_clashExplanation != null)
			str = _clashExplanation;
		else
			if (getType() == ClashType.UNEXPLAINED)
				str = "No _explanation was generated.";
			else
				if (_args == null)
					str = "No specific _explanation was generated. Generic _explanation: " + getType().getExplanation();
				else
					if (getType() == ClashType.ATOMIC)
						str = atomicExplanation();
					else
						if (getType() == ClashType.BOTTOM_PROP)
							str = bottomExplanation();
						else
							if (getType() == ClashType.MAX_CARD)
								str = maxCardinalityExplanation();
							else
								if (getType() == ClashType.FUNC_MAX_CARD)
									str = functionalCardinalityExplanation();
								else
									if (getType() == ClashType.NOMINAL)
										str = nominalExplanation();
									else
										if (getType() == ClashType.MISSING_DATATYPE)
											str = missingDatatypeExplanation();
										else
											if (getType() == ClashType.VALUE_DATATYPE)
												str = valueDatatypeExplanation();
											else
												if (getType() == ClashType.INVALID_LITERAL)
													str = invalidLiteralExplanation();
												else
													if (getType() == ClashType.EMPTY_DATATYPE)
														str = emptyDatatypeExplanation();
													else
														str = _clashExplanation;

		return str;
	}

	public static String describeNode(final Node node)
	{
		final StringBuffer str = new StringBuffer();
		if (node.getNameStr().startsWith("Any member of"))
			str.append(node.getNameStr());
		else
			if (node.isNamedIndividual())
				str.append("Individual " + node.getNameStr());
			else
			{
				final List<ATermAppl> path = node.getPath();
				if (path.isEmpty())
					str.append("There is an anonymous _individual which");
				else
				{
					final ATermAppl first = path.get(0);
					final Iterator<ATermAppl> i = path.iterator();
					String nodeID = "";
					if (first.getName().startsWith("Any member of"))
					{
						nodeID = "Y";
						str.append(first.getName() + ", X, is related to some " + nodeID + ", identified by this path (X ");
						i.next();
					}
					else
					{
						nodeID = "X";
						str.append("There is an anonymous _individual X, identified by this path (" + i.next() + " ");
					}

					while (i.hasNext())
					{
						str.append(i.next() + " ");
						if (i.hasNext())
							str.append("[ ");
					}

					str.append(nodeID);
					for (int count = 0; count < path.size() - 2; count++)
						str.append(" ]");
					str.append("), which");
				}
			}

		return str.toString();
	}

	public String atomicExplanation()
	{
		return describeNode(getNode()) + " is forced to belong to class " + _args[0] + " and its complement";
	}

	public String bottomExplanation()
	{
		return describeNode(getNode()) + " has " + _args[0] + " property";
	}

	public String maxCardinalityExplanation()
	{
		return describeNode(getNode()) + " has more than " + _args[1] + " values for property " + _args[0] + " violating the cardinality restriction";
	}

	public String functionalCardinalityExplanation()
	{
		return describeNode(getNode()) + " has more than " + "one value for the functional property " + _args[0];
	}

	public String missingDatatypeExplanation()
	{
		return "Plain literal " + ATermUtils.toString((ATermAppl) _args[0]) + " does not belong to datatype " + _args[1] + ". Literal value may be missing the rdf:datatype attribute.";
	}

	public String nominalExplanation()
	{
		return describeNode(getNode()) + " is sameAs and differentFrom " + _args[0] + "  at the same time ";
	}

	public String valueDatatypeExplanation()
	{
		return "Literal value " + ATermUtils.toString((ATermAppl) _args[0]) + " does not belong to datatype " + ATermUtils.toString((ATermAppl) _args[1]);
	}

	public String emptyDatatypeExplanation()
	{
		if (_args.length == 1)
			return "Datatype " + ATermUtils.toString((ATermAppl) _args[0]) + " is inconsistent";
		else
		{
			final StringBuffer buffer = new StringBuffer("Intersection of datatypes [");
			for (int i = 0; i < _args.length; i++)
			{
				if (i > 0)
					buffer.append(", ");
				buffer.append(ATermUtils.toString((ATermAppl) _args[i]));
			}
			buffer.append("] is inconsistent");

			return buffer.toString();
		}
	}

	public String invalidLiteralExplanation()
	{
		final ATermAppl literal = (ATermAppl) _args[0];
		final ATermAppl datatype = (ATermAppl) literal.getArgument(2);
		return "Literal value " + ATermUtils.toString(literal) + " is not valid for the rdatatype " + ATermUtils.toString(datatype);
	}

	@Override
	public String toString()
	{
		return "[Clash " + getNode() + " " + getType() + " " + getDepends().toString() + (_args == null ? "" : " " + Arrays.asList(_args)) + "]";
	}

	/**
	 * @param depends the depends to set
	 */
	public void setDepends(final DependencySet depends)
	{
		_depends = depends;
	}

	/**
	 * @return the depends
	 */
	public DependencySet getDepends()
	{
		return _depends;
	}

	/**
	 * @return the node
	 */
	public Node getNode()
	{
		return _node;
	}

	/**
	 * @return the type
	 */
	public ClashType getType()
	{
		return _type;
	}
}
