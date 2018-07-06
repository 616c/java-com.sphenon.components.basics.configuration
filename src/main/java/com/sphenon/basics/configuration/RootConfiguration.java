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
import java.util.regex.*;
import java.io.*;
import java.net.*;
import java.lang.reflect.*;

public class RootConfiguration {

    // -------------------------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------
    // The property tables -----------------------------------------------------------------------------------

    static protected Properties properties = null;
    static protected Properties default_properties = new Properties();

    static protected boolean trace_property_origins = false;

    // -------------------------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------
    // Initialisation ----------------------------------------------------------------------------------------


    static protected boolean config_folders_initialised = false;

    /**
       Initialize the configuration folders from which configuration files will be loaded.
       
       Note: method has no context, since it's really called "very-first".
    */
    static public synchronized void initialiseConfigFolders () {
        if (config_folders_initialised == false) {
            config_folders_initialised = true;

            config_folders  = new Vector<ConfigFolder>();

            config_folders.add(new ConfigFolder("/etc/sphenon", true));
            String home = (String) System.getProperties().get("user.home");
            if (home != null) {
                config_folders.add(new ConfigFolder(home + "/.sphenon", true));
                config_folders.add(new ConfigFolder(home, false));
            }
            String cwd = (String) System.getProperties().get("user.dir");
            if (cwd != null) {
                config_folders.add(new ConfigFolder(cwd + "/.sphenon", true));
                config_folders.add(new ConfigFolder(cwd, false));
            }
        }
    }

    static protected boolean initialised = false;

    static public synchronized void initialise (CallContext context) {
        if (initialised == false) {
            initialised = true;
            
            initialiseConfigFolders();
            
            properties = new java.util.Properties(default_properties);
            
            String isp = System.getProperty("com.sphenon.basics.configuration.IncludeSystemProperties");
            include_system_properties = (isp != null && isp.equals("true") ? true : false);
            BootstrapNotifier.sendCheckpoint(context,  "System Properties: " + (include_system_properties ? "enabled" : "disabled") + " - " + isp);
            
            loadProperties (context, ".properties", com.sphenon.basics.configuration.Configuration.class, true);
            
            includeProperties(context);
            
            if (clarg_properties != null) {
                for (String clarg_property : clarg_properties) {
                    String[] kv = clarg_property.split(":",2);
                    setPropertyEntry(context, kv[0], kv[1]);
                }
            }
            
            CoreInitialiser.initialisePackages(context);
        }
    }
    
    static protected Pattern uri_escape;

    static public String decode(CallContext context, String string) {
        if (uri_escape == null) {
            try {
                uri_escape = Pattern.compile("%([A-Fa-f0-9][A-Fa-f0-9])");
            } catch (PatternSyntaxException pse) {
                ExceptionAssertionProvedFalse.createAndThrow(context, SystemStateMessage.create(context, MessageText.create(context, "Syntax error in regular expression"), ProblemState.ERROR));
                throw (ExceptionAssertionProvedFalse) null; // compiler insists
            }
        }
        Matcher m = uri_escape.matcher(string);
        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            char c1 = m.group(1).toUpperCase().charAt(0);
            char c2 = m.group(1).toUpperCase().charAt(1);
            char c = (char) ((c1 - (c1 > 64 ? 55 : 48)) * 16 + (c2 - (c2 > 64 ? 55 : 48)));
            m.appendReplacement(sb, "");
            sb.append(c);
        }
        m.appendTail(sb);

        return sb.toString();
    }
    
    
    // -------------------------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------
    // Further attributes ------------------------------------------------------------------------------------

    static protected String   host;
    static protected String   user;
    static protected String   os;
    static protected String   osversion;
    static protected String   javaversion;

    static protected String   configuration_name;
    static protected String   configuration_ui_name;
    static protected String   configuration_db_name;
    static protected String   explicit_configuration_variant;
    static protected Vector<String> clarg_properties;

    static protected class ConfigFolder {
        public ConfigFolder(String name, boolean sphenon_specific) { this.name = name; this.sphenon_specific = sphenon_specific; }
        public String  name;
        public boolean sphenon_specific;
    }

    static protected Vector<ConfigFolder> config_folders;    

    static public String breakpoint;
    
    static public Vector<String> checkCommandLineArgs(String[] args) {
        Vector<String> unprocessed = new Vector<String>();
        String clswitch1 = "--configuration-name=";
        String clswitch1a = "--configuration-ui-name=";
        String clswitch1b = "--configuration-db-name=";
        String clswitch2 = "--configuration-variant=";
        String clswitch3 = "--property=";
        String clswitch4 = "--debugger-wait";
        for (int i=0; i<args.length; i++) {
            if (args[i].matches("^"+clswitch1+".*")) {
                setConfigurationName(args[i].substring(clswitch1.length()));
            } else if (args[i].matches("^"+clswitch1a+".*")) {
                setConfigurationUIName(args[i].substring(clswitch1a.length()));
            } else if (args[i].matches("^"+clswitch1b+".*")) {
                setConfigurationDBName(args[i].substring(clswitch1b.length()));
            } else if (args[i].matches("^"+clswitch2+".*")) {
                setExplicitConfigurationVariant(args[i].substring(clswitch2.length()));

              // System.err.println("ERROR: command line switch '--configuration-variant=' not any longer supported. Please replace with '--configuration-name=' and put an appropriate entry in your .configuration file\n");
              // System.exit(-1);
            } else if (args[i].matches("^"+clswitch3+".*")) {
                setPropertyOverride(args[i].substring(clswitch3.length()));
            } else if (args[i].matches("^"+clswitch4)) {
                System.err.println("Press RETURN to continue...");
                try {
                    String s = (new java.io.BufferedReader(new java.io.InputStreamReader(System.in))).readLine();
                    if (s != null) {
                        breakpoint = s.replaceFirst("\n*$","");
                    }
                } catch (java.io.IOException ioe) {
                }
            } else {
                unprocessed.add(args[i]);
            }
        }
        return unprocessed;
    }

    // -------------------------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------
    // Initialise and update property tables -----------------------------------------------------------------

    static protected long created = 0;
    static protected File file = null;
    static protected boolean tried = false;

    /**
       Ensures the loading and optionally updating of the root configuration
       and returns the respective property instance.
     */
    static protected Properties getProperties (CallContext context) {
        if ((properties == null && tried == false) || (properties != null && file != null && created < file.lastModified())) {
            tried = true;
            initialised = false;
            initialise (context);
        }
        return properties;
    }

    // -------------------------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------
    // Actual access to root properties ----------------------------------------------------------------------

    static public String getPropertyEntry (CallContext context, String full_key) {
        Properties properties = null;
        String property = null;
        return
            (
                (properties = getProperties(context)) == null ?
                    null
                  : (
                          (property = properties.getProperty(full_key)) == null
                       && (    include_system_properties == false
                            || (property = getSystemProperty(context, full_key)) == null
                          ) ?
                          null
                        : (  Configuration.javascript_evaluator != null && property.length() > 2 && property.charAt(0) == '\u0000' ?
                                Configuration.evaluateAndCacheProperty(context, properties, full_key, property)
                              : property
                          )
                    )
            );
    }

    static public void appendPropertyEntryVariants (CallContext context, String key, StringBuilder value) {
        if (getVariants(context) != null) {
            for (String variant : getVariants(context)) {
                String variant_key   = key + "-" + variant;
                String variant_value = getPropertyEntry(context, variant_key);
                if (variant_value != null) {
                    value.append(variant_value);
                }
            }
        }
    }

    static protected boolean include_system_properties;

    static protected String getSystemProperty (CallContext context, String full_key) {
        String value = System.getProperty(full_key);
        if (value != null) {
            setPropertyEntry(context, full_key, value);
        }
        return value;
    }

    /**
       Sets a property specified by it's fully qualified name to the given
       value.

       @param name Fully qualified property name
       @param value The value to set
    */
    static public void setPropertyEntry(CallContext call_context, String name, String value) {
        properties.setProperty(name, value);
    }

    // -------------------------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------
    // Load included properties ------------------------------------------------------------------------------

    static protected String[] include_properties;

    static protected synchronized String[] getIncludeProperties (CallContext context) {
        if (include_properties == null) {
            String includes = Configuration.get(context, "com.sphenon", "IncludeProperties", (String) null);
            if (includes != null) {
                include_properties = includes.split(":"); 
            } else {
                include_properties = new String[0];
            }
        }
        return include_properties;
    }

    /**
       Searches, in the currently loaded properties, for an entry named
       com.sphenon.IncludeProperties containing a colon ':' separated path and
       loads for each entry all variants of that property with the given
       name. These are core properties, therefore the locations that are
       searched are java resources of class Configuration as well as the
       environmental property locations, see methods "loadProperties".
     */
    static protected synchronized void includeProperties (CallContext context) {
        if (include_properties == null) {
            String includes = Configuration.get(context, "com.sphenon", "IncludeProperties", (String) null);
            if (includes != null) {
                include_properties = includes.split(":"); 
            } else {
                include_properties = new String[0];
            }
        }
        for (String ip : getIncludeProperties(context)) {
            loadProperties (context, ip, com.sphenon.basics.configuration.Configuration.class, true);
        }
    }

    // -------------------------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------

    /**
       Appends colon separated list of strings to colon separated list of strings,
       but only those parts that are not already contained.

       @param current the list to which entries are appended
       @param additional the list of additional entries
       @param modified will be set to true if entries have been appended, false otherwise
       @return the combined list
     */
    static protected String addTo(String current, String additional, Boolean modified) {
        if (additional == null || additional.isEmpty()) {
            modified = false;
            return current;
        }
        if (current == null || current.isEmpty()) {
            modified = true;
            return additional;
        }
        String[] cs = current.split(":");
        String[] as = additional.split(":");
        for (String a : as) {
            if (a.isEmpty() == false) {
                boolean contained = false;
                for (String c : cs) {
                    if (c.equals(a)) {
                        contained = true;
                        break;
                    }
                }
                if (contained == false) {
                    modified = true;
                    current += ":" + a;
                }
            }
        }
        return current;
    }

    /**
       Checks whether strings differ and if so, whether system has already
       been initialised, and if so, throws an exception

       @param current current string
       @param value new string
     */
    static protected void canSet(String current, String value) {
        boolean modified = false;
        if (value == null || value.isEmpty()) {
            if (current == null || current.isEmpty()) {
                modified = false;
            } else {
                modified = true;
            }
        } else {
            if (current == null || current.isEmpty()) {
                modified = true;
            } else {
                modified = current.equals(value) ? false : true;
            }
        }
        checkInitialisation(modified);
    }

    static public void checkInitialisation(boolean modified) {
        if (modified && initialised) {
            throw new ConfigurationAfterInitialisation();
        }
    }

    // -------------------------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------
    // Configuration variant determination -------------------------------------------------------------------

    static protected Vector<String> hostclasses = null;
    static protected Vector<String> userclasses = null;

    static protected List<String> configuration_variants;
    static protected boolean      configuration_variants_loaded = false;

    /**
       Add a configuration folder from which configuration files will be loaded.
       This folder is appended to the set of default folders.
       
       Note: method has no context, since it's really called "very-first".
       
       @param folder folder to bee appended
     */
    static public void addConfigurationFolder(String folder) {
        initialiseConfigFolders();
        if (folder != null) {
            config_folders.add(new ConfigFolder(folder + "/.sphenon", true));
            config_folders.add(new ConfigFolder(folder, true));
        }
    }
    /**
       Specify a configuration name, which is used as a property in rule
       evaluation in the configuration files.

       Must be called at the earliest possible point in time of execution
       before any initialisation took place. Otherwise, throws an exception.

       Note: method has no context, since it's really called "very-first".

       @param configuration_name_par A simple string naming a configuration
    */
    static public synchronized void setConfigurationName(String configuration_name_par) {
        canSet(configuration_name, configuration_name_par);
        configuration_name = configuration_name_par;
    }

    /**
       Specify a configuration ui name, which is used as a property in rule
       evaluation in the configuration files.

       Must be called at the earliest possible point in time of execution
       before any initialisation took place. Otherwise, throws an exception.

       Note: method has no context, since it's really called "very-first".

       @param configuration_ui_name_par A simple string naming a configuration ui, like "jsp" or "swt"
    */
    static public void setConfigurationUIName(String configuration_ui_name_par) {
        Boolean modified = false;
        configuration_ui_name = addTo(configuration_ui_name, configuration_ui_name_par, modified);
        checkInitialisation(modified);
    }

    /**
       Specify a configuration db name, which is used as a property in rule
       evaluation in the configuration files.

       Must be called at the earliest possible point in time of execution
       before any initialisation took place. Otherwise, throws an exception.

       Note: method has no context, since it's really called "very-first".

       @param configuration_db_name_par A simple string naming a configuration db, like "jpa"
    */
    static public void setConfigurationDBName(String configuration_db_name_par) {
        Boolean modified = false;
        configuration_db_name = addTo(configuration_db_name, configuration_db_name_par, modified);
        checkInitialisation(modified);
    }

    /**
       Specifies a configuration variant, which is appended to the variant
       sequence and therefore the respective property variants take precedence
       of the ones defined by the configuration files.

       This method must be called at the earliest possible point in time of
       execution before any initialisation took place.

       Note: method has no context, since it's really called "very-first".

       @param explicit_configuration_variant_par A colon separated list of variants
    */
    static public void setExplicitConfigurationVariant(String explicit_configuration_variant_par) {
        canSet(explicit_configuration_variant, explicit_configuration_variant_par);
        explicit_configuration_variant = explicit_configuration_variant_par;
    }
    
    /**
       Appends explicit configuration variant if explicat_configuration is set, or appends if
       explicit configuration variant is already specified 
       @param explicit_configuration_variant_par
     */
    static public void appendExplicitConfigurationVariant(String explicit_configuration_variant_par) {
        Boolean modified = false;
        explicit_configuration_variant = addTo(explicit_configuration_variant, explicit_configuration_variant_par, modified);
        checkInitialisation(modified);
    }

    /**
       Specifies the value of a property which overrides any values read from
       property files and influences the initialisation process. It is
       typically invoked when parsing the command line.

       This method must be called at the earliest possible point in time of
       execution before any initialisation took place.

       Note: method has no context, since it's really called "very-first".

       @param override A property and it's value, separated by a colon ":"
    */
    static public void setPropertyOverride(String override) {
        if (clarg_properties == null) {
            clarg_properties = new Vector<String>();
        }
        clarg_properties.add(override);
    }

    static protected boolean matches(Vector<String> strings, String regexp) {
        if (strings != null && regexp != null) {
            for (String string : strings) {
                if (string.matches(regexp)) {
                    return true;
                }
            }
        }
        return false;
    }

    static protected void prepareBasicParameters(CallContext context) {
        host = "?";
        try { host = InetAddress.getLocalHost().getHostName(); } catch (UnknownHostException uhe) {};
        user        = (String) System.getProperty("user.name", "?");
        os          = (String) System.getProperty("os.name", "?");
        osversion   = (String) System.getProperty("os.version", "?");
        javaversion = (String) System.getProperty("java.specification.version", "?");
        
        BootstrapNotifier.sendTrace(context,  "host '" + host + "'");
        BootstrapNotifier.sendTrace(context,  "user '" + user + "'");
        BootstrapNotifier.sendTrace(context,  "os '" + os + "'");
        BootstrapNotifier.sendTrace(context,  "osversion '" + osversion + "'");
        BootstrapNotifier.sendTrace(context,  "javaversion '" + javaversion + "'");
        if (configuration_name == null) {
            configuration_name = "";
        }
        BootstrapNotifier.sendTrace(context,  "configuration '" + configuration_name + "'");
        if (configuration_ui_name == null) {
            configuration_ui_name = "";
        }
        BootstrapNotifier.sendTrace(context,  "configuration ui '" + configuration_ui_name + "'");
        if (configuration_db_name == null) {
            configuration_db_name = "";
        }
        BootstrapNotifier.sendTrace(context,  "configuration db '" + configuration_db_name + "'");
        if (explicit_configuration_variant == null) {
            explicit_configuration_variant = "";
        }
        BootstrapNotifier.sendTrace(context,  "explicit variants '" + explicit_configuration_variant + "'");
    }

    static protected void addVariants(CallContext context, List<String> variants, Set<String> duplicates, String variant_string) {
        if (variant_string != null && variant_string.isEmpty() == false) {
            for (String variant : variant_string.split(":")) {
                if (variant != null && variant.isEmpty() == false && duplicates.contains(variant) == false) {
                    variants.add(variant);
                    duplicates.add(variant);
                }
            }
        }        
    }

    /**
       Parses a variant (configuration) file.

       @param reader     Where the data is read from
       @param variants   Partially calculated variant sequence, which is augmented
       @param duplicates A set used to check for duplicates in the variants list
       @param sourcename Name of source associated with the reader for
                         debugging and error reporting purposes
    */
    static protected void parseVariantFile(CallContext context, Reader reader, List<String> variants, Set<String> duplicates, String sourcename, Queue<String> cvariants, Set<String> cduplicates) {
        BootstrapNotifier.sendCheckpoint(context,  "parsing variant file '" + sourcename + "'");

        BufferedReader br = new BufferedReader(reader);
        String line;
        int linenbr = 0;

        try {
            lines: while ((line = br.readLine()) != null) {
                BootstrapNotifier.sendTrace(context,  "got line '" + line + "'");

                linenbr++;
                if (line.matches("^\\s*$")) { continue lines; }
                if (line.matches("^\\s*#.*")) { continue lines; }
                String[] keyval = line.split("=",-1);
                if (keyval == null || keyval.length != 2) {
                    ExceptionConfigurationError.createAndThrow(context, SystemStateMessage.create(context, MessageText.create(context, "Syntax error in configuration file '%(source)', line %(linenbr), expected exatly one '=' separator: '%(line)'", "source", sourcename, "linenbr", t.s(linenbr), "line", line), ProblemState.ERROR));
                    throw (ExceptionConfigurationError) null; // compiler insists
                }
                if (keyval[0].startsWith("@hostclass:")) {
                    String host_class = keyval[0].substring(11);
                    if (host_class.isEmpty()) {
                        ExceptionConfigurationError.createAndThrow(context, SystemStateMessage.create(context, MessageText.create(context, "Syntax error in configuration file '%(source)', line %(linenbr), host class is empty: '%(line)'", "source", sourcename, "linenbr", t.s(linenbr), "line", line), ProblemState.ERROR));
                        throw (ExceptionConfigurationError) null; // compiler insists
                    }
                    for (String host_entry : keyval[1].split(",")) {
                        if (host_entry != null && host_entry.isEmpty() == false) {
                            if (host_entry.equals(host)) {
                                if (hostclasses == null) {
                                    hostclasses = new Vector<String>();
                                }
                                hostclasses.add(host_class);
                                BootstrapNotifier.sendTrace(context,  "host class '" + host_class + "'");
                            }
                        }
                    }
                    continue lines;
                }
                if (keyval[0].startsWith("@userclass:")) {
                    String user_class = keyval[0].substring(11);
                    if (user_class.isEmpty()) {
                        ExceptionConfigurationError.createAndThrow(context, SystemStateMessage.create(context, MessageText.create(context, "Syntax error in configuration file '%(source)', line %(linenbr), user class is empty: '%(line)'", "source", sourcename, "linenbr", t.s(linenbr), "line", line), ProblemState.ERROR));
                        throw (ExceptionConfigurationError) null; // compiler insists
                    }
                    for (String user_entry : keyval[1].split(",")) {
                        if (user_entry != null && user_entry.isEmpty() == false) {
                            if (user_entry.equals(user)) {
                                if (userclasses == null) {
                                    userclasses = new Vector<String>();
                                }
                                userclasses.add(user_class);
                                BootstrapNotifier.sendTrace(context,  "user class '" + user_class + "'");
                            }
                        }
                    }
                    continue lines;
                }
                if (keyval[0].equals("@include.immediately")) {
                    for (String include : keyval[1].split(",")) {
                        if (include != null && include.isEmpty() == false) {
                            BootstrapNotifier.sendTrace(context,  "including configuration '" + include + "' immediately");
                            tryConfiguration(context, variants, duplicates, include, cvariants, cduplicates);
                        }
                    }
                    continue lines;
                }
                if (keyval[0].equals("@include.afterwards")) {
                    for (String include : keyval[1].split(",")) {
                        if (include != null && include.isEmpty() == false) {
                            BootstrapNotifier.sendTrace(context,  "including configuration '" + include + "' afterwards");
                            if (cduplicates.contains(include) == false) {
                                cvariants.add(include);
                                cduplicates.add(include);
                            }
                        }
                    }
                    continue lines;
                }
                String[] keys = keyval[0].split(",",-1);
                if (keys == null) {
                    ExceptionConfigurationError.createAndThrow(context, SystemStateMessage.create(context, MessageText.create(context, "Syntax error in configuration file '%(source)', line %(linenbr), expected at least one key left of '=' separator: '%(line)'", "source", sourcename, "linenbr", t.s(linenbr), "line", line), ProblemState.ERROR));
                    throw (ExceptionConfigurationError) null; // compiler insists
                }
                for (int i=0; i<keys.length; i++) {
                    if (keys[i] != null && keys[i].length() != 0) {
                        String[] rule = keys[i].split(":",-1);
                        if (rule == null || (rule.length != 2 && rule.length != 3)) {
                            ExceptionConfigurationError.createAndThrow(context, SystemStateMessage.create(context, MessageText.create(context, "Syntax error in configuration file '%(source)', line %(linenbr), a key rule entry must contain either one or two ':' separators: '%(line)'", "source", sourcename, "linenbr", t.s(linenbr), "line", line), ProblemState.ERROR));
                            throw (ExceptionConfigurationError) null; // compiler insists
                        }
                        if (rule[0] == null || rule[0].length() == 0) {
                            ExceptionConfigurationError.createAndThrow(context, SystemStateMessage.create(context, MessageText.create(context, "Syntax error in configuration file '%(source)', line %(linenbr), a key rule key must not be empty: '%(line)'", "source", sourcename, "linenbr", t.s(linenbr), "line", line), ProblemState.ERROR));
                            throw (ExceptionConfigurationError) null; // compiler insists
                        }
                        String inc = rule[1];
                        String exc = rule.length == 2 ? null : rule[2];
                        if (rule[0].equals("host")          && (    (inc != null && host.matches(inc) == false)
                                                                 || (exc != null && host.matches(exc) == true)
                                                               )                                                                  ) { continue lines; }
                        if (rule[0].equals("hostclass")     && (    (inc != null && matches(hostclasses, inc) == false)
                                                                 || (exc != null && matches(hostclasses, exc) == true)
                                                               )                                                                  ) { continue lines; }
                        if (rule[0].equals("user")          && (    (inc != null && user.matches(inc) == false)
                                                                 || (exc != null && user.matches(exc) == true)
                                                               )                                                                  ) { continue lines; }
                        if (rule[0].equals("userclass")     && (    (inc != null && matches(userclasses, inc) == false)
                                                                 || (exc != null && matches(userclasses, exc) == true)
                                                               )                                                                  ) { continue lines; }
                        if (rule[0].equals("os")            && (    (inc != null && os.matches(inc) == false)
                                                                 || (exc != null && os.matches(exc) == true)
                                                               )                                                                  ) { continue lines; }
                        if (rule[0].equals("osversion")     && (    (inc != null && osversion.matches(inc) == false)
                                                                 || (exc != null && osversion.matches(exc) == true)
                                                               )                                                                  ) { continue lines; }
                        if (rule[0].equals("javaversion")   && (    (inc != null && javaversion.matches(inc) == false)
                                                                 || (exc != null && javaversion.matches(exc) == true)
                                                               )                                                                  ) { continue lines; }
                        if (rule[0].equals("configuration") && (    (inc != null && configuration_name.matches(inc) == false)
                                                                 || (exc != null && configuration_name.matches(exc) == true)
                                                               )                                                                  ) { continue lines; }
                        if (rule[0].equals("ui")) {
                            boolean got_one_inc = false;
                            boolean got_one_exc = false;
                            for (String ui_name : configuration_ui_name.split(":")) {
                                got_one_inc = got_one_inc || (inc == null || ui_name.matches(inc));
                                got_one_exc = (exc != null && ui_name.matches(exc));
                                if (got_one_exc) { break; }
                            }
                            if ( ! got_one_inc || got_one_exc)                                                                      { continue lines; }
                        }
                        if (rule[0].equals("db")) {
                            boolean got_one_inc = false;
                            boolean got_one_exc = false;
                            for (String db_name : configuration_db_name.split(":")) {
                                got_one_inc = got_one_inc || (inc == null || db_name.matches(inc));
                                got_one_exc = (exc != null && db_name.matches(exc));
                                if (got_one_exc) { break; }
                            }
                            if ( ! got_one_inc || got_one_exc)                                                                      { continue lines; }
                        }
                        if (rule[0].equals("variants")) {
                            boolean got_one_inc = false;
                            boolean got_one_exc = false;
                            if (explicit_configuration_variant.length() == 0) {
                                got_one_inc = (inc == null || explicit_configuration_variant.matches(inc));
                                got_one_exc = (exc != null && explicit_configuration_variant.matches(exc));
                            } else {
                                for (String variant : variants) {
                                    got_one_inc = got_one_inc || (inc == null || variant.matches(inc));
                                    got_one_exc = (exc != null && variant.matches(exc));
                                    if (got_one_exc) { break; }
                                }
                            }
                            if ( ! got_one_inc || got_one_exc)                                                                      { continue lines; }
                        }
                    }
                }
                BootstrapNotifier.sendTrace(context,  "match, appending '" + keyval[1] + "'");

                addVariants(context, variants, duplicates, keyval[1]);
            }
            BootstrapNotifier.sendTrace(context,  "result '" + join(context, variants) + "'");
        } catch (IOException ioe) {
            ExceptionConfigurationError.createAndThrow(context, ioe, SystemStateMessage.create(context, MessageText.create(context, "Configuration file '%(source)' unreadable", "source", sourcename), ProblemState.ERROR));
            throw (ExceptionConfigurationError) null; // compiler insists
        }
    }

    /**
       Try to open, read and parse a configuration file.

       @param folder     see filename
       @param filename   Parameters folder and filename ar concatenated
       @param variants   Partially calculated variant sequence, which is augmented
       @param duplicates A set used to check for duplicates in the variants list
     */
    static protected void tryConfigurationFile(CallContext context, String folder, String filename, List<String> variants, Set<String> duplicates, Queue<String> cvariants, Set<String> cduplicates) {
        if (folder == null) { return; }

        String path = folder + filename;
        try {
            BootstrapNotifier.sendTrace(context,  "checking configuration '" + path + "'");
            
            File file = new File(path);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                parseVariantFile(context, isr, variants, duplicates, path, cvariants, cduplicates);
                isr.close();
                fis.close();
            }
        } catch (java.io.IOException ioe) {
            ExceptionConfigurationError.createAndThrow(context, ioe, SystemStateMessage.create(context, MessageText.create(context, "Error while reading from '%(file)'", "file", path), ProblemState.ERROR));
            throw (ExceptionConfigurationError) null; // compiler insists
        }
    }

    /**
       Try to open, read and parse a configuration resource.

       @param java_class     Java class used to define and access the package in which the resource is located
       @param resource_name  Name of resource to load
       @param variants       Partially calculated variant sequence, which is augmented
       @param duplicates     A set used to check for duplicates in the variants list
     */
    static protected void tryConfigurationResource(CallContext context, Class java_class, String resource_name, List<String> variants, Set<String> duplicates, Queue<String> cvariants, Set<String> cduplicates) {
        String id = "java-resource:" + java_class.getPackage().getName().replace('.','/') + "/" + resource_name;

        try {
            BootstrapNotifier.sendTrace(context,  "checking configuration '" + id + "'");

            InputStream input = java_class.getResourceAsStream(resource_name);
            if (input != null) {
                InputStreamReader isr = new InputStreamReader(input, "UTF-8");
                parseVariantFile(context, isr, variants, duplicates, id, cvariants, cduplicates);
                isr.close();
                input.close();
            }
        } catch (java.io.IOException ioe) {
            ExceptionConfigurationError.createAndThrow(context, ioe, SystemStateMessage.create(context, MessageText.create(context, "Error while reading from '%(file)'", "file", id), ProblemState.ERROR));
            throw (ExceptionConfigurationError) null; // compiler insists
        }
    }

    static protected void tryConfiguration(CallContext context, List<String> variants, Set<String> duplicates, String variant, Queue<String> cvariants, Set<String> cduplicates) {
        String postfix = (variant == null || variant.isEmpty() ? "" : ("-" + variant));

        tryConfigurationResource(context, com.sphenon.basics.configuration.Configuration.class, ".configuration" + postfix, variants, duplicates, cvariants, cduplicates);

        for (ConfigFolder config_folder : config_folders) {
            tryConfigurationFile(context, config_folder.name, (config_folder.sphenon_specific ? "/.configuration" : "/.sphenon-configuration") + postfix, variants, duplicates, cvariants, cduplicates);
        }
    }

    /**
       Try to load a fixed configuration name.
     */
    static protected void tryLoadConfigurationName(CallContext context) {
        if (configuration_name != null) { return; }

        String id = "java-resource:com/sphenon/basics/configuration/.configuration-name";
        try {
            InputStream input = com.sphenon.basics.configuration.Configuration.class.getResourceAsStream(".configuration-name");
            if (input != null) {
                InputStreamReader isr = new InputStreamReader(input, "UTF-8");
                BufferedReader br = new BufferedReader(isr);
                String line;
                if ((line = br.readLine()) != null) {
                    line.replaceFirst("^[ \r\n]+", "").replaceFirst("[ \r\n]+$", "");
                    if (line.matches("[A-Za-z0-9_]+")) {
                        configuration_name = line;
                    } else {
                        ExceptionConfigurationError.createAndThrow(context, SystemStateMessage.create(context, MessageText.create(context, "Error while reading '%(file)', invalid configuration name syntax '%(line)'", "file", id, "line", line), ProblemState.ERROR));
                        throw (ExceptionConfigurationError) null; // compiler insists
                    }
                }
                br.close();
                isr.close();
                input.close();
            }
        } catch (java.io.IOException ioe) {
            ExceptionConfigurationError.createAndThrow(context, ioe, SystemStateMessage.create(context, MessageText.create(context, "Error while reading from '%(file)'", "file", id), ProblemState.ERROR));
            throw (ExceptionConfigurationError) null; // compiler insists
        }
    }
    
    /**
       Get the current variant sequence

       @return An array of variants
    */
    static public List<String> getVariants(CallContext context) {
        if ( ! configuration_variants_loaded) {
            configuration_variants_loaded = true;
            List<String> variants = new ArrayList<String>();
            Set<String> duplicates = new HashSet<String>();
            BootstrapNotifier.sendTrace(context,  "determining configuration variants...");
            tryLoadConfigurationName(context);

            String syscv = include_system_properties ? System.getProperty("com.sphenon.basics.configuration.ConfigurationVariant") : null;
            BootstrapNotifier.sendTrace(context,  "system property configuration variants '" +  (syscv == null ? "" : syscv)  + "'");
            if (syscv != null && syscv.length() != 0) {
                BootstrapNotifier.sendTrace(context,  "appending system property configuration variants to explicit configuration variants");
                if (explicit_configuration_variant != null && explicit_configuration_variant.length() != 0) {
                    explicit_configuration_variant += ":";
                }
                explicit_configuration_variant += syscv;
            }

            prepareBasicParameters(context);

            BootstrapNotifier.sendTrace(context,  "explicit configuration variants '" +  (explicit_configuration_variant == null ? "" : explicit_configuration_variant)  + "'");
            addVariants(context, variants, duplicates, explicit_configuration_variant);

            Queue<String> cvariants = new LinkedList<String>();
            Set<String> cduplicates = new HashSet<String>();
            cvariants.add("");
            cduplicates.add("");

            String cvar;
            while ((cvar = cvariants.poll()) != null) {
                tryConfiguration(context, variants, duplicates, cvar, cvariants, cduplicates);
            }

            configuration_variants = variants;
            BootstrapNotifier.sendCheckpoint(context, "Configuration variant: '" + join(context, variants) + "'");
        }

        return configuration_variants;
    }

    // -------------------------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------
    // Actual variant aware property loading -----------------------------------------------------------------

    /**
       Loads all properties associated with a class. The properties are
       identified by a name (argument base_resource), e.g. ".properties". This
       method then searches for all currently relevant variants of this
       resource and loads them.

       @param base_resource The base resource name, e.g. ".properties"
       @param from_class    The java class the resource is associated with,
                            this is typically the PackageInitialiser class
                            associated with a package
     */
    static protected void loadProperties (CallContext context, String base_resource, Class from_class) {
        loadProperties (context, base_resource, from_class, false);
    }

    static protected boolean initial_properties_loaded = false;

    /**
       Return all matching (existing and readable) configuration-files from all 
       configuration folders considering all active variants
       I.e. searching for log4jconfig.xml would require to ask for  
       
         name="log4jconfig"
         extension=".xml" 
       
       All matching variants for all configuration_folders would be returned, i.e.
       
       log4jconfig.xml, log4jconfig_test.xml, ...
       
       The last file returned should take precedence.
       Hint: If the returned files cannot be merged then the last one should be used.
         
       @review:al Also hier müßte man nochmal über das (fehlende)sphenon_specific drübersehen
                  Es sollten alle sonst auch benutzen folder kommen, mir unklar ist,
                  was das sphenon_specific eigentlich soll und tut. 

       @review:bw Wozu wird die Funktion benutzt? sphenon_specific bedeutet, daß es sich
                  um einen Ordner handelt, der schon sphenon-specific ist, und daher die
                  Files dieses nicht mehr zu sein brauchen
       
       @param context
       @param name
       @param extension
       @return
    */
    static public ArrayList<URL> getConfigurationFiles(CallContext context, String name, String extension) {
        ArrayList<URL> conf_files = new ArrayList<URL>();
        String file_name = name + extension;
        try {
            URL resource = Configuration.class.getResource(file_name);
            if (resource != null) {
                conf_files.add(resource);
            }
            for (ConfigFolder config_folder : config_folders) {
                File folder = new File(config_folder.name);
                if (folder.exists()) {
                    File conf_file = new File(folder,file_name);
                    if (conf_file.exists() && conf_file.canRead()) {
                        conf_files.add(conf_file.toURI().toURL());
                    }
                }
            }
            if (getVariants(context) != null) {
                for (String variant : getVariants(context)) {
                    file_name = name + "-" + variant + extension;
                    resource = Configuration.class.getResource(file_name);
                    if (resource != null) {
                        conf_files.add(resource);
                    }
                    for (ConfigFolder config_folder : config_folders) {
                        File folder = new File(config_folder.name);
                        if (folder.exists()) {
                            File conf_file = new File(folder,file_name);
                            if (conf_file.exists() && conf_file.canRead()) {
                                conf_files.add(conf_file.toURI().toURL());
                            }
                        }
                    }
                }
            }
        } catch (MalformedURLException e) {
            ExceptionEnvironmentError.createAndThrow(context, e, SystemStateMessage.create(context, MessageText.create(context, "File->URL Convert failed"), ProblemState.ERROR));
            throw (ExceptionEnvironmentFailure) null; // compiler insists
        }
        return conf_files;
    }
    
    /**
       Loads all properties associated with a class. The properties are
       identified by a name (argument base_resource), e.g. ".properties". This
       method then searches possibly in several places for all currently
       relevant variants of this resource and loads them.

       @param base_resource The base resource name, e.g. ".properties"
       @param from_class    The java class the resource is associated with,
                            this is typically the PackageInitialiser class
                            associated with a package
       @param read_config_foldes If true, the sphenon specific environmental
                            config folders (/etc/sphenon, ~/.sphenon,
                            ./.sphenon) are included in the load process
     */
    static protected synchronized void loadProperties (CallContext context, String base_resource, Class from_class, boolean read_config_folders) {
        String resource = base_resource;
        String loaded = (initial_properties_loaded ? null : "");
        loaded = loadPropertiesFromStream(context, from_class, null, resource, loaded);
        if (read_config_folders) {
            for (ConfigFolder config_folder : config_folders) {
                if (config_folder.sphenon_specific) {
                    loaded = loadPropertiesFromStream(context, null, config_folder.name, resource, loaded);
                }
            }
        }
        if (initial_properties_loaded == false) {
            initial_properties_loaded = true;
            Configuration.trace_mode = Configuration.get(context, "com.sphenon.basics.configuration", "DEBUG_TRACE", Configuration.trace_mode);
            if (Configuration.trace_mode) { Configuration.messenger.message(context, MessageText.create(context, "TraceMode ON")); }
            Configuration.trace_origin = Configuration.get(context, "com.sphenon.basics.configuration", "DEBUG_ORIGIN", Configuration.trace_origin);
            if (Configuration.trace_origin) { Configuration.messenger.message(context, MessageText.create(context, "TraceOrigin ON")); }
            trace_property_origins = Configuration.trace_origin;

            BootstrapNotifier.configure(context, Configuration.get(context, "com.sphenon.basics.notification.BootstrapNotifier", "CHECKPOINT", false), Configuration.get(context, "com.sphenon.basics.notification.BootstrapNotifier", "TRACE", false));

            BootstrapNotifier.sendCheckpoint(context,  "Loaded initial property resources: '" + loaded + "'");
        }
        if (getVariants(context) != null) {
            for (String variant : getVariants(context)) {
                resource = base_resource + "-" + variant;
                loadPropertiesFromStream(context, from_class, null, resource, null);
                if (read_config_folders) {
                    for (ConfigFolder config_folder : config_folders) {
                        if (config_folder.sphenon_specific) {
                            loaded = loadPropertiesFromStream(context, null, config_folder.name, resource, null);
                        }
                    }
                }
            }
        }
    }

    /**
       Loads properties from a class resource or from a file into the internal
       property table of this Configuration instance.
      
       @param from_class  A class where the resource file is retrieved from as
                          a resource, if non null, the from_folder argument is
                          ignored
       @param from_folder A normal file system folder where the resource is
                          retrieved from as a file
       @parem resource    The name of the resource, without a preceeding path
       @param loaded      A string for informational purposes, if null the
                          loaded resource is reported via the
                          BootstrapNotifier, if non null, the loaded resource
                          is appended to this argument (loaded) and the result
                          is returned
       @return See loaded argument for description, null if loaded was null
     */
    static protected String loadPropertiesFromStream(CallContext context, Class from_class, String from_folder, String resource, String loaded) {
        try {
            if (from_class != null) {
                InputStream in = from_class.getResourceAsStream(resource);
                if (in != null) {
                    if (loaded == null) {
                        BootstrapNotifier.sendCheckpoint(context,  "Loading property resource '" + from_class.getName() + "'/'" + resource + "'");
                    } else {
                        loaded += (loaded.length() == 0 ? "" : ", ") + from_class.getName() + "/" + resource;
                    }
                    properties.load(in);
                    in.close();
                    // ~~~ debug ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    if (trace_property_origins) {
                        InputStream in4keys = from_class.getResourceAsStream(resource);
                        Properties p4keys = new Properties();
                        p4keys.load(in4keys);
                        in4keys.close();
                        for (String key : p4keys.stringPropertyNames()) {
                            properties.setProperty(key + "@Origin", from_class.getName() + "/" + resource);
                        }
                    }
                    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                }
            } else if (from_folder != null) {
                File file = new File(from_folder + "/" + resource);
                if (file.exists()) {
                    InputStream in = new FileInputStream(file);
                    if (loaded == null) {
                        BootstrapNotifier.sendCheckpoint(context,  "Loading property resource '" + from_folder + "'/'" + resource + "'");
                    } else {
                        loaded += (loaded.length() == 0 ? "" : ", ") + from_folder + "/" + resource;
                    }
                    properties.load(in);
                    in.close();
                    // ~~~ debug ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    if (trace_property_origins) {
                        InputStream in4keys = new FileInputStream(file);
                        Properties p4keys = new Properties();
                        p4keys.load(in4keys);
                        in4keys.close();
                        for (String key : p4keys.stringPropertyNames()) {
                            properties.setProperty(key + "@Origin", from_folder + "/" + resource);
                        }
                    }
                    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                }
            }
        } catch (IOException ioe)  {
            Message message = null;
            if (from_class != null) {
                message = SystemStateMessage.create(context, MessageText.create(context, "Loading of properties failed: " + resource + " from class " + from_class.getName()), ProblemState.ERROR);
            } else if (from_folder != null) {
                message = SystemStateMessage.create(context, MessageText.create(context, "Loading of properties failed: " + resource + " from folder " + from_folder), ProblemState.ERROR);
            }
            ExceptionEnvironmentError.createAndThrow(context, ioe, message);
            throw (ExceptionEnvironmentFailure) null; // compiler insists
        }
        return loaded;
    }

    // -------------------------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------
    // Default properties ------------------------------------------------------------------------------------

    /**
       Sets a specific property identified by it's fully qualified property
       name within the default properties to the given value. Typically used
       in very early initialisation phases to set very basic settings which
       are determined during runtime (e.g. the web app installation path).

       @param name Fully qualified property name
       @param value Values to set
    */
    static public void setDefaultProperty(CallContext call_context, String name, String value) {
        default_properties.put(name, value);
    }

    /**
       Given a class, this method loads the property resources associated with
       that class via the getResource method into the default property
       table. The properties that are loaded are named ".properties",
       ".generated.properties", the deprecated variant ".generated-properties"
       and all current variants of them.

       See package introduction for the distinction between "default
       properties" and "user properties".

       @param class_where_resources_reside The class whose getResourceAsStream method
                                           is used to load the properties,
                                           typically this is the
                                           PackageInitialiser of a package
     */
    static public synchronized void loadDefaultProperties(CallContext context, Class class_where_resources_reside) {
        initialise(context);

        loadDefaultPropertyResource(context, class_where_resources_reside, ".properties");
        loadDefaultPropertyResource(context, class_where_resources_reside, ".generated.properties");
        loadDefaultPropertyResource(context, class_where_resources_reside, ".generated-properties");

        for (String ip : getIncludeProperties(context)) {
            loadDefaultPropertyResource(context, class_where_resources_reside, ip);
        }

        if (getVariants(context) != null) {
            for (String variant : getVariants(context)) {
                loadDefaultPropertyResource(context, class_where_resources_reside, ".properties-" + variant);
                loadDefaultPropertyResource(context, class_where_resources_reside, ".generated.properties-" + variant);
                loadDefaultPropertyResource(context, class_where_resources_reside, ".generated-properties-" + variant);

                for (String ip : getIncludeProperties(context)) {
                    loadDefaultPropertyResource(context, class_where_resources_reside, ip + "-" + variant);
                }
            }
        }
    }

    /**
       Given a class, this method loads the named property resources
       associated with that class via the getResource method into the default
       property table. All current variants are also loaded..

       See package introduction for the distinction between "default
       properties" and "user properties".

       @param class_where_resources_reside The class whose getResourceAsStream method
                                           is used to load the properties,
                                           typically this is the
                                           PackageInitialiser of a package
       @param resource_name                The base name of the property
                                           resources to be loaded.
     */
    static public synchronized void loadDefaultProperties(CallContext context, Class class_where_resources_reside, String resource_name) {
        initialise(context);

        loadDefaultPropertyResource(context, class_where_resources_reside, resource_name);
        if (getVariants(context) != null) {
            for (String variant : getVariants(context)) {
                loadDefaultPropertyResource(context, class_where_resources_reside, resource_name + "-" + variant);
            }
        }
    }

    /**
       Helper: loads a specific resource from a class into the default properties

       @param class_where_resources_reside The class whose getResourceAsStream method
                                           is used to load the properties,
                                           typically this is the
                                           PackageInitialiser of a package
       @param resource The name of the resource to load
     */
    static protected void loadDefaultPropertyResource(CallContext context, Class class_where_resources_reside, String resource) {
        InputStream in = class_where_resources_reside.getResourceAsStream(resource);
        if (in != null) {
            BootstrapNotifier.sendCheckpoint(context,  "Loading default property resource '" + class_where_resources_reside.getName() + "'/'" + resource + "'");
            try {
                default_properties.load(in);
                in.close();
                // ~~~ debug ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                if (trace_property_origins) {
                    InputStream in4keys = class_where_resources_reside.getResourceAsStream(resource);
                    Properties p4keys = new Properties();
                    p4keys.load(in4keys);
                    in4keys.close();
                    for (String key : p4keys.stringPropertyNames()) {
                        properties.setProperty(key + "@Origin", class_where_resources_reside.getName() + "/" + resource);
                    }
                }
                // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            } catch (IOException ioe) {
                Message message = SystemStateMessage.create(context, MessageText.create(context, "Loading of properties failed"), ProblemState.ERROR);
                ExceptionEnvironmentError.createAndThrow(context, ioe, message);
                throw (ExceptionEnvironmentFailure) null; // compiler insists
            }
        }
    }

    static protected String join(CallContext context, List<String> strings) {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        if (strings != null) {
            for (String string : strings) {
                if (first == false) { sb.append(":"); }
                first = false;
                sb.append(string);
            }
        }
        return sb.toString();
    }
}
