package openllet.profiler.utils;

/**
 * Interface used internally for memory-efficient representations of names of profile tree links between profile tree nodes.
 *
 * @author (C) <a href="http://www.javaworld.com/columns/jw-qna-_index.shtml">Vlad Roubtsov</a>, 2003
 */
interface ILink
{
	/**
	 * @return the string that will be used for a {@link IObjectProfileNode#name()} implementation. It is _expected that the implementation will generate the
	 *         return on every call to this method and not keep in memory.
	 */
	String name();

}