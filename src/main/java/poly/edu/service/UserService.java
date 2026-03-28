package poly.edu.service;

import poly.edu.entity.User;

public interface UserService {
    User login(String username, String password);
    User register(User user);
    User findByUsername(String username);
    User update(User user);
}
