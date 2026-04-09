package com.gjq.dto.user;

import com.gjq.dto.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ユーザー検索DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryDTO extends PageDTO {
    /**
     * ユーザー名
     */
    private String username;

    /**
     * 氏名
     */
    private String realName;

    /**
     * 電話番号
     */
    private String phone;

    /**
     * メールアドレス
     */
    private String email;

    /**
     * ロール（0: 一般ユーザー、1: 管理者）
     */
    private Integer role;

    /**
     * ステータス（0: 無効、1: 有効）
     */
    private Integer status;
}