package com.projecthelpdesk.projecthelpdesk.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projecthelpdesk.projecthelpdesk.entity.KnowledgeArticle;
import com.projecthelpdesk.projecthelpdesk.entity.User;
import com.projecthelpdesk.projecthelpdesk.exception.ResourceNotFoundException;
import com.projecthelpdesk.projecthelpdesk.repository.KnowledgeArticleRepository;
import com.projecthelpdesk.projecthelpdesk.repository.UserRepository;

@Service
public class KnowledgeService {

    private final KnowledgeArticleRepository articleRepository;
    private final UserRepository userRepository;

    public KnowledgeService(KnowledgeArticleRepository articleRepository, UserRepository userRepository) {
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
    }

    public List<KnowledgeArticle> getAllPublished() {
        return articleRepository.findByPublishedTrueOrderByCreatedAtDesc();
    }

    public List<KnowledgeArticle> getByCategory(String category) {
        return articleRepository.findByCategoryAndPublishedTrue(category);
    }

    public List<KnowledgeArticle> search(String query) {
        return articleRepository.search(query);
    }

    public List<String> getAllCategories() {
        return articleRepository.findAllCategories();
    }

    public KnowledgeArticle getArticle(Long id) {
        KnowledgeArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
        article.setViewCount(article.getViewCount() + 1);
        return articleRepository.save(article);
    }

    @Transactional
    public KnowledgeArticle createArticle(KnowledgeArticle article, String email) {
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        article.setAuthor(author);
        return articleRepository.save(article);
    }

    @Transactional
    public KnowledgeArticle updateArticle(Long id, KnowledgeArticle updated) {
        KnowledgeArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
        if (updated.getTitle() != null) article.setTitle(updated.getTitle());
        if (updated.getContent() != null) article.setContent(updated.getContent());
        if (updated.getCategory() != null) article.setCategory(updated.getCategory());
        if (updated.getTags() != null) article.setTags(updated.getTags());
        article.setPublished(updated.isPublished());
        return articleRepository.save(article);
    }

    @Transactional
    public void deleteArticle(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Article not found");
        }
        articleRepository.deleteById(id);
    }

    @Transactional
    public KnowledgeArticle vote(Long id, boolean helpful) {
        KnowledgeArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
        if (helpful) {
            article.setHelpful(article.getHelpful() + 1);
        } else {
            article.setNotHelpful(article.getNotHelpful() + 1);
        }
        return articleRepository.save(article);
    }
}
