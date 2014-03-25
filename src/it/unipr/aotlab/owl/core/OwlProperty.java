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

/**
 * Represents a propriety of a class.
 *
 * @author <a href="mailto:tomamic@ce.unipr.it">Michele Tomaiuolo</a> -
 *         <a href="http://aot.ce.unipr.it">University of Parma</a>
 */
public class OwlProperty extends OwlResource {
	public static final int UNLIMITED = -1;
	public static final int DEFAULT_MINCARD = 0;
	public static final int DEFAULT_MAXCARD = UNLIMITED;

	public OwlProperty() { super(null); }
	public OwlProperty(String name) { super(name); }
	public OwlProperty(String namespace, String name) { super(namespace, name); }

	OwlResource domain;
	public OwlResource getDomain() { return domain; }
	public void setDomain(OwlResource domain) { this.domain = domain; }

	OwlResource range;
	public OwlResource getRange() { return range; }
	public void setRange(OwlResource range) { this.range = range; }

	/*
	boolean datatype;
	public boolean getDatatype() { return datatype; }
	public void setDatatype(boolean datatype) { this.datatype = datatype; }

	boolean functional;
	public boolean getFunctional() { return functional; }
	public void setFunctional(boolean functional) { this.functional = functional; }
	*/

	int minCardinality;
	public int getMinCardinality() { return minCardinality; }
	public void setMinCardinality(int minCardinality) { this.minCardinality = minCardinality; }

	int maxCardinality;
	public int getMaxCardinality() { return maxCardinality; }
	public void setMaxCardinality(int maxCardinality) { this.maxCardinality = maxCardinality; }
}
