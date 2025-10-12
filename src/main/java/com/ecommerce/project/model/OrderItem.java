package com.ecommerce.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    private Double discount;
    private Double orderedProductPrice;
    private Integer quantity;

    // this table will have foreign key of order table
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    // this table will have foreign key of product table
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
