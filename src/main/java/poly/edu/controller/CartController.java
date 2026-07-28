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
import poly.edu.entity.Promotion;
import poly.edu.entity.User;
import poly.edu.model.Cart;
import poly.edu.model.CartItem;
import poly.edu.repository.OrderItemRespository;
import poly.edu.repository.OrderRepository;
import poly.edu.service.OrderService;
import poly.edu.service.ProductService;
import poly.edu.service.PromotionService;
import poly.edu.service.ShippingService;
import poly.edu.service.GhnService;
import poly.edu.config.GhnProperties;

import poly.edu.entity.Payment;
import poly.edu.service.PaymentService;

import java.util.List;

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

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private ShippingService shippingService;

    @Autowired
    private GhnService ghnService;

    @Autowired
    private GhnProperties ghnProperties;

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

        Integer userId = currentUserId(session);
        refreshCartPromotions(cart, userId);

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

        Integer userId = currentUserId(session);

        CartItem existed = cart.getItem(id);
        int existingQty = (existed != null) ? existed.getQuantity() : 0;
        int wantQty = existingQty + qty;

        CartItem preview = new CartItem();
        preview.setProductId(id);
        preview.setQuantity(wantQty);
        applyPromotion(preview, userId);

        int needed = wantQty + preview.getBonusQuantity();

        if (needed > p.getStock()) {
            session.setAttribute(
                "stockMessage",
                "Sản phẩm chỉ còn " + p.getStock() + " trong kho"
                + (preview.getBonusQuantity() > 0 ? " (chương trình mua 1 tặng 1 cần gấp đôi số lượng tồn kho)!" : "!")
            );

            return "redirect:/cart";
        }

        CartItem item = new CartItem();
        item.setProductId(p.getId());
        item.setProductName(p.getName());
        item.setImage(p.getImage());
        item.setQuantity(qty);
        item.setStock(p.getStock());
        applyPromotion(item, userId);

        cart.add(item);

        CartItem merged = cart.getItem(id);
        if (merged != null) {
            applyPromotion(merged, userId);
        }

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

        Integer userId = currentUserId(session);

        CartItem item = new CartItem();
        item.setProductId(p.getId());
        item.setProductName(p.getName());
        item.setImage(p.getImage());
        item.setQuantity(qty);
        item.setStock(p.getStock());
        applyPromotion(item, userId);

        int needed = item.getQuantity() + item.getBonusQuantity();
        if (needed > p.getStock()) {
            int maxPaidQty = (item.getBonusQuantity() > 0) ? p.getStock() / 2 : p.getStock();
            item.setQuantity(Math.max(maxPaidQty, 0));
            applyPromotion(item, userId);
        }

        if (item.getQuantity() <= 0) {
            session.setAttribute(
                "stockMessage",
                p.getName() + " hiện đã hết hàng!"
            );
            return "redirect:/product/detail?id=" + id;
        }

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
        Integer userId = currentUserId(session);

        if (p != null) {
            int stock = (p.getStock() == null) ? 0 : p.getStock();

            CartItem preview = new CartItem();
            preview.setProductId(id);
            preview.setQuantity(qty);
            applyPromotion(preview, userId);

            int maxPaidQty = (preview.getBonusQuantity() > 0) ? stock / 2 : stock;

            if (qty > maxPaidQty) {
                qty = maxPaidQty;

                session.setAttribute(
                    "stockMessage",
                    p.getName() + " chỉ còn " + stock + " sản phẩm trong kho"
                    + (preview.getBonusQuantity() > 0 ? " (chương trình mua 1 tặng 1 cần gấp đôi số lượng tồn kho)!" : "!")
                );
            }

            if (qty < 1) {
                cart.remove(id);
                return "redirect:/cart";
            }
        }

        cart.update(id, qty);

        CartItem updated = cart.getItem(id);
        if (updated != null) {
            applyPromotion(updated, userId);
        }

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

        Integer userId = currentUserId(session);
        refreshCartPromotions(cart, userId);

        model.addAttribute("cart", cart);
        model.addAttribute("mode", mode);

        // ===== THÔNG TIN PHÍ SHIP =====
        boolean freeShip = promotionService.isFreeShipEligible(userId);
        model.addAttribute("freeShip", freeShip);
        model.addAttribute("districts", shippingService.getSupportedDistricts());
        model.addAttribute("shippingFees", shippingService.getFeeTable());

        // FIX: THÊM MỚI - danh sách tỉnh/thành thật từ GHN + mã tỉnh TP.HCM để FE so sánh
        model.addAttribute("ghnProvinces", ghnService.getProvinces());
        model.addAttribute("tphcmProvinceId", ghnProperties.getTphcmProvinceId());

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
            @RequestParam(value = "district", required = false) String district,
            @RequestParam(value = "provinceId", required = false) Integer provinceId,
            @RequestParam(value = "toDistrictId", required = false) Integer toDistrictId,
            @RequestParam(value = "toWardCode", required = false) String toWardCode,
            @RequestParam(value = "toProvinceName", required = false) String toProvinceName,
            @RequestParam(value = "toDistrictName", required = false) String toDistrictName,
            @RequestParam(value = "toWardName", required = false) String toWardName,
            @RequestParam(value = "customerBank", required = false) String customerBank,
            @RequestParam(value = "customerAccount", required = false) String customerAccount,
            @RequestParam(value = "mode", required = false) String mode,
            HttpSession session) {

        boolean isBuyNow = "buynow".equals(mode);

        Cart cart = (Cart) session.getAttribute(isBuyNow ? "buyNowCart" : "cart");

        if (cart == null || cart.isEmpty()) {
            return isBuyNow ? "redirect:/" : "redirect:/cart";
        }

        Integer userId = currentUserId(session);

        refreshCartPromotions(cart, userId);

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

            int needed = c.getQuantity() + c.getBonusQuantity();

            if (product.getStock() < needed) {

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

        double originalAmount = cart.getOriginalTotalAmount();
        double chargeAmount = cart.getTotalAmount();
        double discountAmount = cart.getDiscountAmount();

        // FIX: xác định TP.HCM hay ngoài tỉnh -> quyết định phương thức giao + có áp ưu đãi hay không
        boolean isTphcm = (provinceId == null) || ghnService.isTPHCM(provinceId);

        long shippingFee;
        String shippingMethod;

        if (isTphcm) {
            // ===== GIỮ NGUYÊN LUỒNG CŨ: shipper nội bộ + bảng phí quận + ưu đãi freeship =====
            shippingMethod = "NOI_BO";
            boolean freeShip = promotionService.isFreeShipEligible(userId);
            shippingFee = shippingService.calculateFee(district, freeShip);
        } else {
            // ===== NGOÀI TP.HCM: GHN, KHÔNG áp ưu đãi freeship =====
            shippingMethod = "GHN";
            if (toDistrictId == null || toWardCode == null) {
                session.setAttribute("stockMessage", "Vui lòng chọn đầy đủ Tỉnh/Thành và Phường/Xã để tính phí ship.");
                return "redirect:/cart/checkout" + (isBuyNow ? "?mode=buynow" : "");
            }
            int totalQty = cart.getItems().stream().mapToInt(CartItem::getQuantity).sum();
            int weightGram = ghnProperties.getDefaultItemWeightGram() * Math.max(totalQty, 1);
            try {
                shippingFee = ghnService.calculateFee(toDistrictId, toWardCode, weightGram);
            } catch (Exception e) {
                session.setAttribute("stockMessage", "Không tính được phí ship GHN: " + e.getMessage());
                return "redirect:/cart/checkout" + (isBuyNow ? "?mode=buynow" : "");
            }
        }

        double finalTotal = chargeAmount + shippingFee;

        Order order = new Order();

        User user = (User) session.getAttribute("user");

        if(user != null){
            order.setUser(user);
        }

        order.setFullname(fullname);
        order.setEmail(email);
        order.setPhone(phone);
        order.setAddress(address);
        order.setDistrict(isTphcm ? district : null);
        order.setOriginalAmount(originalAmount);
        order.setDiscountAmount(discountAmount);
        order.setShippingFee((double) shippingFee);
        order.setTotalAmount(finalTotal);
        order.setStatus("CHO_XAC_NHAN");
        order.setCreatedDate(LocalDateTime.now());

        // FIX: lưu thông tin mô hình lai
        order.setShippingMethod(shippingMethod);
        if (!isTphcm) {
            order.setGhnToProvinceId(provinceId);
            order.setGhnToDistrictId(toDistrictId);
            order.setGhnToWardCode(toWardCode);
            order.setGhnFee((double) shippingFee);
            order.setGhnToProvinceName(toProvinceName);
            order.setGhnToDistrictName(toDistrictName);
            order.setGhnToWardName(toWardName);
        }

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
            payment.setPaymentContent("DH" + order.getId());
        }

        payment.setPaymentDate(LocalDateTime.now());

        paymentService.save(payment);

        for (CartItem c : cart.getItems()) {

            OrderItem item = new OrderItem();

            Product product =
                    productService.findById(c.getProductId());

            int totalTaken = c.getQuantity() + c.getBonusQuantity();

            product.setStock(
                    product.getStock() - totalTaken
            );

            productService.save(product);

            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(totalTaken);
            item.setBonusQuantity(c.getBonusQuantity());
            item.setPrice(c.getPrice());
            item.setOriginalPrice(c.getOriginalPrice());
            item.setPromoLabel(c.getPromoLabel());
            item.setSubtotal(c.getQuantity() * c.getPrice());

            orderItemRepo.save(item);
        }

        if (isBuyNow) {
            session.removeAttribute("buyNowCart");
        } else {
            session.removeAttribute("cart");
        }

        if ("BANKING".equals(paymentMethod)) {
            return "redirect:/cart/payment-waiting/" + order.getId();
        }

        return "cart/success";
    }

    @GetMapping("/payment-waiting/{orderId}")
    public String paymentWaiting(@PathVariable("orderId") Integer orderId, HttpSession session, Model model) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        Order order = orderRepo.findById(orderId).orElse(null);

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

    private Integer currentUserId(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return (user != null) ? user.getId() : null;
    }

    private void applyPromotion(CartItem item, Integer userId) {

        Product product = productService.findById(item.getProductId());

        if (product == null || product.getPrice() == null) {
            item.setOriginalPrice(0);
            item.setPrice(0);
            item.setBonusQuantity(0);
            item.setPromoLabel(null);
            item.setPromoType(null);
            return;
        }

        double original = product.getPrice();
        item.setOriginalPrice(original);
        item.setStock(product.getStock());
        item.setProductName(product.getName());
        item.setImage(product.getImage());

        List<Promotion> eligible = promotionService.getEligiblePromotions(userId);
        Promotion promo = promotionService.getPromotionForProduct(item.getProductId(), eligible);

        if (promo == null || promo.getDiscountType() == null) {
            item.setPrice(original);
            item.setBonusQuantity(0);
            item.setPromoLabel(null);
            item.setPromoType(null);
            return;
        }

        item.setPromoLabel(promo.getBadgeText());
        item.setPromoType(promo.getDiscountType());

        switch (promo.getDiscountType()) {

            case "PERCENT": {
                double percent = (promo.getDiscountValue() == null) ? 0 : promo.getDiscountValue();
                double finalPrice = original - (original * percent / 100);
                item.setPrice(Math.max(finalPrice, 0));
                item.setBonusQuantity(0);
                break;
            }

            case "AMOUNT": {
                double amount = (promo.getDiscountValue() == null) ? 0 : promo.getDiscountValue();
                double finalPrice = original - amount;
                item.setPrice(Math.max(finalPrice, 0));
                item.setBonusQuantity(0);
                break;
            }

            case "GIFT": {
                item.setPrice(original);
                item.setBonusQuantity(item.getQuantity());
                break;
            }

            default: {
                item.setPrice(original);
                item.setBonusQuantity(0);
            }
        }
    }

    private void refreshCartPromotions(Cart cart, Integer userId) {
        for (CartItem item : cart.getItems()) {
            applyPromotion(item, userId);
        }
    }

}