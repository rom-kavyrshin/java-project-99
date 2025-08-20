package hexlet.code.app.service;

import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.dto.UserDTO;
import hexlet.code.app.dto.UserUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.repositories.UserRepository;
import hexlet.code.app.component.CustomValidator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CustomValidator validator;


    public UserService(UserRepository userRepository, UserMapper userMapper, CustomValidator validator) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.validator = validator;
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

    public UserDTO update(long id, UserUpdateDTO userUpdateDTO) {
        validator.validate(userUpdateDTO);

        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        userMapper.update(userUpdateDTO, user);
        userRepository.save(user);

        return userMapper.map(user);
    }

    public void delete(long id) {
        userRepository.deleteById(id);
    }
}
