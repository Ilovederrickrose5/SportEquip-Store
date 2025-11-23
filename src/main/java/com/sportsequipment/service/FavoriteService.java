package com.sportsequipment.service;

import com.sportsequipment.dto.ProductDTO;
import com.sportsequipment.entity.Favorite;
import java.util.List;

public interface FavoriteService {
    Favorite addToFavorite(Long productId);
    void removeFromFavorite(Long productId);
    List<ProductDTO> getFavoriteProducts();
    boolean isProductFavorite(Long productId);
}