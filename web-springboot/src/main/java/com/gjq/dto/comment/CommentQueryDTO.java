package com.gjq.dto.comment;

import com.gjq.dto.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * コメント検索DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentQueryDTO extends PageDTO {
    /**
     * アイテムID
     */
    private Long itemId;
    
    /**
     * ユーザーID
     */
    private Long userId;
    
    /**
     * トップレベルコメントのみを検索するかどうか
     */
    private Boolean onlyParent;
}