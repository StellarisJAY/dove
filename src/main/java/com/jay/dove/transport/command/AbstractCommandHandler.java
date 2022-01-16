package com.jay.dove.transport.command;

import com.jay.dove.config.Configs;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2022/01/08 11:10
 */
@Slf4j
public abstract class AbstractCommandHandler implements CommandHandler{

    private final ProcessorManager processorManager;
    private ExecutorService defaultExecutor;
    private final CommandFactory commandFactory;

    public AbstractCommandHandler(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
        this.processorManager = new ProcessorManager();
    }

    public AbstractCommandHandler(ProcessorManager processorManager, CommandFactory commandFactory){
        this.processorManager = processorManager;
        this.commandFactory = commandFactory;
    }

    public AbstractCommandHandler(ExecutorService defaultExecutor, CommandFactory commandFactory){
        this.defaultExecutor = defaultExecutor;
        this.processorManager = new ProcessorManager();
        this.commandFactory = commandFactory;
    }

    @Override
    public void registerProcessor(CommandCode cmd, Processor processor) {
        processorManager.registerProcessor(cmd, processor);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void handleCommand(ChannelHandlerContext context, Object msg) {
        if(msg instanceof List){
            // handle multiple commands
            Runnable task = ()->{
                List commands = (List)msg;
                for(Object command : commands){
                    process(context, command);
                }
            };
            if(Configs.dispatchListToExecutor()){
                getDefaultExecutor().submit(task);
            }else{
                task.run();
            }
        }else{
            // handle one command
            process(context, msg);
        }
    }

    /**
     * process one command
     * @param context {@link ChannelHandlerContext}
     * @param msg {@link RemotingCommand}
     */
    private void process(ChannelHandlerContext context, Object msg){
        try{
            RemotingCommand command = (RemotingCommand) msg;
            // server side fail-fast, check timeout
            if(command.getTimeoutMillis() <= System.currentTimeMillis()){
                // create timeout response and send
                RemotingCommand response = commandFactory.createTimeoutResponse(command.getId(), "request timeout");
                context.channel().writeAndFlush(response);
            }
            else{
                // get the command code
                CommandCode code = command.getCommandCode();
                // find processor for this command code
                Processor processor = processorManager.getProcessor(code);
                // process command
                processor.process(context, command);
            }
        }catch (Throwable e){
            handleException(context, e);
        }
    }

    private void handleException(ChannelHandlerContext context, Throwable cause){
        // handle exception here
        log.error("process error: ",cause);
    }

    @Override
    public ExecutorService getDefaultExecutor() {
        return defaultExecutor;
    }

    @Override
    public void registerDefaultExecutor(ExecutorService executor) {
        this.defaultExecutor = executor;
    }
}
