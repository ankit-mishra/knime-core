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
 *   Oct 7, 2019 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.core.data.probability.nominal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Holds information shared by multiple {@link ProbabilityDistributionCell ProbabilityDistributionCells} created by the
 * same process e.g. the values the distribution is defined over.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
final class NominalDistributionCellMetaData {

    private static final MemoryAlertAwareGuavaCache CACHE = new MemoryAlertAwareGuavaCache();

    private final LinkedHashMap<String, Integer> m_valueMap = new LinkedHashMap<>();

    private final String[] m_values;

    private final UUID m_id;

    NominalDistributionCellMetaData(final String[] values) {
        this(UUID.randomUUID(), values);
    }

    private NominalDistributionCellMetaData(final UUID id, final String[] values) {
        m_id = id;
        Arrays.stream(values).forEach(v -> m_valueMap.put(v, m_valueMap.size()));
        m_values = values;
        CACHE.put(m_id, this);
    }

    /**
     * @param value the cell for which to retrieve the index
     * @return the index of <b>cell</b> or -1 if <b>cell</b> is unknown
     */
    int getIndex(final String value) {
        final Integer index = m_valueMap.get(value);
        return index == null ? -1 : index.intValue();
    }

    String getValueAtIndex(final int idx) {
        if (idx < 0) {
            throw new IndexOutOfBoundsException("The index must not be negative.");
        } else if (idx >= m_values.length) {
            throw new IndexOutOfBoundsException(String
                .format("The index must be smaller than the number of values (%s) but was %s.", m_values.length, idx));
        } else {
            return m_values[idx];
        }
    }

    int size() {
        return m_valueMap.size();
    }

    Set<String> getValues() {
        return Collections.unmodifiableSet(m_valueMap.keySet());
    }

    void write(final ObjectOutputStream out) throws IOException {
        out.writeObject(m_id);
        out.writeObject(m_values);
    }

    static NominalDistributionCellMetaData read(final ObjectInputStream in)
        throws ClassNotFoundException, IOException, ExecutionException {
        final UUID id = (UUID)in.readObject();
        return CACHE.get(id, () -> new NominalDistributionCellMetaData(id, (String[])in.readObject()));
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (obj instanceof NominalDistributionCellMetaData) {
            final NominalDistributionCellMetaData other = (NominalDistributionCellMetaData)obj;
            return Arrays.equals(m_values, other.m_values);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(m_values);
    }

}
