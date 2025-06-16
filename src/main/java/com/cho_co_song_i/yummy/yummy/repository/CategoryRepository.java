package com.cho_co_song_i.yummy.yummy.repository;

import com.cho_co_song_i.yummy.yummy.entity.CategoryTbl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryTbl, Long> {
    Optional<CategoryTbl> findByCategoryGroupCodeAndCategoryGroupNameAndCategoryName(
            String categoryGroupCode,
            String categoryGroupName,
            String categoryName
    );
}
