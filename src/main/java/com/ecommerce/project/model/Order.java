package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Email  // this will ensure that the email is valid
    @Column(nullable = false)
    private String email;

    // Reverse map to Order Items.
    // CascadeType.PERSIST: Automatically persists (save) associated OrderItem entities when a new Order is saved.
    //
    //CascadeType.MERGE: Automatically merges (updates) associated OrderItem entities when an existing Order is updated.
    //
    //CascadeType.REMOVE: Automatically removes (deletes) associated OrderItem entities when the Order is removed.
    //
    //orphanRemoval = true: Automatically removes OrderItem entities that are no longer part of the orderItems list (i.e., if an OrderItem is disassociated from the Order).
    @OneToMany(mappedBy = "order", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    private LocalDate orderDate;
    private Double totalAmount;
    private String orderStatus;

    @OneToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;
}
