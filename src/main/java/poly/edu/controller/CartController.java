package poly.edu.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.ui.Model;

import jakarta.servlet.http.HttpSession;
import poly.edu.entity.Order;
import poly.edu.entity.OrderItem;
import poly.edu.entity.Product;
import poly.edu.entity.User;
import poly.edu.model.Cart;
import poly.edu.model.CartItem;
import poly.edu.repository.OrderItemRespository;
import poly.edu.service.OrderService;
import poly.edu.service.ProductService;

import poly.edu.entity.Payment;
import poly.edu.service.PaymentService;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ProductService productService;
    @Autowired
    private OrderService orderService;

    
    @Autowired
    private OrderItemRespository orderItemRepo;
    
    @Autowired
    private PaymentService paymentService;
    
    
    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        Cart cart = (Cart) session.getAttribute("cart");

        if (cart == null) {
            cart = new Cart();
            session.setAttribute("cart", cart);
        }

        model.addAttribute("cart", cart);
        return "cart/index";
    }

    @PostMapping("/add/{id}")
    public String addToCart(
            @PathVariable("id") Integer id,
            @RequestParam(value = "qty", defaultValue = "1") int qty,
            HttpSession session) {

        Cart cart = (Cart) session.getAttribute("cart");

        if (cart == null) {
            cart = new Cart();
            session.setAttribute("cart", cart);
        }

        Product p = productService.findById(id);

        if (p == null) {
            return "redirect:/";
        }

        // ===== KIỂM TRA TỒN KHO =====
        if (p.getStock() == null) {
            p.setStock(0);
        }

        if (qty > p.getStock()) {
            session.setAttribute(
                "stockMessage",
                "Sản phẩm chỉ còn " + p.getStock() + " trong kho!"
            );

            return "redirect:/cart";
        }

        CartItem item = new CartItem();
        item.setProductId(p.getId());
        item.setProductName(p.getName());
        item.setPrice(p.getPrice());
        item.setImage(p.getImage());
        item.setQuantity(qty);
        item.setStock(p.getStock());

        cart.add(item);

        return "redirect:/cart";
    }
    @GetMapping("/remove/{id}")
    public String remove(@PathVariable("id") Integer id, HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart != null) {
            cart.remove(id);
        }
        return "redirect:/cart";
        
    }
    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        Cart cart = (Cart) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {
            return "redirect:/cart";
        }

        model.addAttribute("cart", cart);
        return "cart/checkout";
    }
    @PostMapping("/pay")
    public String pay(
    		@RequestParam("paymentMethod") String paymentMethod,
            @RequestParam("fullname") String fullname,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            HttpSession session) {

        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            return "redirect:/cart";
        }
     // ===== KIỂM TRA TOÀN BỘ TỒN KHO TRƯỚC =====

        for (CartItem c : cart.getItems()) {

            Product product =
                    productService.findById(c.getProductId());

            if (product == null) {

                session.setAttribute(
                        "stockMessage",
                        "Sản phẩm không tồn tại!"
                );

                return "redirect:/cart";
            }

            if (product.getStock() == null) {
                product.setStock(0);
            }

            if (product.getStock() < c.getQuantity()) {

                session.setAttribute(
                        "stockMessage",
                        product.getName()
                        + " chỉ còn "
                        + product.getStock()
                        + " sản phẩm trong kho!"
                );

                return "redirect:/cart";
            }
        }
        
        // ===== 1. TẠO ORDER =====
        
        Order order = new Order();

        User user = (User) session.getAttribute("user");

        if(user != null){
            order.setUser(user);
        }

        order.setFullname(fullname);
        order.setEmail(email);
        order.setPhone(phone);
        order.setAddress(address);
        order.setTotalAmount(cart.getTotalAmount());
        order.setStatus("CHO_XAC_NHAN");
        order.setCreatedDate(LocalDateTime.now());
       

        orderService.save(order);
        
        Payment payment = new Payment();

        payment.setOrder(order);
        payment.setPaymentMethod(paymentMethod);

        if("COD".equals(paymentMethod)){
            payment.setPaymentStatus("CHO_THANH_TOAN");
        }

        if("BANKING".equals(paymentMethod)){
            payment.setPaymentStatus("CHO_XAC_NHAN");
        }

        payment.setPaymentDate(LocalDateTime.now());

        paymentService.save(payment);
        
        for (CartItem c : cart.getItems()) {

            OrderItem item = new OrderItem();

            Product product =
                    productService.findById(c.getProductId());

            // ===== TRỪ TỒN KHO =====

            product.setStock(
                    product.getStock() - c.getQuantity()
            );

            productService.save(product);

            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(c.getQuantity());
            item.setPrice(c.getPrice());
            item.setSubtotal(c.getQuantity() * c.getPrice());
            

            orderItemRepo.save(item);
        }

        session.removeAttribute("cart"); // clear giỏ hàng

        return "cart/success";
    }

}
