package com.milktea.payment.gateway;

import com.milktea.common.exception.BusinessException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** Selects the registered strategy for a payment method. */
@Component
@RequiredArgsConstructor
public class PaymentGatewayFactory {
    private final List<PaymentGateway> gateways;
    private Map<String, PaymentGateway> gatewayMap = Map.of();

    @PostConstruct
    public void init() {
        gatewayMap = gateways.stream().collect(Collectors.toUnmodifiableMap(
                PaymentGateway::getMethod, Function.identity()));
    }

    public PaymentGateway getGateway(String method) {
        PaymentGateway gateway = method == null ? null : gatewayMap.get(method);
        if (gateway == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Phương thức thanh toán không được hỗ trợ: " + method);
        }
        return gateway;
    }
}
