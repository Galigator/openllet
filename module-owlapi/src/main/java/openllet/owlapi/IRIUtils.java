package openllet.owlapi;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.semanticweb.owlapi.model.IRI;

/**
 * A set of function usefull to manage IRIs
 *
 * @since 2.5.1
 */
public interface IRIUtils
{
	final static Random _random = new Random();
	final static AtomicLong _atomic = new AtomicLong(0); // Avoid problem when used across multiple deployments on sames Virtuals machines.

	public static final long _timeShift = (2016L - 1970L) * 365L * 24L * 60L * 60L * 1000L; // 2016 because this function appear in this year.

	/**
	 * We remove an huge part of the time the flow before the application first start. The aim is just to get shorter ids that will stayed ordered among runs.
	 *
	 * @return a short string that describe a point in time.
	 * @since 2.5.1
	 */
	public static String shortTime()
	{
		return shortTime(System.currentTimeMillis());
	}

	public static String shortTime(final long epochMilli)
	{
		return Long.toHexString((epochMilli - _timeShift));
	}

	public static Instant instantFromShortTime(final String hexTime)
	{
		return Instant.ofEpochMilli(Long.parseLong(hexTime, 16) + _timeShift);
	}

	/**
	 * @param rand is a String generated with the the randStr() method.
	 * @return an Instant as parsed from a String generated with the randStr() method.
	 * @since 2.6.0
	 */
	public static Instant instantFromRandStr(final String rand)
	{
		return instantFromShortTime(rand.substring(rand.indexOf(OWLHelper._innerSeparator) + 1, rand.lastIndexOf(OWLHelper._innerSeparator)));
	}

	/**
	 * @return create an random string base on a random generator, the short time, and a atomic long.
	 * @since 2.6.0
	 */
	public static String randStr()
	{
		return _atomic.getAndIncrement() + OWLHelper._innerSeparator + shortTime() + OWLHelper._innerSeparator + Integer.toHexString(_random.nextInt());
	}

	/**
	 * @param begin will be placed at the start of the result.
	 * @return a random String with the given String placed at the start and two inner separator around the random part.
	 * @since 2.5.1
	 */
	public static String randId(final String begin)
	{
		return begin + OWLHelper._innerSeparator + randStr() + OWLHelper._innerSeparator;
	}

	/**
	 * @param resource to check
	 * @return true if the String is an IRI.
	 * @since 2.5.1
	 */
	@Deprecated
	public static boolean isIRI(final String resource)
	{
		return resource != null && (resource.startsWith(OWLHelper._protocol) || resource.startsWith(OWLHelper._secureProtocol));
	}

	/**
	 * @param iri an iri that is potentially valid or with a namespace separator.
	 * @return The iri without the part that show the namespace as separate object as the individual name.
	 */
	public static String iriModel2iri(final String iri)
	{
		return (!iri.startsWith("{")) ? iri : iri.replaceAll("[\\{\\}]", "");
	}

	public static <T> String base(final Class<T> clazz)
	{
		return OWLHelper._protocol + clazz.getPackage().getName() + OWLHelper._webSeparator + clazz.getSimpleName();
	}

	public static <T> String core(final Class<T> clazz)
	{
		return OWLHelper._protocol + clazz.getPackage().getName() + OWLHelper._entitySeparator + clazz.getSimpleName();
	}

	public static <T> String core(final Class<T> clazz, final Method method)
	{
		return OWLHelper._protocol + clazz.getPackage().getName() + OWLHelper._entitySeparator + method.getName();
	}

	/**
	 * Work for property and individual
	 *
	 * @param clazz of the entity. The entity msut have a clazz, at least its class of creation.
	 * @param entity the name of the object/individual.
	 * @param <T> the type of the clazz
	 * @return an IRI that is standard to the clazz and entity
	 */
	public static <T> IRI name(final Class<T> clazz, final String entity)
	{
		return IRI.create(base(clazz) + OWLHelper._entitySeparator + entity);
	}

	/**
	 * @param <T> type of the class
	 * @param clazz of the individual use to general the iri of the individual
	 * @return a random iri that fit the name of an individiual
	 * @since 2.5.1
	 */
	public static <T> IRI rand(final Class<T> clazz)
	{
		return IRI.create(base(clazz) + OWLHelper._entitySeparator + randId(clazz.getSimpleName()));
	}

	public static <T> IRI rand(final Class<T> clazz, final String entity)
	{
		return IRI.create(base(clazz) + OWLHelper._webSeparator + entity + OWLHelper._entitySeparator + randId(clazz.getSimpleName()));
	}

	public static <T> IRI method(final Class<T> clazz, final Method m)
	{
		return IRI.create(core(clazz, m));
	}

	public static <T> IRI clazz(final Class<T> clazz)
	{
		return IRI.create(core(clazz));
	}

	public static <T> IRI ontology(final Class<T> clazz, final String purpose, final String entity)
	{
		return IRI.create(base(clazz) + OWLHelper._webSeparator + purpose + OWLHelper._entitySeparator + entity);
	}
}
