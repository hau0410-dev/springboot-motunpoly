package poly.edu.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import poly.edu.repository.OrderRepository;
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

    @Autowired
    private OrderRepository orderRepo;

    @Value("${sepay.bank.account}")
    private String bankAccount;

    @Value("${sepay.bank.name}")
    private String bankName;

    @Value("${sepay.bank.holder}")
    private String bankHolder;


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

    @PostMapping("/buynow/{id}")
    public String buyNow(
            @PathVariable("id") Integer id,
            @RequestParam(value = "qty", defaultValue = "1") int qty,
            HttpSession session) {

        Product p = productService.findById(id);

        if (p == null) {
            return "redirect:/";
        }

        if (p.getStock() == null) {
            p.setStock(0);
        }

        if (qty < 1) {
            qty = 1;
        }

        if (p.getStock() <= 0) {
            session.setAttribute(
                "stockMessage",
                p.getName() + " hiện đã hết hàng!"
            );
            return "redirect:/product/detail?id=" + id;
        }

        if (qty > p.getStock()) {
            qty = p.getStock();
        }

        CartItem item = new CartItem();
        item.setProductId(p.getId());
        item.setProductName(p.getName());
        item.setPrice(p.getPrice());
        item.setImage(p.getImage());
        item.setQuantity(qty);
        item.setStock(p.getStock());

        Cart buyNowCart = new Cart();
        buyNowCart.add(item);

        session.setAttribute("buyNowCart", buyNowCart);

        return "redirect:/cart/checkout?mode=buynow";
    }

    @GetMapping("/remove/{id}")
    public String remove(@PathVariable("id") Integer id, HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart != null) {
            cart.remove(id);
        }
        return "redirect:/cart";
    }

    @PostMapping("/update/{id}")
    public String updateQuantity(
            @PathVariable("id") Integer id,
            @RequestParam("qty") int qty,
            HttpSession session) {

        Cart cart = (Cart) session.getAttribute("cart");

        if (cart == null) {
            return "redirect:/cart";
        }

        if (qty < 1) {
            qty = 1;
        }

        Product p = productService.findById(id);

        if (p != null) {
            int stock = (p.getStock() == null) ? 0 : p.getStock();

            if (qty > stock) {
                qty = stock;

                session.setAttribute(
                    "stockMessage",
                    p.getName() + " chỉ còn " + stock + " sản phẩm trong kho!"
                );
            }

            if (qty < 1) {
                cart.remove(id);
                return "redirect:/cart";
            }
        }

        cart.update(id, qty);

        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String checkout(
            @RequestParam(value = "mode", required = false) String mode,
            HttpSession session, Model model) {

        Cart cart;

        if ("buynow".equals(mode)) {
            cart = (Cart) session.getAttribute("buyNowCart");

            if (cart == null || cart.isEmpty()) {
                return "redirect:/";
            }

        } else {
            cart = (Cart) session.getAttribute("cart");

            if (cart == null || cart.isEmpty()) {
                return "redirect:/cart";
            }

            mode = "cart";
        }

        model.addAttribute("cart", cart);
        model.addAttribute("mode", mode);

        User user = (User) session.getAttribute("user");
        if (user != null) {
            Payment lastBanking = paymentService.findLastBankingInfo(user.getId());
            if (lastBanking != null) {
                model.addAttribute("savedBank", lastBanking.getCustomerBank());
                model.addAttribute("savedAccount", lastBanking.getCustomerAccount());
            }
            model.addAttribute("savedFullname", user.getFullName());
            model.addAttribute("savedEmail", user.getEmail());
        }

        return "cart/checkout";
    }

    @PostMapping("/pay")
    public String pay(
    		@RequestParam("paymentMethod") String paymentMethod,
            @RequestParam("fullname") String fullname,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            @RequestParam(value = "customerBank", required = false) String customerBank,
            @RequestParam(value = "customerAccount", required = false) String customerAccount,
            @RequestParam(value = "mode", required = false) String mode,
            HttpSession session) {

        boolean isBuyNow = "buynow".equals(mode);

        Cart cart = (Cart) session.getAttribute(isBuyNow ? "buyNowCart" : "cart");

        if (cart == null || cart.isEmpty()) {
            return isBuyNow ? "redirect:/" : "redirect:/cart";
        }

        for (CartItem c : cart.getItems()) {

            Product product =
                    productService.findById(c.getProductId());

            if (product == null) {

                session.setAttribute(
                        "stockMessage",
                        "Sản phẩm không tồn tại!"
                );

                return isBuyNow ? "redirect:/" : "redirect:/cart";
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

                return isBuyNow ? "redirect:/" : "redirect:/cart";
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
            payment.setPaymentStatus("CHO_THANH_TOAN");
            payment.setCustomerBank(customerBank);
            payment.setCustomerAccount(customerAccount);
            // order.getId() đã có giá trị ngay sau orderService.save() (GenerationType.IDENTITY)
            payment.setPaymentContent("DH" + order.getId());
        }

        payment.setPaymentDate(LocalDateTime.now());

        paymentService.save(payment);

        for (CartItem c : cart.getItems()) {

            OrderItem item = new OrderItem();

            Product product =
                    productService.findById(c.getProductId());

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

        if (isBuyNow) {
            session.removeAttribute("buyNowCart");
        } else {
            session.removeAttribute("cart");
        }

        // ===== BANKING: đưa khách sang trang chờ quét QR thay vì báo thành công ngay =====
        if ("BANKING".equals(paymentMethod)) {
            return "redirect:/cart/payment-waiting/" + order.getId();
        }

        return "cart/success";
    }

    // ===== TRANG CHỜ THANH TOÁN QUA QR (SePay) =====
    @GetMapping("/payment-waiting/{orderId}")
    public String paymentWaiting(@PathVariable("orderId") Integer orderId, HttpSession session, Model model) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        Order order = orderRepo.findById(orderId).orElse(null);

        // Chặn xem trang chờ thanh toán của đơn hàng người khác (kiểm tra quyền sở hữu)
        if (order == null || order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            return "redirect:/orders";
        }

        Payment payment = paymentService.findByOrderId(orderId);

        if (payment != null && "THANH_CONG".equals(payment.getPaymentStatus())) {
            return "redirect:/orders/" + orderId;
        }

        model.addAttribute("order", order);
        model.addAttribute("payment", payment);
        model.addAttribute("bankAccount", bankAccount);
        model.addAttribute("bankName", bankName);
        model.addAttribute("bankHolder", bankHolder);

        return "cart/payment-waiting";
    }

}