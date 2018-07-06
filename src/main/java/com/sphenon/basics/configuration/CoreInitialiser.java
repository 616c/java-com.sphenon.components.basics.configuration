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

import java.util.*;
import java.io.*;
import java.net.*;
import java.lang.reflect.*;

public class CoreInitialiser {

    // Package Initialisation ----------------------------------------------------------------------

    /**
       Invoke the package initialisers of the packages accrording to the
       property entry com.sphenon.PackageInitialisers. Additionally, lookup a
       property entry com.sphenon.InitialiserIndex (this entry can be created
       auomatically with a special jar utility 'makejaridx.pl'). Then, for
       each initialiser in that index, look for a property named
       ".AutoInitialise". If this entry is true, invoke the respective
       PackageIntialiser, too.

       Additionally, if there is a property entry InitialisationPhases, which
       contains a comma separated list of phase names, the above process is
       repeated for each phase, and the respective property entries checked
       above are postfix with ".Phase.<phasename>", where <phasename> is
       replaced accordingly.
     */
    static protected synchronized void initialisePackages (CallContext context) {
        BootstrapNotifier.sendCheckpoint(context, "Primary initialisation phase: begin...");
        initialisePackages (context, null);
        BootstrapNotifier.sendCheckpoint(context, "Primary initialisation phase: done.");

        String phases = Configuration.get(context, "com.sphenon", "InitialisationPhases", (String) null);
        if (phases != null) {
            for (String phase : phases.split(",")) {
                BootstrapNotifier.sendCheckpoint(context, "Initialisation phase '" + phase + "': begin...");
                initialisePackages (context, phase);
                BootstrapNotifier.sendCheckpoint(context, "Initialisation phase '" + phase + "': done.");
            }
        }
    }

    /**
       Helper method, initialises one phase. See initialisePackages(CallContext).
    */
    static protected void initialisePackages (CallContext context, String phase) {
        String phase_postfix = (phase == null ? "" : (".Phase." + phase));

        String initialisers = Configuration.get(context, "com.sphenon", "PackageInitialisers" + phase_postfix, (String) null);
        if (initialisers != null) {
            BootstrapNotifier.sendTrace(context,  "PackageInitialisers '" + initialisers + "'");
            for (String initialiser : initialisers.split(":")) {
                initialisePackage(context, initialiser, phase);
            }
        }
        String auto_initialisers = Configuration.get(context, "com.sphenon", "InitialiserIndex", (String) null);
        if (auto_initialisers != null) {
            BootstrapNotifier.sendTrace(context,  "InitialiserIndex '" + auto_initialisers + "'");
            for (String auto_initialiser : auto_initialisers.split(":")) {
                BootstrapNotifier.sendTrace(context,  "AutoInitialiser '" + auto_initialiser + "'");
                if (Configuration.get(context, auto_initialiser, "AutoInitialise" + phase_postfix, false) == true) {
                    initialisePackage(context, auto_initialiser, phase, true);
                }
            }
        }
    }

    static protected Set<String> initialised_packages = new HashSet();

    static public boolean isAvailable(CallContext context, String package_path) {
        return (    initialised_packages != null
                 && (    initialised_packages.contains(package_path)
                      || initialised_packages.contains(package_path + "PackageInitialiser")
                    )
               );
    }

    /**
       Initialise the PackageInitialiser identified by the given
       argument. Maintains a hashtable to guarantee that each initialiser is
       only called once. The given class must provide a static method named
       "initialise" which takes a parameter of type CallContext.

       @param class_name The fully qualified name of the package initialiser
                         to invoke
     */
    static public synchronized void initialisePackage (CallContext context, String class_name) {
        initialisePackage(context, class_name, null);
    }

    /**
       Phase specific worker. See initialisePackage(CallContext, String).
     */
    static protected void initialisePackage (CallContext context, String class_name, String phase) {
        initialisePackage(context, class_name, phase, false);
    }

    static protected void initialisePackage (CallContext context, String class_name, String phase, boolean optional) {
        String phase_postfix = (phase == null ? "" : ("-Phase-" + phase));

        if (optional == false) {
            int cnl = class_name.length();
            if (class_name.charAt(cnl-1) == '?') {
                class_name = class_name.substring(0, cnl-1);
                optional = true;
            }
        }

        // don't do it twice
        if (initialised_packages.contains(class_name + phase_postfix)) { return; }
        initialised_packages.add(class_name + phase_postfix);

        BootstrapNotifier.sendCheckpoint(context,  "Initialising '" + class_name + "'...");

        String initialiser = class_name;
        if (initialiser.matches("^[a-z0-9.]+$")) {
            // EMOS2 PackageManager
            if (phase == null) {
                Class pic = null;
                try {
                    pic = com.sphenon.basics.cache.ClassCache.getClassForName(context, initialiser);
                } catch (ClassNotFoundException cnfe) {
                    try {
                        initialiser += ".PackageManager";
                        pic = com.sphenon.basics.cache.ClassCache.getClassForName(context, initialiser);
                    } catch (ClassNotFoundException cnfe2) {
                        if (optional) {
                            BootstrapNotifier.sendCheckpoint(context,  "Initialising '" + class_name + "' - skipped: package not present in distribution.");
                            return;
                        } else {
                            Message message = SystemStateMessage.create(context, MessageText.create(context, "Initialisation of package '%(class_name)' failed (class not found, EMOS2)", "class_name", class_name), ProblemState.ERROR);
                            ExceptionConfigurationError.createAndThrow(context, cnfe, message);
                            throw (ExceptionConfigurationError) null; // compiler insists
                        }
                    }
                }

                try {
                    Method im = pic.getMethod("getSingleton", CallContext.class);
                    im.invoke(null, context);
                } catch (NoSuchMethodException nsme) {
                    Message message = SystemStateMessage.create(context, MessageText.create(context, "Initialisation of package '%(class_name)' failed (method not found, EMOS2)", "class_name", class_name), ProblemState.ERROR);
                    ExceptionConfigurationError.createAndThrow(context, nsme, message);
                    throw (ExceptionConfigurationError) null; // compiler insists
                } catch (IllegalAccessException iae) {
                    Message message = SystemStateMessage.create(context, MessageText.create(context, "Initialisation of package '%(class_name)' failed", "class_name", class_name), ProblemState.ERROR);
                    ExceptionConfigurationError.createAndThrow(context, iae, message);
                    throw (ExceptionConfigurationError) null; // compiler insists
                } catch (InvocationTargetException ite) {
                    Message message = SystemStateMessage.create(context, MessageText.create(context, "Initialisation of package '%(class_name)' failed", "class_name", class_name), ProblemState.ERROR);
                    ExceptionConfigurationError.createAndThrow(context, ite, message);
                    throw (ExceptionConfigurationError) null; // compiler insists
                }
            }
        } else {
            // EMOS1 PackageInitialiser
            Class ic = null;
            try {
                ic = com.sphenon.basics.cache.ClassCache.getClassForName(context, initialiser);
            } catch (ClassNotFoundException cnfe) {
                try {
                    initialiser += "PackageInitialiser";
                    ic = com.sphenon.basics.cache.ClassCache.getClassForName(context, initialiser);
                } catch (ClassNotFoundException cnfe2) {
                    if (optional) {
                        BootstrapNotifier.sendCheckpoint(context,  "Initialising '" + class_name + "' - skipped: class not present in distribution.");
                        return;
                    }
                    Message message = SystemStateMessage.create(context, MessageText.create(context, "Initialisation of package '%(class_name)' failed (class not found, EMOS1)", "class_name", class_name), ProblemState.ERROR);
                    ExceptionConfigurationError.createAndThrow(context, cnfe, message);
                    throw (ExceptionConfigurationError) null; // compiler insists
                }
            }
            Method im = null;
            try {
                if (phase == null) {
                    im = ic.getMethod("initialise", com.sphenon.basics.context.CallContext.class);
                } else {
                    im = ic.getMethod("initialise", com.sphenon.basics.context.CallContext.class, java.lang.String.class);
                }
            } catch (NoSuchMethodException nsme) {
                Message message = SystemStateMessage.create(context, MessageText.create(context, "Initialisation of package '%(class_name)' failed (method not found, EMOS1)", "class_name", class_name), ProblemState.ERROR);
                ExceptionConfigurationError.createAndThrow(context, nsme, message);
                throw (ExceptionConfigurationError) null; // compiler insists
            }
            try {
                if (phase == null) {
                    im.invoke(null, context);
                } else {
                    im.invoke(null, context, phase);
                }
            } catch (IllegalAccessException iae) {
                Message message = SystemStateMessage.create(context, MessageText.create(context, "Initialisation of package '%(class_name)' failed", "class_name", class_name), ProblemState.ERROR);
                ExceptionConfigurationError.createAndThrow(context, iae, message);
                throw (ExceptionConfigurationError) null; // compiler insists
            } catch (InvocationTargetException ite) {
                Message message = SystemStateMessage.create(context, MessageText.create(context, "Initialisation of package '%(class_name)' failed", "class_name", class_name), ProblemState.ERROR);
                ExceptionConfigurationError.createAndThrow(context, ite, message);
                throw (ExceptionConfigurationError) null; // compiler insists
            }
        }

        BootstrapNotifier.sendCheckpoint(context,  "Initialising '" + class_name + "' - done.");
    }
}
