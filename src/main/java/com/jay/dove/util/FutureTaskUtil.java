package com.jay.dove.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2022/01/11 10:34
 */
@Slf4j
public class FutureTaskUtil {
    public static <T> T getFutureTaskResult(RunStateRecordedFutureTask<T> futureTask){
        if(futureTask != null){
            try{
                return futureTask.get();
            } catch (ExecutionException | InterruptedException e) {
                log.error("future task error, ", e);
            }catch (IllegalStateException e){
                log.error("future task run state error, ", e);
            }
        }
        return null;
    }
}
