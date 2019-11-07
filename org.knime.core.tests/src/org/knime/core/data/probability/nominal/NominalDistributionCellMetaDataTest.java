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
 *   Nov 6, 2019 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.core.data.probability.nominal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.knime.core.data.probability.nominal.NominalDistributionCellMetaData;
import org.knime.core.data.util.memory.MemoryAlertSystem;

/**
 * Unit tests for NominalDistributionCellMetaData.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public class NominalDistributionCellMetaDataTest {

    private static final String[] VALUES = new String[]{"A", "B", "C"};

    private static final NominalDistributionCellMetaData TEST_INSTANCE = new NominalDistributionCellMetaData(VALUES);

    /**
     * Tests the get index method.
     */
    @Test
    public void testGetIndex() {
        for (int i = 0; i < VALUES.length; i++) {
            assertEquals(i, TEST_INSTANCE.getIndex(VALUES[i]));
        }
        assertEquals(-1, TEST_INSTANCE.getIndex("Z"));
    }

    /**
     * Tests the getValueAtIndex method.
     */
    @Test
    public void testGetValueAtIndex() {
        for (int i = 0; i < VALUES.length; i++) {
            assertEquals(VALUES[i], TEST_INSTANCE.getValueAtIndex(i));
        }
    }

    /**
     * Tests the size method.
     */
    @Test
    public void testSize() {
        assertEquals(VALUES.length, TEST_INSTANCE.size());
    }

    /**
     * Tests the getValues method.
     */
    @Test
    public void testGetValues() {
        Set<String> expected = Arrays.stream(VALUES).collect(Collectors.toCollection(LinkedHashSet::new));
        assertEquals(expected, TEST_INSTANCE.getValues());
    }

    /**
     * Tests writing and reading without memory alerts, so that
     * the object should still be cached.
     *
     * @throws Exception if something went awry with the streams
     */
    @Test
    public void testWriteRead() throws Exception {
        try (PipedInputStream pipedIn = new PipedInputStream();
                final PipedOutputStream pipedOut = new PipedOutputStream(pipedIn);
                final ObjectOutputStream objOut = new ObjectOutputStream(pipedOut);
                final ObjectInputStream objIn = new ObjectInputStream(pipedIn)) {
            final NominalDistributionCellMetaData meta = new NominalDistributionCellMetaData(VALUES);
            meta.write(objOut);
            final NominalDistributionCellMetaData readBackIn = NominalDistributionCellMetaData.read(objIn);
            assertTrue(meta == readBackIn);
        }
    }

    /**
     * Tests writing and reading with a memory alert, so that the
     * cache is cleared and the object has to be reinstantiated.
     *
     * @throws Exception if something went awry with the streams
     */
    @Test
    public void testWriteReadWithMemoryAlert() throws Exception {
        try (PipedInputStream pipedIn = new PipedInputStream();
                final PipedOutputStream pipedOut = new PipedOutputStream(pipedIn);
                final ObjectOutputStream objOut = new ObjectOutputStream(pipedOut);
                final ObjectInputStream objIn = new ObjectInputStream(pipedIn)) {
            final NominalDistributionCellMetaData meta = new NominalDistributionCellMetaData(VALUES);
            meta.write(objOut);
            MemoryAlertSystem.getInstance().sendMemoryAlert();
            // wait for the memory alert to be processed
            Thread.sleep(1000);
            final NominalDistributionCellMetaData readBackIn = NominalDistributionCellMetaData.read(objIn);
            assertTrue(meta != readBackIn);
        }
    }
}
