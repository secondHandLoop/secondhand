package com.secondhand.user.service;

import com.secondhand.common.entity.Status;
import com.secondhand.common.output.ApiResponseHelper;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ManagerUserListService extends ApiResponseHelper {

    private final UserRepository userRepository;

    public ManagerUserListService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Map<String, Object> service(Integer page, Integer limit, String keyword, String role, String status) {
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

        List<Map<String, Object>> body = new ArrayList<>();
        for (User user : result.getContent()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", user.getId());
            item.put("email", user.getEmail());
            item.put("username", user.getUsername());
            item.put("phone", user.getPhone());
            item.put("profilePhoto", user.getProfilePhoto());
            item.put("score", user.getScore());
            item.put("description", user.getDescription());
            item.put("role", user.getRole());
            item.put("status", user.getStatus());
            item.put("emailVerifiedAt", user.getEmailVerifiedAt());
            item.put("lastLoginAt", user.getLastLoginAt());
            item.put("createAt", user.getCreateAt());
            item.put("updateAt", user.getUpdateAt());
            body.add(item);
        }

        Map<String, Object> pageInfo = new LinkedHashMap<>();
        pageInfo.put("page", pageNo);
        pageInfo.put("limit", pageSize);
        pageInfo.put("total", result.getTotalElements());

        return success(body, pageInfo);
    }
}
