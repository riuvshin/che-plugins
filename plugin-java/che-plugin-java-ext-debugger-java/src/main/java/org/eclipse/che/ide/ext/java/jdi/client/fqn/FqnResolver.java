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
package org.eclipse.che.ide.ext.java.jdi.client.fqn;

import org.eclipse.che.ide.api.project.tree.VirtualFile;

import javax.validation.constraints.NotNull;

/**
 * @author Evgen Vidolob
 */
public interface FqnResolver {
    @NotNull
    String resolveFqn(@NotNull VirtualFile file);
}