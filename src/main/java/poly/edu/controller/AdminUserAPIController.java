package poly.edu.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import poly.edu.entity.User;
import poly.edu.repository.UserRepository;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserAPIController {

    @Autowired
    private UserRepository userRepo;

    // ===== 1. DANH SÁCH ===== (giống index())
    @GetMapping
    public List<User> list() {
        return userRepo.findAll();
    }

    // ===== 2. CHI TIẾT ===== (giống edit())
    @GetMapping("/{id}")
    public User getById(@PathVariable("id") Integer id) {
        return userRepo.findById(id).orElse(null);
    }

    // ===== 3. CREATE ===== (giống save())
    @PostMapping
    public User create(@RequestBody User user) {
        return userRepo.save(user);
    }

    // ===== 4. UPDATE =====
    @PutMapping("/{id}")
    public User update(@PathVariable("id") Integer id, @RequestBody User user) {
        User old = userRepo.findById(id).orElse(null);

        if (old != null) {
            user.setId(id);
            return userRepo.save(user);
        }
        return null;
    }

    // ===== 5. DELETE ===== (giống delete())
    @DeleteMapping("/{id}")
    public String delete(@PathVariable("id") Integer id) {
        userRepo.deleteById(id);
        return "Deleted successfully";
    }
}