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

public class Configuration {

    static protected ConfigurationMessenger messenger = new ConfigurationMessenger();

    static protected boolean trace_mode = false;
    static protected boolean trace_origin = false;

    static {
        // don't do this: initialise(RootContext.getRootContext());
    }

    static public Vector<String> checkCommandLineArgs(String[] args) {
        return RootConfiguration.checkCommandLineArgs(args);
    }

    static protected boolean initialised = false;

    static public synchronized boolean isInitialised(CallContext context) {
        return initialised;
    }

    static public synchronized void initialise (CallContext context) {
        
        if (initialised == false) {
            initialised = true;
            RootConfiguration.initialise(context);

            // we do this here, since
            // a) there's no config package in message already available
            // b) before initpackages because the main purpose is to use it during package init
            com.sphenon.basics.message.TraceMessage.prepend_depth_number     = get(context, "com.sphenon.basics.message.TraceMessage", "PrependDepthNumber", false);
            com.sphenon.basics.message.TraceMessage.prepend_depth_whitespace = get(context, "com.sphenon.basics.message.TraceMessage", "PrependDepthWhitespace", false);
            com.sphenon.basics.message.TraceMessage.prepend_caller_info      = get(context, "com.sphenon.basics.message.TraceMessage", "PrependCallerInfo", false);
            com.sphenon.basics.monitoring.ProblemException.stack_trace_length = get(context, "com.sphenon.basics.message.classes.MessageTextClass", "StackTraceLength", 10);

            com.sphenon.basics.cache.ClassCache.debug = get(context, "com.sphenon.basics.cache.ClassCache", "Debug", false);
            com.sphenon.basics.cache.ClassCache.not_found_notice_every = get(context, "com.sphenon.basics.cache.ClassCache", "NotFoundNoticeEvery", 1000);
            com.sphenon.basics.cache.ClassCache.not_found_notice_maximum = get(context, "com.sphenon.basics.cache.ClassCache", "NotFoundNoticeMaximum", 50);
            com.sphenon.basics.cache.ClassCache.not_found_dump_maximum = get(context, "com.sphenon.basics.cache.ClassCache", "NotFoundDumpMaximum", 10);
        }
    }

    static public void setMessenger (CallContext context, ConfigurationMessenger m) {
        messenger = m;
    }

    // -------------------------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------
    // Configuration instance --------------------------------------------------------------------------------

    protected String client_id;

    protected Configuration (CallContext context, String client_id) {
        this.client_id = client_id;
    }

    /**
       Creates a configuration instance with an id, which is used as a prefix
       for all property accesses via this instance.

       @param client_id A dot separated property key prefix
     */
    static public Configuration create (CallContext context, String client_id) {
        return new Configuration(context, client_id);
    }

    /**
       Creates a configuration instance with an id, which is used as a prefix
       for all property accesses via this instance. The class name of the
       argument ist used as the id.

       @param a_class the name of this class is used as the id
     */
    static public Configuration create (CallContext context, Class a_class) {
        return new Configuration(context, a_class.getName());
    }

    /**
       Creates a configuration instance without id.

       @param client_id A dot separated property key prefix
     */
    static public Configuration create (CallContext context) {
        return new Configuration(context, null);
    }

    public String getClientId (CallContext context) {
    	return this.client_id;
    }

    // -------------------------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------
    // Typed single property access (basic getter/setter) ----------------------------------------------------

    /**
       Get a property of type String, identified by the prefix of this
       Configuration instance and a key (see getProperty, main accessor), if
       no entry is found, return the default value. Do not search in
       parent entries.

       @param key    Last part of the entry, must in any case be present
       @param default_value If no property entry is found, return this value
       @return The value of the property entry found, or null if none was found
     */
    public String get (CallContext context, String key, String default_value) {
        return get(context, this.client_id, key, default_value, false, false);
    }

    /**
       Get a required property of type String, like {@link get}

       @param key    Last part of the entry, must in any case be present
       @param default_value In this method only used to determine the return type
       @return The value of the property entry found, or null if none was found
       @throws ConfigurationEntryNotFound if no configuration entry is found
     */
    public String mustGet (CallContext context, String key, String default_value) throws ConfigurationEntryNotFound {
        return get(context, this.client_id, key, default_value, false, true);
    }
    
    /**
       Get a property of type String, identified by the prefix of this
       Configuration instance and a key (see getProperty, main accessor), if
       no entry is found, return the default value. Search in parent entries,
       too.

       @param key    Last part of the entry, must in any case be present
       @param default_value If no property entry is found, return this value
       @return The value of the property entry found, or null if none was found
     */
    public String getRecursive (CallContext context, String key, String default_value) {
        return get(context, this.client_id, key, default_value, true, false);
    }
    
    /**
       Get a property of type String, identified by a prefix and a key (see
       getProperty, main accessor), if no entry is found, return the default
       value. Do not search in parent entries.

       @param prefix First part of the entry, optionally shortened
       @param key    Last part of the entry, must in any case be present
       @param default_value If no property entry is found, return this value
       @return The value of the property entry found, or null if none was found
     */
    static public String get (CallContext context, String prefix, String key, String default_value) {
        return get(context, prefix, key, default_value, false, false);
    }

    /**
       Get a property of type String, identified by a prefix and a key (see
       getProperty, main accessor), if no entry is found, return the default
       value. Search in parent entries, too.

       @param prefix First part of the entry, optionally shortened
       @param key    Last part of the entry, must in any case be present
       @param default_value If no property entry is found, return this value
       @return The value of the property entry found, or null if none was found
     */
    static public String getRecursive (CallContext context, String prefix, String key, String default_value) {
        return get(context, prefix, key, default_value, true, false);
    }

    /**
       Get a property of type String, identified by a prefix and a key (see
       getProperty, main accessor), if no entry is found, return the default
       value. Optionally search in parent entries, too.

       @param prefix First part of the entry, optionally shortened
       @param key    Last part of the entry, must in any case be present
       @param default_value If no property entry is found, return this value
       @param search_in_parents_also See description at method getProperty
       @return The value of the property entry found, or null if none was found
     */
    static public String get (CallContext context, String prefix, String key, String default_value, boolean search_in_parents_also, boolean throw_exception) {
        String property = getProperty(context, prefix, key, search_in_parents_also);
        if (property == null) {
            if (throw_exception) {
                throw new ConfigurationEntryNotFound();
            } else {
                return default_value;
            }
        }

        return property;
    }

    /**
       Abbreviated variant, does not throw exception.
    */
    static public String get (CallContext context, String prefix, String key, String default_value, boolean search_in_parents_also) {
        return get(context, prefix, key, default_value, search_in_parents_also, false);
    }

    /**
       Set a property of type String, identified by the prefix of this
       Configuration instance and a key (see setProperty, main accessor).

       @param key    Last part of the entry, must in any case be present
       @param value  The value to be set
     */
    public void set(CallContext context, String key, String value) {
        setProperty(context, key, value);
    }

    /**
       Get a property of type boolean, identified by the prefix of this
       Configuration instance and a key (see getProperty, main accessor), if
       no entry is found, return the default value. Do not search in
       parent entries.

       @param key    Last part of the entry, must in any case be present
       @param default_value If no property entry is found, return this value
       @return The value of the property entry found, or null if none was found
     */
    public boolean get (CallContext context, String key, boolean default_value) {
        return get(context, this.client_id, key, default_value, false, false);
    }

    /**
       Get a required property of type boolean, like {@link get}

       @param key    Last part of the entry, must in any case be present
       @param default_value In this method only used to determine the return type
       @return The value of the property entry found, or null if none was found
       @throws ConfigurationEntryNotFound if no configuration entry is found
     */
    public boolean mustGet (CallContext context, String key, boolean default_value) throws ConfigurationEntryNotFound {
        return get(context, this.client_id, key, default_value, false, true);
    }

    /**
       Get a property of type boolean, identified by the prefix of this
       Configuration instance and a key (see getProperty, main accessor), if
       no entry is found, return the default value. Search in parent entries,
       too.

       @param key    Last part of the entry, must in any case be present
       @param default_value If no property entry is found, return this value
       @return The value of the property entry found, or null if none was found
     */
    public boolean getRecursive (CallContext context, String key, boolean default_value) {
        return get(context, this.client_id, key, default_value, true, false);
    }

    /**
       Get a property of type boolean, identified by a prefix and a key (see
       getProperty, main accessor), if no entry is found, return the default
       value. Do not search in parent entries.

       @param prefix First part of the entry, optionally shortened
       @param key    Last part of the entry, must in any case be present
       @param default_value If no property entry is found, return this value
       @return The value of the property entry found, or null if none was found
     */
    static public boolean get (CallContext context, String prefix, String key, boolean default_value) {
        return get(context, prefix, key, default_value, false, false);
    }

    /**
       Get a property of type boolean, identified by a prefix and a key (see
       getProperty, main accessor), if no entry is found, return the default
       value. Search in parent entries, too.

       @param prefix First part of the entry, optionally shortened
       @param key    Last part of the entry, must in any case be present
       @param default_value If no property entry is found, return this value
       @return The value of the property entry found, or null if none was found
    */
    static public boolean getRecursive (CallContext context, String prefix, String key, boolean default_value) {
        return get(context, prefix, key, default_value, true, false);
    }

    /**
       Get a property of type boolean, identified by a prefix and a key (see
       getProperty, main accessor), if no entry is found, return the default
       value. Optionally search in parent entries, too.

       @param prefix First part of the entry, optionally shortened
       @param key    Last part of the entry, must in any case be present
       @param default_value If no property entry is found, return this value
       @param search_in_parents_also See description at method getProperty
       @return The value of the property entry found, or null if none was found
     */
    static public boolean get (CallContext context, String prefix, String key, boolean default_value, boolean search_in_parents_also, boolean throw_exception) {
        String property = getProperty(context, prefix, key, search_in_parents_also);
        if (property == null) {
            if (throw_exception) {
                throw new ConfigurationEntryNotFound();
            } else {
                return default_value;
            }
        }

        if (property.equals("true") || property.equals("TRUE")) {
            return true;
        } else if (property.equals("false") || property.equals("FALSE")) {
            return false;
        } else {
            messenger.message(context, MessageTextClass.createMessageTextClass(context, ConfigurationStringPool.get(context, "0.0.4" /* Property '%(key)' contains invalid entry '%(entry)' */), "key", prefix + "." + key, "entry", property));
            return default_value;
        }
    }

    /**
       Abbreviated variant, does not throw exception.
    */
    static public boolean get (CallContext context, String prefix, String key, boolean default_value, boolean search_in_parents_also) {
        return get(context, prefix, key, default_value, search_in_parents_also, false);
    }

    /**
       Set a property of type boolean, identified by the prefix of this
       Configuration instance and a key (see setProperty, main accessor).

       @param key    Last part of the entry, must in any case be present
       @param value  The value to be set
     */
    public void set(CallContext context, String key, boolean value) {
        setProperty(context, key, value ? "true" : "false");
    }

    /**
       Utility to convert strings to integer.
       @param key the string to convert
     */
    static protected int convertToInt(CallContext context, String prefix, String key, String value, int default_value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            messenger.message(context, MessageTextClass.createMessageTextClass(context, ConfigurationStringPool.get(context, "0.0.4" /* Property '%(key)' contains invalid entry '%(entry)' */), "key", prefix + "." + key, "entry", value));
            return default_value;
        }
    }

    /**
       Get a property of type int, identified by the prefix of this
       Configuration instance and a key (see getProperty, main accessor), if
       no entry is found, return the default value. Do not search in
       parent entries.

       @param key    Last part of the entry, must in any case be present
       @param default_value If no property entry is found, return this value
       @return The value of the property entry found, or null if none was found
     */
    public int get (CallContext context, String key, int default_value) {
        String property = this.getProperty(context, key);
        if (property == null) { return default_value; }

        return convertToInt(context, this.client_id, key, property, default_value);
    }

    /**
       Get a property of type int, identified by a prefix and a key (see
       getProperty, main accessor), if no entry is found, return the default
       value. Do not search in parent entries.

       @param prefix First part of the entry, optionally shortened
       @param key    Last part of the entry, must in any case be present
       @param default_value If no property entry is found, return this value
       @return The value of the property entry found, or null if none was found
     */
    static public int get (CallContext context, String prefix, String key, int default_value) {
        String property = get(context, prefix, key, null, false, false);
        if (property == null) { return default_value; }

        return convertToInt(context, prefix, key, property, default_value);
    }

    /**
       Get a property of type long, identified by the prefix of this
       Configuration instance and a key (see getProperty, main accessor), if
       no entry is found, return the default value. Do not search in
       parent entries.

       @param key    Last part of the entry, must in any case be present
       @param default_value If no property entry is found, return this value
       @return The value of the property entry found, or null if none was found
    */
    public long get (CallContext context, String key, long default_value) {
        String property = this.getProperty(context, key);
        if (property == null) { return default_value; }
        
        try {
            return Long.parseLong(property);
        } catch (NumberFormatException nfe) {
            messenger.message(context, MessageTextClass.createMessageTextClass(context, ConfigurationStringPool.get(context, "0.0.4" /* Property '%(key)' contains invalid entry '%(entry)' */), "key", this.client_id + "." + key, "entry", property));
            return default_value;
        }
    }

    /**
       Set a property of type int, identified by the prefix of this
       Configuration instance and a key (see setProperty, main accessor).

       @param key    Last part of the entry, must in any case be present
       @param value  The value to be set
     */
    public void set(CallContext context, String key, int value) {
        setProperty(context, key, new Integer(value).toString());
    }

    /**
       Get a property of type ArrayList, identified by the prefix of this
       Configuration instance and a key (see getProperty, main accessor), if
       no entry is found, return the default value. Do not search in
       parent entries.

       @param key    Last part of the entry, must in any case be present
       @param default_value If no property entry is found, return this value
       @return The value of the property entry found, or null if none was found
     */
    public ArrayList get (CallContext context, String key, ArrayList default_value) {
        String property = this.getProperty(context, key);
        if (property == null) { return default_value; }

        StringTokenizer t = new StringTokenizer(property, ", ");
        int n             = t.countTokens();
        ArrayList l       = new ArrayList();
        for (int i = 0; i < n; i++) {
            l.add(t.nextToken().trim());
        }
        return l;
    }

    // -------------------------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------
    // General single property access (basic getter/setter) --------------------------------------------------

    /**
       Sets a property specified by it's fully qualified name to the given
       value.

       @param key The name of the property
       @param value The value to set
    */
    protected void setProperty(CallContext context, String key, String value) {
        String full_key = (key == null ? this.client_id : (this.client_id == null ? key : (new StringBuffer(this.client_id).append('.').append(key)).toString()));
        ConfigurationContext cc = ConfigurationContext.get((Context) context);
        if (cc != null) {
            cc.setPropertyEntry(context, full_key, value);
        } else {
            RootConfiguration.setPropertyEntry(context, full_key, value);
        }
    }

    /**
       Retrieves a property by name. The prefix of this Configuration instance
       is prepended before accessing the property tables.

       @param key The name of the property
       @return The value of the entry or null if none is found. Note: that
               value may be the result of an optional JavaScript evaluation
     */

    protected String getProperty (CallContext context, String key) {
        return getProperty (context, this.client_id, key);
    }

    // -------------------------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------
    // Property access helpers -------------------------------------------------------------------------------

    /**
       Abbreviated version of property accessor. Does not search in parents.

       @param prefix First part of the entry, must in any case be present
       @param key    Last part of the entry, must in any case be present
       @return The value of the property entry found, or null if none was found
     */
    static protected String getProperty (CallContext context, String prefix, String key) {
        return getProperty (context, prefix, key, false);
    }

    /**
       Main property accessor. Builds a property name by concatenating the
       prefix and key (with a dot if both are given), searches for that entry,
       optionally evaluates it if it is a JavaScript entry, and optionally
       searches in "parent" entries by successively reducing the prefix at
       it's tail, splitting up the last dot-separated part. E.g., if prefix is
       a.b.c and key is D, then the entries a.b.c.D, a.b.D, a.D and D are
       tested in this order.

       @param prefix First part of the entry, optionally shortened
       @param key    Last part of the entry, must in any case be present
       @param search_in_parents_also See description above
       @return The value of the property entry found, or null if none was found
     */
    static protected String getProperty (CallContext context, String prefix, String key, boolean search_in_parents_also) {
        String full_key = (key == null ? prefix : (prefix == null ? key : (new StringBuffer(prefix).append('.').append(key)).toString()));
        ConfigurationContext cc = ConfigurationContext.get((Context) context);
        String value = (cc != null ? cc.getPropertyEntry(context, full_key) : RootConfiguration.getPropertyEntry(context, full_key));

        if (trace_mode) {
            messenger.message(context, MessageText.create(context, "Property '%(full_key)'  => '%(value)'", "full_key", full_key, "value", value));
            if (trace_origin) {
                messenger.message(context, MessageText.create(context, "         [%(origin)]", "origin", cc != null ? "context" : RootConfiguration.getPropertyEntry(context, full_key + "@Origin")));
            }
        }

        if (value == null && search_in_parents_also && prefix != null && prefix.length() != 0) {
            int last_dot = prefix.lastIndexOf('.');
            if (last_dot == -1) {
                return getProperty (context, null, key, search_in_parents_also);
            } else {
                return getProperty (context, prefix.substring(0,last_dot), key, search_in_parents_also);
            }
        }
        return value;
    }

    // -------------------------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------
    // JavaScript property evaluation ------------------------------------------------------------------------

    /**
       Evaluates a JavaScript expression and returns the result. The scope of
       the expression comprises the current context.

       @param expression The JavaScript expression to be evaluated
       @return The result of the evaluation
    */
    static public String evaluateJavaScript(CallContext context, String expression) {
        return evaluateJavaScript(context, expression, null);
    }

    static public String evaluateJavaScript(CallContext context, String expression, java.util.Hashtable parameters) {

        if (javascript_evaluator == null) {
            Message message = SystemStateMessage.create(context, MessageText.create(context, "No javascript evaluator registered, but function called "), ProblemState.ERROR);
            ExceptionImpossibleState.createAndThrow(context, message);
        }

        return parameters != null ? javascript_evaluator.evaluate(context, expression, parameters) : javascript_evaluator.evaluate(context, expression);
    }

    static protected ConfigurationJavaScriptEvaluator javascript_evaluator;

    /**
       Attaches a JavaScript evaluation engines to the configuration
       subsystem.

       @param jse A JavaScript evaluator
    */
    static public void setConfigurationJavaScriptEvaluator (CallContext context, ConfigurationJavaScriptEvaluator jse) {
        javascript_evaluator = jse;
        java.util.Hashtable parameters = new java.util.Hashtable();
        parameters.put("config", Configuration.create(context, (String) null));
        jse.setParameters(context, parameters);
    }

    /**
       Optinally evaluates a property value as a JavaScript expression and
       optionally caches the result in the given property instance.

       @param properties Where the evaluated entry is optionally stored in
       @param key        The fully qualified property entry key
       @param property   The already retrieved value of that property
     */
    static protected String evaluateAndCacheProperty (CallContext context, Properties properties, String key, String property) {
        if (property.charAt(1) == '\u0000') { return property.substring(1); }
        boolean js       = false;
        boolean variants = false;
        boolean cache    = false;
        String expression = null;
        int pos = -1;
        if (property.charAt(0) == '\u0000' && (pos = property.indexOf(':')) != -1) {
            String ppdef = property.substring(1, pos);
            for (String pp : ppdef.split("/")) {
                switch (pp) {
                    case "JavaScript" : js       = true; break;
                    case "Variants"   : variants = true; break;
                    case "Cache"      : cache    = true; break;
                    default           : ExceptionPreConditionViolation.createAndThrow(context, SystemStateMessage.create(context, MessageText.create(context, "Invalid property preprocessing entry '%(property)': '%(entry)'", "property", key, "entry", ppdef), ProblemState.ERROR));
                                        break;
                }
            }
            String value = property.substring(pos + 1);
            if (js) {
                value = evaluateJavaScript(context, (expression = value));
                if (trace_mode) { messenger.message(context, MessageText.create(context, "Property '%(key)'  =>  '%(expression)'  ==JS==>  '%(value)'", "key", key, "expression", expression, "value", value)); }
            }
            if (variants) {
                StringBuilder sb = new StringBuilder();
                sb.append(value == null ? "" : value);
                RootConfiguration.appendPropertyEntryVariants(context, key, sb);
                value = sb.toString();
                if (trace_mode) { messenger.message(context, MessageText.create(context, "Property '%(key)'  =>  plus variants  =>  '%(value)'", "key", key, "value", value)); }
            }
            if (cache) {
                if (value == null) {
                    ExceptionPreConditionViolation.createAndThrow(context, SystemStateMessage.create(context, MessageText.create(context, "Cannot cache property '%(property)', result is null", "property", key), ProblemState.ERROR));
                }
                properties.setProperty(key, value);
            }
            return value;
        }
        return property;
    }

    //

    /**
       See {@link RootConfiguration}, method  loadDefaultProperties.
     */
    static public void loadDefaultProperties(CallContext context, Class class_where_resources_reside) {
        RootConfiguration.loadDefaultProperties(context, class_where_resources_reside);
    }

    /**
       See {@link RootConfiguration}, method  loadDefaultProperties.
     */
    static public void loadDefaultProperties(CallContext context, Class class_where_resources_reside, String resource_name) {
        RootConfiguration.loadDefaultProperties(context, class_where_resources_reside, resource_name);
    }
    
    public String toString() {
        return this.client_id + ":" + super.toString();
    }
}

