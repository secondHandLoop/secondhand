package com.secondhand.user.repository;
import com.secondhand.common.entity.Status;
import com.secondhand.user.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    List<Follow> findByFollowerIdAndStatus(Long followerId, Status status);

    List<Follow> findByFolloweeIdAndStatus(Long followeeId, Status status);

    boolean existsByFollowerIdAndFolloweeIdAndStatus(Long followerId, Long followeeId, Status status);
}
