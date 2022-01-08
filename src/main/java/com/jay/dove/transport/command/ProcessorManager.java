package com.jay.dove.transport.command;

import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *  Processor manager.
 *  manages Processors for each CommandCode
 * </p>
 *
 * @author Jay
 * @date 2022/01/08 11:11
 */
public class ProcessorManager {
    private final ConcurrentHashMap<CommandCode, Processor> processorMap = new ConcurrentHashMap<>(16);

    public void registerProcessor(CommandCode cmd, Processor processor){
        processorMap.putIfAbsent(cmd, processor);
    }

    public Processor getProcessor(CommandCode cmd){
        return processorMap.get(cmd);
    }
}
