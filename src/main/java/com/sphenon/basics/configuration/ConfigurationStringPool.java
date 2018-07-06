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
import com.sphenon.basics.variatives.*;
import com.sphenon.basics.variatives.classes.*;

public class ConfigurationStringPool extends StringPoolClass {
    static protected ConfigurationStringPool singleton = null;

    static public ConfigurationStringPool getSingleton (CallContext cc) {
        if (singleton == null) {
            singleton = new ConfigurationStringPool(cc);
        }
        return singleton;
    }

    static public VariativeString get(CallContext cc, String id) {
        return VariativeStringClass.createVariativeStringClass(cc, id, getSingleton(cc));
    }

    static public String get(CallContext cc, String id, String isolang) {
        return getSingleton(cc).getString(cc, id, isolang);
    }

    protected ConfigurationStringPool (CallContext cc) {
        super(cc);
        /*************************************************/
        /* THE FOLLOWING SECTION IS PARTIALLY GENERATED. */
        /* BE CAREFUL WHEN EDITING MANUALLY !            */
        /*                                               */
        /* See StringPool.java for explanation.          */
        /*************************************************/
        //BEGINNING-OF-STRINGS
        //P-0-com.sphenon.basics.configuration
        //F-0-0-Configuration.java
        addEntry(cc, "0.0.0", "en", "Configuration file '%(configfile)' not found");
        addEntry(cc, "0.0.0", "de", "Konfigurations-Datei '%(configfile)' nicht gefunden");
        addEntry(cc, "0.0.1", "en", "Invalid configuration file '%(configfile)'");
        addEntry(cc, "0.0.1", "de", "Fehlerhafte Konfigurations-Datei '%(configfile)'");
        addEntry(cc, "0.0.2", "en", "Error during closing of configuration file '%(configfile)'; but configuration seemingly was read successfully; details: %(reason)");
        addEntry(cc, "0.0.2", "de", "Fehler beim Schließen der Konfigurations-Datei '%(configfile)'; die Konfiguration wurde jedoch anscheinend erfolgreich gelesen; Details: %(reason)");
        addEntry(cc, "0.0.3", "en", "No configuration file in use while looking for '%(looking_for)' (tried default '%(configfile)', but does not exist, current working directory is '%(cwd)'); details: %(reason)");
        addEntry(cc, "0.0.3", "de", "Bei der Suche nach '%(looking_for)' wird keine Konfigurations-Datei benutzt (die Vorgabe '%(configfile)' wurde probiert, existiert aber nicht, aktuelles Arbeitsverzeichnis ist '%(cwd)'); Details: %(reason)");
        addEntry(cc, "0.0.4", "en", "Property '%(key)' contains invalid entry '%(entry)'");
        addEntry(cc, "0.0.4", "de", "Eigenschaft '%(key)' enthält ungültigen Eintrag '%(entry)'");
        //END-OF-STRINGS
        /*************************************************/
    }
}
