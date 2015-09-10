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
package org.eclipse.che.plugin.docker.client;

import com.google.inject.ImplementedBy;

/**
 * Detects container OOM and put message about it to container's log processor
 *
 * @author Alexander Garagatyi
 */
@ImplementedBy(DockerOOMDetector.NoOpDockerOOMDetector.class)
public interface DockerOOMDetector {

    void stopDetection(String container);

    void startDetection(String container, LogMessageProcessor startContainerLogProcessor);

    class NoOpDockerOOMDetector implements DockerOOMDetector {
        @Override
        public void stopDetection(String container) {
        }

        @Override
        public void startDetection(String container, LogMessageProcessor startContainerLogProcessor) {
        }
    }
}
