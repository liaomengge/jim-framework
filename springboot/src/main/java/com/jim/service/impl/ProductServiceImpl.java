package com.jim.service.impl;

import com.jim.config.RabbitMQConfig;
import com.jim.dao.generated.entity.Product;
import com.jim.dao.generated.mapper.ProductMapper;
import com.jim.service.BaseService;
import com.jim.service.ProductService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by jiang on 2016/12/22.
 */
@Service
public class ProductServiceImpl extends BaseService implements ProductService, RabbitTemplate.ConfirmCallback {

    @Autowired
    private ProductMapper productMapper;

    private RabbitTemplate rabbitTemplate;

    public ProductServiceImpl(RabbitTemplate rabbitTemplate){
        this.rabbitTemplate=rabbitTemplate;
        this.rabbitTemplate.setConfirmCallback(this);
    }

    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        this.logger.info(" 消息id:" + correlationData);
        if (ack) {
            this.logger.info("消息发送确认成功");
        } else {
            this.logger.info("消息发送确认失败:" + cause);

        }
    }

    @Override
    public Product getById(Long id) {
        return this.productMapper.selectByPrimaryKey(id);
    }

    @Override
    public void save(Product product) {

        if(null==product.getId()){
            this.productMapper.insert(product);
        }
        else {
            this.productMapper.updateByPrimaryKey(product);

        }
        String uuid = UUID.randomUUID().toString();
        CorrelationData correlationId = new CorrelationData(uuid);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, product.getName(),correlationId);
    }
}
