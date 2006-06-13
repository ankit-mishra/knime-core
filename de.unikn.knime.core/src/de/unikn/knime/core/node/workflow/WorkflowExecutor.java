/* 
 * -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 * 
 * Copyright, 2003 - 2006
 * Universitaet Konstanz, Germany.
 * Lehrstuhl fuer Angewandte Informatik
 * Prof. Dr. Michael R. Berthold
 * 
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner.
 * -------------------------------------------------------------------
 * 
 * History
 *   09.05.2005 (mb): created
 *   12.01.2006 (mb): clean up for code review
 */
package de.unikn.knime.core.node.workflow;


/**
 * Interface for a workflow executor.
 * 
 * @author M. Berthold, University of Konstanz
 * @author Thorsten Meinl, University of Konstanz
 */
public interface WorkflowExecutor {
    /** Execute all nodes in workflow - return when all nodes
     * are executed (or at least Workflow claims to be done).
     */
    public void executeAll();
    
    /** Execute all nodes in workflow leading to a certain node.
     * Return when all nodes are executed (or at least Workflow
     * claims to be done).
     * 
     * @param nodeID id of node to be executed.
     */
    public void executeUpToNode(final int nodeID);
}
