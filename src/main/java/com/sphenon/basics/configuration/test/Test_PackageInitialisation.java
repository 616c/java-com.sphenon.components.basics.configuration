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
import com.sphenon.basics.debug.*;
import com.sphenon.basics.message.*;

import com.sphenon.basics.notification.BootstrapNotifier;

import com.sphenon.basics.monitoring.*;

import com.sphenon.basics.configuration.*;
import com.sphenon.basics.testing.TestRun;
import com.sphenon.basics.testing.TestResult;
import com.sphenon.basics.testing.TestResult_ExceptionRaised;

import java.io.*;

public class Test_PackageInitialisation extends com.sphenon.basics.testing.classes.TestBase {

    static public void main(String[] args) {
        Context context = com.sphenon.basics.context.classes.RootContext.getRootContext ();
        TestRun test_run = new com.sphenon.basics.testing.classes.ClassTestRun(context);
        TestResult result = (new Test_PackageInitialisation(context, "com.sphenon.basics.configuration.ConfigurationPackageInitialiser")).perform(context, test_run);
        Dumper.dump(context, "TestResult", result);
        if (result  == TestResult.OK) {
            System.err.println("TEST OK.");
        } else {
            System.err.println("*** TEST FAILED ***");
        }
    }

    public Test_PackageInitialisation (CallContext context, String initialiser) {
        this.initialiser = initialiser;
    }

    public String getId (CallContext context) {
        if (this.id == null) {
            this.id = "PackageInitialisation";
        }
        return this.id;
    }

    protected String initialiser;

    public String getIinitialiser (CallContext context) {
        return this.initialiser;
    }

    public TestResult perform (CallContext context, TestRun test_run) {

        try {
            CoreInitialiser.initialisePackage (context, initialiser);
        } catch (Throwable t) {
            return new TestResult_ExceptionRaised(context, t);
        }

        BootstrapNotifier.sendCheckpoint(context,  "Package initialised: '" + this.initialiser + "'");

        return TestResult.OK;
    }
}
