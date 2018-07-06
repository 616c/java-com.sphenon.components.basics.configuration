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

public class ConfigurationPackageInitialiser {

    static protected boolean initialised = false;

    static {
        initialise(RootContext.getRootContext());
    }

    static public synchronized void initialise (CallContext context) {
        
        if (initialised == false) {
            initialised = true;

            // may be we come from here, but if not, we need this first
            Configuration.initialise(context);

            // note: the configuration package is special,
            // here we load "local.properties", not ".properties", as in every other package,
            // since ".properties" is the default place for the whole configuration where
            // users will place their own config, so there's the need for a place
            // for "Configuration"-specific config
            Configuration.loadDefaultProperties(context, com.sphenon.basics.configuration.ConfigurationPackageInitialiser.class, ".local.properties");
        }
    }
}
