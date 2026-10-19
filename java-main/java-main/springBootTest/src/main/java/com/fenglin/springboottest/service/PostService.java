package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.dto.PostForm;
import com.fenglin.springboottest.dto.PostVO;
import com.fenglin.springboottest.entity.Post;
import com.fenglin.springboottest.entity.PostLike;
import com.fenglin.springboottest.mapper.PostLikeMapper;
import com.fenglin.springboottest.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文章相关业务：列表、新增、编辑、删除、点赞
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final PostLikeMapper postLikeMapper;

    /**
     * 查询全部文章（按时间倒序），并把点赞用户拼装成 likes 数组
     */
    public List<PostVO> listPosts() {
        List<Post> posts = postMapper.selectList(
                new QueryWrapper<Post>().orderByDesc("ts"));
        if (posts.isEmpty()) {
            return List.of();
        }
        // 一次性查出所有点赞记录，按 post_id 分组
        Map<Long, List<String>> likeMap = postLikeMapper.selectList(null)
                .stream()
                .collect(Collectors.groupingBy(
                        PostLike::getPostId,
                        Collectors.mapping(PostLike::getUsername, Collectors.toList())));

        return posts.stream().map(p -> {
            PostVO vo = new PostVO();
            vo.setId(p.getId());
            vo.setAuthor(p.getAuthor());
            vo.setEmoji(p.getEmoji());
            vo.setTitle(p.getTitle());
            vo.setBody(p.getBody());
            vo.setTs(p.getTs());
            vo.setLikes(likeMap.getOrDefault(p.getId(), List.of()));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 新增文章
     */
    public PostVO addPost(String author, PostForm form) {
        Post post = new Post();
        post.setAuthor(author);
        fillAndCheck(post, form);
        post.setTs(System.currentTimeMillis());
        postMapper.insert(post);
        return listPosts().stream()
                .filter(p -> p.getId().equals(post.getId()))
                .findFirst()
                .orElseThrow(() -> new BizException("文章不见了喵"));
    }

    /**
     * 编辑文章（仅作者本人可编辑）
     */
    public void updatePost(String username, Long id, PostForm form) {
        Post post = getOwnedPost(username, id);
        fillAndCheck(post, form);
        postMapper.updateById(post);
    }

    /**
     * 删除文章（仅作者本人可删除），同时清理点赞记录
     */
    @Transactional
    public void deletePost(String username, Long id) {
        getOwnedPost(username, id);
        postMapper.deleteById(id);
        postLikeMapper.delete(
                new QueryWrapper<PostLike>().eq("post_id", id));
    }

    /**
     * 点赞 / 取消点赞（切换），返回最新点赞用户列表
     */
    @Transactional
    public List<String> toggleLike(String username, Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BizException("文章不见了喵");
        }
        PostLike exist = postLikeMapper.selectOne(
                new QueryWrapper<PostLike>()
                        .eq("post_id", id)
                        .eq("username", username));
        if (exist != null) {
            postLikeMapper.deleteById(exist.getId());
        } else {
            PostLike like = new PostLike();
            like.setPostId(id);
            like.setUsername(username);
            postLikeMapper.insert(like);
        }
        return postLikeMapper.selectList(
                        new QueryWrapper<PostLike>().eq("post_id", id))
                .stream()
                .map(PostLike::getUsername)
                .collect(Collectors.toList());
    }

    /** 校验并填充标题 / 正文 / 表情 */
    private void fillAndCheck(Post post, PostForm form) {
        if (form == null
                || form.getTitle() == null || form.getTitle().trim().isEmpty()
                || form.getBody() == null || form.getBody().trim().isEmpty()) {
            throw new BizException("标题和正文都要写哦～");
        }
        post.setTitle(form.getTitle().trim());
        post.setBody(form.getBody().trim());
        if (form.getEmoji() != null && !form.getEmoji().isEmpty()) {
            post.setEmoji(form.getEmoji());
        } else {
            post.setEmoji("🐱");
        }
    }

    /** 取文章并校验归属 */
    private Post getOwnedPost(String username, Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BizException("文章不见了喵");
        }
        if (!post.getAuthor().equals(username)) {
            throw new BizException("只能操作自己的文章喵～");
        }
        return post;
    }
}
