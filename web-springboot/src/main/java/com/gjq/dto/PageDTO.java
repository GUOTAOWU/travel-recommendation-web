package com.gjq.dto;

import lombok.Data;

/**
 * ページング検索基礎DTO
 */
@Data
public class PageDTO {
    /**
     * 現在のページ番号
     */
    private Integer current = 1;

    /**
     * 1ページあたりの表示件数
     */
    private Integer size = 10;
}