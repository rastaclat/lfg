package me.bramar.task.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.bramar.task.entity.CreditCardInfo;
import me.bramar.task.mapper.CreditCardInfoMapper;
import me.bramar.task.service.ICreditCardInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Transactional(rollbackFor = Exception.class)
@Service
public class CreditCardInfoServiceImpl extends ServiceImpl<CreditCardInfoMapper, CreditCardInfo> implements ICreditCardInfoService {


    @Override
    public synchronized void updateSuccessNum(String cardNumber) {
        CreditCardInfo cardInfo = this.getById(cardNumber);
        if (cardInfo != null) {
            Integer successNum = cardInfo.getSuccessNum();
            if (successNum == null) {
                successNum = 0;
            }
            cardInfo.setSuccessNum(successNum + 1);
            this.updateById(cardInfo);
        }
    }

    @Override
    public synchronized void updateFailNum(String cardNumber) {
        CreditCardInfo cardInfo = this.getById(cardNumber);
        if (cardInfo != null) {
            Integer failNum = cardInfo.getFailNum();
            if (failNum == null) {
                failNum = 0;
            }
            cardInfo.setSuccessNum(failNum + 1);
            this.updateById(cardInfo);
        }
    }
}
