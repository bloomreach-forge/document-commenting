/**
 * Copyright 2016-2022 Bloomreach B.V. (<a href="http://www.bloomreach.com">http://www.bloomreach.com</a>)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         <a href="http://www.apache.org/licenses/LICENSE-2.0">http://www.apache.org/licenses/LICENSE-2.0</a>
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onehippo.forge.document.commenting.cms.impl;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.iterators.AbstractIteratorDecorator;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class SimpleListDataProvider<T extends Serializable> implements IDataProvider<T> {

    private static final long serialVersionUID = 1L;

    private List<T> list;

    public SimpleListDataProvider(List<T> list) {
        this.list = list;
    }

    @Override
    public void detach() {
    }

    @Override
    public Iterator<? extends T> iterator(final long first, final long count) {
        return new AbstractIteratorDecorator(list.listIterator((int) first)) {
            private int iterationCount;
 
            @Override
            public boolean hasNext() {
                return super.hasNext() && (iterationCount < (int) count);
            }

            @Override
            public Object next() {
                try {
                    return super.next();
                } finally {
                    iterationCount += 1;
                }
            }
        };
    }

    @Override
    public long size() {
        return list.size();
    }

    @Override
    public IModel<T> model(T object) {
        return new Model<T>(object);
    }

}
