package com.phoenix.alipay.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.phoenix.alipay.config.ApiException;
import com.phoenix.alipay.config.RetEnum;
import com.phoenix.alipay.consts.AlipayConsts;
import com.phoenix.alipay.util.QrCodeUtil;
import com.phoenix.alipay.vo.AlipayJsonRootBean;
import com.phoenix.alipay.service.AlipayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.UUID;

/**
 * @author tony
 * @date 2020/8/19 13:42
 */
@Slf4j
@Service
public class AlipayServiceImpl implements AlipayService {

    /**
     * 支付宝请求地址
     */
    private static String aliUrl = "https://openapi.alipay.com/gateway.do";
    /**
     * 支付宝应用ID
     */
    private static String aliAppId = "2021002125689431";
    /**
     * 本地通过"支付宝开放平台开发助手"生成的私钥
     */
    private static String aliAppPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCXgQmAqNKALwaTkR+mz/ySozl5xg0LdHWibcQV7AZLk8ey4qfALlWFfzo1YsVMSj0pDz8DZVEt9WalkHCeEeYS/acvmwfrYw1/+IMntfa90kLRQeiGdVd1BCToqpAg53L6350Vpfw1MRVfiDMxflIgAyeWe3cZcoM3oRn+I0SkCsLRZKY+ngtGxeZ9b+LneNh3Ze6+1CL8q+iAEpf6juzpYdFGD1F4uFTg18rAIZVBInd+yR4IiNKfK9uqWNn+CPQ9VDmQzZouFPh1KGpkf3JzTXBocUp0RQIuNS045grIt5fWIepSEolH3n/pC7CZBrUsLgoddXCQ6EvhjqRqV+jLAgMBAAECggEAR3ZtRgvb5uzHnFmq5WNbzstBNChwRMP1LN27zV9chM+7Nazl1afFXUfq6/2f9OFbwf9YCMEErrn/Zf8gr97feHu/HZxx+tzpJEDTlKqnLVh9ffkptiqpSx/eTQUPIQYrXRyXGvwFYA27+CjlhOYB1OnIBZpNTmqObHIIMbDvErTpUGXSokxmHiKjHwA6PIMZ/JzjAquDSQb+m8ugBYcEIXBpwn/1iIoNFhfSJ7vfHJxSk4XnyKU1UOT2HjB8+RhGT9fEDL1DyQu9XOlXR0/4qRUjmg6yBX8VWA4/fAe9CBhaijc6H2u9s5JmV5sZoYV9xb9lhEplIIZedCnKcJxHaQKBgQDgvk2LSM1FjGzT+OhdpwVWAqJZxzvAJ0zgGeGACWWNK6YG/QLLNwIxmB06WmAL3KoN+mmXPf7kNi59iMLm6dUPdEwwftU6PzGm4B5hjiiHaKJh5TzduxEcyDiKUr6cm3LWbQnalN5yJGgK67vZSncmzvnLgywoMh02JVZJ8Ev9fQKBgQCskySYZWoDtYWfppRHsNFEMxpB1bZMa3eyks88Hy42jfAJCJTPQrNM6bwT1XIcqcgCspqLe2tVChvGzCA82ixtykw7nXdY/8cI3e8HEWQ1uNee+m/CdU2tV1DWDwMbqcy2dTpgXEnaRtCVSVr+dspTe2V1SWdKaptLLano9Apx5wKBgQDE1gfmgLnAOxApyywAskLbJO16giOT1RfggupckF5P+TWtkzU6NJCITTj0HalXWknaekK2wwaWa9nl+rzxL0V7BpmgM8WfATAEfvA88xwOFJd79CGN9cQ0GCxlAbmOQhufL9rchVwaTkaNog9hbXXUGJzxII6xGcs53BOqKHr1pQKBgHITymsbDtto0O3aNca2MX89sKu4VAtsNvHKbkc17VyOsw+lpojjdJbxIyyROh+Mc/Skj9iIO0k5p2bgZKcVKa1hvsbhrpUcbzuDSZCIhI+mopIqxlnrJO/i+kKmJc3RjstF7tLn7sHlcdhdZ1Myv0g0F7tSTH86vXESvHCLcaQdAoGAZPW5WdASLy61r5G3Isq10UkQQxttNBvZfChqsQ1bCEFekBPs9PjCAlB+lcdvC6wJoCvLYArPScSQTEcfrqp94gUDaw+QtoSu541QURim1lk4g5NTTFX6yCqpWdJcsjrmIxHyaxjC5oU43OX37cxMTuxguOoIynafn7XlePZKpA8=\n";
    /**
     * 支付宝应用设置本地公钥后生成对应的支付宝公钥（非本地生成的公钥）
     */
    private static String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArGv3pABTJ3RIudPwgOqXVaXmOL0+ChGGZK3J/UhR8lucrQ65wRnCq5r6P+/3bL10ZGRGm9iB6eprv1NfdIxJeeCLg6YiPaMiRG+83mgWX6bZudP8ThkVo8G4aqG53D237cgC0/hJU9LyGyNzUc2ZiQ9w6qKWvaG9/jADW97moBRLUaRbJ987QhW8FaX08lMYof8VLXaY/wKFa8IxUcb2lvBKjNCXg1c8u0Ye69rew76u3NsdmdyZBuQnjSdx/qzFMsGWg5We3WJSY/Ko1OQpuV8kM2JcOVOR79YycxJe3e0shn7s5EiGHYtPRthvuWEtwLlzrHsC0axN/nZbE05BGQIDAQAB";
    ;
    /**
     * 支付宝回调的接口地址
     */
    private static String aliNotifyUrl = "http://localhost:8080/alinotify";

    @Override
    public String newAliOrder() throws Exception {
        log.info("开始调用支付宝生成支付二维码...");
        //实例化客户端
        AlipayClient alipayClient = new DefaultAlipayClient(aliUrl, aliAppId, aliAppPrivateKey, "json", "utf-8", alipayPublicKey, "RSA2");
        //设置请求参数
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        model.setOutTradeNo("2020082688888888888888899");
        model.setTotalAmount("1");
        model.setSubject("充值");
        //如果没有店铺号可不设置
        // model.setStoreId("9527");
        model.setQrCodeTimeoutExpress("1000m");
        request.setBizModel(model);
        //支付宝异步通知地址
        request.setNotifyUrl(aliNotifyUrl);
        log.info("创建支付宝订单，请求参数：{} ", JSONObject.toJSONString(request));
        //调用接口
        AlipayTradePrecreateResponse response = alipayClient.execute(request);

        log.info("创建支付宝订单，返回值：{} ", JSONObject.toJSONString(response));
        if (!response.isSuccess()) {
            throw new ApiException(RetEnum.MachineOrderAlipayException);
        }
        AlipayJsonRootBean alipayJsonRootBean = JSONObject.parseObject(response.getBody(), AlipayJsonRootBean.class);
        if (!AlipayConsts.SuccessCode.equals(alipayJsonRootBean.getAlipay_trade_precreate_response().getCode())) {
            throw new ApiException(RetEnum.MachineOrderAlipayException);
        }
        //成功


        // 将返回的二维码存储至本地
        String uuid = UUID.randomUUID().toString();
        String path = "./qrCode/" + uuid + "AliPay.jpg";
        String httpPath = "app/alipay/qrcode" + uuid + "AliPay.jpg";
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        QrCodeUtil.createQrCode(new FileOutputStream(file), response.getQrCode());


        return httpPath;
    }

    @Override
    public void aliNotify(Map<String, String> param) throws Exception {
        log.info("支付宝异步回调接口数据处理");
        //只有支付成功后，支付宝才会回调应用接口，可直接获取支付宝响应的参数
        String order_id = param.get(AlipayConsts.AliOutTradeNo);
        //出于安全考虑，通过支付宝回传的订单号查询支付宝交易信息
        AlipayTradeQueryResponse aliResp = queryOrder(order_id);
        if (!AlipayConsts.SuccessCode.equals(aliResp.getCode())) {
            //返回值非10000
            throw new ApiException(RetEnum.MachineOrderAlipayException, aliResp.getSubMsg());
        }
        if (!AlipayConsts.AliTradeSuccess.equals(aliResp.getTradeStatus()) && !AlipayConsts.AliTradeFinished.equals(aliResp.getTradeStatus())) {
            //支付宝订单状态不是支付成功
            throw new ApiException(RetEnum.MachineOrderAliUnPay);
        }
        //可对支付宝响应参数AlipayTradeQueryResponse进行处理

    }

    @Override
    public AlipayTradeQueryResponse queryOrder(String orderId) throws Exception {
        log.info("查询支付宝订单，订单编号为：{}", orderId);
        AlipayClient alipayClient = new DefaultAlipayClient(aliUrl, aliAppId, aliAppPrivateKey, "json", "utf-8", alipayPublicKey, "RSA2");
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(orderId);
        request.setBizModel(model);
        AlipayTradeQueryResponse response = alipayClient.execute(request);
        log.info("查询支付宝订单，返回数据：{}", response);
        return response;
    }


}
