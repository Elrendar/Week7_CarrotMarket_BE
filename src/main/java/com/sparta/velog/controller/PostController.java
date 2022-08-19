package com.sparta.velog.controller;

import com.sparta.velog.domain.PostEntity;
import com.sparta.velog.service.PostService;
import com.sparta.velog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/posts")
@RestController
public class PostController {
    private final PostService postService;

    @GetMapping("/posts/search")
    public Model search(String keyword, Model model) {
        List<PostEntity> searchList = postService.search(keyword);
        return model.addAttribute("searchList", searchList);
    }
}
