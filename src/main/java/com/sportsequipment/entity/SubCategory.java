package com.sportsequipment.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "sub_category",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "main_category_id"})
        })
public class SubCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_category_id", nullable = false)
    private MainCategory mainCategory;

    @OneToMany(mappedBy = "subCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ThirdCategory> thirdCategories;

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

    public MainCategory getMainCategory() {
        return mainCategory;
    }

    public void setMainCategory(MainCategory mainCategory) {
        this.mainCategory = mainCategory;
    }

    public List<ThirdCategory> getThirdCategories() {
        return thirdCategories;
    }

    public void setThirdCategories(List<ThirdCategory> thirdCategories) {
        this.thirdCategories = thirdCategories;
    }
}
    