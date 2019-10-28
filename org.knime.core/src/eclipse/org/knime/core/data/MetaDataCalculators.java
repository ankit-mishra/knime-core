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
 *   Oct 11, 2019 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.core.data;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.knime.core.data.DataValue.UtilityFactory;
import org.knime.core.data.meta.MetaData;
import org.knime.core.data.meta.MetaDataCreator;
import org.knime.core.data.meta.MetaDataRegistry;
import org.knime.core.node.util.CheckUtils;

/**
 * Provides calculators that can calculate meta data from actual data.<br/>
 * This is done by retrieving the {@link MetaDataCreator creators} for all {@link DataValue} interfaces the
 * {@link DataType} of the current column contains that declare that they have {@link MetaData}. A {@link DataValue} has
 * {@link MetaData} if its {@link UtilityFactory} returns true in {@link UtilityFactory#hasMetaData()} in which case
 * {@link UtilityFactory#getMetaDataCreator()} must return an instance of {@link MetaDataCreator}.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
final class MetaDataCalculators {

    private MetaDataCalculators() {
    }

    interface MetaDataCalculator {
        void update(final DataCell cell);

        void merge(final MetaDataCalculator other);

        List<MetaData> createMetaData();
    }

    static MetaDataCalculator createCalculator(final DataColumnSpec colSpec, final boolean createMetaData,
        final boolean dropMetaData) {
        if (dropMetaData && !createMetaData) {
            return NullMetaDataCalculator.INSTANCE;
        }
        return new MetaDataCalculatorImpl(colSpec, !dropMetaData, createMetaData);
    }

    static MetaDataCalculator copy(final MetaDataCalculator calculator) {
        if (calculator == NullMetaDataCalculator.INSTANCE) {
            return NullMetaDataCalculator.INSTANCE;
        } else {
            assert calculator instanceof MetaDataCalculatorImpl : "Unknown MetaDataCalculator implementation "
                + calculator.getClass().getName();
            return new MetaDataCalculatorImpl((MetaDataCalculatorImpl)calculator);
        }
    }

    /**
     * A dummy implementation of {@link MetaDataCalculator} that doesn't actually do any computation. Note that
     * attempting to merge the singleton with any object other than itself will cause an assertion error since this
     * indicates an implementation error.
     *
     * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
     */
    private enum NullMetaDataCalculator implements MetaDataCalculator {
            INSTANCE;

        /**
         * {@inheritDoc}
         */
        @Override
        public void update(final DataCell cell) {
            // do nothing
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void merge(final MetaDataCalculator other) {
            assert other == this : "Attempting to merge NullMetaDataCalculator with anything but itself."
                + "This indicates an implementation error.";
            // do nothing
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<MetaData> createMetaData() {
            return Collections.emptyList();
        }

    }

    /**
     * Implementation that actually creates {@link MetaData}.
     *
     * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
     */
    private static class MetaDataCalculatorImpl implements MetaDataCalculator {

        private final Collection<MetaDataCreator<?>> m_metaDataCreators;

        private final boolean m_updateMetaData;

        MetaDataCalculatorImpl(final DataColumnSpec spec, final boolean initializeWithSpec,
            final boolean updateMetaData) {
            m_metaDataCreators = MetaDataRegistry.INSTANCE.getCreators(spec.getType());
            m_updateMetaData = updateMetaData;
            if (initializeWithSpec) {
                m_metaDataCreators.forEach(m -> merge(m,
                    spec.getMetaDataOfType(m.getMetaDataClass()).orElseThrow(() -> new IllegalStateException(
                        String.format("No meta data for type %s in column %s.", m.getClass(), spec)))));
            }
        }

        // the compatibility of creator and other is ensured at runtime
        @SuppressWarnings("unchecked")
        private static void merge(@SuppressWarnings("rawtypes") final MetaDataCreator creator, final MetaData other) {
            CheckUtils.checkState(creator.getMetaDataClass().isInstance(other),
                "Expected meta data of class '%s' but received meta data of class '%s'.",
                creator.getMetaDataClass().getName(), other.getClass().getName());
            creator.merge(other);
        }

        /**
         * Copies <b>toCopy</b> by also copying all {@link MetaDataCreator DataValueMetaDataCreators} it contains. This
         * means that any later change to <b>toCopy</b> does NOT affect the newly created instance.
         *
         * @param toCopy the MetaDataCalculator to copy
         */
        MetaDataCalculatorImpl(final MetaDataCalculatorImpl toCopy) {
            m_metaDataCreators =
                toCopy.m_metaDataCreators.stream().map(MetaDataCreator::copy).collect(Collectors.toList());
            m_updateMetaData = toCopy.m_updateMetaData;
        }

        @Override
        public void update(final DataCell cell) {
            if (m_updateMetaData) {
                m_metaDataCreators.forEach(c -> c.update(cell));
            }
        }

        @Override
        public List<MetaData> createMetaData() {
            return m_metaDataCreators.stream().map(MetaDataCreator::create).collect(Collectors.toList());
        }

        @Override
        public void merge(final MetaDataCalculator other) {
            assert other instanceof MetaDataCalculatorImpl : "Attempting to merge a MetaDataCalculatorImpl object with "
                + "a MetaDataCalculator of another type. This indicates an implementation error.";
            merge((MetaDataCalculatorImpl)other);
        }

        @SuppressWarnings("unchecked")
        private void merge(final MetaDataCalculatorImpl other) {
            final Iterator<MetaDataCreator<?>> otherCreators = other.m_metaDataCreators.iterator();
            for (MetaDataCreator<?> creator : m_metaDataCreators) {
                assert otherCreators.hasNext();
                final MetaDataCreator<?> otherCreator = otherCreators.next();
                assert creator.getClass().equals(otherCreator.getClass());
                creator.merge(creator.getClass().cast(otherCreator));
            }
        }
    }

}
