package com.sphenon.basics.configuration.factories;

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
import com.sphenon.basics.message.*;
import com.sphenon.basics.exception.*;

public class PropertyModifier {
    public PropertyModifier (CallContext context) {
    }
    protected String name;
    public String getName (CallContext context) {
        return this.name;
    }
    public void setName (CallContext context, String name) {
        this.name = name;
    }
    protected String value;
    public String getValue (CallContext context) {
        return this.value;
    }
    public void setValue (CallContext context, String value) {
        this.value = value;
    }
    protected boolean prepend;
    public boolean getPrepend (CallContext context) {
        return this.prepend;
    }
    public boolean defaultPrepend (CallContext context) {
        return false;
    }
    public void setPrepend (CallContext context, boolean prepend) {
        this.prepend = prepend;
    }
    protected boolean append;
    public boolean getAppend (CallContext context) {
        return this.append;
    }
    public boolean defaultAppend (CallContext context) {
        return false;
    }
    public void setAppend (CallContext context, boolean append) {
        this.append = append;
    }
    protected boolean replace;
    public boolean getReplace (CallContext context) {
        return this.replace;
    }
    public boolean defaultReplace (CallContext context) {
        return false;
    }
    public void setReplace (CallContext context, boolean replace) {
        this.replace = replace;
    }
    protected String separator;
    public String getSeparator (CallContext context) {
        return this.separator;
    }
    public String defaultSeparator (CallContext context) {
        return "";
    }
    public void setSeparator (CallContext context, String separator) {
        this.separator = separator;
    }
}
