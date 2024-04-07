package me.bramar.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 配置信息实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("config_info")
public class ConfigInfo {

    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    /**
     * 浏览器配置 bit
     */
    @TableField(value = "browser_type")
    private String browserType;

    /**
     *操作系统平台
     */
    @TableField(value = "os_type")
    private String osType;

}
