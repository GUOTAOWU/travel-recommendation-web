package com.gjq.common.exception;

import com.gjq.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * グローバル例外ハンドラー
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * バリデーション（引数チェック）例外の処理
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        log.error("バリデーション例外が発生しました", e);
        return Result.error(400, e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    /**
     * 業務例外の処理
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("業務例外が発生しました", e);
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * その他の例外の処理
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("システム例外が発生しました", e);
        return Result.error("システムエラーが発生しました。管理者に連絡してください");
    }
}