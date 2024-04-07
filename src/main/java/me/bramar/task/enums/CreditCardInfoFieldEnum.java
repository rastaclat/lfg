package me.bramar.task.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CreditCardInfoFieldEnum {
    CARD_NUMBER,
    MONTH_NUM,
    YEAR_NUM,
    SECURITY_CODE,
    FULL_NAME,
    FIRST_NAME,
    LAST_NAME,
    ADDRESS,
    CITY,
    PHONE_NUMBER,
    STATE,
    ZIP_CODE,
    EMAIL,
    PASSWORD;
}
