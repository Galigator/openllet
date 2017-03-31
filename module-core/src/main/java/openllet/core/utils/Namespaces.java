// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com
//
// ---
// Portions Copyright (c) 2003 Ron Alford, Mike Grove, Bijan Parsia, Evren Sirin
// Alford, Grove, Parsia, Sirin parts of this source code are available under the terms of the MIT License.
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

package openllet.core.utils;

/**
 * Define frequently used namespace here so modules will not be dependant on either Jena or OWLAPI vocabularies.
 *
 * @author Evren Sirin
 */
public class Namespaces
{
	public static final String OWL = "http://www.w3.org/2002/07/owl#";
	public static final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
	public static final String SWRL = "http://www.w3.org/2003/11/swrl#";
	public static final String SWRLB = "http://www.w3.org/2003/11/swrlb#";
	public static final String XSD = "http://www.w3.org/2001/XMLSchema#";

	// W3C OWL-S Namespaces.
	public static final String ACTOR = "http://www.daml.org/services/owl-s/1.2/ActorDefault.owl#";
	public static final String COUNTRY = "http://www.daml.org/services/owl-s/1.2/Country.owl#";
	public static final String GENERIC_EXPR = "http://www.daml.org/services/owl-s/1.2/generic/Expression.owl#";
	public static final String GENERIC_LIST = "http://www.daml.org/services/owl-s/1.2/generic/ObjectList.owl#";
	public static final String GROUNDING = "http://www.daml.org/services/owl-s/1.2/Grounding.owl#";
	public static final String HOBBS_TIME_ENTRY = "http://www.isi.edu/~hobbs/damltime/time-entry.owl#";
	public static final String PAN_TIME_ENTRY = "http://www.isi.edu/~pan/damltime/time-entry.owl#";
	public static final String PAN_TIME_ZONE = "http://www.isi.edu/~pan/damltime/timezone-ont.owl#";
	public static final String PROCESS = "http://www.daml.org/services/owl-s/1.2/Process.owl#";
	public static final String PROFILE = "http://www.daml.org/services/owl-s/1.2/profile.owl#";
	public static final String PROFILE_PARAMETERS = "http://www.daml.org/services/owl-s/1.2/ProfileAdditionalParameters.owl#";
	public static final String SERVICE = "http://www.daml.org/services/owl-s/1.2/Service.owl#";
	public static final String SERVICE_CATEGORY = "http://www.daml.org/services/owl-s/1.2/ServiceCategory.owl#";
	public static final String SERVICE_PARAMETERS = "http://www.daml.org/services/owl-s/1.2/ServiceParameter.owl#";
	public static final String YALE_DRS = "http://cs-www.cs.yale.edu/homes/dvm/daml/drsonto040520.owl#";

	// WORD NET Namespaces.
	public static final String WORDNETN_31 = "http://wordnet-rdf.princeton.edu/wn31/";
	public static final String WORDNET_LEMON = "http://lemon-model.net/lemon#";
	public static final String WORDNET_PRINCETON = "http://wordnet-rdf.princeton.edu/ontology#";
}
