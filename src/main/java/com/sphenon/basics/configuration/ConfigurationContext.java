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
import com.sphenon.basics.message.*;
import com.sphenon.basics.exception.*;

import java.util.Properties;
import java.util.List;

public class ConfigurationContext extends SpecificContext {

    static public ConfigurationContext getOrCreate(Context context) {
        ConfigurationContext configuration_context = (ConfigurationContext) context.getSpecificContext(ConfigurationContext.class);
        if (configuration_context == null) {
            configuration_context = new ConfigurationContext(context);
            context.setSpecificContext(ConfigurationContext.class, configuration_context);
        }
        return configuration_context;
    }

    static public ConfigurationContext get(Context context) {
        ConfigurationContext configuration_context = (ConfigurationContext) context.getSpecificContext(ConfigurationContext.class);
        return configuration_context;
    }

    static public ConfigurationContext create(Context context) {
        ConfigurationContext configuration_context = new ConfigurationContext(context);
        context.setSpecificContext(ConfigurationContext.class, configuration_context);
        return configuration_context;
    }

    protected ConfigurationContext (Context context) {
        super(context);
        this.properties = null;
    }

    protected Properties properties;

    public void instantiateLocalProperties(CallContext context) {
        this.setProperties(context, new Properties());
    }

    public void setProperties(CallContext context, Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties(CallContext cc) {
        ConfigurationContext configuration_context;
        Properties p = this.properties;
        if (p != null) { return this.properties; }

        if ((configuration_context = (ConfigurationContext) this.getLocationContext(ConfigurationContext.class)) != null) {
            p = configuration_context.getProperties(cc);
            if (p != null) { return this.properties; }
        }

        if ((configuration_context = (ConfigurationContext) this.getCallContext(ConfigurationContext.class)) != null) {
            p = configuration_context.getProperties(cc);
            if (p != null) { return this.properties; }
        }

        return null;
    }

    public String getPropertyEntry (CallContext context, String full_key) {
        String property =
            (
                properties == null ?
                    null
                  : (
                      (property = properties.getProperty(full_key)) == null ?
                          null
                        : (  Configuration.javascript_evaluator != null && property.length() > 2 && property.charAt(0) == '\u0000' ?
                                Configuration.evaluateAndCacheProperty(context, properties, full_key, property)
                              : property
                          )
                    )
            );
        if (property == null) {
            ConfigurationContext configuration_context;
            property = 
                (configuration_context = (ConfigurationContext) this.getLocationContext(ConfigurationContext.class)) != null ?
                    configuration_context.getPropertyEntry(context, full_key)
                  : null
                ;
        }
        if (property == null) {
            ConfigurationContext configuration_context;
            property = 
                (configuration_context = (ConfigurationContext) this.getCallContext(ConfigurationContext.class)) != null ?
                    configuration_context.getPropertyEntry(context, full_key)
                  : null
                ;
        }
        if (property == null) {
            property = RootConfiguration.getPropertyEntry(context, full_key);
        }
        return property;
    }

    public void setPropertyEntry(CallContext call_context, String name, String value) {
        if (properties != null) {
            properties.setProperty(name, value);
            return;
        }
        ConfigurationContext configuration_context;
        configuration_context = (ConfigurationContext) this.getLocationContext(ConfigurationContext.class);
        if (configuration_context != null) {
            configuration_context.setPropertyEntry(call_context, name, value);
            return;
        }
        configuration_context = (ConfigurationContext) this.getCallContext(ConfigurationContext.class);
        if (configuration_context != null) {
            configuration_context.setPropertyEntry(call_context, name, value);
            return;
        }
        RootConfiguration.setPropertyEntry(call_context, name, value);
    }

    public List<String> getConfigurationVariants (CallContext context) {
        return RootConfiguration.getVariants(context);
    }
}
