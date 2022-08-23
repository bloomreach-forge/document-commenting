/**
 * Copyright 2016-2022 Bloomreach (http://www.bloomreach.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onehippo.forge.document.commenting.cms.api;

/**
 * Document commenting exception.
 */
public class CommentingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CommentingException() {
        super();
    }

    public CommentingException(String message) {
        super(message);
    }

    public CommentingException(Throwable nested) {
        super(nested);
    }

    public CommentingException(String msg, Throwable nested) {
        super(msg, nested);
    }

}
