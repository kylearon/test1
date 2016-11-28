package com.soartech.simjr.activator;

import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimJrActivatorManager
{
    private static Logger logger = LoggerFactory.getLogger(SimJrActivatorManager.class);
    
    public static void startActivators()
    {
        logger.info("Starting SimJrActivators");
        ServiceLoader<SimJrActivator> loader = ServiceLoader.load(SimJrActivator.class);

        for (SimJrActivator activator : loader)
        {
            logger.info("Starting SimJrActivator: " + activator.getClass().getName());
            activator.start();
        }
    }
}
