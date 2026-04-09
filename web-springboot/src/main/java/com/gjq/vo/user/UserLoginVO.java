package com.gjq.vo.user;

import com.gjq.client.FileClient;
import com.gjq.entity.User;
import lombok.Data;


@Data
public class UserLoginVO {
   
    private UserInfo userInfo;

    
    private String token;

    public UserLoginVO(User user, String token, FileClient fileClient) {
        this.userInfo = new UserInfo(user, fileClient);
        this.token = token;
    }
} 