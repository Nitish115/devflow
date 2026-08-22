package com.devflow.devflow.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentMentionRepository extends JpaRepository<CommentMention, Long> {

    List<CommentMention> findAllByCommentId(Long commentId);

    void deleteAllByCommentId(Long commentId);
}
