package com.jay.dove.transport.command;

import com.jay.dove.config.DoveConfigs;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

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
            List commands = (List)msg;
            // handle multiple commands
            Runnable task = ()->{
                for(Object command : commands){
                    process(context, command);
                }
            };
            // check whether dispatch task to executor or not
            if(DoveConfigs.dispatchListToExecutor() && getDefaultExecutor() != null){
                // try submit task to executor
                try{
                    getDefaultExecutor().submit(task);
                }catch (RejectedExecutionException e){
                    // task rejected by executor
                    for(Object obj : commands){
                        // send error response
                        RemotingCommand command = (RemotingCommand)obj;
                        RemotingCommand response = commandFactory.createExceptionResponse(command.getId(), "command rejected by command handler executor");
                        context.channel().writeAndFlush(response);
                    }
                }
            }else{
                /*
                    run task using I/O thread.
                    This option is expensive.
                 */
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
