/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.svn.shared;

import java.util.List;

import org.eclipse.che.dto.shared.DTO;

/**
 * Request DTO for both lock and unlock requests.
 */
@DTO
public interface LockRequest {

    /**
     * Returns the path of the project.
     * 
     * @return the project path
     */
    String getProjectPath();

    void setProjectPath(String path);

    LockRequest withProjectPath(String path);

    /**
     * Returns the targets to lock.
     * 
     * @return the targets
     */
    List<String> getTargets();

    void setTargets(List<String> targets);

    LockRequest withTargets(List<String> targets);

    /**
     * The force option allows svn to steal locks from other users.<br>
     * without force, svn leaves the files to the previous lock owner and warns.
     * 
     * @return the 'force' value
     */
    boolean isForce();

    LockRequest withForce(boolean force);

    void setForce(boolean force);
}
