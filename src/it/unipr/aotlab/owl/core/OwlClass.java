/*****************************************************************
 OWLBeans is a toolkit to manipulate ontologies.
 Its main purpose is to extract JavaBeans from OWL documents.
 Copyright (C) 2004 University of Parma.

 GNU Lesser General Public License

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation,
 version 2.1 of the License.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the
 Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA.
 *****************************************************************/

package it.unipr.aotlab.owl.core;

import java.util.*;

/**
 * Represents a single class of the ontology.
 *
 * @author <a href="mailto:tomamic@ce.unipr.it">Michele Tomaiuolo</a> -
 *         <a href="http://aot.ce.unipr.it">University of Parma</a>
 */
public class OwlClass extends OwlResource {
	public OwlClass() { super(null); }
	public OwlClass(String name) { super(name); }
	public OwlClass(String namespace, String name) { super(namespace, name); }

	List parents; // Parents are OwlResource objects
	public List getParents() { return parents; }
	public void setParents(List parents) { this.parents = parents; }

	Map properties; // Properties are OwlProperty objects
	public Map getProperties() { return properties; }
	public void setProperties(Map properties) { this.properties = properties; }
}
