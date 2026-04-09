package com.gjq.dto.useraction;

import com.gjq.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ユーザー行動検索DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "ユーザー行動検索DTO")
public class UserActionQueryDTO extends PageDTO {
    
    /**
     * ユーザーID
     */
    @Schema(description = "ユーザーID")
    private Long userId;
    
    /**
     * ユーザー名
     */
    @Schema(description = "ユーザー名")
    private String username;
    
    /**
     * 観光スポットID
     */
    @Schema(description = "観光スポットID")
    private Long itemId;
    
    /**
     * 観光スポット名
     */
    @Schema(description = "観光スポット名")
    private String itemTitle;
    
    /**
     * アクションタイプ（0: 閲覧、1: 予約）
     */
    @Schema(description = "アクションタイプ：0-閲覧、1-予約")
    private Integer actionType;
}