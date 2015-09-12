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

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonStreamParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;

/**
 * Docker daemon sends chunked data in response. One chunk isn't always one JSON object so need to read full chunk at once to be able
 * restore JSON object. This reader merges (if needs) few chunks until get full JSON object that we can parse.
 *
 * @author Alexander Garagatyi
 */
public class JsonMessageReader<T> {
    private static final Gson GSON = new Gson();

    private final JsonStreamParser    streamParser;
    private final Class<T>            messageClass;
    private final PushbackInputStream inputStream;

    private boolean firstRead = false;

    /**
     * @param source source of messages in JSON format
     * @param messageClass class of the message object where JSON messages should be parsed.
     *                     Because of erasure of generic information in runtime in some cases
     *                     we can't get parameter class of current class.
     */
    public JsonMessageReader(InputStream source, Class<T> messageClass) {
        this.inputStream = new PushbackInputStream(source);
        this.streamParser = new JsonStreamParser(new InputStreamReader(source));
        this.messageClass = messageClass;
    }

    public T next() throws IOException {
        if (firstRead) {
            int firstByte = inputStream.read();
            if (firstByte == -1) {
                return null;
            } else {
                inputStream.unread(firstByte);
                firstRead = false;
            }
        }
        if (streamParser.hasNext()) {
            try {
                return GSON.fromJson(streamParser.next(), messageClass);
            } catch (JsonIOException e) {
                throw new IOException(e);
            } catch (JsonParseException ignore) {
            }
        }
        return null;
    }
}
