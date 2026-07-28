package com.secondhand.user.bootstrap;

import com.secondhand.user.entity.Role;
import com.secondhand.user.entity.User;
import com.secondhand.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Seeds a default manager account on startup so the admin dashboard can be logged
 * into out of the box. Idempotent: it only creates the account when the configured
 * username does not already exist.
 *
 * <p>Configurable via {@code admin.bootstrap.*} (defaults username=admin,
 * password=admin123). The default password is intentionally weak for local use —
 * change or disable it (ADMIN_BOOTSTRAP_ENABLED=false) before any real deployment.
 */
@Component
public class AdminAccountBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountBootstrap.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${admin.bootstrap.enabled:true}")
    private boolean enabled;

    @Value("${admin.bootstrap.username:admin}")
    private String username;

    @Value("${admin.bootstrap.password:admin123}")
    private String password;

    @Value("${admin.bootstrap.email:admin@local}")
    private String email;

    public AdminAccountBootstrap(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates the default manager account if it is enabled and not already present.
     *
     * @param args startup arguments (unused)
     */
    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }
        // Email is unique; skip rather than crash startup if the placeholder is taken.
        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("Admin bootstrap skipped: email '{}' already in use.", email);
            return;
        }

        long now = Instant.now().getEpochSecond();
        User admin = new User();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(Role.MANAGER);
        admin.setEmailVerifiedAt(now);
        admin.setLastLoginAt(0);
        userRepository.save(admin);

        log.warn("Seeded default admin account (username='{}'). Change the password before deploying.", username);
    }
}
