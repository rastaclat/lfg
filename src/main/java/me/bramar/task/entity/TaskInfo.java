package me.bramar.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务信息实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("task_info")
public class TaskInfo {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(exist = false)
    private final BooleanProperty selected = new SimpleBooleanProperty();

    private String taskId;

    private String taskUrl;

    private String taskMsg;

    private String pageInfo;

    private String realTaskUrl;

    private String openTime;

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

}
