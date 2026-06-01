package com.csm.service;

import com.csm.model.Comment;
import com.csm.model.ServiceRequest;
import com.csm.model.User;
import com.csm.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository repo;

    @Transactional
    public Comment add(ServiceRequest req, User author, String content) {
        Comment c = new Comment();
        c.setServiceRequest(req);
        c.setAuthor(author);
        c.setContent(content.trim());
        return repo.save(c);
    }

    @Transactional(readOnly = true)
    public List<Comment> getForRequest(ServiceRequest req) {
        return repo.findByServiceRequestOrderByCreatedAtAsc(req);
    }
}
