package com.jay.dove.transport.command;

import com.jay.dove.transport.connection.Connection;

/**
 * <p>
 *  Default command factory interface
 * </p>
 *
 * @author Jay
 * @date 2022/01/13 14:31
 */
public interface CommandFactory {

    /**
     * create a request with request body and command code
     * @param requestBody {@link Object}
     * @param commandCode {@link CommandCode}
     * @return {@link RemotingCommand}
     */
    RemotingCommand createRequest(Object requestBody, CommandCode commandCode);

    /**
     * create a response with response body and command code
     * @param id request id
     * @param response response body
     * @param commandCode {@link CommandCode}
     * @return {@link RemotingCommand}
     */
    RemotingCommand createResponse(int id, Object response, CommandCode commandCode);

    /**
     * create a timeout response
     * @param id request id
     * @param response response body
     * @return {@link RemotingCommand}
     */
    RemotingCommand createTimeoutResponse(int id, Object response);

    RemotingCommand createExceptionResponse(int id, String errMsg);

    RemotingCommand createExceptionResponse(int id, Throwable cause);
}
