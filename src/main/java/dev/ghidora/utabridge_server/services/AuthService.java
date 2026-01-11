package dev.ghidora.utabridge_server.services;


import dev.ghidora.utabridge_server.models.User;
import dev.ghidora.utabridge_server.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final IdentityTokenVerifier identityTokenVerifier;
    private final UserRepository userRepository;

    public AuthService(IdentityTokenVerifier identityTokenVerifier, UserRepository userRepository) {
        this.identityTokenVerifier = identityTokenVerifier;
        this.userRepository = userRepository;
    }

    public User getOrCreateUser(String email, String name, String pictureUrl, String providerId) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setPictureUrl(pictureUrl);
            user.setProviderId(providerId);
            user.setProvider(identityTokenVerifier.getProvider());
            return userRepository.save(user);
        });
    }
}
