package hexlet.code.app.controllers;

import hexlet.code.app.model.User;
import hexlet.code.app.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping(path = "/users")
    public List<User> users() {
        return userRepository.findAll();
    }

    @PostMapping(path = "/users")
    public User createUser() {
        var user = new User();
        counter++;
        user.setFirstName("Name" + counter);
        user.setLastName("LastName" + counter);
        user.setEmail("email" + counter);
        return userRepository.save(user);
    }

    long counter = 0;

}
