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
 *   Oct 27, 2008 (wiswedel): created
 */
package org.knime.core.node.exec.dataexchange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.knime.core.node.FSConnectionFlowVariableProvider;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.node.workflow.CredentialsProvider;
import org.knime.core.node.workflow.CredentialsStore;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.ICredentials;
import org.knime.core.node.workflow.VariableType.CredentialsType;
import org.knime.core.node.workflow.VariableType.FSConnectionType;

/**
 * Settings helper that reads/writes the port object ID that is used by the {@link PortObjectRepository}.
 *
 * @author Bernd Wiswedel, University of Konstanz
 * @noreference This class is not intended to be referenced by clients.
 * @since 3.1
 */
public final class PortObjectIDSettings {

    private Integer m_id;
    private List<FlowVariable> m_flowVariables;
    private boolean m_copyData;
    private CredentialsProvider m_credentialsProvider;
    private FSConnectionFlowVariableProvider m_fsConnectionsProvider;

    /** Constructor, which sets a null ID (no id). */
    public PortObjectIDSettings() {
        // empty
    }

    /** Loads the settings from a NodeSettings object.
     * @param settings to load from.
     * @throws InvalidSettingsException If no settings present or invalid.
     */
    public void loadSettings(final NodeSettingsRO settings)
        throws InvalidSettingsException {
        m_id = settings.getInt("portobject_ID");
        m_copyData = settings.getBoolean("copyData");
        m_flowVariables = new ArrayList<FlowVariable>();
        // added for cluster version 1.0.2
        if (settings.containsKey("flowVariables")) {
            NodeSettingsRO sub = settings.getNodeSettings("flowVariables");
            for (String key : sub.keySet()) {
                final NodeSettingsRO child = sub.getNodeSettings(key);

                final String name = child.getString("name");
                final String typeS = child.getString("class");
                if (typeS == null || name == null) {
                    throw new InvalidSettingsException("name or type is null");
                }

                final FlowVariable v;
                /* Flow variables of types Credentials and FSConnection are handled separately, using the
                 * credentialsProvider and fsConnectionProvider members, respectively. */
                if (typeS.equals(CredentialsType.INSTANCE.getIdentifier())) {
                    CheckUtils.checkState(m_credentialsProvider != null, "No credentials provider set");
                    final ICredentials credentials = m_credentialsProvider.get(name);
                    v = CredentialsStore.newCredentialsFlowVariable(credentials.getName(), credentials.getLogin(),
                        credentials.getPassword(), false, false);
                } else if (typeS.equals(FSConnectionType.INSTANCE.getIdentifier())) {
                    v = m_fsConnectionsProvider.flowVariableFor(name).orElse(null);
                } else {
                    v = FlowVariable.load(child);
                }

                m_flowVariables.add(v);
            }
        }
    }

    /** Saves the current settings to a NodeSettings object.
     * @param settings To write to. */
    public void saveSettings(final NodeSettingsWO settings) {
        if (m_id != null) {
            settings.addInt("portobject_ID", m_id);
        }
        settings.addBoolean("copyData", m_copyData);
        NodeSettingsWO sub = settings.addNodeSettings("flowVariables");
        int index = 0;
        for (FlowVariable fv : getFlowVariables()) {
            NodeSettingsWO child = sub.addNodeSettings("flowVar_" + (index++));
            fv.save(child);
        }
    }

    /**
     * Get the currently set ID or null if none have been set.
     * @return the id
     */
    public Integer getId() {
        return m_id;
    }

    /**
     * Set new ID for the port object, setting null invalidates the settings.
     * @param id the id to set
     */
    public void setId(final Integer id) {
        m_id = id;
    }

    /** Set list of flow variables to be exposed by the node.
     * @param flowVariables the flowVariables to set
     */
    public void setFlowVariables(final List<FlowVariable> flowVariables) {
        m_flowVariables = flowVariables;
    }

    /** List of flow variables to be exposed, never null.
     * @return the flowVariables
     */
    public List<FlowVariable> getFlowVariables() {
        if (m_flowVariables == null) {
            return Collections.emptyList();
        }
        return m_flowVariables;
    }

    /** @param copyData the copyData to set */
    public void setCopyData(final boolean copyData) {
        m_copyData = copyData;
    }

    /** @return the copyData */
    public boolean isCopyData() {
        return m_copyData;
    }

    /**
     * Sets the credentials provider to read the credentials from (in case there are credentials flow variables to be loaded).
     * Only required for loading the settings.
     *
     * @param cp the credentials provider
     */
    public void setCredentialsProvider(final CredentialsProvider cp) {
        m_credentialsProvider = cp;
    }

    /**
     * Sets the file system connection flow variable provider to read file system connection flow variables from.
     * Only required for loading the settings.
     *
     * @param provider the file system connection flow variable provider
     */
    public void setFSConnectionFlowVariableProvider(final FSConnectionFlowVariableProvider provider) {
        m_fsConnectionsProvider = provider;
    }

}
