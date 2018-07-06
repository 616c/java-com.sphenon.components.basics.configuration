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

import com.sphenon.basics.configuration.*;

public class Factory_ConfigurationContext {

    public void Factory_ConfigurationContext(CallContext context) {
    }

    public ConfigurationContext create(CallContext context) {
        ConfigurationContext result_context = ConfigurationContext.create(this.context);
        if (this.property_modifiers != null) {
            for (PropertyModifier property_modifier : property_modifiers) {
                String name = property_modifier.getName(context);
                String current = null;
                String value =     (   (     property_modifier.getPrepend(context)
                                         && (current = result_context.getPropertyEntry(context, name)) != null
                                       ) ?
                                         ( current + property_modifier.getSeparator(context) )
                                       : ""
                                   )
                                 + property_modifier.getValue(context)
                                 + (   (     property_modifier.getAppend(context)
                                         && (current = result_context.getPropertyEntry(context, name)) != null
                                       ) ?
                                         ( property_modifier.getSeparator(context) + current )
                                       : ""
                                   )
                               ; 
                result_context.setPropertyEntry(context, name, value);
            }
        }
        return result_context;
    }

    protected Context context;

    public Context getContext (CallContext context) {
        return this.context;
    }

    public void setContext (CallContext context, Context arg_context) {
        this.context = arg_context;
    }

    protected PropertyModifier[] property_modifiers;

    public PropertyModifier[] getPropertyModifiers (CallContext context) {
        return this.property_modifiers;
    }

    public PropertyModifier[] defaultPropertyModifiers (CallContext context) {
        return null;
    }

    public void setPropertyModifiers (CallContext context, PropertyModifier[] property_modifiers) {
        this.property_modifiers = property_modifiers;
    }
}
