package com.sportsequipment.controller;

import com.sportsequipment.dto.ProductDTO;
import com.sportsequipment.entity.Product;
import com.sportsequipment.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品控制器，处理商品的CRUD操作
 * @author system
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // 获取所有商品
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            return ResponseEntity.ok(productService.getProductsByPage(page, size));
        } else {
            return ResponseEntity.ok(productService.getAllProducts());
        }
    }

    // 根据主分类ID获取商品
    @GetMapping("/category/{mainCategoryId}")
    public ResponseEntity<List<ProductDTO>> getProductsByMainCategory(@PathVariable Long mainCategoryId) {
        List<ProductDTO> products = productService.getAllProducts().stream()
                .filter(p -> p.getMainCategoryId() != null && p.getMainCategoryId().equals(mainCategoryId))
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    // 根据ID获取商品
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // 管理员接口：创建商品
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody Product product) {
        return ResponseEntity.ok(productService.createProduct(product));
    }

    // 管理员接口：更新商品
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody Product product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    // 管理员接口：删除商品
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
    