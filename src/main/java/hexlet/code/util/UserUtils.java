package hexlet.code.util;

import hexlet.code.model.User;
import hexlet.code.repositories.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UserUtils {

    private final UserRepository userRepository;

    public UserUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        var email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> getUsernameNotFoundException(email));
    }

    public UsernameNotFoundException getUsernameNotFoundException(String email) {
        return new UsernameNotFoundException("User " + email + " not found");
    }

    public boolean isOwner(long id) {
        return getCurrentUser().getId() == id;
    }
}
