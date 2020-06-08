package openllet.atom;

import java.util.Iterator;

/**
 * Standard List
 *
 * @since 2.6.0
 */
public interface SList<Atom> extends Iterable<Atom>
{

	/**
	 * Checks if this list is the empty list.
	 *
	 * @return true if this list is empty, false otherwise.
	 */
	public boolean isEmpty();

	/**
	 * Gets the length (number of elements) of this list.
	 *
	 * @return the length of this list.
	 */
	public int getLength();

	/**
	 * Gets the first element of this list.
	 *
	 * @return the first element of this list.
	 */
	public Atom getFirst();

	/**
	 * Gets the last element of this list.
	 *
	 * @return the last element of this list.
	 */
	public Atom getLast();

	/**
	 * Gets the empty list associated to this list.
	 *
	 * @return the empty list.
	 */
	public SList<Atom> getEmpty();

	/**
	 * Gets the tail (all but the first element) of this list.
	 *
	 * @return the tail of this list.
	 */
	public SList<Atom> getNext();

	/**
	 * Gets the index of the first occurance of a term in this list.
	 * Lookup starts at a given index (0 being the first element).
	 *
	 * @param el the element to look for.
	 * @param start the starting position of the lookup. Negative start
	 *            implies searching backwards from the tail of the list.
	 *
	 * @return the index of the first occurance of el in this list,
	 *         or -1 if el does not occur.
	 *
	 * @throws IllegalArgumentException when start &gt; length of list ||
	 *             start &lt; -length
	 *
	 * @see #lastIndexOf
	 */
	public int indexOf(Atom el, int start);

	/**
	 * Gets the last occurance of a term in this list.
	 * Lookup starts at a given index (0 being the first element).
	 *
	 * @param el the element to look for.
	 * @param start the starting position of the lookup.
	 *
	 * @return the index of the last occurance of el in this list,
	 *         or -1 if el does not occur.
	 *
	 * @see #indexOf
	 */
	public int lastIndexOf(Atom el, int start);

	/**
	 * Concatenates a list to this list.
	 *
	 * @param rhs the list to concatenate to this list.
	 *
	 * @return the concatenation of this list and rhs
	 */
	public SList<Atom> concat(SList<Atom> rhs);

	/**
	 * Appends an element to this list.
	 *
	 * @param el the element to append to this list.
	 *
	 * @return a list with el appended to it.
	 */
	public SList<Atom> append(Atom el);

	/**
	 * Gets the element at a specific index of this list.
	 *
	 * @param i the index of the required element.
	 *
	 * @return the ith element of this list.
	 *
	 * @throws IndexOutOfBoundsException if i does not refer
	 *             to a position in this list.
	 */
	public Atom elementAt(int i);

	/**
	 * Removes one occurance of an element from this list.
	 *
	 * @param el the element to be removed.
	 *
	 * @return this list with one occurance of el removed.
	 */
	public SList<Atom> remove(Atom el);

	/**
	 * Removes the element at a specific index in this list.
	 *
	 * @param i the index of the element to be removed.
	 *
	 * @return a list with the ith element removed.
	 *
	 * @throws IndexOutOfBoundsException if i does not refer
	 *             to a position in this list.
	 */
	public SList<Atom> removeElementAt(int i);

	/**
	 * Removes all occurances of an element in this list.
	 *
	 * @param el the element to be removed.
	 *
	 * @return this list with all occurances of el removed.
	 */
	public SList<Atom> removeAll(Atom el);

	/**
	 * Inserts a term in front of this list.
	 *
	 * @param el the element to be inserted.
	 *
	 * @return a list with el inserted.
	 */
	public SList<Atom> insert(Atom el);

	/**
	 * Inserts an element at a specific position in this list.
	 *
	 * @param el the element to be inserted.
	 * @param i the index at which to insert.
	 *
	 * @return a list with el inserted as ith element.
	 */
	public SList<Atom> insertAt(Atom el, int i);

	/**
	 * Gets the prefix (all but the last element) of this list.
	 *
	 * @return the prefix of this list.
	 */
	public SList<Atom> getPrefix();

	/**
	 * Gets a portion (slice) of this list.
	 *
	 * @param start the start of the slice (included).
	 * @param end the end of the slice (excluded).
	 *
	 * @return the portion of this list between start and end.
	 */
	public SList<Atom> getSlice(int start, int end);

	/**
	 * Replaces a specific term in this list with another.
	 *
	 * @param el the element to be put into this list.
	 * @param i the index of the element in this list to be replaced.
	 *
	 * @return this list with the ith element replaced by el.
	 *
	 * @throws IndexOutOfBoundsException if i does not refer
	 *             to a position in this list.
	 */
	public SList<Atom> replace(Atom el, int i);

	/**
	 * Reverses the elements of this list.
	 *
	 * @return a reverse order copy of this list.
	 */
	public SList<Atom> reverse();

	/**
	 * Retrieves an element from a dictionary list.
	 * A dictionary list is a list of [key,value] pairs.
	 *
	 * @param key the key to look for
	 *
	 * @return the value associated with key, or null when key is not present.
	 */
	public Atom dictGet(Atom key);

	/**
	 * Sets the value for an element in a dictionary list.
	 * A dictionary list is a list of [key,value] pairs.
	 *
	 * @param key the key to set
	 * @param value the value to associate with key
	 *
	 * @return the new dictionary list
	 */
	public SList<Atom> dictPut(Atom key, Atom value);

	/**
	 * Removes an element from a dictionary list.
	 * A dictionary list is a list of [key,value] pairs.
	 *
	 * @param key the key to remove
	 *
	 * @return the new dictionary list
	 */
	public SList<Atom> dictRemove(Atom key);

	@Override
	default Iterator<Atom> iterator()
	{
		return new Iterator<>()
		{
			private SList<Atom> _current = SList.this;

			@Override
			public boolean hasNext()
			{
				return !_current.isEmpty();
			}

			@Override
			public Atom next()
			{
				final Atom atom = _current.getFirst();
				_current = _current.getNext();
				return atom;
			}
		};
	}
}
