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

import com.sphenon.basics.context.CallContext;
import com.sphenon.basics.context.Context;
import com.sphenon.basics.variatives.tplinst.*;

public class ConfigurationMessenger {
    public ConfigurationMessenger() {
    }

    public void message (CallContext call_context, Variative_String_ message) {
        Context context = Context.create(call_context);
        System.err.println(message.getVariant_String_(context));
    }
}
