package com.ayral.openstuff.proxy;

import com.ayral.openstuff.Integration;
import com.ayral.openstuff.integration.OCAssembler;

import java.io.File;

public class OpenStuffCommon {
    public void preinit(File configFile)
    {
        System.out.println("pre init cote commun");
    }

    public void init()
    {
        Integration.init();
        OCAssembler.init();
    }

    public void postinit()
    {

    }
}
