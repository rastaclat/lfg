package me.bramar.task.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ip代理实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ip_proxy_info")
public class IpProxyInfo {
    @TableId
    private Integer id;

    @TableField("proxy_type")
    private String proxyType;

    @TableField("host")
    private String host;

    @TableField("port")
    private Integer port;

    @TableField("user_name")
    private String userName;

    @TableField("password")
    private String password;

    @TableField("refresh_link")
    private String refreshLink;
}
