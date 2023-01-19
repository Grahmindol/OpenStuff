package com.ayral.openstuff.proxy;

import com.ayral.openstuff.manual.Manual;

import java.io.File;

public class OpenStuffClient extends OpenStuffCommon
{
    @Override
    public void preinit(File configFile)
    {
        super.preinit(configFile);
        System.out.println("pre init cote client");
        Manual.preInit();
    }

    @Override
    public void init()
    {
        super.init();
    }

    @Override
    public void postinit()
    {
        super.postinit();
    }
}
