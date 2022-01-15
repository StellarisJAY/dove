package com.jay.dove.transport.command;

import com.jay.dove.transport.connection.Connection;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2022/01/13 14:31
 */
public interface CommandFactory {

    RemotingCommand createRequest(Object request);

    RemotingCommand createResponse(int id, Object response);

    RemotingCommand createExceptionResponse(int id, String errMsg);

    RemotingCommand createExceptionResponse(int id, Throwable cause);

    RemotingCommand createTimeoutResponse(int id, String message);
}
