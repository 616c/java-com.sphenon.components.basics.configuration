package com.sphenon.basics.configuration;

/****************************************************************************
  Copyright 2001-2018 Sphenon GmbH

  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  License for the specific language governing permissions and limitations
  under the License.
*****************************************************************************/

import com.sphenon.basics.context.*;
import com.sphenon.basics.context.classes.*;
import com.sphenon.basics.variatives.*;
import com.sphenon.basics.message.*;
import com.sphenon.basics.message.classes.*;
import com.sphenon.basics.exception.*;
import com.sphenon.basics.monitoring.ProblemState;
import com.sphenon.basics.notification.BootstrapNotifier;

import java.util.Map;
import java.util.Set;
import java.util.Collection;

public class ConfigurationMap implements Map<String,Object> {

    protected Configuration configuration;

    public ConfigurationMap (CallContext context, Configuration configuration) {
        this.configuration = configuration;
    }

    protected void unsupported() {
        CallContext context = RootContext.getFallbackCallContext();
        Message message = SystemStateMessage.create(context, MessageText.create(context, "Configuration Maps do support solely simple gets"), ProblemState.ERROR);
        ExceptionContractViolation.createAndThrow(context, message);
    }

    public void clear() {
        unsupported();
    }

    public boolean containsKey(Object key) {
        return (this.configuration.get(RootContext.getFallbackCallContext(), (String) key, (String) null) != null);
    }

    public boolean containsValue(Object value) {
        unsupported();
        return false;
    }

    public Set<Map.Entry<String,Object>> entrySet() {
        unsupported();
        return null;
    }

    public Object get(Object key) {
        return this.configuration.get(RootContext.getFallbackCallContext(), (String) key, (String) null);
    }

    public boolean isEmpty() {
        unsupported();
        return false;
    }

    public Set<String> keySet() {
        unsupported();
        return null;
    }

    public Object put(String key, Object value) {
        unsupported();
        return null;
    }

    public void putAll(Map<? extends String,? extends Object> m) {
        unsupported();
    }

    public Object remove(Object key) {
        unsupported();
        return null;
    }

    public int size() {
        unsupported();
        return 0;
    }

    public Collection<Object> values() {
        unsupported();
        return null;
    }
}
