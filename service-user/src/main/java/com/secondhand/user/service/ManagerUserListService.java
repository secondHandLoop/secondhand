package com.secondhand.user.service;

import com.secondhand.common.entity.Status;
import com.secondhand.common.output.ApiResponse;
import com.secondhand.user.dto.ManagerUserListItemResponse;
import com.secondhand.common.output.PageInfoResponse;
import com.secondhand.user.dto.UserResponseMapper;
import com.secondhand.user.entity.Role;
import com.secondhand.user.entity.User;
import com.secondhand.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Provides paginated user search for manager dashboards.
 */
@Service
public class ManagerUserListService {

    private final UserRepository userRepository;

    public ManagerUserListService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns a filtered page of users with pagination metadata for admins.
     */
    public ApiResponse<List<ManagerUserListItemResponse>> listUsers(Integer page, Integer limit, String keyword, String role, String status) {
        int pageNo = (page == null || page < 1) ? 1 : page;
        int pageSize = (limit == null || limit < 1) ? 20 : limit;

        Specification<User> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                String likeValue = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("email")), likeValue),
                        cb.like(cb.lower(root.get("username")), likeValue),
                        cb.like(cb.lower(root.get("phone")), likeValue)
                ));
            }

            if (StringUtils.hasText(role)) {
                predicates.add(cb.equal(root.get("role"), Role.valueOf(role.trim().toUpperCase(Locale.ROOT))));
            }

            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), Status.valueOf(status.trim().toUpperCase(Locale.ROOT))));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> result = userRepository.findAll(
                specification,
                PageRequest.of(pageNo - 1, pageSize, Sort.by(Sort.Direction.DESC, "updateAt"))
        );

        List<ManagerUserListItemResponse> body = new ArrayList<>();
        for (User user : result.getContent()) {
            body.add(UserResponseMapper.toManagerUserListItem(user));
        }

        PageInfoResponse pageInfo = new PageInfoResponse();
        pageInfo.setPage(pageNo);
        pageInfo.setLimit(pageSize);
        pageInfo.setTotal(result.getTotalElements());

        return ApiResponse.success(body, pageInfo);
    }
}
