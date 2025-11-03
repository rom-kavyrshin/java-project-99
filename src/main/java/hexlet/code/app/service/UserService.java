package hexlet.code.app.service;

import hexlet.code.app.component.CustomValidator;
import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.dto.UserDTO;
import hexlet.code.app.dto.UserUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.repositories.UserRepository;
import hexlet.code.app.util.UserUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsManager {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CustomValidator validator;
    private final UserUtils userUtils;


    public UserService(
            UserRepository userRepository,
            UserMapper userMapper,
            CustomValidator validator,
            UserUtils userUtils
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.validator = validator;
        this.userUtils = userUtils;
    }

    public List<UserDTO> getAll() {
        return userRepository.findAll().stream()
                .map(userMapper::map)
                .toList();
    }

    public UserDTO getById(long id) {
        return userRepository.findById(id)
                .map(userMapper::map)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
    }

    public UserDTO create(UserCreateDTO userCreateDTO) {
        validator.validate(userCreateDTO);

        var user = userMapper.map(userCreateDTO);
        userRepository.save(user);
        return userMapper.map(user);
    }

    @PreAuthorize("@userUtils.isOwner(#id)")
    public UserDTO update(long id, UserUpdateDTO userUpdateDTO) {
        validator.validate(userUpdateDTO);

        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        userMapper.update(userUpdateDTO, user);
        userRepository.save(user);

        return userMapper.map(user);
    }

    @PreAuthorize("@userUtils.isOwner(#id)")
    public void delete(long id) {
        userRepository.deleteById(id);
    }

    @Override
    public void createUser(UserDetails user) {

    }

    @Override
    public void updateUser(UserDetails user) {

    }

    @Override
    public void deleteUser(String username) {

    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {

    }

    @Override
    public boolean userExists(String username) {
        return false;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> userUtils.getUsernameNotFoundException(email));
    }
}
