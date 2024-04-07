package me.bramar.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import me.bramar.task.entity.CreditCardInfo;

public interface ICreditCardInfoService extends IService<CreditCardInfo> {
    /**
     * 成功数+1
     */
    void updateSuccessNum(String cardNumber);

    /**
     * 失败数+1
     */
    void updateFailNum(String cardNumber);
}
