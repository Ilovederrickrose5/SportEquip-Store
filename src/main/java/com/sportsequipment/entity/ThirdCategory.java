package com.sportsequipment.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "third_category",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "sub_category_id"})
        })
public class ThirdCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id", nullable = false)
    private SubCategory subCategory;

    @OneToMany(mappedBy = "thirdCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SubCategory getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(SubCategory subCategory) {
        this.subCategory = subCategory;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
    