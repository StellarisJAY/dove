package com.jay.dove.transport.callback;

import com.jay.dove.transport.command.RemotingCommand;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2022/01/07 19:43
 */
public class DefaultInvokeFuture implements InvokeFuture{

    private RemotingCommand response;

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private InvokeCallback callback;

    public DefaultInvokeFuture(InvokeCallback callback) {
        this.callback = callback;
    }

    @Override
    public RemotingCommand awaitResponse() throws InterruptedException {
        countDownLatch.await();
        return response;
    }

    @Override
    public RemotingCommand awaitResponse(long timeout, TimeUnit timeUnit) throws TimeoutException, InterruptedException {
        if(!countDownLatch.await(timeout, timeUnit)){
            throw new TimeoutException("future timeout");
        }
        return response;
    }

    @Override
    public void putResponse(RemotingCommand response) {
        countDownLatch.countDown();
        this.response = response;
        // callback
        this.executeCallback();
    }

    @Override
    public void executeCallback() {
        if(callback != null){
            callback.getExecutor().submit(()->{
                try{
                    callback.onComplete(response);
                }catch (Exception e){
                    callback.exceptionCaught(e);
                }
            });
        }
    }
}
