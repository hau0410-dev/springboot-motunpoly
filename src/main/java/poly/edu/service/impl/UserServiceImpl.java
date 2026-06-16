package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import poly.edu.entity.User;
import poly.edu.repository.UserRepository;
import poly.edu.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepo;

    

    @Override
    public User login(String username, String password) {
        User user = userRepo.findByUsername(username);
        if (user != null && password.equals(user.getPassword())) {

            return user;
        }
        return null;
    }

    @Override
    public User register(User user) {
      
        return userRepo.save(user);
    }
    @Override
    public User findByUsername(String username) {
        return userRepo.findByUsername(username);
    }
    @Override
    public User findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    @Override
    public User update(User user) {
        return userRepo.save(user);
    }
}
