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
 * Represents a single resource of the ontology.
 *
 * @author <a href="mailto:tomamic@ce.unipr.it">Michele Tomaiuolo</a> -
 *         <a href="http://aot.ce.unipr.it">University of Parma</a>
 */
public class OwlResource {
	public static final String ANONYMOUS = "anonymous";
	public static final String NS_SEPARATOR = "#";
	public static final String DATATYPES_NS = "http://www.w3.org/2001/XMLSchema#";

	String name = null;
	String[] trans = null;

	public OwlResource() { this(null); }
	public OwlResource(String name) { setName(name); }
	public OwlResource(String namespace, String name) {
		this.namespace = namespace;
		setName(name);
	}

	public void setName(String name) { this.name = name; trans = null; }
	public String getName() { return name; }

	void transformName() {
		if (trans == null) trans = OwlHelper.transformName((name != null) ? name : ANONYMOUS);
	}

	public String getSrcName() { transformName(); return trans[OwlHelper.SRC]; }
	public String getClsName() { transformName(); return trans[OwlHelper.CLS]; }
	public String getObjName() { transformName(); return trans[OwlHelper.OBJ]; }
	public String getStdName() { transformName(); return trans[OwlHelper.STD]; }
	public String getCstName() { transformName(); return trans[OwlHelper.CST]; }

	String namespace;
	public void setNamespace(String namespace) { this.namespace = namespace; }
	public String getNamespace() { return namespace; }

	public String toString() { return name; }
}
