package poly.edu.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import poly.edu.entity.Order;
import poly.edu.entity.OrderItem;
import poly.edu.entity.Product;
import poly.edu.model.Cart;
import poly.edu.model.CartItem;
import poly.edu.repository.OrderItemRespository;
import poly.edu.service.OrderService;
import poly.edu.service.ProductService;

@RestController
@RequestMapping("/api/cart")
public class CartAPIController {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemRespository orderItemRepo;

    // ===== 1. XEM CART =====
    @GetMapping
    public Cart viewCart(HttpSession session) {

        Cart cart = (Cart) session.getAttribute("cart");

        if (cart == null) {
            cart = new Cart();
            session.setAttribute("cart", cart);
        }

        return cart;
    }

    // ===== 2. ADD TO CART =====
    @PostMapping("/add/{id}")
    public Object addToCart(@PathVariable("id") Integer id,
                            @RequestParam(value = "qty", defaultValue = "1") int qty,
                            HttpSession session) {

        Cart cart = (Cart) session.getAttribute("cart");

        if (cart == null) {
            cart = new Cart();
            session.setAttribute("cart", cart);
        }

        Product p = productService.findById(id);

        if (p == null) {
            return "Product không tồn tại";
        }

        CartItem item = new CartItem();
        item.setProductId(p.getId());
        item.setProductName(p.getName());
        item.setPrice(p.getPrice());
        item.setImage(p.getImage());
        item.setQuantity(qty);

        cart.add(item);

        return cart;
    }

    // ===== 3. REMOVE =====
    @DeleteMapping("/remove/{id}")
    public Cart remove(@PathVariable("id") Integer id, HttpSession session) {

        Cart cart = (Cart) session.getAttribute("cart");

        if (cart != null) {
            cart.remove(id);
        }

        return cart;
    }

    // ===== 4. CHECKOUT (xem trước) =====
    @GetMapping("/checkout")
    public Object checkout(HttpSession session) {

        Cart cart = (Cart) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {
            return "Cart rỗng";
        }

        return cart;
    }

    // ===== 5. PAY =====
    @PostMapping("/pay")
    public String pay(@RequestParam("fullname") String fullname,
                      @RequestParam("email") String email,
                      @RequestParam("phone") String phone,
                      @RequestParam("address") String address,
                      HttpSession session) {

        Cart cart = (Cart) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {
            return "Cart rỗng";
        }

        // ===== TẠO ORDER =====
        Order order = new Order();
        order.setFullname(fullname);
        order.setEmail(email);
        order.setPhone(phone);
        order.setAddress(address);
        order.setTotalAmount(cart.getTotalAmount());
        order.setStatus("ĐANG_GIAO");
        order.setCreatedDate(LocalDateTime.now());

        orderService.save(order);

        // ===== TẠO ORDER ITEM =====
        for (CartItem c : cart.getItems()) {

            OrderItem item = new OrderItem();

            Product product = productService.findById(c.getProductId());

            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(c.getQuantity());
            item.setPrice(c.getPrice());
            item.setSubtotal(c.getQuantity() * c.getPrice());

            orderItemRepo.save(item);
        }

        session.removeAttribute("cart");

        return "Đặt hàng thành công";
    }
}