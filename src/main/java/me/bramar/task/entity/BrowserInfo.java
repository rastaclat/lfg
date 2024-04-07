package me.bramar.task.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 浏览器及指纹对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("browser_info")
public class BrowserInfo {

    //浏览器id
    @TableId("browser_id")
    private String browserId;

    //指纹id
    @TableField("finger_print_id")
    private String fingerPrintId;

    //BrowserTypeEnum bit,ads
    @TableField("browser_type")
    private String browserType;

    //刷新URL
    @TableField("refresh_proxy_url")
    private String refreshProxyUrl;

    //创建时间
    @TableField("created_time")
    private Date createdTime;

    //更新时间
    @TableField("created_time")
    private Date updateTime;

    //打开时间
    @TableField("oper_time")
    private Date operTime;

    //关闭时间
    @TableField("close_time")
    private Date closeTime;
}
