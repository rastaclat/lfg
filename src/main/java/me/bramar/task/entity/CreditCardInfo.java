package me.bramar.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditCardInfo {
    @TableId(type = IdType.NONE)
    private String cardNumber;

    @TableField(exist = false)
    private final BooleanProperty selected = new SimpleBooleanProperty();

    private String monthNum;
    private String yearNum;
    private String securityCode;
    private String fullName;
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String phoneNumber;
    private Integer phoneNumberCountry;
    private String state;
    private String zipCode;
    private String email;
    private String password;
    private Integer successNum;
    private Integer failNum;
    private String isActive; //1已使用,0未使用

    @TableField(exist = false)
    private String regionCode;

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
