package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.payload.OrderItemDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repositories.*;
import com.ecommerce.project.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OrderServiceImpl implements OrderService{
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AuthUtil authUtil;  // to enable to work with an authenticated user.
    
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ModelMapper modelMapper;

    // 1. Get the cart of the user
    // 2. Create a new order with payment information
    // 3. Get the cart items of the cart and put to order items
    // 4. Place Order
    // update product stock
    // clear cart
    // send back order summary
    @Transactional
    @Override
    public OrderDTO placeOrder(String emailId, Long addressId,
                               String paymentMethod, String pgName,
                               String pgPaymentId, String pgStatus,
                               String pgResponseMessage) {

        Cart cart = cartRepository.findCartByEmail(emailId);
        if ( cart == null) {
            throw new ResourceNotFoundException("Email Id not found", "Cart", "email", "emailId");
        }

        // public ResourceNotFoundException(String message, String resourceName, String field, String fieldName)
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "Id", addressId));

        Order order = new Order();
        order.setEmail(emailId);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order Accepted");
        order.setAddress(address);

        Payment payment = new Payment(paymentMethod, pgPaymentId,
                pgStatus, pgResponseMessage, pgName);
        payment.setOrder(order);

        // save payment first because of cascade type all
        payment = paymentRepository.save(payment);
        // set saved payment to order as the payment id is generated after save.
        order.setPayment(payment);
        // now save order.  Note the order table has payment id as foreign key.
        Order savedOrder = orderRepository.save(order);

        // Get items from cart and put to order items
        List<CartItem> cartItems = cart.getCartItems();
        if (cartItems.isEmpty()) {
            throw new APIException("Cart is empty. Add items to cart before placing order");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
//
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderedProductPrice(cartItem.getProductPrice());

            orderItem.setOrder(savedOrder);

            orderItems.add(orderItem);
        }
        // now save all order items.  The order id is foreign key in order items table.
        // So Order is also saved and OrderItems are also saved at this point.
        List<OrderItem> savedOrderItems = orderItemRepository.saveAll(orderItems);

        // Update product stock
        for (OrderItem orderItem : savedOrderItems) {
            int quantity = orderItem.getQuantity();
            Product product = orderItem.getProduct();
            if (product.getQuantity() < quantity) {
                throw new APIException("Product " + product.getProductName() + " is out of stock");
            }
            product.setQuantity(product.getQuantity() - quantity);
            productRepository.save(product);

            // remove the item from the cart
            cartService.deleteProductFromCart(cart.getCartId(), product.getProductId());
        }

        // send back order summary
        OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
        savedOrderItems.forEach(item -> {
            ProductDTO productDTO = modelMapper.map(item.getProduct(), ProductDTO.class);
            OrderItemDTO orderItemDTO = modelMapper.map(item, OrderItemDTO.class);
            orderItemDTO.setProduct(productDTO);

            orderDTO.getOrderItems().add(orderItemDTO);
        });

        orderDTO.setAddressId(addressId);

        // String paymentMethod, String pgPaymentId,
        // String pgStatus, String pgResponseMessage,
        // String pgName
        return orderDTO;
    }
}
