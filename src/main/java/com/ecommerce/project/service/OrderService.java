package com.ecommerce.project.service;

import com.ecommerce.project.payload.OrderDTO;
import jakarta.transaction.Transactional;


public interface OrderService   {
    @Transactional
    public OrderDTO placeOrder(String emailId, Long addressId,
                        String paymentMethod, String pgName,
                        String pgPaymentId, String pgStatus,
                        String pgResponseMessage);
}
