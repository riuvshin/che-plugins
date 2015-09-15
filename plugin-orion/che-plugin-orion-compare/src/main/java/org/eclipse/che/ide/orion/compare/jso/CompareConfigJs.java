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
package org.eclipse.che.ide.orion.compare.jso;

import com.google.gwt.core.client.JavaScriptObject;

import org.eclipse.che.ide.orion.compare.CompareConfig;
import org.eclipse.che.ide.orion.compare.FileOptions;

/**
 * @author Evgen Vidolob
 */
public class CompareConfigJs extends JavaScriptObject implements CompareConfig {
    protected CompareConfigJs() {
    }

    @Override
    public final native void setElementId(String id)/*-{
        this.parentDivId = id;
    }-*/;

    @Override
    public final native void setOldFile(FileOptions oldFile)/*-{
        this.oldFile = oldFile;
    }-*/;

    @Override
    public final native void setNewFile(FileOptions newFile)/*-{
        this.newFile = newFile;
    }-*/;

    @Override
    public final native void setShowTitle(boolean showTitle)/*-{
        this.showTitle = showTitle;
    }-*/;

    @Override
    public final native void setShowLineStatus(boolean showLineStatus)/*-{
        this.showLineStatus = showLineStatus;
    }-*/;
}
