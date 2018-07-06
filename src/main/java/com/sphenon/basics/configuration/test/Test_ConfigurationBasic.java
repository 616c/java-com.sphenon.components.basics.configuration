package com.sphenon.basics.configuration.test;

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
import com.sphenon.basics.message.*;
import com.sphenon.basics.exception.*;
import com.sphenon.basics.configuration.*;
import com.sphenon.basics.monitoring.*;
import com.sphenon.basics.testing.TestRun;
import com.sphenon.basics.testing.TestResult;
import com.sphenon.basics.testing.TestResult_ExceptionRaised;

public class Test_ConfigurationBasic extends com.sphenon.basics.testing.classes.TestBase {

    protected boolean trace = false;

    public Test_ConfigurationBasic (CallContext context) {
        this.trace = false;
    }

    public Test_ConfigurationBasic (CallContext context, boolean trace) {
        this.trace = trace;
    }

    public String getId (CallContext context) {
        if (this.id == null) {
            this.id = "ConfigurationBasic";
        }
        return this.id;
    }

    static public void main(String[] args) {
        // info();

        Context context = com.sphenon.basics.context.classes.RootContext.getRootContext ();
        TestRun test_run = new com.sphenon.basics.testing.classes.ClassTestRun(context);
        TestResult result = (new Test_ConfigurationBasic(context, true)).perform(context, test_run);
        System.err.println(result);
        if (result  == TestResult.OK) {
            System.err.println("TEST OK.");
        } else {
            System.err.println("*** TEST FAILED ***");
        }
    }

    public TestResult perform(CallContext context, TestRun test_run) {
        if (trace) { System.out.println( "main..." ); }

        if (trace) { System.out.println( "main, creating root context..." ); }

        Configuration cfg = Configuration.create(context, "com.sphenon.basics.configuration");
        
        String result = null;
        
        result = cfg.get(context, "Hans", "DEFAULT-WERT");
        if (trace) { System.out.println( "main, retrieving property 'Hans': " + result); }

        if ( ! "4711".equals(result)) { return new TestResult_ExceptionRaised(context, new Throwable("expected '4711', got '" + result + "'")); }

        result = cfg.get(context, "Hans.Werner", "DEFAULT-WERT");
        if (trace) { System.out.println( "main, retrieving property 'Hans.Werner': " + result); }

        if ( ! "0815".equals(result)) { return new TestResult_ExceptionRaised(context, new Throwable("expected '0815', got '" + result + "'")); }

        result = cfg.get(context, "Hans.Meier.Willy", "DEFAULT-WERT");
        if (trace) { System.out.println( "main, retrieving property 'Hans.Meier.Willy': " + result); }

        if ( ! "DEFAULT-WERT".equals(result)) { return new TestResult_ExceptionRaised(context, new Throwable("expected 'DEFAULT-WERT', got '" + result + "'")); }

        boolean bool_result;

        bool_result = cfg.get(context, "com.sphenon.basics.configuration.Wert", false);
        if (trace) { System.out.println( "main, retrieving property 'com.sphenon.basics.configuration.Wert': " + bool_result); }
        if ( bool_result == true) { return new TestResult_ExceptionRaised(context, new Throwable("expected 'false', got '" + bool_result + "'")); }

        bool_result = cfg.getRecursive(context, "Wert", false);
        if (trace) { System.out.println( "main, retrieving property 'Wert' (+ parents): " + bool_result); }
        if ( bool_result != true) { return new TestResult_ExceptionRaised(context, new Throwable("expected 'true', got '" + bool_result + "'")); }

        bool_result = cfg.getRecursive(context, "Wert2", false);
        if (trace) { System.out.println( "main, retrieving property 'Wert2' (+ parents): " + bool_result); }
        if ( bool_result != true) { return new TestResult_ExceptionRaised(context, new Throwable("expected 'true', got '" + bool_result + "'")); }

        bool_result = cfg.get(context, "com.sphenon.basics.configuration", "Wert", false, true);
        if (trace) { System.out.println( "main, retrieving property 'com.sphenon.basics.configuration.Wert' (+ parents): " + bool_result); }
        if ( bool_result != true) { return new TestResult_ExceptionRaised(context, new Throwable("expected 'true', got '" + bool_result + "'")); }

        bool_result = cfg.get(context, "com.sphenon.basics", "Wert", false, true);
        if (trace) { System.out.println( "main, retrieving property 'com.sphenon.basics.Wert' (+ parents): " + bool_result); }
        if ( bool_result != true) { return new TestResult_ExceptionRaised(context, new Throwable("expected 'true', got '" + bool_result + "'")); }

        bool_result = cfg.get(context, "com.sphenon", "Wert", false, true);
        if (trace) { System.out.println( "main, retrieving property 'com.sphenon.Wert' (+ parents): " + bool_result); }
        if ( bool_result != true) { return new TestResult_ExceptionRaised(context, new Throwable("expected 'true', got '" + bool_result + "'")); }

        bool_result = cfg.get(context, "com", "Wert", false, true);
        if (trace) { System.out.println( "main, retrieving property 'com.Wert' (+ parents): " + bool_result); }
        if ( bool_result != true) { return new TestResult_ExceptionRaised(context, new Throwable("expected 'true', got '" + bool_result + "'")); }

        bool_result = cfg.get(context, null, "Wert", false, true);
        if (trace) { System.out.println( "main, retrieving property 'Wert' (+ parents): " + bool_result); }
        if ( bool_result != true) { return new TestResult_ExceptionRaised(context, new Throwable("expected 'true', got '" + bool_result + "'")); }

        bool_result = cfg.get(context, "com.sphenon.basics.configuration", "Wert2", false, true);
        if (trace) { System.out.println( "main, retrieving property 'com.sphenon.basics.configuration.Wert2' (+ parents): " + bool_result); }
        if ( bool_result != true) { return new TestResult_ExceptionRaised(context, new Throwable("expected 'true', got '" + bool_result + "'")); }

        bool_result = cfg.get(context, "com.sphenon.basics", "Wert2", false, true);
        if (trace) { System.out.println( "main, retrieving property 'com.sphenon.basics.Wert2' (+ parents): " + bool_result); }
        if ( bool_result != true) { return new TestResult_ExceptionRaised(context, new Throwable("expected 'true', got '" + bool_result + "'")); }

        bool_result = cfg.get(context, "com.sphenon", "Wert2", false, true);
        if (trace) { System.out.println( "main, retrieving property 'com.sphenon.Wert2' (+ parents): " + bool_result); }
        if ( bool_result != true) { return new TestResult_ExceptionRaised(context, new Throwable("expected 'true', got '" + bool_result + "'")); }

        bool_result = cfg.get(context, "com", "Wert2", false, true);
        if (trace) { System.out.println( "main, retrieving property 'com.Wert2' (+ parents): " + bool_result); }
        if ( bool_result == true) { return new TestResult_ExceptionRaised(context, new Throwable("expected 'false', got '" + bool_result + "'")); }

        bool_result = cfg.get(context, null, "Wert2", false, true);
        if (trace) { System.out.println( "main, retrieving property 'Wert2' (+ parents): " + bool_result); }
        if ( bool_result == true) { return new TestResult_ExceptionRaised(context, new Throwable("expected 'false', got '" + bool_result + "'")); }

        result = cfg.get(context, "Hasi", "nix");
        if (trace) { System.out.println( "main, retrieving property 'Hasi': " + result); }

        if ( ! "normal".equals(result)) { return new TestResult_ExceptionRaised(context, new Throwable("expected 'normal', got '" + result + "'")); }

        {
            Context c = Context.create(context);
            ConfigurationContext cc = ConfigurationContext.create(c);
            cc.setPropertyEntry(c, "com.sphenon.basics.configuration.Hasi", "ueberladen");

            result = cfg.get(c, "Hasi", "nix");
            if (trace) { System.out.println( "main, retrieving property 'Hasi': " + result); }

            if ( ! "ueberladen".equals(result)) { return new TestResult_ExceptionRaised(context, new Throwable("expected 'ueberladen', got '" + result + "'")); }
        }

        {
            Context c = Context.create(context);
            LocationContext lc = RootContext.createLocationContext();

            ConfigurationContext ccc = ConfigurationContext.create(c);
            ccc.setPropertyEntry(c, "com.sphenon.basics.configuration.Hasi", "ueberladen");

            ConfigurationContext lcc = ConfigurationContext.create((Context) lc);
            lcc.setPropertyEntry(c, "com.sphenon.basics.configuration.Hasi", "lokal_ueberladen");

            result = cfg.get(c, "Hasi", "nix");
            if (trace) { System.out.println( "main, retrieving property 'Hasi': " + result); }

            if ( ! "lokal_ueberladen".equals(result)) { return new TestResult_ExceptionRaised(context, new Throwable("expected 'lokal_ueberladen', got '" + result + "'")); }
        }

        if (trace) { System.out.println( "main done." ); }

        return TestResult.OK;
    }
}
