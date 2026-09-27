package com.example.jwt.domain.user;

import com.example.jwt.core.generic.ExtendedServiceImpl;
import com.example.jwt.domain.module.ModuleServiceClient;
import java.util.UUID;
import org.slf4j.Logger;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ExtendedServiceImpl<User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final ModuleServiceClient moduleServiceClient;

    public UserServiceImpl(
        UserRepository repository,
        Logger logger,
        PasswordEncoder passwordEncoder,
        ModuleServiceClient moduleServiceClient) {

        super(repository, logger);
        this.passwordEncoder = passwordEncoder;
        this.moduleServiceClient = moduleServiceClient;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return ((UserRepository) repository)
            .findByEmail(email)
            .map(UserDetailsImpl::new)
            .orElseThrow(() -> new UsernameNotFoundException(email));
    }

    @Override
    public User register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return save(user);
    }

    @Override
    public void assignModule(UUID userId, UUID moduleId) {
        findById(userId);

        moduleServiceClient.checkModule(moduleId);
        moduleServiceClient.assignModule(userId, moduleId);
    }
}
