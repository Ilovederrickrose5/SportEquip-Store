package com.sportsequipment.exception;

import com.sportsequipment.dto.ApiErrorCode;
import com.sportsequipment.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 
 * 统一处理所有异常，返回标准化的错误响应
 */
@ControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        /**
         * 处理资源不存在异常 (404)
         */
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
                        ResourceNotFoundException ex,
                        HttpServletRequest request) {
                logger.warn("资源不存在: {}", ex.getMessage());

                ApiResponse<Void> response = ApiResponse.<Void>notFound(ex.getMessage());
                response.setPath(request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        /**
         * 处理未授权异常 (401)
         */
        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<ApiResponse<Void>> handleUnauthorizedException(
                        UnauthorizedException ex,
                        HttpServletRequest request) {
                logger.warn("未授权访问: {}", ex.getMessage());

                ApiResponse<Void> response = ApiResponse.<Void>unauthorized(ex.getMessage());
                response.setPath(request.getRequestURI());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        /**
         * 处理验证异常 (400)
         */
        @ExceptionHandler(ValidationException.class)
        public ResponseEntity<ApiResponse<Void>> handleValidationException(
                        ValidationException ex,
                        HttpServletRequest request) {
                logger.warn("验证失败: {}", ex.getMessage());

                ApiResponse<Void> response = ApiResponse.<Void>badRequest(ex.getMessage());
                response.setPath(request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        /**
         * 处理权限拒绝异常 (403)
         */
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
                        AccessDeniedException ex,
                        HttpServletRequest request) {
                logger.warn("权限拒绝: {}", ex.getMessage());

                ApiResponse<Void> response = ApiResponse.<Void>forbidden("无权访问");
                response.setPath(request.getRequestURI());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        /**
         * 处理认证异常 (401)
         */
        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
                        AuthenticationException ex,
                        HttpServletRequest request) {
                logger.warn("认证失败: {}", ex.getMessage());

                ApiResponse<Void> response = ApiResponse.<Void>unauthorized("认证失败，请重新登录");
                response.setPath(request.getRequestURI());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        /**
         * 处理凭证错误异常 (401)
         */
        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(
                        BadCredentialsException ex,
                        HttpServletRequest request) {
                logger.warn("凭证错误: {}", ex.getMessage());

                ApiResponse<Void> response = ApiErrorCode.AUTH_PASSWORD_ERROR.toResponse();
                response.setPath(request.getRequestURI());
                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(response);
        }

        /**
         * 处理文件上传异常 (500)
         */
        @ExceptionHandler(MultipartException.class)
        public ResponseEntity<ApiResponse<Void>> handleFileUploadException(
                        MultipartException ex,
                        HttpServletRequest request) {
                logger.error("文件上传失败: {}", ex.getMessage());

                ApiResponse<Void> response = ApiErrorCode.FILE_UPLOAD_ERROR.toResponse();
                response.setPath(request.getRequestURI());
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(response);
        }

        /**
         * 处理文件存储异常 (500)
         */
        @ExceptionHandler(FileStorageException.class)
        public ResponseEntity<ApiResponse<Void>> handleFileStorageException(
                        FileStorageException ex,
                        HttpServletRequest request) {
                logger.error("文件存储失败: {}", ex.getMessage());

                ApiResponse<Void> response = ApiResponse.<Void>serverError(ex.getMessage());
                response.setPath(request.getRequestURI());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        /**
         * 处理文件上传异常 (400/500)
         */
        @ExceptionHandler(FileUploadException.class)
        public ResponseEntity<ApiResponse<Void>> handleCustomFileUploadException(
                        FileUploadException ex,
                        HttpServletRequest request) {
                logger.error("文件上传异常: {}", ex.getMessage());

                ApiResponse<Void> response;
                HttpStatus status;

                if (ex.getMessage().contains("大小")) {
                        response = ApiErrorCode.FILE_SIZE_EXCEEDED.toResponse();
                        status = HttpStatus.BAD_REQUEST;
                } else {
                        response = ApiErrorCode.FILE_UPLOAD_ERROR.toResponse();
                        status = HttpStatus.INTERNAL_SERVER_ERROR;
                }
                response.setPath(request.getRequestURI());
                return ResponseEntity.status(status).body(response);
        }

        /**
         * 处理参数验证异常 (400)
         * 自动捕获 @Valid 注解的验证失败
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValidException(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                logger.warn("参数验证失败: {}", ex.getMessage());

                // 获取所有验证错误信息
                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach(error -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                });

                ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>badRequest("请求参数验证失败");
                response.setPath(request.getRequestURI());
                response.setErrors(errors);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        /**
         * 处理非法参数异常 (400)
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
                        IllegalArgumentException ex,
                        HttpServletRequest request) {
                logger.warn("非法参数: {}", ex.getMessage());

                ApiResponse<Void> response = ApiResponse.<Void>badRequest(ex.getMessage());
                response.setPath(request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        /**
         * 处理运行时异常 (500)
         */
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<ApiResponse<Void>> handleRuntimeException(
                        RuntimeException ex,
                        HttpServletRequest request) {
                logger.error("运行时异常: {}", ex.getMessage(), ex);

                ApiResponse<Void> response = ApiResponse.<Void>serverError(ex.getMessage());
                response.setPath(request.getRequestURI());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        /**
         * 处理所有未捕获的异常 (500)
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Void>> handleGlobalException(
                        Exception ex,
                        HttpServletRequest request) {
                logger.error("未知异常: {}", ex.getMessage(), ex);

                ApiResponse<Void> response = ApiResponse.<Void>serverError("服务器内部错误，请稍后重试");
                response.setPath(request.getRequestURI());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
}