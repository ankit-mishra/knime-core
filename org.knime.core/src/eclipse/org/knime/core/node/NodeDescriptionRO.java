/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   Nov 4, 2019 (Mark Ortmann, KNIME GmbH, Berlin, Germany): created
 */
package org.knime.core.node;

import org.knime.core.node.NodeFactory.NodeType;
import org.w3c.dom.Element;

/**
 *
 * @author Mark Ortmann, KNIME GmbH, Berlin, Germany
 * @since 4.1
 */
public interface NodeDescriptionRO {

    /**
     * Returns a description for an input port.
     *
     * @param index the index of the input port, starting at 0
     * @return an input port description or <code>null</code> if no description exists
     */
    public String getInportDescription(final int index);

    /**
     * Returns a name for an input port.
     *
     * @param index the index of the input port, starting at 0
     * @return an input port name or <code>null</code> if not name is known
     */
    public String getInportName(final int index);

    /**
     * Returns the name of the interactive view if such a view exists. Otherwise <code>null</code> is returned.
     *
     * @return name of the interactive view or <code>null</code>
     */
    public String getInteractiveViewName();

    /**
     * Returns the name of this node.
     *
     * @return the node's name or <code>null</code> if no name is known
     * @see NodeFactory#getNodeName()
     */
    public String getNodeName();

    /**
     * Returns a description for an output port.
     *
     * @param index the index of the output port, starting at 0
     * @return an output port description or <code>null</code> if not description exists
     */
    public String getOutportDescription(final int index);

    /**
     * Returns a name for an output port.
     *
     * @param index the index of the output port, starting at 0
     * @return an output port name or <code>null</code> if no name is known
     */
    public String getOutportName(final int index);

    /**
     * Returns the type of the node.
     *
     * @return the node's type
     */
    public NodeType getType();

    /**
     * Returns a description for a view.
     *
     * @param index the index of the view, starting at 0
     * @return a view description or <code>null</code> if no description exists
     */
    public String getViewDescription(final int index);

    /**
     * The XML description can be used with the <code>NodeFactoryHTMLCreator</code> in order to get a converted HTML
     * description of it, which fits the overall KNIME HTML style.
     *
     * @return XML description of this node
     */
    public Element getXMLDescription();

    /**
     * Returns whether the node is deprecated.
     *
     * @return <code>true</code> if the node is deprecated, <code>false</code> otherwise
     */
    public boolean isDeprecated();

}
