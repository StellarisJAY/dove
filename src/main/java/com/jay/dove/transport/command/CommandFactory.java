package com.jay.dove.transport.command;

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
     * create a request with serializable request body
     * @param requestBody serializable request body
     * @param commandCode {@link CommandCode}
     * @param clazz Class
     * @param <T> Type
     * @return {@link RemotingCommand}
     */
    <T> RemotingCommand createRequest(T requestBody, CommandCode commandCode, Class<T> clazz);

    /**
     * create a response with response body and command code
     * @param id request id
     * @param response response body
     * @param commandCode {@link CommandCode}
     * @return {@link RemotingCommand}
     */
    RemotingCommand createResponse(int id, Object response, CommandCode commandCode);

    /**
     * create a response with serializer
     * @param id response id
     * @param content response entity to be serialized
     * @param clazz response entity clazz
     * @param commandCode {@link CommandCode}
     * @param <T> response entity type
     * @return {@link RemotingCommand}
     */
    default <T> RemotingCommand createResponse(int id, T content, Class<T> clazz, CommandCode commandCode){
        return null;
    }
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
