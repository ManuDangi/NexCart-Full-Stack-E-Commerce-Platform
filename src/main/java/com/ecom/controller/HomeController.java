package com.ecom.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class HomeController {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private ProductService productService;

	@Autowired
	private UserService userService;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private CartService cartService;
	
	@Value("${image.upload.path}")
    private String uploadPath;

	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {
		if (p != null) {
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);
			// Assuming your UserDtls class now has the getId() method
			Integer countCart = cartService.getCountCart(userDtls.getId());
			m.addAttribute("countCart", countCart);
		}
		List<Category> allActiveCategory = categoryService.getAllActiveCategory();
		m.addAttribute("categorys", allActiveCategory);
	}

	@GetMapping("/")
	public String index(Model m) {
		// Assuming your Category and Product classes now have the getId() method
		List<Category> allActiveCategory = categoryService.getAllActiveCategory().stream()
				.sorted((c1, c2) -> c2.getId().compareTo(c1.getId())).limit(6).toList();
		List<Product> allActiveProducts = productService.getAllActiveProducts("").stream()
				.sorted((p1, p2) -> p2.getId().compareTo(p1.getId())).limit(8).toList();
		m.addAttribute("category", allActiveCategory);
		m.addAttribute("products", allActiveProducts);
		return "index";
	}

	@GetMapping("/signin")
	public String login() {
		return "login";
	}

	@GetMapping("/register")
	public String register() {
		return "register";
	}

	@GetMapping("/products")
	public String products(Model m, @RequestParam(value = "category", defaultValue = "") String category,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "12") Integer pageSize,
			@RequestParam(defaultValue = "") String ch) {

		List<Category> categories = categoryService.getAllActiveCategory();
		m.addAttribute("paramValue", category);
		m.addAttribute("categories", categories);

		Page<Product> page;
		if (StringUtils.isEmpty(ch)) {
			page = productService.getAllActiveProductPagination(pageNo, pageSize, category);
		} else {
			page = productService.searchActiveProductPagination(pageNo, pageSize, category, ch);
		}

		m.addAttribute("products", page.getContent());
		m.addAttribute("productsSize", page.getContent().size());
		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "product";
	}

	@GetMapping("/product/{id}")
	public String product(@PathVariable int id, Model m) {
		Product productById = productService.getProductById(id);
		m.addAttribute("product", productById);
		return "view_product";
	}

	@PostMapping("/saveUser")
	public String saveUser(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, RedirectAttributes ra)
			throws IOException {
		
		// Assuming your UserDtls class now has the getEmail() method
		Boolean existsEmail = userService.existsEmail(user.getEmail());

		if (existsEmail) {
			ra.addFlashAttribute("errorMsg", "Email already exists");
		} else {
			String imageName = "default.jpg";
			if (!file.isEmpty()) {
				// Generate a unique filename to prevent collisions
				String originalFilename = file.getOriginalFilename();
				String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
				imageName = UUID.randomUUID().toString() + fileExtension;
			}
			
			// Assuming your UserDtls class now has the setProfileImage() method
			user.setProfileImage(imageName);
			UserDtls saveUser = userService.saveUser(user);

			if (saveUser != null) {
				if (!file.isEmpty()) {
					Path uploadDir = Paths.get(uploadPath);
					if (!Files.exists(uploadDir)) {
						Files.createDirectories(uploadDir);
					}
					Path filePath = uploadDir.resolve(imageName);
					Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
				}
				ra.addFlashAttribute("succMsg", "Registration successful");
			} else {
				ra.addFlashAttribute("errorMsg", "Something went wrong on the server");
			}
		}
		return "redirect:/register";
	}

	@GetMapping("/forgot-password")
	public String showForgotPassword() {
		return "forgot_password";
	}

	@PostMapping("/forgot-password")
	public String processForgotPassword(@RequestParam String email, HttpServletRequest request, RedirectAttributes ra)
			throws UnsupportedEncodingException, MessagingException {

		UserDtls userByEmail = userService.getUserByEmail(email);

		if (ObjectUtils.isEmpty(userByEmail)) {
			ra.addFlashAttribute("errorMsg", "Invalid email");
		} else {
			String resetToken = UUID.randomUUID().toString();
			userService.updateUserResetToken(email, resetToken);

			String url = CommonUtil.generateUrl(request) + "/reset-password?token=" + resetToken;
			boolean mailSent = commonUtil.sendMail(url, "Password Reset Link", email);

			if (mailSent) {
				ra.addFlashAttribute("succMsg", "Password reset link sent. Please check your email.");
			} else {
				ra.addFlashAttribute("errorMsg", "Something went wrong on the server. Email could not be sent.");
			}
		}
		return "redirect:/forgot-password";
	}

	@GetMapping("/reset-password")
	public String showResetPassword(@RequestParam String token, Model m) {
		UserDtls userByToken = userService.getUserByToken(token);
		if (userByToken == null) {
			m.addAttribute("msg", "Your link is invalid or has expired!");
			return "message";
		}
		m.addAttribute("token", token);
		return "reset_password";
	}

	@PostMapping("/reset-password")
	public String resetPassword(@RequestParam String token, @RequestParam String password, Model m) {
		UserDtls userByToken = userService.getUserByToken(token);
		if (userByToken == null) {
			m.addAttribute("msg", "Your link is invalid or has expired!");
		} else {
			// Assuming your UserDtls class now has the setPassword() and setResetToken() methods
			userByToken.setPassword(passwordEncoder.encode(password));
			userByToken.setResetToken(null);
			userService.updateUser(userByToken);
			m.addAttribute("msg", "Password changed successfully.");
		}
		return "message";
	}

	@GetMapping("/search")
	public String searchProduct(@RequestParam String ch) {
		// Redirect to the paginated product list with the search query
		return "redirect:/products?ch=" + ch;
	}
}