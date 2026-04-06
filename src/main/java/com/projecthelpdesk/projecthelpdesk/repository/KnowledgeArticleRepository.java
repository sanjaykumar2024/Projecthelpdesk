package com.projecthelpdesk.projecthelpdesk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.projecthelpdesk.projecthelpdesk.entity.KnowledgeArticle;

public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {

    List<KnowledgeArticle> findByPublishedTrueOrderByCreatedAtDesc();

    List<KnowledgeArticle> findByCategoryAndPublishedTrue(String category);

    @Query("SELECT a FROM KnowledgeArticle a WHERE a.published = true AND " +
            "(LOWER(a.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(a.tags) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<KnowledgeArticle> search(@Param("q") String query);

    @Query("SELECT DISTINCT a.category FROM KnowledgeArticle a WHERE a.published = true AND a.category IS NOT NULL")
    List<String> findAllCategories();
}
