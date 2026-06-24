/*
 * File: ServiceResponse.java
 * Project: FitFlow - Interactive Workout Assistant
 * Course: UMGC CMSC 495
 * Phase: Phase I Source Code
 * Week: 4
 * Version: v0.4.03
 * Author: David Lewis
 * Last Updated: 2026-06-07
 *
 * Purpose:
 * Provides a standard response object for communication between the
 * presentation/controllers layer and the integration/service layer.
 *
 * Dependencies:
 * Java Standard Library only.
 *
 */

/**
 * Generic response wrapper used by FitFlow integration methods.
 *
 * @param <T> The type of data returned by the service call.
 */

package service;

public class ServiceResponse<T> {
    private final boolean success;
    private final String message;
    private final T data;
    private final String errorCode;

    /**
     * Creates a new service response.
     *
     * @param success Indicates whether the operation completed successfully.
     * @param message User-friendly success or error message.
     * @param data Optional returned data.
     * @param errorCode Optional internal error code for troubleshooting.
     */
    public ServiceResponse(boolean success, String message, T data, String errorCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
    }

    /**
     * Creates a successful response with returned data.
     *
     * @param message User-friendly success message.
     * @param data Data returned by the service method.
     * @param <T> Type of returned data.
     * @return Successful ServiceResponse instance.
     */
    public static <T> ServiceResponse<T> success(String message, T data) {
        return new ServiceResponse<T>(true, message, data, null);
    }

    /**
     * Creates a successful response without returned data.
     *
     * @param message User-friendly success message.
     * @param <T> Type placeholder for response.
     * @return Successful ServiceResponse instance.
     */
    public static <T> ServiceResponse<T> success(String message) {
        return new ServiceResponse<T>(true, message, null, null);
    }

    /**
     * Creates an error response.
     *
     * @param message User-friendly error message.
     * @param errorCode Internal error code for documentation and debugging.
     * @param <T> Type placeholder for response.
     * @return Error ServiceResponse instance.
     */
    public static <T> ServiceResponse<T> error(String message, String errorCode) {
        return new ServiceResponse<T>(false, message, null, errorCode);
    }

    /**
     * Returns whether the request succeeded.
     *
     * @return true when successful; false otherwise.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the response message.
     *
     * @return User-friendly response message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns optional response data.
     *
     * @return Data object, or null when no data is returned.
     */
    public T getData() {
        return data;
    }

    /**
     * Returns optional error code.
     *
     * @return Error code, or null for successful responses.
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Formats the response for console testing and debugging.
     *
     * @return Readable response summary.
     */
    @Override
    public String toString() {
        return "ServiceResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", errorCode='" + errorCode + '\'' +
                '}';
    }
}