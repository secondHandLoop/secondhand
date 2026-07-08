package com.secondhand.user.service;

import com.secondhand.common.entity.Status;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.common.output.ApiResponse;
import com.secondhand.user.dto.FollowUserResponse;
import com.secondhand.user.dto.UserResponseMapper;
import com.secondhand.user.entity.Follow;
import com.secondhand.user.entity.User;
import com.secondhand.user.repository.FollowRepository;
import com.secondhand.user.repository.UserRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manages follow relationships between users: following, unfollowing, listing a
 * user's following and followers, and checking follow status.
 */
@Service
public class FollowService {

    @Resource
    private FollowRepository followRepository;

    @Resource
    private UserRepository userRepository;

    /**
     * Makes the current user follow the target user, reactivating a previously
     * removed follow row instead of creating a duplicate.
     *
     * @param currentUserId the authenticated follower
     * @param targetUserId  the user to follow
     * @return an empty success response
     */
    public ApiResponse<Void> follow(Long currentUserId, Long targetUserId) {
        AssertUtil.notNull(currentUserId, "用户未登录");
        AssertUtil.notNull(targetUserId, "被关注用户不存在");
        AssertUtil.isFalse(currentUserId.equals(targetUserId), "不能关注自己");
        AssertUtil.isTrue(userRepository.existsById(targetUserId), "被关注用户不存在");

        Follow follow = followRepository.findByFollowerIdAndFolloweeId(currentUserId, targetUserId)
                .orElseGet(() -> {
                    Follow created = new Follow();
                    created.setFollowerId(currentUserId);
                    created.setFolloweeId(targetUserId);
                    return created;
                });
        follow.setStatus(Status.ACTIVE);
        follow.setUpdateAt(System.currentTimeMillis() / 1000);
        followRepository.save(follow);
        return ApiResponse.success();
    }

    /**
     * Makes the current user stop following the target user via a soft delete.
     *
     * @param currentUserId the authenticated follower
     * @param targetUserId  the user to unfollow
     * @return an empty success response
     */
    public ApiResponse<Void> unfollow(Long currentUserId, Long targetUserId) {
        AssertUtil.notNull(currentUserId, "用户未登录");
        Optional<Follow> existing = followRepository.findByFollowerIdAndFolloweeId(currentUserId, targetUserId);
        if (existing.isPresent()) {
            Follow follow = existing.get();
            follow.setStatus(Status.DEL);
            follow.setUpdateAt(System.currentTimeMillis() / 1000);
            followRepository.save(follow);
        }
        return ApiResponse.success();
    }

    /**
     * Lists the users the current user is following.
     *
     * @param currentUserId the authenticated user
     * @return each followed user, all flagged isFollowing=true
     */
    public ApiResponse<List<FollowUserResponse>> listFollowing(Long currentUserId) {
        AssertUtil.notNull(currentUserId, "用户未登录");
        List<Follow> follows = followRepository.findByFollowerIdAndStatus(currentUserId, Status.ACTIVE);
        Map<Long, User> users = loadUsers(follows.stream().map(Follow::getFolloweeId).collect(Collectors.toList()));
        List<FollowUserResponse> body = new ArrayList<>(follows.size());
        for (Follow follow : follows) {
            User user = users.get(follow.getFolloweeId());
            if (user == null) {
                continue;
            }
            body.add(UserResponseMapper.toFollowUser(user, true));
        }
        return ApiResponse.success(body);
    }

    /**
     * Lists the users who follow the current user, flagging which of them the
     * current user follows back.
     *
     * @param currentUserId the authenticated user
     * @return each follower with isFollowing reflecting a mutual follow
     */
    public ApiResponse<List<FollowUserResponse>> listFollowers(Long currentUserId) {
        AssertUtil.notNull(currentUserId, "用户未登录");
        List<Follow> followers = followRepository.findByFolloweeIdAndStatus(currentUserId, Status.ACTIVE);

        Set<Long> followingBack = new HashSet<>();
        for (Follow f : followRepository.findByFollowerIdAndStatus(currentUserId, Status.ACTIVE)) {
            followingBack.add(f.getFolloweeId());
        }

        Map<Long, User> users = loadUsers(followers.stream().map(Follow::getFollowerId).collect(Collectors.toList()));
        List<FollowUserResponse> body = new ArrayList<>(followers.size());
        for (Follow follow : followers) {
            User user = users.get(follow.getFollowerId());
            if (user == null) {
                continue;
            }
            body.add(UserResponseMapper.toFollowUser(user, followingBack.contains(user.getId())));
        }
        return ApiResponse.success(body);
    }

    /**
     * Batch-loads users by id into a map, avoiding a per-row query (N+1).
     *
     * @param ids the user ids to load
     * @return a map from user id to the loaded user, empty when no ids are given
     */
    private Map<Long, User> loadUsers(List<Long> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
    }

    /**
     * Reports whether the current user is following the target user.
     *
     * @param currentUserId the authenticated user
     * @param targetUserId  the user to check
     * @return true when an active follow exists
     */
    public ApiResponse<Boolean> isFollowing(Long currentUserId, Long targetUserId) {
        AssertUtil.notNull(currentUserId, "用户未登录");
        boolean following = followRepository
                .existsByFollowerIdAndFolloweeIdAndStatus(currentUserId, targetUserId, Status.ACTIVE);
        return ApiResponse.success(following);
    }
}
