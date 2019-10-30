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
 *   Oct 9, 2019 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.core.data.probability;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.meta.MetaData;
import org.knime.core.data.meta.MetaDataSerializer;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.config.ConfigRO;
import org.knime.core.node.config.ConfigWO;
import org.knime.core.node.util.CheckUtils;

/**
 * TODO figure out if we could just reuse NominalDistributionMetaData
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 * @since 4.1
 */
public final class NominalDistributionValueMetaData implements MetaData {

    static final String CFG_VALUES = "values";

    private Set<String> m_values;

    public NominalDistributionValueMetaData(final String[] values) {
        m_values = toLinkedHashSet(values);
    }

    NominalDistributionValueMetaData(final Collection<String> values) {
        m_values = new LinkedHashSet<>(values);
    }

    private NominalDistributionValueMetaData(final LinkedHashSet<String> values) {
        m_values = values;
    }

    private static <T> LinkedHashSet<T> toLinkedHashSet(final T[] values) {
        return Arrays.stream(values).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * The returned set is guaranteed to have a fixed order.
     *
     * @return the {@link DataCell values} this distribution is defined over
     */
    public Set<String> getValues() {
        return Collections.unmodifiableSet(m_values);
    }

    /**
     * Serializer for {@link NominalDistributionValueMetaData} objects.
     *
     * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
     */
    public static final class NominalDistributionValueMetaDataSerializer
        implements MetaDataSerializer<NominalDistributionValueMetaData> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void save(final NominalDistributionValueMetaData metaData, final ConfigWO config) {
            CheckUtils.checkNotNull(metaData, "The meta data provided to the serializer was null.");
            config.addStringArray(CFG_VALUES, metaData.m_values.toArray(new String[0]));
        }

        /**
         * {@inheritDoc}
         *
         * @throws InvalidSettingsException
         */
        @Override
        public NominalDistributionValueMetaData load(final ConfigRO config) throws InvalidSettingsException {
            final String[] values = config.getStringArray(CFG_VALUES);
            return new NominalDistributionValueMetaData(values);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Class<NominalDistributionValueMetaData> getMetaDataClass() {
            return NominalDistributionValueMetaData.class;
        }

    }

}
