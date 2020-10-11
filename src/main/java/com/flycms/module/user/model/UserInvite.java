package com.flycms.module.user.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author cs
 * @date 2020/10/11
 */

@Data
public class UserInvite implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    /**
     * 被邀请人ID
     */
    private Long toUserId;
    /**
     * 邀请人ID
     */
    private Long formUserId;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Date createTime;
}
