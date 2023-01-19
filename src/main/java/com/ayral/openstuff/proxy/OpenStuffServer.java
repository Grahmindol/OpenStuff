package com.ayral.openstuff.proxy;

import java.io.File;

public class OpenStuffServer extends OpenStuffCommon
{
    @Override
    public void preinit(File configFile)
    {
        super.preinit(configFile);
        System.out.println("pre init cote server");
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
